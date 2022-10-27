package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService {
    @Override
    public ResponseResult list(AuthDto authDto) {
        LambdaQueryWrapper<ApUserRealname> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(authDto.getStatus()!=null,ApUserRealname::getStatus,authDto.getStatus());
        Page<ApUserRealname> page = new Page<>(authDto.getPage(), authDto.getSize());
        page(page,wrapper);
        PageResponseResult result = new PageResponseResult(authDto.getPage(), authDto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }
}
