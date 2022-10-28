package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.user.pojos.ApUserFan;
import com.heima.user.mapper.ApUserFanMapper;
import com.heima.user.service.ApUserFanService;
import com.heima.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApUserFanServiceImpl extends ServiceImpl<ApUserFanMapper, ApUserFan> implements ApUserFanService {
    @Autowired
    private ApUserService apUserService;

    @Override
    public void addApUserFan(Integer userId, Integer authorId) {
        ApUserFan apUserFan = new ApUserFan();
        apUserFan.setUserId(authorId);
        apUserFan.setFansId(userId);
        apUserFan.setFansName(apUserService.getById(userId).getName());
        save(apUserFan);
    }

    @Override
    public void removeApUserFan(Integer userId, Integer authorId) {
        LambdaQueryWrapper<ApUserFan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApUserFan::getUserId,authorId);
        wrapper.eq(ApUserFan::getFansId,userId);
        remove(wrapper);
    }
}
