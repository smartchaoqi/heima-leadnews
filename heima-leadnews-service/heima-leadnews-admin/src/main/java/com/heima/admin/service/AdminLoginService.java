package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AdUserLoginDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

public interface AdminLoginService extends IService<AdUser> {

    public ResponseResult login(AdUserLoginDto adUserLoginDto);
}
