package org.artifactory.webapp.servlet;

import org.artifactory.common.ConstantValues;
import org.artifactory.security.AccessLogger;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.SessionUtils;
import org.springframework.security.core.Authentication;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArtifactoryCsrfFilter extends DelayedFilterBase {

    private final String HEADER_NAME = "X-Requested-With";

    private static final Set<String> METHODS_TO_IGNORE;
    static {
        Set<String> mti = new HashSet<>();
        mti.add("GET");
        mti.add("OPTIONS");
        mti.add("HEAD");
        METHODS_TO_IGNORE = Collections.unmodifiableSet(mti);
    }

    @Override
    public void initLater(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (shouldSkipFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (METHODS_TO_IGNORE.contains(httpRequest.getMethod()) || ! ConstantValues.csrfFilterEnable.getBoolean()) {
            chain.doFilter(request, response);
            return;
        }
        Authentication authentication = SessionUtils.getAuthentication(httpRequest);
        if (authentication != null) {
            if (httpRequest.getHeader(HEADER_NAME) == null) {
                AccessLogger.disapproved(true, authentication, "Cross-Site Request Forgery");
                HttpUtils.sendErrorResponse((HttpServletResponse) response, HttpServletResponse.SC_FORBIDDEN, "Request was blocked. Please refer to access.log");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}