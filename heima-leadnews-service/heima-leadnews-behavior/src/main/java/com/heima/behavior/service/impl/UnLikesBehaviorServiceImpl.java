package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.UnLikesBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.bahavior.dtos.UnLikesBehaviorDto;
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
public class UnLikesBehaviorServiceImpl implements UnLikesBehaviorService {

    @Autowired
    private CacheService cacheService;

    @Override
    public ResponseResult unLikeBehavior(UnLikesBehaviorDto dto) {
        ApUser user = BehaviorThreadLocalUtil.getUser();
        if (user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        String setJson =(String) cacheService.hGet(BehaviorConstants.UN_LIKES_BEHAVIOR, user.getId().toString());
        HashSet hashSet = JSON.parseObject(setJson, HashSet.class);
        if (hashSet==null){
            hashSet=new HashSet();
        }
        if (BehaviorConstants.UN_LIKE.equals(dto.getType())){
            hashSet.add(dto.getArticleId());
        }
        if (BehaviorConstants.CANCEL_UN_LIKE.equals(dto.getType())){
            hashSet.remove(dto.getArticleId());
        }
        cacheService.hPut(BehaviorConstants.UN_LIKES_BEHAVIOR, user.getId().toString(),JSON.toJSONString(hashSet));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
