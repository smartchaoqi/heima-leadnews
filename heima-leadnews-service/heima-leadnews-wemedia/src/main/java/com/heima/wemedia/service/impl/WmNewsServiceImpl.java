package com.heima.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        //1.检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数检查
        dto.checkParam();
        //获取当前登录人的信息
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //分页条件查询
        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNews::getUserId, user.getId());
        wrapper.orderByDesc(WmNews::getCreatedTime);
        if (dto.getChannelId() != null) {
            wrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        //时间范围查询
        if (dto.getBeginPubDate() != null && dto.getEndPubDate() != null) {
            wrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            wrapper.like(WmNews::getTitle, dto.getKeyword());
        }
        page(page, wrapper);
        //返回结果

        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }

    @Override
    public ResponseResult submitNews(WmNewsDto wmNewsDto) {
        //条件判断
        if (wmNewsDto == null || wmNewsDto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //保存修改文章
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(wmNewsDto, wmNews);
        if (wmNewsDto.getImages() != null && wmNewsDto.getImages().size() > 0) {
            String imageStr = StringUtils.join(wmNewsDto.getImages(), ",");
            wmNews.setImages(imageStr);
        }
        //封面类型为自动 -1
        if (wmNewsDto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);

        //判断是否为草稿
        if (wmNewsDto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        //不是草稿,保存文章内容图片与素材关系
        //提取文章内容里的图片信息
        List<String> materials = ectractUrlInfo(wmNewsDto.getContent());
        saveRelativeInfoForContent(materials, wmNews.getId());

        //不是草稿,保存文章封面图片与素材关系
        saveRelativeInfoForCover(wmNewsDto, wmNews, materials);
       return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {

        List<String> images = dto.getImages();

        //如果当前封面类型为自动，则设置封面类型的数据
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            //多图
            if (materials.size() >= 3) {
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size() >= 1 && materials.size() < 3) {
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            //修改文章
            if (images != null && images.size() > 0) {
                wmNews.setImages(StringUtils.join(images, ","));
            }
            updateById(wmNews);
        }
        if (images != null && images.size() > 0) {
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }

    }

    /**
     * 处理文章内容图片与素材得关系
     *
     * @param materials
     * @param newsId
     */
    private void saveRelativeInfoForContent(List<String> materials, Integer newsId) {
        saveRelativeInfo(materials, newsId, WemediaConstants.WM_CONTENT_REFERENCE);
    }

    @Autowired
    private WmMaterialService wmMaterialService;

    /**
     * 保存文章图片与素材的关系
     *
     * @param materials
     * @param newsId
     * @param type
     */
    private void saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        if (materials != null && !materials.isEmpty()) {
            //通过图片url查询素材id
            LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(WmMaterial::getUrl, materials);
            wrapper.eq(WmMaterial::getUserId, WmThreadLocalUtil.getUser().getId());
            List<WmMaterial> dbMaterials = wmMaterialService.list(wrapper);

            //判断素材是否有效
            if (dbMaterials == null || dbMaterials.size() == 0) {
                //手动抛出异常   第一个功能：能够提示调用者素材失效了，第二个功能，进行数据的回滚
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            if (materials.size() != dbMaterials.size()) {
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            List<Integer> idList = dbMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());

            wmNewsMaterialMapper.saveRelations(idList, newsId, type);
        }
    }

    /**
     * 提取文章内容里的图片信息
     *
     * @param content
     * @return
     */
    private List<String> ectractUrlInfo(String content) {
        List<Map> maps = JSON.parseArray(content, Map.class);
        List<String> result = new ArrayList<>();
        for (Map map : maps) {
            if (WemediaConstants.WM_NEWS_TYPE_IMAGE.equals(map.get("type"))) {
                result.add(map.get("value").toString());
            }
        }
        return result;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    private void saveOrUpdateWmNews(WmNews wmNews) {
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        Date now = new Date();
        wmNews.setCreatedTime(now);
        wmNews.setSubmitedTime(now);
        wmNews.setEnable((short) 1);//默认上架
        if (wmNews.getId() == null) {
            save(wmNews);
        } else {
            //删除文章图片与素材关系
            wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }
}
