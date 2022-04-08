package com.hao.postgres.config;

import com.hao.postgres.util.SecurityContextUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Received request: {} {} [{}]", request.getMethod(), request.getRequestURI(),
            SecurityContextUtils.getRequestContext());
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        long duration = System.currentTimeMillis() - startTime.get();
        startTime.remove();
        if(duration > 3000) {
            log.warn("Response: {} {} {} [{}], slow api handling has token {} ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(),
                SecurityContextUtils.getRequestContext(),
                duration);
        } else {
            log.info("Response: {} {} {} [{}]",
                request.getMethod(), request.getRequestURI(), response.getStatus(),
                SecurityContextUtils.getRequestContext());
        }
    }

}
