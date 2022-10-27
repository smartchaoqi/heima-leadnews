package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;


    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.findAll(dto);
    }

    @PostMapping("/submit")
    public ResponseResult summitNews(@RequestBody WmNewsDto wmNewsDto){
        return wmNewsService.submitNews(wmNewsDto);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDownUpDto dto){
        return wmNewsService.downOrUp(dto);
    }

    @PostMapping("/list_vo")
    public ResponseResult listVo(@RequestBody WmNewsAuthDto dto){
        return wmNewsService.listVo(dto);
    }

    @GetMapping("/one_vo/{id}")
    public ResponseResult oneVo(@PathVariable Long id){
        return wmNewsService.oneVo(id);
    }

    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody WmNewsAuthDto dto){
        return wmNewsService.authFail(dto);
    }

    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody WmNewsAuthDto dto){
        return wmNewsService.authPass(dto);
    }

}
