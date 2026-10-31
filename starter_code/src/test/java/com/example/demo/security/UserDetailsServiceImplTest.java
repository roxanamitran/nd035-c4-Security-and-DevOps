package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl service;

	@Test
	void loadUserByUsername_userExists_returnsUserDetails() {
		User user = new User();
		user.setUsername("roxana");
		user.setPassword("hashed");
		when(userRepository.findByUsername("roxana")).thenReturn(user);

		UserDetails details = service.loadUserByUsername("roxana");

		assertEquals("roxana", details.getUsername());
		assertEquals("hashed", details.getPassword());
	}

	@Test
	void loadUserByUsername_userMissing_throwsException() {
		when(userRepository.findByUsername("ghost")).thenReturn(null);

		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
	}
}
