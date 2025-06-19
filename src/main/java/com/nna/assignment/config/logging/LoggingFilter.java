package com.nna.assignment.config.logging;

import com.nna.assignment.exception.GlobalExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@WebFilter("/*")
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long duration = System.currentTimeMillis() - startTime;

        logRequest(requestWrapper);
        logResponse(responseWrapper, duration);

        responseWrapper.copyBodyToResponse();
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        logger.info(">>> Request: [{} {}] Headers: {} Body: {}",
                request.getMethod(), request.getRequestURI(), request.getHeaderNames(), body);
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        logger.info("<<< Response: [{}] Duration: {} ms Body: {}", response.getStatus(), duration, body);
    }
}
