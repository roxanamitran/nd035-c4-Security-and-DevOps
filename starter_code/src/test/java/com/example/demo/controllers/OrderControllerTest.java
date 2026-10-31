package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderController orderController;

	private User user;

	@BeforeEach
	void setUp() {
		Item item = new Item();
		item.setId(1L);
		item.setPrice(new BigDecimal("4.00"));

		Cart cart = new Cart();
		cart.addItem(item);

		user = new User();
		user.setUsername("roxana");
		user.setCart(cart);
		cart.setUser(user);
	}

	@Test
	void submit_existingUser_savesOrderBuiltFromCart() {
		when(userRepository.findByUsername("roxana")).thenReturn(user);

		ResponseEntity<UserOrder> response = orderController.submit("roxana");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		UserOrder order = response.getBody();
		assertEquals(1, order.getItems().size());
		assertEquals(new BigDecimal("4.00"), order.getTotal());
		assertSame(user, order.getUser());
		verify(orderRepository).save(order);
	}

	@Test
	void submit_unknownUser_returnsNotFound() {
		when(userRepository.findByUsername("ghost")).thenReturn(null);

		ResponseEntity<UserOrder> response = orderController.submit("ghost");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(orderRepository);
	}

	@Test
	void getOrdersForUser_existingUser_returnsTheirOrders() {
		List<UserOrder> orders = List.of(new UserOrder(), new UserOrder());
		when(userRepository.findByUsername("roxana")).thenReturn(user);
		when(orderRepository.findByUser(user)).thenReturn(orders);

		ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("roxana");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(2, response.getBody().size());
	}

	@Test
	void getOrdersForUser_unknownUser_returnsNotFound() {
		when(userRepository.findByUsername("ghost")).thenReturn(null);

		ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("ghost");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(orderRepository);
	}
}
