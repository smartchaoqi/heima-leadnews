package com.heima.behavior.service;

import com.heima.model.bahavior.dtos.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorService {
    ResponseResult likesBehavior(LikesBehaviorDto dto);
}
