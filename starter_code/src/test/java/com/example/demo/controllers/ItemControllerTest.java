package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemController itemController;

	@Test
	void getItems_returnsAllItems() {
		when(itemRepository.findAll()).thenReturn(List.of(new Item(), new Item()));

		ResponseEntity<List<Item>> response = itemController.getItems();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(2, response.getBody().size());
	}

	@Test
	void getItemById_itemExists_returnsItem() {
		Item item = new Item();
		item.setId(1L);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

		ResponseEntity<Item> response = itemController.getItemById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(item, response.getBody());
	}

	@Test
	void getItemById_itemMissing_returnsNotFound() {
		when(itemRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseEntity<Item> response = itemController.getItemById(99L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	void getItemsByName_itemsFound_returnsThem() {
		when(itemRepository.findByName("Widget")).thenReturn(List.of(new Item()));

		ResponseEntity<List<Item>> response = itemController.getItemsByName("Widget");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
	}

	@Test
	void getItemsByName_emptyList_returnsNotFound() {
		when(itemRepository.findByName("Nothing")).thenReturn(List.of());

		ResponseEntity<List<Item>> response = itemController.getItemsByName("Nothing");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	void getItemsByName_nullList_returnsNotFound() {
		when(itemRepository.findByName("Nothing")).thenReturn(null);

		ResponseEntity<List<Item>> response = itemController.getItemsByName("Nothing");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
}
