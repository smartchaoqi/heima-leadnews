package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            //2.审核文章内容
            boolean isTextScan = handleTextScan((String) textAndImages.get("context"),wmNews);
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
//        for (String image : images) {
//            byte[] bytes = fileStorageService.downLoadFile(image);
//        }
        List<byte[]> collect = images.stream().map(k -> fileStorageService.downLoadFile(k)).collect(Collectors.toList());
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
