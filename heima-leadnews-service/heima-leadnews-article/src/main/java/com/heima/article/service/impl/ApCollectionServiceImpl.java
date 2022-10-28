package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApCollectionMapper;
import com.heima.article.service.ApCollectionService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.article.pojos.ApCollection;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ApCollectionServiceImpl extends ServiceImpl<ApCollectionMapper, ApCollection> implements ApCollectionService {

    @Override
    public ResponseResult collectionBehavior(CollectionBehaviorDto dto) {
        if (ArticleConstants.COLLECTION.equals(dto.getOperation())) {
            ApCollection apCollection = new ApCollection();
            apCollection.setCollectionTime(new Date());
            apCollection.setArticleId(dto.getEntryId());
            apCollection.setType(dto.getType());
            apCollection.setPublishedTime(dto.getPublishedTime());
            save(apCollection);
        } else if (ArticleConstants.CANCEL_COLLECTION.equals(dto.getOperation())) {
            LambdaQueryWrapper<ApCollection> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApCollection::getArticleId,dto.getEntryId());
            wrapper.eq(ApCollection::getType,dto.getType());
            remove(wrapper);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
