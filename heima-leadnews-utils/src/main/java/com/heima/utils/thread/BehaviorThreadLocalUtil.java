package com.heima.utils.thread;

import com.heima.model.user.pojos.ApUser;

public class BehaviorThreadLocalUtil {
    public static final ThreadLocal<ApUser> BEHAVIOR_USER_THREAD_LOCAL=new ThreadLocal<>();

    public static void setUser(ApUser apUser){
        BEHAVIOR_USER_THREAD_LOCAL.set(apUser);
    }

    public static ApUser getUser(){
        return BEHAVIOR_USER_THREAD_LOCAL.get();
    }

    public static void clear(){
        BEHAVIOR_USER_THREAD_LOCAL.remove();
    }
}
