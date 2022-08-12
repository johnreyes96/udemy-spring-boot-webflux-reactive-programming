package com.bolsadeideas.springboot.webflux.app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.File;

import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductService;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class ProductControllerTest {

	@Spy
	@InjectMocks
	private ProductController productController;
	@Spy
	private Product productSpy;
	@Mock
	private ProductService productService;

	@BeforeEach
	void init() {
		productController = new ProductController();
		productSpy = new Product();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void createWithPhotoWhenProductCreateAtIsNullMustSetItAndReturnResponseEntityOfProductPersistedTest() {
		String filename = "filename";
		FilePart file = Mockito.mock(FilePart.class);
		doReturn(filename).when(file).filename();
		doNothing().when(productSpy).setPhotoWithFormattedName(Mockito.anyString(), Mockito.eq(filename));
		doReturn(Mono.empty()).when(file).transferTo(Mockito.any(File.class));
		doReturn(Mono.just(productSpy)).when(productService).save(productSpy);
		doReturn("id").when(productSpy).getId();

		StepVerifier.create(productController.createWithPhoto(productSpy, file))
		.expectNextCount(1)
		.expectComplete()
		.verify();
		
		assertNotNull(productSpy.getCreateAt());
	}
}
