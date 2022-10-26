package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {
    @Autowired
    private WmNewsService wmNewsService;

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    @Override
    public ResponseResult one(Long id) {
        WmNews wmNews = wmNewsService.getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_NOT_EXIST);
        }
        return ResponseResult.okResult(wmNews);
    }

    @Override
    public ResponseResult delNews(Long id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_ID_NOT_EXISTS);
        }
        WmNews wmNews = wmNewsService.getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_NOT_EXIST);
        }
        if (WmNews.Status.PUBLISHED.getCode()==(wmNews.getStatus())){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_IS_RELEASE);
        }
        wmNewsService.removeById(id);

        //删除文章与素材关系
        LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNewsMaterial::getNewsId, id);
        List<Integer> wmNewsMaterialIds = wmNewsMaterialMapper.selectList(wrapper).stream().map(WmNewsMaterial::getId).collect(Collectors.toList());
        wmNewsMaterialMapper.deleteBatchIds(wmNewsMaterialIds);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult newsDownUp(WmNewsDownUpDto dto) {
        if (dto==null||dto.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_ID_NOT_EXISTS);
        }
        WmNews wmNews = wmNewsService.getById(dto.getId());
        if (wmNews==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_NOT_EXIST);
        }
        if (WmNews.Status.PUBLISHED.getCode()!=wmNews.getStatus()){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEWS_IS_NOT_RELEASE);
        }
        wmNews.setEnable(dto.getEnable());
        wmNewsService.updateById(wmNews);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult saveChannel(WmChannel adChannel) {
        int count = count(new LambdaQueryWrapper<WmChannel>().eq(WmChannel::getName, adChannel.getName()));
        if (count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.CHANNEL_IS_REPEAT);
        }
        save(adChannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult channelList(WmChannelDto dto) {
        dto.checkParam();
        Page<WmChannel> page = new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getName()),WmChannel::getName,dto.getName());
        wrapper.orderByAsc(WmChannel::getCreatedTime);
        page(page,wrapper);

        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return ResponseResult.okResult(pageResponseResult);
    }

    @Override
    public ResponseResult del(Long id) {
        int count = wmNewsService.count(new LambdaQueryWrapper<WmNews>().eq(WmNews::getChannelId, id));
        if (count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.CHANNEL_HAS_REFERENCE);
        }
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}