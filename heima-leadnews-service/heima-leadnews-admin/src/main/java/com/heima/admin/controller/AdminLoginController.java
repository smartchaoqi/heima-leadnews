package com.heima.admin.controller;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AdminLoginController {

    @PostMapping("/login/in")
    public ResponseResult login(){
        log.info("success ~~");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
