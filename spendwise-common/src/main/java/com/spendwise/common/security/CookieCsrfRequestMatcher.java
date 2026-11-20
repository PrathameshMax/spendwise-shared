package com.spendwise.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Servlet resource servers: requires CSRF protection only where CSRF is possible,
 * as decided by {@link CsrfPolicy}.
 */
public final class CookieCsrfRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        return CsrfPolicy.requiresCsrfProtection(
                request.getMethod(),
                request.getHeader(HttpHeaders.COOKIE) != null,
                request.getHeader(HttpHeaders.AUTHORIZATION));
    }
}