package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
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
	}

	@Test
	void createUser_passwordTooShort_returnsBadRequest() {
		ResponseEntity<User> response = userController.createUser(request("short", "short"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void createUser_nullPassword_returnsBadRequest() {
		ResponseEntity<User> response = userController.createUser(request(null, null));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	void createUser_passwordsDoNotMatch_returnsBadRequest() {
		ResponseEntity<User> response = userController.createUser(request("password123", "different123"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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
