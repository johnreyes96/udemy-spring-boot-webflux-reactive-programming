package com.bolsadeideas.springboot.webflux.app.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductService;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
			.expectNextMatches(productExpected -> productSpy.equals(productExpected.getBody()))
			.expectComplete()
			.verify();
		
		assertNotNull(productSpy.getCreateAt());
		verify(file).filename();
		verify(productSpy).setPhotoWithFormattedName(Mockito.anyString(), Mockito.eq(filename));
		verify(file).transferTo(Mockito.any(File.class));
		verify(productService).save(productSpy);
	}

	@Test
	public void createWithPhotoWhenProductCreateAtIsNotNullMustNotSetItAndReturnResponseEntityOfProductPersistedTest() {
		Date createAt = new Date();
		String filename = "filename";
		FilePart file = Mockito.mock(FilePart.class);
		doReturn(createAt).when(productSpy).getCreateAt();
		doReturn(filename).when(file).filename();
		doNothing().when(productSpy).setPhotoWithFormattedName(Mockito.anyString(), Mockito.eq(filename));
		doReturn(Mono.empty()).when(file).transferTo(Mockito.any(File.class));
		doReturn(Mono.just(productSpy)).when(productService).save(productSpy);
		doReturn("id").when(productSpy).getId();

		StepVerifier.create(productController.createWithPhoto(productSpy, file))
			.expectNextMatches(productExpected -> productSpy.equals(productExpected.getBody()))
			.expectComplete()
			.verify();
		
		assertEquals(createAt, productSpy.getCreateAt());
		verify(file).filename();
		verify(productSpy).setPhotoWithFormattedName(Mockito.anyString(), Mockito.eq(filename));
		verify(file).transferTo(Mockito.any(File.class));
		verify(productService).save(productSpy);
	}

	@Test
	public void uploadWhenFindByIdReturnProductThenMustReturnResponseEntityOfProductPersistedTest() {
		String id = "id";
		String filename = "filename";
		FilePart file = Mockito.mock(FilePart.class);
		doReturn(Mono.just(productSpy)).when(productService).findById(id);
		doReturn(filename).when(file).filename();
		doNothing().when(productSpy).setPhotoWithFormattedName(Mockito.anyString(), Mockito.eq(filename));
		doReturn(Mono.empty()).when(file).transferTo(Mockito.any(File.class));
		doReturn(Mono.just(productSpy)).when(productService).save(productSpy);

		StepVerifier.create(productController.upload(id, file))
			.expectNextMatches(productExpected -> productSpy.equals(productExpected.getBody()))
			.expectComplete()
			.verify();
		
		verify(productService).findById(id);
		verify(file).filename();
		verify(productSpy).setPhotoWithFormattedName(Mockito.anyString(), Mockito.eq(filename));
		verify(file).transferTo(Mockito.any(File.class));
		verify(productService).save(productSpy);
	}

	@Test
	public void uploadWhenFindByIdReturnMonoEmptyThenMustReturnResponseEntityNotFoundTest() {
		String id = "id";
		FilePart file = Mockito.mock(FilePart.class);
		doReturn(Mono.empty()).when(productService).findById(id);

		StepVerifier.create(productController.upload(id, file))
			.expectNextMatches(notFound -> ResponseEntity.notFound().build().equals(notFound))
			.expectComplete()
			.verify();
		
		verify(productService).findById(id);
		verify(file, Mockito.never()).filename();
		verify(productSpy, Mockito.never()).setPhotoWithFormattedName(Mockito.anyString(),
				Mockito.anyString());
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
		verify(productService, Mockito.never()).save(Mockito.any());
	}

	@Test
	public void listWhenFindAllReturnEmptyProductThenMustReturnResponseEntityFluxEmptyTest() {
		Flux<Product> products = Flux.empty();
		doReturn(products).when(productService).findAll();

		StepVerifier.create(productController.list())
			.expectNextMatches(productsExpected -> products.equals(productsExpected.getBody()))
			.expectComplete()
			.verify();
		
		verify(productService).findAll();
	}

	@Test
	public void listWhenFindAllReturnProductsThenMustReturnResponseEntityProductsFluxTest() {
		Flux<Product> products = Flux.just(new Product());
		doReturn(products).when(productService).findAll();

		StepVerifier.create(productController.list())
			.expectNextMatches(productsExpected -> products.equals(productsExpected.getBody()))
			.expectComplete()
			.verify();
		
		verify(productService).findAll();
	}

	@Test
	public void viewWhenFindByIdReturnProductThenMustReturnResponseEntityProductTest() {
		Product product = new Product();
		String id = "id";
		doReturn(Mono.just(product)).when(productService).findById(id);

		StepVerifier.create(productController.view(id))
			.expectNextMatches(productExpected -> product.equals(productExpected.getBody()))
			.expectComplete()
			.verify();
		
		verify(productService).findById(id);
	}

	@Test
	public void viewWhenFindByIdReturnMonoEmptyThenMustReturnResponseEntityNotFoundTest() {
		String id = "id";
		doReturn(Mono.empty()).when(productService).findById(id);

		StepVerifier.create(productController.view(id))
			.expectNextMatches(notFound -> ResponseEntity.notFound().build().equals(notFound))
			.expectComplete()
			.verify();
		
		verify(productService).findById(id);
	}

	@Test
	public void createWhenProductCreateAtIsNullMustSaveProductAndReturnResponseEntityMapTest() {
		Product product = new Product();
		product.setId("id");
		Mono<Product> monoProduct = Mono.just(product);
		String message = "Product created successfully";
		Date dateSmaller = new Date();
		doReturn(Mono.just(product)).when(productService).save(product);

		StepVerifier.create(productController.create(monoProduct))
			.expectNextMatches(mapExpected -> 3 == mapExpected.getBody().size()
					&& product.equals((Product) mapExpected.getBody().get("product"))
					&& message.equals((String) mapExpected.getBody().get("message"))
					&& dateSmaller.getTime() < ((Date) mapExpected.getBody().get("timestamp")).getTime())
			.expectComplete()
			.verify();

		assertNotNull(product.getCreateAt());
		verify(productService).save(product);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createWhenProductCreateAtIsNotNullMustNotSetItAndSaveProductThrowExceptionReturnResponseEntityMapTest() {
		Date createAt = new Date();
		FieldError fieldError = Mockito.mock(FieldError.class);
		List<FieldError> list = new ArrayList<>();
		list.add(fieldError);
		WebExchangeBindException exception = Mockito.mock(WebExchangeBindException.class);
		Product product = new Product();
		product.setCreateAt(createAt);
		Mono<Product> monoProduct = Mono.just(product);
		String error = "The field name is empty";
		List<String> errors = new ArrayList<>();
		errors.add(error);
		Date dateSmaller = new Date();
		doReturn(Mono.error(exception)).when(productService).save(product);
		doReturn(list).when(exception).getFieldErrors();
		doReturn("name").when(fieldError).getField();
		doReturn("is empty").when(fieldError).getDefaultMessage();

		StepVerifier.create(productController.create(monoProduct))
			.expectNextMatches(mapExpected -> 3 == mapExpected.getBody().size()
					&& 1 == ((List<String>) mapExpected.getBody().get("errors")).size()
					&& error.equals(((List<String>) mapExpected.getBody().get("errors")).get(0))
					&& dateSmaller.getTime() < ((Date) mapExpected.getBody().get("timestamp")).getTime()
					&& HttpStatus.BAD_REQUEST.value() == (int) mapExpected.getBody().get("status"))
			.expectComplete()
			.verify();

		assertEquals(createAt, product.getCreateAt());
		verify(productService).save(product);
		verify(exception).getFieldErrors();
		verify(fieldError).getField();
		verify(fieldError).getDefaultMessage();
	}

	@Test
	public void editWhenProductByIdReturnProductMustSetNamePriceAndCategoryAndReturnResponseEntityOfProductPersistedTest() {
		String id = "id";
		Category category = new Category();
		Product product = new Product();
		product.setName("name");
		product.setPrice(2d);
		product.setCategory(category);
		Product productPersisted = new Product();
		productPersisted.setId("id");
		productPersisted.setName(product.getName());
		productPersisted.setPrice(product.getPrice());
		productPersisted.setCategory(product.getCategory());
		doReturn(Mono.just(productPersisted)).when(productService).findById(id);
		doReturn(Mono.just(productPersisted)).when(productService).save(productPersisted);

		StepVerifier.create(productController.edit(product, id))
			.expectNextMatches(productResult -> productPersisted.equals(productResult.getBody()))
			.expectComplete()
			.verify();

		verify(productService).findById(id);
		verify(productService).save(productPersisted);
	}

	@Test
	public void editWhenProductByIdReturnMonoEmptyMustNotSetAnyAttributeAndReturnResponseEntityNotFoundTest() {
		String id = "id";
		doReturn(Mono.empty()).when(productService).findById(id);

		StepVerifier.create(productController.edit(null, id))
			.expectNextMatches(notFound -> ResponseEntity.notFound().build().equals(notFound))
			.expectComplete()
			.verify();

		verify(productService).findById(id);
		verify(productService, Mockito.never()).save(Mockito.any());
	}
	
	@Test
	public void deleteWhenProductByIdReturnProductMustDeleteProductAndReturnResponseEntityNoContentTest() {
		String id = "id";
		Product product = new Product();
		doReturn(Mono.just(product)).when(productService).findById(id);
		doReturn(Mono.empty()).when(productService).delete(product);

		StepVerifier.create(productController.delete(id))
			.expectNextMatches(notFound -> HttpStatus.NO_CONTENT.equals(notFound.getStatusCode()))
			.expectComplete()
			.verify();

		verify(productService).findById(id);
		verify(productService).delete(product);
	}

	@Test
	public void deleteWhenProductByIdReturnMonoEmptyMustReturnResponseEntityNotFoundTest() {
		String id = "id";
		doReturn(Mono.empty()).when(productService).findById(id);

		StepVerifier.create(productController.delete(id))
			.expectNextMatches(notFound -> HttpStatus.NOT_FOUND.equals(notFound.getStatusCode()))
			.expectComplete()
			.verify();

		verify(productService).findById(id);
		verify(productService, Mockito.never()).delete(Mockito.any());
	}
}
