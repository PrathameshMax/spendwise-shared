package com.spendwise.common.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive resource servers: requires CSRF protection only where CSRF is possible,
 * as decided by {@link CsrfPolicy}.
 */
public final class CookieCsrfServerWebExchangeMatcher implements ServerWebExchangeMatcher {

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        boolean required = CsrfPolicy.requiresCsrfProtection(
                request.getMethod().name(),
                request.getHeaders().containsKey(HttpHeaders.COOKIE),
                request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        return required ? MatchResult.match() : MatchResult.notMatch();
    }
}