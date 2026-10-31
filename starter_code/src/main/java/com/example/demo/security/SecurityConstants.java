package com.example.demo.security;

public final class SecurityConstants {

	public static final long EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000L;
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String SIGN_UP_URL = "/api/user/create";

	private SecurityConstants() {
	}
}
