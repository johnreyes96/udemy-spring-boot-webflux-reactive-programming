package com.bolsadeideas.springboot.webflux.app;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductoService service;

	@Test
	public void listarTest() {
		
		client.get()
		.uri("/api/v2/productos")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class)
		.consumeWith(response -> {
			List<Producto> productos = response.getResponseBody();
			productos.forEach(producto -> {
				System.out.println(producto.getNombre());
			});
			Assertions.assertThat(productos.size() > 0).isTrue();
		});
	}

	@Test
	public void verTest() {
		Producto producto = service.findByNombre("TV Panasonic Pantalla LCD").block();
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody().jsonPath("$.id").isNotEmpty()
 				.jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");
	}

	@Test
	public void ver2Test() {
		Producto producto = service.findByNombre("TV Panasonic Pantalla LCD").block();
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto product = response.getResponseBody();
			
			Assertions.assertThat(product.getId()).isNotEmpty();
			Assertions.assertThat(product.getId().length() > 0).isTrue();
			Assertions.assertThat(product.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");
		});
	}
	
	@Test
	public void crearTest() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post()
		.uri("/api/v2/productos")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody().jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Mesa comedor")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
	}
	
	@Test
	public void crear2Test() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post()
		.uri("/api/v2/productos")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto product = response.getResponseBody();
			
			Assertions.assertThat(product.getId()).isNotEmpty();
			Assertions.assertThat(product.getNombre()).isEqualTo("Mesa comedor");
			Assertions.assertThat(product.getCategoria().getNombre()).isNotEmpty();
		});
	}
}