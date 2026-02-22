package ru.mitrohinayulya.zabotushka.interceptor;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a method for automatic token refresh and retry when the access token
/// has expired (HTTP 401). Refresh is attempted once; if it also fails,
/// the error is propagated — a re-login is needed.
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RefreshTokenOnExpiry {
}
