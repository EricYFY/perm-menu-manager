package com.example.permmenu.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EnvInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String envId = request.getHeader("X-Env-ID");
        if (envId != null && !envId.isEmpty()) {
            String uri = request.getRequestURI();
            if (uri.contains("/fun-permission") || uri.contains("/flow-ump-config")) {
                DynamicDataSourceContextHolder.push(envId + "_second");
            } else if (uri.contains("/menu") || uri.contains("/session")) {
                DynamicDataSourceContextHolder.push(envId + "_master");
            }
            // For /env and others, do not push, let it use default or handle it manually
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String envId = request.getHeader("X-Env-ID");
        if (envId != null && !envId.isEmpty()) {
            String uri = request.getRequestURI();
            if (uri.contains("/fun-permission") || uri.contains("/flow-ump-config") || uri.contains("/menu") || uri.contains("/session")) {
                DynamicDataSourceContextHolder.poll();
            }
        }
    }
}
