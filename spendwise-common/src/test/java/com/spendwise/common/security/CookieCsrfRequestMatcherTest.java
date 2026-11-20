package com.spendwise.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CookieCsrfRequestMatcherTest {

    private final CookieCsrfRequestMatcher matcher = new CookieCsrfRequestMatcher();

    @Test
    void cookiePostWithoutBearerRequiresCsrf() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/transactions");
        request.addHeader(HttpHeaders.COOKIE, "SESSION=abc");

        assertThat(matcher.matches(request)).isTrue();
    }

    @Test
    void bearerPostWithCookieDoesNotRequireCsrf() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/transactions");
        request.addHeader(HttpHeaders.COOKIE, "SESSION=abc");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-value");

        assertThat(matcher.matches(request)).isFalse();
    }

    @Test
    void postWithoutCookieDoesNotRequireCsrf() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/transactions");

        assertThat(matcher.matches(request)).isFalse();
    }

    @Test
    void cookieGetDoesNotRequireCsrf() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/transactions");
        request.addHeader(HttpHeaders.COOKIE, "SESSION=abc");

        assertThat(matcher.matches(request)).isFalse();
    }
}