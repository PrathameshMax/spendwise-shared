package com.spendwise.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;

import static org.assertj.core.api.Assertions.assertThat;

class CookieCsrfServerWebExchangeMatcherTest {

    private final CookieCsrfServerWebExchangeMatcher matcher = new CookieCsrfServerWebExchangeMatcher();

    @Test
    void cookiePostWithoutBearerRequiresCsrf() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/analytics")
                        .header(HttpHeaders.COOKIE, "SESSION=abc"));

        assertThat(isMatch(exchange)).isTrue();
    }

    @Test
    void bearerPostWithCookieDoesNotRequireCsrf() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/analytics")
                        .header(HttpHeaders.COOKIE, "SESSION=abc")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-value"));

        assertThat(isMatch(exchange)).isFalse();
    }

    @Test
    void postWithoutCookieDoesNotRequireCsrf() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/analytics"));

        assertThat(isMatch(exchange)).isFalse();
    }

    @Test
    void cookieGetDoesNotRequireCsrf() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/analytics")
                        .header(HttpHeaders.COOKIE, "SESSION=abc"));

        assertThat(isMatch(exchange)).isFalse();
    }

    private boolean isMatch(MockServerWebExchange exchange) {
        MatchResult result = matcher.matches(exchange).block();
        return result != null && result.isMatch();
    }
}