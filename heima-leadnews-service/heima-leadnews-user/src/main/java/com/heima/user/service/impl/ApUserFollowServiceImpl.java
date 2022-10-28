package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.UserFollowConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.user.mapper.ApUserFollowMapper;
import com.heima.user.service.ApUserFanService;
import com.heima.user.service.ApUserFollowService;
import com.heima.user.service.ApUserService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class ApUserFollowServiceImpl extends ServiceImpl<ApUserFollowMapper, ApUserFollow> implements ApUserFollowService{
    @Autowired
    private ApUserService apUserService;

    @Autowired
    private ApUserFanService apUserFanService;
    @Override
    public ResponseResult userFollow(UserRelationDto dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        LambdaQueryWrapper<ApUserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApUserFollow::getUserId,user.getId());
        wrapper.eq(ApUserFollow::getFollowId,dto.getAuthorId());
        ApUserFollow dbUserFollow = getOne(wrapper);

        if (UserFollowConstants.FOLLOW.equals(dto.getOperation())){
            if (dbUserFollow!=null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);
            }
            dbUserFollow=new ApUserFollow();
            dbUserFollow.setFollowId(dto.getAuthorId());
            dbUserFollow.setFollowName(apUserService.getById(dto.getAuthorId()).getName());
            dbUserFollow.setUserId(user.getId());
            dbUserFollow.setCreatedTime(new Date());
            save(dbUserFollow);
            apUserFanService.addApUserFan(user.getId(),dto.getAuthorId());
        }else if (UserFollowConstants.CANCEL_FOLLOW.equals(dto.getOperation())){
            if (dbUserFollow==null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST);
            }
            removeById(dbUserFollow.getId());
            apUserFanService.removeApUserFan(user.getId(),dto.getAuthorId());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
