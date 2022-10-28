package com.heima.behavior.service;

import com.heima.model.bahavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ReadBehaviorService {
    ResponseResult readBehavior(ReadBehaviorDto dto);
}
