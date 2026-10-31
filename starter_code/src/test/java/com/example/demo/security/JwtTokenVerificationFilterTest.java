package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class JwtTokenVerificationFilterTest {

	private final Algorithm algorithm = Algorithm.HMAC512("test-secret-test-secret-test-secret");

	private JwtTokenVerificationFilter filter;
	private FilterChain chain;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		filter = new JwtTokenVerificationFilter(mock(AuthenticationManager.class), algorithm);
		chain = mock(FilterChain.class);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private String tokenFor(String username, Algorithm signingAlgorithm) {
		return JWT.create()
				.withSubject(username)
				.withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
				.sign(signingAlgorithm);
	}

	@Test
	void validToken_authenticatesUserAndContinuesChain() throws IOException, ServletException {
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor("roxana", algorithm));

		filter.doFilterInternal(request, response, chain);

		assertEquals("roxana", SecurityContextHolder.getContext().getAuthentication().getName());
		verify(chain).doFilter(request, response);
	}

	@Test
	void noAuthorizationHeader_staysAnonymousAndContinuesChain() throws IOException, ServletException {
		filter.doFilterInternal(request, response, chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(chain).doFilter(request, response);
	}

	@Test
	void headerWithoutBearerPrefix_staysAnonymous() throws IOException, ServletException {
		request.addHeader(HttpHeaders.AUTHORIZATION, tokenFor("roxana", algorithm));

		filter.doFilterInternal(request, response, chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(chain).doFilter(request, response);
	}

	@Test
	void tokenSignedWithWrongKey_staysAnonymous() throws IOException, ServletException {
		Algorithm otherAlgorithm = Algorithm.HMAC512("a-completely-different-secret-key");
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor("roxana", otherAlgorithm));

		filter.doFilterInternal(request, response, chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(chain).doFilter(request, response);
	}

	@Test
	void garbageToken_staysAnonymous() throws IOException, ServletException {
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token");

		filter.doFilterInternal(request, response, chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(chain).doFilter(request, response);
	}
}
