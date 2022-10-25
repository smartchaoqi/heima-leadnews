package com.heima.admin.controller;

import com.heima.admin.service.AdminLoginService;
import com.heima.model.admin.dtos.AdUserLoginDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/login")
public class AdminLoginController {

    @Autowired
    private AdminLoginService adminLoginService;

    @PostMapping("/in")
    public ResponseResult login(@RequestBody AdUserLoginDto adUserDto){
        return adminLoginService.login(adUserDto);
    }
}
