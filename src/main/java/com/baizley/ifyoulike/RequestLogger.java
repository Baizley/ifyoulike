package com.baizley.ifyoulike;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Component
public class RequestLogger extends AbstractRequestLoggingFilter {
    private final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

    public RequestLogger() {
        setIncludeClientInfo(true);
        setIncludeHeaders(true);
        setIncludeQueryString(true);
        setBeforeMessagePrefix("");
    }

    @Override
    protected boolean shouldLog(HttpServletRequest httpServletRequest) {
        return Optional
                .ofNullable(httpServletRequest.getHeader("Accept"))
                .map(acceptHeader -> acceptHeader.contains(TEXT_HTML_VALUE))
                .orElse(false);
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        logger.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        // Only log the request once.
    }
}
