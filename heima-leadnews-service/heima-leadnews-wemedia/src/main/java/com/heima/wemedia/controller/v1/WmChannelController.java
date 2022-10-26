package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class WmChannelController {

    @Autowired
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    @GetMapping("/one/{id}")
    public ResponseResult one(@PathVariable Long id){
        return wmChannelService.one(id);
    }

    @GetMapping("/del_news/{id}")
    public ResponseResult delNews(@PathVariable Long id){
        return wmChannelService.delNews(id);
    }

    @PostMapping("/down_or_up")
    public ResponseResult newsDownUp(@RequestBody WmNewsDownUpDto dto){
        return wmChannelService.newsDownUp(dto);
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmChannel adChannel){
        return wmChannelService.saveChannel(adChannel);
    }
}
