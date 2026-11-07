package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CartRepository cartRepository;

	@Mock
	private BCryptPasswordEncoder encoder;

	@InjectMocks
	private UserController userController;

	private ListAppender<ILoggingEvent> appender;
	private Logger logger;

	@BeforeEach
	void attachAppender() {
		logger = (Logger) LoggerFactory.getLogger(UserController.class);
		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach
	void detachAppender() {
		logger.detachAppender(appender);
	}

	private List<String> logged() {
		return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
	}

	private CreateUserRequest request(String password, String confirm) {
		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("roxana");
		request.setPassword(password);
		request.setConfirmPassword(confirm);
		return request;
	}

	@Test
	void createUser_validRequest_savesUserWithEncodedPassword() {
		when(encoder.encode("password123")).thenReturn("hashed");

		ResponseEntity<User> response = userController.createUser(request("password123", "password123"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		User user = response.getBody();
		assertNotNull(user);
		assertEquals("roxana", user.getUsername());
		assertEquals("hashed", user.getPassword());
		assertNotNull(user.getCart());
		verify(cartRepository).save(user.getCart());
		verify(userRepository).save(user);
		assertEquals(List.of("event=CREATE_USER_SUCCESS username=roxana"), logged());
	}

	@Test
	void createUser_passwordTooShort_returnsBadRequest() {
		ResponseEntity<User> response = userController.createUser(request("short", "short"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(List.of("event=CREATE_USER_FAILURE username=roxana reason=PASSWORD_TOO_SHORT"), logged());
	}

	@Test
	void createUser_nullPassword_returnsBadRequest() {
		ResponseEntity<User> response = userController.createUser(request(null, null));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(List.of("event=CREATE_USER_FAILURE username=roxana reason=PASSWORD_TOO_SHORT"), logged());
	}

	@Test
	void createUser_passwordsDoNotMatch_returnsBadRequest() {
		ResponseEntity<User> response = userController.createUser(request("password123", "different123"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(List.of("event=CREATE_USER_FAILURE username=roxana reason=PASSWORD_MISMATCH"), logged());
	}

	@Test
	void findById_userExists_returnsUser() {
		User user = new User();
		user.setUsername("roxana");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		ResponseEntity<User> response = userController.findById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("roxana", response.getBody().getUsername());
	}

	@Test
	void findById_userMissing_returnsNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseEntity<User> response = userController.findById(1L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	void findByUserName_userExists_returnsUser() {
		User user = new User();
		user.setUsername("roxana");
		when(userRepository.findByUsername("roxana")).thenReturn(user);

		ResponseEntity<User> response = userController.findByUserName("roxana");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(user, response.getBody());
	}

	@Test
	void findByUserName_userMissing_returnsNotFound() {
		when(userRepository.findByUsername("ghost")).thenReturn(null);

		ResponseEntity<User> response = userController.findByUserName("ghost");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
}
