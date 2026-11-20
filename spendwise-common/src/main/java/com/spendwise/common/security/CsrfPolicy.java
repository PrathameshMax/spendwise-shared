package com.spendwise.common.security;

import java.util.Locale;
import java.util.Set;

/**
 * Decides which requests reaching a SpendWise resource server need CSRF protection.
 *
 * CSRF exploits credentials a browser attaches on its own: cookies. A request
 * authenticated with a bearer token cannot be forged cross-site, because the
 * attacker's page cannot make the browser attach that header. So protection is
 * required only for a state-changing request that carries cookies and no bearer
 * token. Every request SpendWise sends today (gateway token relay, service-to-
 * service calls, CLI clients) carries a bearer token and is unaffected; a
 * cookie-authenticated request, should one ever reach a service, is checked
 * instead of being trusted. Browser CSRF for the web app is enforced at the
 * gateway (CookieServerCsrfTokenRepository).
 *
 * Kept free of servlet and reactive types so both auto-configurations can use it
 * without loading the other stack's classes.
 */
public final class CsrfPolicy {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final String BEARER_PREFIX = "bearer ";

    private CsrfPolicy() {
    }

    public static boolean requiresCsrfProtection(String method, boolean hasCookies, String authorization) {
        if (method == null || SAFE_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
            return false;
        }
        return hasCookies && !isBearer(authorization);
    }

    private static boolean isBearer(String authorization) {
        return authorization != null
                && authorization.length() > BEARER_PREFIX.length()
                && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length());
    }
}