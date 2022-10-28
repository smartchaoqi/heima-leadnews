package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_collection")
public class ApCollection implements Serializable {
    @TableId(value = "id",type = IdType.ID_WORKER)
    private Long id;

    @TableField("entry_id")
    private Long entryId;

    @TableField("article_id")
    private Long articleId;

    @TableField("type")
    private Short type;

    @TableField("collection_time")
    private Date collectionTime;

    @TableField("published_time")
    private Date publishedTime;
}
