package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsService wmNewsService;

    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsService.getById(id);
        if (wmNews==null){
            throw new RuntimeException("WmNewsAutoScanServiceImpl 文章不存在");
        }

        if (WmNews.Status.SUBMIT.getCode()==wmNews.getStatus()){
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);

            //自管理的敏感词过滤
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"),wmNews);
            if (!isSensitive) return;

            //2.审核文章内容
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"),wmNews);
            if (!isTextScan) return;
            //3.审核图片
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"),wmNews);
            if (!isImageScan) return;
            //4.保存app端文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if (!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端文章数据失败");
            }
            //回填数据
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,WmNews.Status.PUBLISHED.getCode(), "审核成功");
        }

    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自管理的敏感词审核
     * @param context
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String context, WmNews wmNews) {
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> collect = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        SensitiveWordUtil.initMap(collect);

        Map<String, Integer> map = SensitiveWordUtil.matchWords(context);
        if (map.size()>0){
            updateWmNews(wmNews,WmNews.Status.FAIL.getCode(),"文章中存在违规内容"+map);
            return false;
        }
        return true;
    }

    @Resource
    private IArticleClient articleClient;

    @Autowired
    private WmChannelService wmChannelService;

    @Autowired
    private WmUserService wmUserService;

    /**
     * 保存app端文章数据
     * @param wmNews
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        //文章布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelService.getById(wmNews.getChannelId());
        if (wmChannel!=null) {
            dto.setChannelName(wmChannel.getName());
        }
        //作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserService.getById(wmNews.getUserId());
        if (wmUser!=null) {
            dto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if (wmNews.getArticleId()!=null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());
        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;
    }

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private Tess4jClient tess4jClient;

    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag=true;
        if (images==null||images.size()==0){
            return true;
        }
        images=images.stream().distinct().collect(Collectors.toList());
        List<byte[]> collect=new ArrayList<>();

        try{
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                BufferedImage read = ImageIO.read(byteArrayInputStream);
                String result = tess4jClient.doOCR(read);
                //过滤文字
                boolean isSensitive = handleSensitiveScan(result, wmNews);
                if (!isSensitive){
                    return false;
                }

                collect.add(bytes);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Map map = greenImageScan.imageScan(collect);
            if (map!=null){
                //审核失败
                if (map.get("suggestion").equals("block")){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.FAIL.getCode(), "当前文章存在违规内容");
                }
                //不确定
                if (map.get("suggestion").equals("review")){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.ADMIN_AUTH.getCode(), "当前文章存在不确定内容");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return flag;
    }

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    /**
     *  审核文本内容
     * @param context
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String context, WmNews wmNews) {
        boolean flag=true;
        if ((wmNews.getTitle()+context).length()==0){
            return true;
        }
        try {
            Map map = greenTextScan.greeTextScan(wmNews.getTitle()+context);
            if (map!=null){
                //审核失败
                if (map.get("suggestion").equals("block")){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.FAIL.getCode(), "当前文章存在违规内容");
                }
                //不确定
                if (map.get("suggestion").equals("review")){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.ADMIN_AUTH.getCode(), "当前文章存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag=false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改文章内容
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews,short status,String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsService.updateById(wmNews);
    }

    /**
     * 自媒体文章内容提取文本和图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        String content = wmNews.getContent();
        StringBuilder builder = new StringBuilder();
        List<String> images=new ArrayList<>();
        if (StringUtils.isNotBlank(content)){
            List<Map> maps = JSONArray.parseArray(content, Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")){
                    builder.append(map.get("value"));
                }
                if (map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }

        if (StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String,Object> map=new HashMap<>();
        map.put("content",builder.toString());
        map.put("images",images);
        return map;
    }
}
