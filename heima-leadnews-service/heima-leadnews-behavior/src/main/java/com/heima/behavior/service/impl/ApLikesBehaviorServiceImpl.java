package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApLikesBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.bahavior.dtos.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.BehaviorThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;

@Slf4j
@Transactional
@Service
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {
    @Autowired
    private CacheService cacheService;

    @Override
    public ResponseResult likesBehavior(LikesBehaviorDto dto) {
        ApUser user = BehaviorThreadLocalUtil.getUser();
        if (user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Map<Object, Object> map = cacheService.hGetAll(BehaviorConstants.LIKES_BEHAVIOR);
        //点赞
        String setJson =(String) map.get(BehaviorConstants.buildLikesKey(dto.getArticleId(), dto.getOperation(), dto.getType()));
        HashSet hashSet = JSON.parseObject(setJson, HashSet.class);
        if (hashSet==null){
            hashSet=new HashSet();
        }
        hashSet.add(user.getId());
        cacheService.hPut(BehaviorConstants.LIKES_BEHAVIOR,BehaviorConstants.buildLikesKey(dto.getArticleId(), dto.getOperation(), dto.getType()),JSON.toJSONString(hashSet));

        if (BehaviorConstants.SURE_THUMB_SUP.equals(dto.getOperation())){
            //去掉不点赞
            String setJson2 =(String) map.get(BehaviorConstants.buildLikesKey(dto.getArticleId(), BehaviorConstants.CANCEL_SURE_THUMB_SUP, dto.getType()));
            HashSet hashSet2 = JSON.parseObject(setJson2, HashSet.class);
            if (hashSet2==null){
                hashSet2=new HashSet();
            }
            hashSet2.remove(user.getId());
            cacheService.hPut(BehaviorConstants.LIKES_BEHAVIOR,BehaviorConstants.buildLikesKey(dto.getArticleId(), BehaviorConstants.CANCEL_SURE_THUMB_SUP, dto.getType()),JSON.toJSONString(hashSet));
        }else{
            //去掉点赞
            String setJson2 =(String) map.get(BehaviorConstants.buildLikesKey(dto.getArticleId(), BehaviorConstants.SURE_THUMB_SUP, dto.getType()));
            HashSet hashSet2 = JSON.parseObject(setJson2, HashSet.class);
            if (hashSet2==null){
                hashSet2=new HashSet();
            }
            hashSet2.remove(user.getId());
            cacheService.hPut(BehaviorConstants.LIKES_BEHAVIOR,BehaviorConstants.buildLikesKey(dto.getArticleId(), BehaviorConstants.SURE_THUMB_SUP, dto.getType()),JSON.toJSONString(hashSet));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
