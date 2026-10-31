package com.example.demo.security;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.model.requests.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Algorithm algorithm;

	public JwtLoginFilter(AuthenticationManager authenticationManager, Algorithm algorithm) {
		super(authenticationManager);
		this.algorithm = algorithm;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		LoginRequest credentials = readCredentials(request);
		return getAuthenticationManager().authenticate(
				new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password()));
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain, Authentication authResult) {
		String token = JWT.create()
				.withSubject(authResult.getName())
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME_MS))
				.sign(algorithm);
		response.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token);
	}

	private LoginRequest readCredentials(HttpServletRequest request) {
		try {
			LoginRequest credentials = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
			if (credentials == null) {
				throw new BadCredentialsException("Empty login request");
			}
			return credentials;
		} catch (IOException e) {
			throw new BadCredentialsException("Malformed login request", e);
		}
	}
}
