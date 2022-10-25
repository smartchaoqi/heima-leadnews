package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdminLoginService;
import com.heima.model.admin.dtos.AdUserLoginDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class AdminLoginServiceImpl extends ServiceImpl<AdUserMapper,AdUser> implements AdminLoginService{
    @Override
    public ResponseResult login(AdUserLoginDto dto) {
        if (dto==null || StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        LambdaQueryWrapper<AdUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdUser::getName,dto.getName());
        AdUser dbAdUser = getOne(wrapper);
        if (dbAdUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        String salt = dbAdUser.getSalt();
        String password = dto.getPassword();
        String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
        if (!pswd.equals(dbAdUser.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR,"密码错误");
        }
        dbAdUser.setLoginTime(new Date());
        updateById(dbAdUser);
        String token = AppJwtUtil.getToken(dbAdUser.getId().longValue());
        Map<String,Object> map=new HashMap<>();
        map.put("token",token);
        dbAdUser.setSalt("");
        dbAdUser.setPassword("");
        map.put("user",dbAdUser);
        return ResponseResult.okResult(map);
    }
}
