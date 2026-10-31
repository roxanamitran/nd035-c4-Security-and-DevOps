package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.LoginRequest;
import com.example.demo.model.requests.ModifyCartRequest;

class ModelTest {

	private Item item;

	@BeforeEach
	void setUp() {
		item = new Item();
		item.setId(1L);
		item.setName("Widget");
		item.setDescription("A small widget");
		item.setPrice(new BigDecimal("3.00"));
	}

	@Test
	void cart_addItem_increasesTotalAndItemCount() {
		Cart cart = new Cart();

		cart.addItem(item);
		cart.addItem(item);

		assertEquals(2, cart.getItems().size());
		assertEquals(new BigDecimal("6.00"), cart.getTotal());
	}

	@Test
	void cart_removeItem_decreasesTotalAndItemCount() {
		Cart cart = new Cart();
		cart.addItem(item);
		cart.addItem(item);

		cart.removeItem(item);

		assertEquals(1, cart.getItems().size());
		assertEquals(new BigDecimal("3.00"), cart.getTotal());
	}

	@Test
	void cart_removeItemFromEmptyCart_doesNotCrash() {
		Cart cart = new Cart();

		cart.removeItem(item);

		assertTrue(cart.getItems().isEmpty());
		assertEquals(new BigDecimal("-3.00"), cart.getTotal());
	}

	@Test
	void cart_gettersAndSetters_storeValues() {
		Cart cart = new Cart();
		User user = new User();

		cart.setId(5L);
		cart.setUser(user);
		cart.setItems(List.of(item));
		cart.setTotal(BigDecimal.TEN);

		assertEquals(5L, cart.getId());
		assertSame(user, cart.getUser());
		assertEquals(1, cart.getItems().size());
		assertEquals(BigDecimal.TEN, cart.getTotal());
	}

	@Test
	void item_gettersAndSetters_storeValues() {
		assertEquals(1L, item.getId());
		assertEquals("Widget", item.getName());
		assertEquals("A small widget", item.getDescription());
		assertEquals(new BigDecimal("3.00"), item.getPrice());
	}

	@Test
	void item_equalsIsBasedOnId() {
		Item sameId = new Item();
		sameId.setId(1L);
		Item otherId = new Item();
		otherId.setId(2L);
		Item noId = new Item();

		assertEquals(item, item);
		assertEquals(item, sameId);
		assertEquals(item.hashCode(), sameId.hashCode());
		assertNotEquals(item, otherId);
		assertNotEquals(item, noId);
		assertNotEquals(noId, item);
		assertEquals(noId, new Item());
		assertFalse(item.equals(null));
		assertFalse(item.equals("not an item"));
	}

	@Test
	void user_gettersAndSetters_storeValues() {
		User user = new User();
		Cart cart = new Cart();

		user.setId(7L);
		user.setUsername("roxana");
		user.setPassword("secret");
		user.setCart(cart);

		assertEquals(7L, user.getId());
		assertEquals("roxana", user.getUsername());
		assertEquals("secret", user.getPassword());
		assertSame(cart, user.getCart());
	}

	@Test
	void userOrder_createFromCart_copiesItemsTotalAndUser() {
		User user = new User();
		Cart cart = new Cart();
		cart.setUser(user);
		cart.addItem(item);

		UserOrder order = UserOrder.createFromCart(cart);

		assertEquals(1, order.getItems().size());
		assertEquals(new BigDecimal("3.00"), order.getTotal());
		assertSame(user, order.getUser());
	}

	@Test
	void userOrder_gettersAndSetters_storeValues() {
		UserOrder order = new UserOrder();
		User user = new User();

		order.setId(9L);
		order.setUser(user);
		order.setItems(List.of(item));
		order.setTotal(BigDecimal.ONE);

		assertEquals(9L, order.getId());
		assertSame(user, order.getUser());
		assertEquals(1, order.getItems().size());
		assertEquals(BigDecimal.ONE, order.getTotal());
	}

	@Test
	void createUserRequest_gettersAndSetters_storeValues() {
		CreateUserRequest request = new CreateUserRequest();

		request.setUsername("roxana");
		request.setPassword("password123");
		request.setConfirmPassword("password123");

		assertEquals("roxana", request.getUsername());
		assertEquals("password123", request.getPassword());
		assertEquals("password123", request.getConfirmPassword());
	}

	@Test
	void modifyCartRequest_gettersAndSetters_storeValues() {
		ModifyCartRequest request = new ModifyCartRequest();

		request.setUsername("roxana");
		request.setItemId(4L);
		request.setQuantity(2);

		assertEquals("roxana", request.getUsername());
		assertEquals(4L, request.getItemId());
		assertEquals(2, request.getQuantity());
	}

	@Test
	void loginRequest_recordExposesValues() {
		LoginRequest request = new LoginRequest("roxana", "password123");

		assertEquals("roxana", request.username());
		assertEquals("password123", request.password());
	}
}
