package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmMaterialDto dto){
        return wmMaterialService.findList(dto);
    }

    @GetMapping("/del_picture/{id}")
    public ResponseResult delPicture(@PathVariable Long id){
        return wmMaterialService.delPicture(id);
    }

    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollect(@PathVariable Long id){
        return wmMaterialService.cancelCollect(id);
    }

    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable Long id){
        return wmMaterialService.collect(id);
    }
}
