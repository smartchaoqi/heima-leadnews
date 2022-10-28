package com.heima.common.constants;

public class BehaviorConstants {
    /**
     * 点赞
     */
    public static final String LIKES_BEHAVIOR="likeBehavior";
    /**
     * 阅读
     */
    public static final String READ_BEHAVIOR="readBehavior";
    /**
     * 不喜欢
     */
    public static final String UN_LIKES_BEHAVIOR="unLikeBehavior";

    /**
     * 点赞
     */
    public static final Short SURE_THUMB_SUP=0;

    /**
     * 取消点赞
     */
    public static final Short CANCEL_SURE_THUMB_SUP=1;

    /**
     * 文章
     */
    public static final Short ARTICLE=0;

    /**
     * 动态
     */
    public static final Short DYNAMIC=1;

    /**
     * 评论
     */
    public static final Short COMMENT=2;

    public static String buildLikesKey(Long articleId,Short operation,Short type){
        return articleId.toString()+"_"+operation.toString()+"_"+type.toString();
    }
}
