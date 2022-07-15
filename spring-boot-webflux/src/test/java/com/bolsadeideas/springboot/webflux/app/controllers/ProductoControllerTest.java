package com.bolsadeideas.springboot.webflux.app.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bolsadeideas.springboot.webflux.app.models.services.IProductoService;

public class ProductoControllerTest {
	
	WebTestClient client;
	
	@MockBean
    private IProductoService service;
	
	@Test
	public void listarTest() throws Exception {
		client = WebTestClient.bindToServer()
				.baseUrl("http://localhost:8090")
				.build();

		client.get()
		.uri("/api/productos/listar")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON);
	}
}
