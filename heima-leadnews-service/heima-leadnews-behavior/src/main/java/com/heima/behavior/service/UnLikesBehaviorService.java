package com.heima.behavior.service;

import com.heima.model.bahavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface UnLikesBehaviorService {
    ResponseResult unLikeBehavior(UnLikesBehaviorDto dto);
}
