package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.article.pojos.ApCollection;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCollectionService extends IService<ApCollection> {
    ResponseResult collectionBehavior(CollectionBehaviorDto dto);
}
