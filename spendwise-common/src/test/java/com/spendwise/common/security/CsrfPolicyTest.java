package com.spendwise.common.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CsrfPolicyTest {

    private static final String BEARER = "Bearer eyJhbGciOiJSUzI1NiJ9.payload.signature";

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD", "OPTIONS", "TRACE", "get"})
    void safeMethodsNeverRequireCsrf(String method) {
        assertThat(CsrfPolicy.requiresCsrfProtection(method, true, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE", "post"})
    void stateChangingCookieRequestWithoutBearerRequiresCsrf(String method) {
        assertThat(CsrfPolicy.requiresCsrfProtection(method, true, null)).isTrue();
    }

    @Test
    void bearerTokenRequestDoesNotRequireCsrfEvenWithCookies() {
        assertThat(CsrfPolicy.requiresCsrfProtection("POST", true, BEARER)).isFalse();
    }

    @Test
    void bearerSchemeIsCaseInsensitive() {
        assertThat(CsrfPolicy.requiresCsrfProtection("POST", true, "bearer abc")).isFalse();
    }

    @Test
    void requestWithoutCookiesDoesNotRequireCsrf() {
        assertThat(CsrfPolicy.requiresCsrfProtection("POST", false, null)).isFalse();
    }

    @Test
    void basicCredentialsWithCookiesStillRequireCsrf() {
        assertThat(CsrfPolicy.requiresCsrfProtection("DELETE", true, "Basic dXNlcjpwYXNz")).isTrue();
    }

    @Test
    void emptyBearerValueIsNotTreatedAsBearer() {
        assertThat(CsrfPolicy.requiresCsrfProtection("POST", true, "Bearer ")).isTrue();
    }

    @Test
    void missingMethodDoesNotRequireCsrf() {
        assertThat(CsrfPolicy.requiresCsrfProtection(null, true, null)).isFalse();
    }
}