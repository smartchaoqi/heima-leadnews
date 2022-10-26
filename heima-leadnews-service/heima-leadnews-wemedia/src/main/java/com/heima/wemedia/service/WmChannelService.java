package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    public ResponseResult findAll();

    ResponseResult one(Long id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult delNews(Long id);

    ResponseResult newsDownUp(WmNewsDownUpDto dto);

    ResponseResult saveChannel(WmChannel adChannel);

    ResponseResult channelList(WmChannelDto dto);

    ResponseResult del(Long id);

    ResponseResult update(WmChannel wmChannel);
}