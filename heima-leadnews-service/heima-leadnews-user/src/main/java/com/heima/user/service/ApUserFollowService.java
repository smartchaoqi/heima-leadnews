package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUserFollow;

public interface ApUserFollowService extends IService<ApUserFollow> {
    ResponseResult userFollow(UserRelationDto dto);
}
