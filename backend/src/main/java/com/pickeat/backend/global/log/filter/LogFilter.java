package com.pickeat.backend.global.log.filter;

import com.pickeat.backend.global.log.LogWriter;
import com.pickeat.backend.global.log.model.http.RequestLog;
import com.pickeat.backend.global.log.model.http.ResponseLog;
import com.pickeat.backend.global.utility.SystemRequestChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogFilter extends OncePerRequestFilter {

    private final SystemRequestChecker systemRequestChecker;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (systemRequestChecker.isSystemRequest(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper cacheRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cacheResponse = new ContentCachingResponseWrapper(response);
        setupMdcContext(cacheRequest);

        try {
            filterChain.doFilter(cacheRequest, cacheResponse);
            LogWriter.info(this.getClass(), RequestLog.of(cacheRequest));
        } finally {
            LogWriter.info(this.getClass(), ResponseLog.of(cacheRequest, cacheResponse));
            cacheResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void setupMdcContext(ContentCachingRequestWrapper cacheRequest) {
        MDC.put("request_id", UUID.randomUUID().toString().substring(0, 8));
        MDC.put("request_uri", cacheRequest.getRequestURI());
        MDC.put("client_ip", cacheRequest.getRemoteAddr());
    }
}
