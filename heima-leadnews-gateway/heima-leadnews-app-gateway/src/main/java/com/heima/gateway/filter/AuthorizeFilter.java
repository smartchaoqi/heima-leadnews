package com.heima.gateway.filter;


import com.heima.gateway.util.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {
    /**
     * 过滤方法
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //判断是否登录
        if (request.getURI().getPath().contains("/login")){
            return chain.filter(exchange);
        }

        //获取token
        String token = request.getHeaders().getFirst("token");

        //判断token是否存在
        if (!StringUtils.hasText(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //判断token是否有效
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            int result = AppJwtUtil.verifyToken(claimsBody);
            if (result==1||result==2){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            Object id = claimsBody.get("id");
            ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.add("userId", id + "");
            }).build();
            exchange.mutate().request(httpRequest);
        }catch (Exception e){
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //放行
        return chain.filter(exchange);
    }

    /**
     * 优先级设置
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
