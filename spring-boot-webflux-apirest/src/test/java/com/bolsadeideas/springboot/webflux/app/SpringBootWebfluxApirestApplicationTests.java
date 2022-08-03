package com.bolsadeideas.springboot.webflux.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductService service;
	
	@Value("${config.base.endpoint}")
	private String url;

	@Test
	public void listarTest() {
		
		client.get()
		.uri(url)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Product.class)
		.consumeWith(response -> {
			List<Product> products = response.getResponseBody();
			products.forEach(product -> {
				System.out.println(product.getName());
			});
			Assertions.assertThat(products.size() > 0).isTrue();
		});
	}

	@Test
	public void verTest() {
		Product product = service.findByName("TV Panasonic Pantalla LCD").block();
		
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody().jsonPath("$.id").isNotEmpty()
 				.jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");
	}

	@Test
	public void ver2Test() {
		Product product = service.findByName("TV Panasonic Pantalla LCD").block();
		
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Product.class)
		.consumeWith(response -> {
			Product productResponse = response.getResponseBody();
			
			Assertions.assertThat(productResponse.getId()).isNotEmpty();
			Assertions.assertThat(productResponse.getId().length() > 0).isTrue();
			Assertions.assertThat(productResponse.getName()).isEqualTo("TV Panasonic Pantalla LCD");
		});
	}
	
	@Test
	public void crearTest() {
		Category category = service.findCategoryByName("Muebles").block();
		Product product = new Product("Mesa comedor", 100.00, category);
		
		client.post()
		.uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(product), Product.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody().jsonPath("$.producto.id").isNotEmpty()
				.jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
				.jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}
	
	@Test
	public void crear2Test() {
		Category category = service.findCategoryByName("Muebles").block();
		Product product = new Product("Mesa comedor", 100.00, category);
		
		client.post()
		.uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(product), Product.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		.consumeWith(response -> {
			Object object = response.getResponseBody().get("producto");
			Product productConverted = new ObjectMapper().convertValue(object, Product.class);
			Assertions.assertThat(productConverted.getId()).isNotEmpty();
			Assertions.assertThat(productConverted.getName()).isEqualTo("Mesa comedor");
			Assertions.assertThat(productConverted.getCategory().getName()).isNotEmpty();
		});
	}
	
	@Test
	public void editarTest() {
		Product product = service.findByName("Sony Notebook").block();
		Category category = service.findCategoryByName("Electrónico").block();
		Product productEdit = new Product("Asus Notebook", 700.00, category);
		
		client.put()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(productEdit), Product.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody().jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Asus Notebook")
				.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
	}
	
	@Test
	public void eliminarTest() {
		Product product = service.findByName("Mica Cómoda 5 Cajones").block();
		
		client.delete()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();
		
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody().isEmpty();
	}
}
