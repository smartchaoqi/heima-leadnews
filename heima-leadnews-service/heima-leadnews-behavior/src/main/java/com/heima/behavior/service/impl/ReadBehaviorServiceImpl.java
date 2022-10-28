package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ReadBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.bahavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.BehaviorThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Slf4j
@Transactional
public class ReadBehaviorServiceImpl implements ReadBehaviorService {

    @Autowired
    private CacheService cacheService;

    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        ApUser user = BehaviorThreadLocalUtil.getUser();
        if (user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Integer count = (Integer) cacheService.hGet(BehaviorConstants.READ_BEHAVIOR, dto.getArticleId().toString()+"_"+user.getId());
        if (count==null){
            count=0;
        }
        count+=dto.getCount();
        cacheService.hPut(BehaviorConstants.READ_BEHAVIOR, dto.getArticleId().toString()+"_"+user.getId(),count.toString());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
