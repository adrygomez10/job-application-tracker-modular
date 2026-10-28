package dev.adriangm.tracker.applications.adapter.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLogFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException
    {
        String requestId = UUID.randomUUID().toString();
        long start = System.nanoTime();
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            // No URL, query string, credentials, notes or company names in logs.
            LOG
                .info(
                    "request_completed method={} status={} duration_ms={}",
                    request.getMethod(),
                    response.getStatus(),
                    (System.nanoTime() - start) / 1_000_000
                );
            MDC.remove("requestId");
        }
    }
}
