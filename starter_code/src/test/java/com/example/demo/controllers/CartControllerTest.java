package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

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
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CartRepository cartRepository;

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private CartController cartController;

	private User user;
	private Cart cart;
	private Item item;

	@BeforeEach
	void setUp() {
		cart = new Cart();
		user = new User();
		user.setUsername("roxana");
		user.setCart(cart);

		item = new Item();
		item.setId(1L);
		item.setPrice(new BigDecimal("2.50"));
	}

	private ModifyCartRequest request(String username, long itemId, int quantity) {
		ModifyCartRequest request = new ModifyCartRequest();
		request.setUsername(username);
		request.setItemId(itemId);
		request.setQuantity(quantity);
		return request;
	}

	@Test
	void addToCart_validRequest_addsItemsAndUpdatesTotal() {
		when(userRepository.findByUsername("roxana")).thenReturn(user);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

		ResponseEntity<Cart> response = cartController.addTocart(request("roxana", 1L, 3));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(3, response.getBody().getItems().size());
		assertEquals(new BigDecimal("7.50"), response.getBody().getTotal());
		verify(cartRepository).save(cart);
	}

	@Test
	void addToCart_unknownUser_returnsNotFound() {
		when(userRepository.findByUsername("ghost")).thenReturn(null);

		ResponseEntity<Cart> response = cartController.addTocart(request("ghost", 1L, 1));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(cartRepository);
	}

	@Test
	void addToCart_unknownItem_returnsNotFound() {
		when(userRepository.findByUsername("roxana")).thenReturn(user);
		when(itemRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseEntity<Cart> response = cartController.addTocart(request("roxana", 99L, 1));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(cartRepository);
	}

	@Test
	void removeFromCart_validRequest_removesItemsAndUpdatesTotal() {
		cart.addItem(item);
		cart.addItem(item);
		cart.addItem(item);
		when(userRepository.findByUsername("roxana")).thenReturn(user);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

		ResponseEntity<Cart> response = cartController.removeFromcart(request("roxana", 1L, 2));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().getItems().size());
		assertEquals(new BigDecimal("2.50"), response.getBody().getTotal());
		verify(cartRepository).save(cart);
	}

	@Test
	void removeFromCart_unknownUser_returnsNotFound() {
		when(userRepository.findByUsername("ghost")).thenReturn(null);

		ResponseEntity<Cart> response = cartController.removeFromcart(request("ghost", 1L, 1));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(cartRepository);
	}

	@Test
	void removeFromCart_unknownItem_returnsNotFound() {
		when(userRepository.findByUsername("roxana")).thenReturn(user);
		when(itemRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseEntity<Cart> response = cartController.removeFromcart(request("roxana", 99L, 1));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(cartRepository);
	}
}
