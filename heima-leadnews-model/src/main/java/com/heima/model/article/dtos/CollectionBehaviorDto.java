package com.heima.model.article.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class CollectionBehaviorDto {
    private Long entryId;
    //0收藏    1取消收藏
    private Short operation;
    private Date publishedTime;
    //0文章    1动态
    private Short type;
}
