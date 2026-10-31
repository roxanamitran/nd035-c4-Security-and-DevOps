package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;

class JwtLoginFilterTest {

	private final Algorithm algorithm = Algorithm.HMAC512("test-secret-test-secret-test-secret");

	private AuthenticationManager authenticationManager;
	private JwtLoginFilter filter;

	@BeforeEach
	void setUp() {
		authenticationManager = mock(AuthenticationManager.class);
		filter = new JwtLoginFilter(authenticationManager, algorithm);
	}

	private MockHttpServletRequest requestWithBody(String json) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
		request.setContent(json.getBytes(StandardCharsets.UTF_8));
		return request;
	}

	@Test
	void attemptAuthentication_validJson_passesCredentialsToAuthenticationManager() {
		Authentication expected = new UsernamePasswordAuthenticationToken("roxana", null);
		when(authenticationManager.authenticate(any())).thenReturn(expected);

		Authentication result = filter.attemptAuthentication(
				requestWithBody("{\"username\":\"roxana\",\"password\":\"password123\"}"),
				new MockHttpServletResponse());

		assertEquals(expected, result);
		ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
		verify(authenticationManager).authenticate(captor.capture());
		assertEquals("roxana", captor.getValue().getPrincipal());
		assertEquals("password123", captor.getValue().getCredentials());
	}

	@Test
	void attemptAuthentication_malformedJson_throwsBadCredentials() {
		assertThrows(BadCredentialsException.class, () -> filter.attemptAuthentication(
				requestWithBody("this is not json"), new MockHttpServletResponse()));
	}

	@Test
	void attemptAuthentication_emptyJsonValue_throwsBadCredentials() {
		assertThrows(BadCredentialsException.class, () -> filter.attemptAuthentication(
				requestWithBody("null"), new MockHttpServletResponse()));
	}

	@Test
	void successfulAuthentication_addsSignedTokenToAuthorizationHeader() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		Authentication authentication = new UsernamePasswordAuthenticationToken("roxana", null);

		filter.successfulAuthentication(new MockHttpServletRequest(), response, mock(FilterChain.class),
				authentication);

		String header = response.getHeader(HttpHeaders.AUTHORIZATION);
		assertNotNull(header);
		assertTrue(header.startsWith(SecurityConstants.TOKEN_PREFIX));

		String token = header.substring(SecurityConstants.TOKEN_PREFIX.length());
		DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
		assertEquals("roxana", decoded.getSubject());
		assertTrue(decoded.getExpiresAt().getTime() > System.currentTimeMillis());
	}
}
