package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_user_follow")
public class ApUserFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("follow_id")
    private Integer followId;

    @TableField("follow_name")
    private String followName;

    @TableField("level")
    private Short level;

    @TableField("is_notice")
    private Short isNotice;

    @TableField("created_time")
    private Date createdTime;
}
