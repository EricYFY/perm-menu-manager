package com.example.permmenu.config;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class EnvInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String envId = request.getHeader("X-Env-ID");
        if (envId != null && !envId.isEmpty()) {
            String uri = request.getRequestURI();
            String targetDs = null;
            if (uri.contains("/fun-permission") || uri.contains("/flow-ump-config")) {
                targetDs = envId + "_second";
                DynamicDataSourceContextHolder.push(targetDs);
            } else if (uri.contains("/menu") || uri.contains("/session")) {
                targetDs = envId + "_master";
                DynamicDataSourceContextHolder.push(targetDs);
            }
            if (targetDs != null) {
                log.info("【多数据源切面】请求路径: [{}], Header X-Env-ID: [{}], 动态切换数据源到: [{}]", uri, envId, targetDs);
            }
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
