package com.example.demo.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenVerificationFilter extends BasicAuthenticationFilter {

	private final JWTVerifier verifier;

	public JwtTokenVerificationFilter(AuthenticationManager authenticationManager, Algorithm algorithm) {
		super(authenticationManager);
		this.verifier = JWT.require(algorithm).build();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			authenticate(header.substring(SecurityConstants.TOKEN_PREFIX.length()));
		}
		chain.doFilter(request, response);
	}

	private void authenticate(String token) {
		try {
			String username = verifier.verify(token).getSubject();
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(
					new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
			SecurityContextHolder.setContext(context);
		} catch (JWTVerificationException e) {
			logger.debug("Rejected invalid JWT", e);
		}
	}
}
