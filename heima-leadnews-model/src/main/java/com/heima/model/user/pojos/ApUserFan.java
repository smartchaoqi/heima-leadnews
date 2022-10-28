package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP用户粉丝信息表(ApUserFan)表实体类
 *
 * @author makejava
 * @since 2022-10-28 19:25:21
 */
@Data
@TableName("ap_user_fan")
public class ApUserFan implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    private Short id;
    //用户ID
    private Integer userId;
    //粉丝ID
    private Integer fansId;
    //粉丝昵称
    private String fansName;
    //粉丝忠实度 0 正常 1 潜力股 2 勇士 3 铁杆 4 老铁
    private Short level;
    //创建时间
    private Date createdTime;
    //是否可见我动态
    private Short isDisplay;
    //是否屏蔽私信
    private Short isShieldLetter;
    //是否屏蔽评论
    private Short isShieldComment;
}
