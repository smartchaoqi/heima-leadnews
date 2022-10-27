package com.heima.common.constants;

public class AuthConstants {
    /**
     * 状态
     0 创建中
     1 待审核
     2 审核失败
     9 审核通过
     */
    public static final Short CREATED=0;
    public static final Short WAIT_AUTH=1;
    public static final Short AUTH_FAIL=2;
    public static final Short AUTH_SUCCESS=9;
}
