package com.heima.behavior.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.BehaviorThreadLocalUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BehaviorTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("userId");
        if (userId!=null){
            ApUser apUser = new ApUser();
            apUser.setId(Integer.parseInt(userId));
            BehaviorThreadLocalUtil.setUser(apUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BehaviorThreadLocalUtil.clear();
    }
}
