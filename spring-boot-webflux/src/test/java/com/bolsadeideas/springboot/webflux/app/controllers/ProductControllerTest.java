package com.bolsadeideas.springboot.webflux.app.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Date;
import org.assertj.core.util.DateUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.bolsadeideas.springboot.webflux.app.models.documents.Category;
import com.bolsadeideas.springboot.webflux.app.models.documents.Product;
import com.bolsadeideas.springboot.webflux.app.models.services.IProductService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.controllers")
public class ProductControllerTest {

	@Spy
	@InjectMocks
	private ProductController productController;
	@Mock
	private IProductService service;

	@BeforeEach
	void init() {
		productController = new ProductController();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void categoriesWhenFindAllReturnZeroElementsThenMustReturnFluxEmptyTest() {
		doReturn(Flux.empty()).when(service).findAllCategories();

		StepVerifier.create(productController.categories())
		.expectNextCount(0)
		.expectComplete()
		.verify();

		verify(service).findAllCategories();
	}

	@Test
	public void categoriesWhenFindAllReturnOneElementThenMustReturnFluxWithOneCategoryTest() {
		Category category = new Category();
		category.setName("Deporte");
		doReturn(Flux.just(category)).when(service).findAllCategories();

		StepVerifier.create(productController.categories())
		.expectNextMatches(categoryExpected -> "Deporte".equals(categoryExpected.getName()))
		.expectComplete()
		.verify();

		verify(service).findAllCategories();
	}

	@Test
	public void categoriesWhenFindAllReturnElementsThenMustReturnFluxWithCategoriesTest() {
		Category sport = new Category();
		sport.setName("Deporte");
		Category furniture = new Category();
		furniture.setName("Muebles");
		doReturn(Flux.just(sport, furniture)).when(service).findAllCategories();

		StepVerifier.create(productController.categories())
		.expectNextMatches(categoryExpected -> "Deporte".equals(categoryExpected.getName()))
		.expectNextMatches(categoryExpected -> "Muebles".equals(categoryExpected.getName()))
		.expectComplete()
		.verify();

		verify(service).findAllCategories();
	}

	@Test
	public void viewPhotoWhenResourceThrowAnExceptionThenMustReturnMalformedURLExceptionTest() throws MalformedURLException {
		Path path = Mockito.mock(Path.class);
		String photoName = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2-SonyCamaraHDDigital";
		doReturn(path).when(productController).getPath();
		doReturn(path).when(path).resolve(photoName);
		doReturn(path).when(path).toAbsolutePath();
		doThrow(new MalformedURLException()).when(productController).getResource(path);
		
		Assertions.assertThrows(
				MalformedURLException.class,
	           () -> productController.viewPhoto(photoName)
	    );

		verify(productController).getPath();
		verify(path).resolve(photoName);
		verify(path).toAbsolutePath();
		verify(productController).getResource(path);
	}

	@Test
	public void viewPhotoWhenResourceIsValidThenMustReturnResponseEntityOKWithImageInBodyTest() throws MalformedURLException {
		Path path = Mockito.mock(Path.class);
		String photoName = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2-SonyCamaraHDDigital";
		Resource image = Mockito.mock(Resource.class);
		ResponseEntity<Resource> resourceExpected = ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"")
				.body(image);
		doReturn(path).when(productController).getPath();
		doReturn(path).when(path).resolve(photoName);
		doReturn(path).when(path).toAbsolutePath();
		doReturn(image).when(productController).getResource(path);
		
		StepVerifier.create(productController.viewPhoto(photoName))
		.expectNext(resourceExpected)
		.expectComplete()
		.verify();

		verify(productController).getPath();
		verify(path).resolve(photoName);
		verify(path).toAbsolutePath();
		verify(productController).getResource(path);
	}

	@Test
	public void viewWhenFindProductByIdThenMustSetProductAndTitleAndReturnMonoStringTest() {
		Product product = new Product();
		product.setId("id");
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.just(product)).when(service).findById(id);
		doReturn(model).when(model).addAttribute("product", product);
		doReturn(model).when(model).addAttribute("title", "Detalle del producto");

		StepVerifier.create(productController.view(model, id))
		.expectNextMatches(expected -> "view".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute("product", product);
		verify(model).addAttribute("title", "Detalle del producto");
	}

	@Test
	public void viewWhenFindProductByIdDoesNotExistsThenMustCreateNewProductAndThrowExceptionAndReturnMonoWithARedirectTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.empty()).when(service).findById(id);

		StepVerifier.create(productController.view(model, id))
		.expectNextMatches(redirectExpected -> "redirect:/list?error=no+existe+el+producto".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model, Mockito.never()).addAttribute(Mockito.anyString(), Mockito.any());
	}

	@Test
	public void listWhenFindAllWithNameUpperCaseReturnEmptyThenMustSetFluxEmptyAndTitleAndReturnStringTest() {
		Flux<Product> products = Flux.empty();
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllWithNameUpperCase();
		doReturn(model).when(model).addAttribute("products", products);
		doReturn(model).when(model).addAttribute("title", "Detalle Producto");

		String response = productController.list(model);

		Assert.assertEquals("list", response);
		verify(service).findAllWithNameUpperCase();
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void listWhenFindAllWithNameUpperCaseReturnElementsThenMustSetProductsAndTitleAndReturnStringTest() {
		Product laptop = new Product();
		laptop.setName("Sony Notebook");
		Product smartphone = new Product();
		smartphone.setName("Apple iPod");
		Flux<Product> products = Flux.just(laptop, smartphone);
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllWithNameUpperCase();
		doReturn(model).when(model).addAttribute("products", products);
		doReturn(model).when(model).addAttribute("title", "Detalle Producto");

		String response = productController.list(model);

		Assert.assertEquals("list", response);
		verify(service).findAllWithNameUpperCase();
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void createMustSetNewProductAndTitleAndNameButtonAndReturnStringTest() {
		Model model = Mockito.mock(Model.class);
		doReturn(model).when(model).addAttribute(Mockito.eq("product"), Mockito.any(Product.class));
		doReturn(model).when(model).addAttribute("title", "Crear producto");
		doReturn(model).when(model).addAttribute("button", "Crear");

		StepVerifier.create(productController.create(model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(model).addAttribute(Mockito.eq("product"), Mockito.any(Product.class));
		verify(model).addAttribute("title", "Crear producto");
		verify(model).addAttribute("button", "Crear");
	}

	@Test
	public void editWhenFindProductByIdThenMustSetProductAndTitleAndButtonAndReturnMonoStringTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		Product product = new Product();
		Mono<Product> productMono = Mono.just(product);
		doReturn(productMono).when(service).findById(id);
		doReturn(model).when(model).addAttribute("product", productMono);
		doReturn(model).when(model).addAttribute("title", "Editar producto");
		doReturn(model).when(model).addAttribute("button", "Editar");

		StepVerifier.create(productController.edit(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute(Mockito.eq("product"), Mockito.any()); // Return MonoDefaultIfEmpty
		verify(model).addAttribute("title", "Editar producto");
		verify(model).addAttribute("button", "Editar");
	}

	@Test
	public void editWhenFindProductByIdDoesNotExistsThenMustSetNewProductAndTitleAndButtonAndReturnMonoStringTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		Mono<Product> product = Mono.empty();
		doReturn(product).when(service).findById(id);
		doReturn(model).when(model).addAttribute("product", product);
		doReturn(model).when(model).addAttribute("title", "Editar producto");
		doReturn(model).when(model).addAttribute("button", "Editar");

		StepVerifier.create(productController.edit(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute(Mockito.eq("product"), Mockito.any()); // Return MonoDefaultIfEmpty
		verify(model).addAttribute("title", "Editar producto");
		verify(model).addAttribute("button", "Editar");
	}

	@Test
	public void editV2WhenFindProductByIdThenMustSetProductAndTitleAndButtonAndReturnMonoStringTest() {
		Product product = new Product();
		product.setId("id");
		product.setName("TV Panasonic Pantalla LCD");
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.just(product)).when(service).findById(id);
		doReturn(model).when(model).addAttribute("product", product);
		doReturn(model).when(model).addAttribute("title", "Editar producto");
		doReturn(model).when(model).addAttribute("button", "Editar");

		StepVerifier.create(productController.editV2(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute("product", product);
		verify(model).addAttribute("title", "Editar producto");
		verify(model).addAttribute("button", "Editar");
	}

	@Test
	public void editV2WhenFindProductByIdDoesNotExistsThenMustSetNewProductAndTitleAndButtonAndReturnMonoStringTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.empty()).when(service).findById(id);

		StepVerifier.create(productController.editV2(id, model))
		.expectNextMatches(redirectExpected -> "redirect:/list?error=no+existe+el+producto".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model, Mockito.never()).addAttribute(Mockito.anyString(), Mockito.any());
	}

	@Test
	public void saveWhenBindingResultHasErrorsThenMustSetTitleAndButtonAndReturnMonoStringTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Model model = Mockito.mock(Model.class);
		FilePart file = Mockito.mock(FilePart.class);
		doReturn(true).when(result).hasErrors();
		doReturn(model).when(model).addAttribute("title", "Errores en formulario producto");
		doReturn(model).when(model).addAttribute("button", "Guardar");
		
		StepVerifier.create(productController.save(null, result, model, null, null))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(result).hasErrors();
		verify(model).addAttribute("title", "Errores en formulario producto");
		verify(model).addAttribute("button", "Guardar");	
		verify(result).hasErrors();
		verify(status, Mockito.never()).setComplete();
		verify(service, Mockito.never()).findCategoryById(Mockito.anyString());
		verify(file, Mockito.never()).filename();
		verify(service, Mockito.never()).save(Mockito.any(Product.class));
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
	}

	@Test
	public void saveWhenResultHasNotErrorsAndFindCategoriaByIdHasNotElementThenMustReturnRedirectTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Product product = new Product();
		Category category = new Category();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		category.setId(id);
		product.setCategory(category);
		FilePart file = Mockito.mock(FilePart.class);
		Model model = Mockito.mock(Model.class);
		doReturn(false).when(result).hasErrors();
		doNothing().when(status).setComplete();
		doReturn(Mono.empty()).when(service).findCategoryById(id);
		
		StepVerifier.create(productController.save(product, result, model, file, status))
		.expectNextMatches(redirectExpected -> "redirect:/list?success=producto+guardado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(result).hasErrors();
		verify(status).setComplete();
		verify(service).findCategoryById(id);
		verify(file, Mockito.never()).filename();
		verify(service, Mockito.never()).save(Mockito.any(Product.class));
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
	}

	@Test
	public void saveWhenResultHasNotErrorsAndFindCategoriaByIdHasElementAndProductCreateAtIsNullThenMustReturnRedirectWithProductCreateAtTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Product product = new Product();
		Category category = new Category();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		category.setId(id);
		category.setName("Deporte");
		product.setCategory(category);
		product.setId("bce26c12-553e-4b20-a593-6dbc9d8dfdd2");
		product.setName("Sony Camara HD Digital");
		FilePart file = Mockito.mock(FilePart.class);
		Model model = Mockito.mock(Model.class);
		Date dateSmaller = new Date();
		doReturn(false).when(result).hasErrors();
		doNothing().when(status).setComplete();
		doReturn(Mono.just(category)).when(service).findCategoryById(id);
		doReturn("").when(file).filename();
		doReturn(Mono.just(product)).when(service).save(product);
		
		StepVerifier.create(productController.save(product, result, model, file, status))
		.expectNextMatches(redirectExpected -> "redirect:/list?success=producto+guardado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		Assertions.assertNotNull(product.getCreateAt());
		Assertions.assertTrue(dateSmaller.getTime() < product.getCreateAt().getTime());
		Assertions.assertTrue(product.getCreateAt().getTime() < DateUtil.now().getTime());
		verify(result).hasErrors();
		verify(status).setComplete();
		verify(service).findCategoryById(id);
		verify(file, Mockito.atLeastOnce()).filename();
		verify(service).save(product);
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
	}

	@Test
	public void saveWhenResultHasNotErrorsAndFindCategoriaByIdHasElementAndFilenameIsNotEmptyThenMustReturnRedirectWithPhotoSettedTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Product product = new Product();
		Category category = new Category();
		Date createAt = new Date();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		category.setId(id);
		category.setName("Deporte");
		product.setCategory(category);
		product.setId("bce26c12-553e-4b20-a593-6dbc9d8dfdd2");
		product.setName("Sony Camara HD Digital");
		product.setCreateAt(createAt);
		FilePart file = Mockito.mock(FilePart.class);
		Model model = Mockito.mock(Model.class);
		doReturn(false).when(result).hasErrors();
		doNothing().when(status).setComplete();
		doReturn(Mono.just(category)).when(service).findCategoryById(id);
		doReturn("filename").when(file).filename();
		doReturn(Mono.just(product)).when(service).save(product);
		doReturn(Mono.empty()).when(file).transferTo(Mockito.any(File.class));
		doReturn("").when(productController).getUploadPath();
		
		StepVerifier.create(productController.save(product, result, model, file, status))
		.expectNextMatches(redirectExpected -> "redirect:/list?success=producto+guardado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		Assertions.assertNotNull(product.getCreateAt());
		Assertions.assertEquals(createAt, product.getCreateAt());
		Assertions.assertTrue(product.getPhoto().contains("-filename"));
		verify(result).hasErrors();
		verify(status).setComplete();
		verify(service).findCategoryById(id);
		verify(file, Mockito.atLeastOnce()).filename();
		verify(service).save(product);
		verify(file).transferTo(Mockito.any(File.class));
		verify(productController).getUploadPath();
	}

	@Test
	public void deleteWhenFindByIdHasNotElementThenMustNotDeleteProductAndReturnRedirectTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		doReturn(Mono.empty()).when(service).findById(id);
		
		StepVerifier.create(productController.delete(id))
		.expectNextMatches(redirectExpected -> "redirect:/list?error=no+existe+el+producto+a+eliminar".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(service, Mockito.never()).delete(Mockito.any(Product.class));
	}

	@Test
	public void deleteWhenFindByIdHasElementThenMustDeleteProductAndReturnRedirectTest() {
		Product product = new Product();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		product.setId(id);
		product.setName("Hewlett Packard Multifuncional");
		doReturn(Mono.just(product)).when(service).findById(id);
		doReturn(Mono.empty()).when(service).delete(product);
		
		StepVerifier.create(productController.delete(id))
		.expectNextMatches(redirectExpected -> "redirect:/list?success=producto+eliminado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(service).delete(product);
	}

	@Test
	public void listDataDriverWhenFindAllWithNameUpperCaseHasNotElementsThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Model model = Mockito.mock(Model.class);
		doReturn(Flux.empty()).when(service).findAllWithNameUpperCase();
		doReturn(model).when(model).addAttribute(Mockito.eq("products"), Mockito.any(ReactiveDataDriverContextVariable.class));
		doReturn(model).when(model).addAttribute("title", "Listado de productos");
		
		String result = productController.listDataDriver(model);

		Assertions.assertEquals("list", result);
		verify(service).findAllWithNameUpperCase();
		verify(model).addAttribute(Mockito.eq("products"), Mockito.any(ReactiveDataDriverContextVariable.class));
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void listDataDriverWhenFindAllWithNameUpperCaseThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Product product = new Product();
		Model model = Mockito.mock(Model.class);
		doReturn(Flux.just(product)).when(service).findAllWithNameUpperCase();
		doReturn(model).when(model).addAttribute(Mockito.eq("products"), Mockito.any(ReactiveDataDriverContextVariable.class));
		doReturn(model).when(model).addAttribute("title", "Listado de productos");
		
		String result = productController.listDataDriver(model);

		Assertions.assertEquals("list", result);
		verify(service).findAllWithNameUpperCase();
		verify(model).addAttribute(Mockito.eq("products"), Mockito.any(ReactiveDataDriverContextVariable.class));
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void listFullWhenFindAllWithNameUpperCaseRepeatHasNotElementsThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Flux<Product> products = Flux.empty();
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllWithNameUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("products", products);
		doReturn(model).when(model).addAttribute("title", "Listado de productos");
		
		String result = productController.listFull(model);

		Assertions.assertEquals("list", result);
		verify(service).findAllWithNameUpperCaseRepeat();
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void listFullWhenFindAllWithNameUpperCaseRepeatThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Product product = new Product();
		Flux<Product> products = Flux.just(product);
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllWithNameUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("products", products);
		doReturn(model).when(model).addAttribute("title", "Listado de productos");
		
		String result = productController.listFull(model);

		Assertions.assertEquals("list", result);
		verify(service).findAllWithNameUpperCaseRepeat();
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void listChunkedWhenFindAllWithNameUpperCaseRepeatHasNotElementsThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Flux<Product> products = Flux.empty();
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllWithNameUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("products", products);
		doReturn(model).when(model).addAttribute("title", "Listado de productos");
		
		String result = productController.listChunked(model);

		Assertions.assertEquals("list-chunked", result);
		verify(service).findAllWithNameUpperCaseRepeat();
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void listChunkedWhenFindAllWithNameUpperCaseRepeatThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Product product = new Product();
		Flux<Product> products = Flux.just(product);
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllWithNameUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("products", products);
		doReturn(model).when(model).addAttribute("title", "Listado de productos");
		
		String result = productController.listChunked(model);

		Assertions.assertEquals("list-chunked", result);
		verify(service).findAllWithNameUpperCaseRepeat();
		verify(model).addAttribute("products", products);
		verify(model).addAttribute("title", "Listado de productos");
	}

	@Test
	public void getResourceWhenPathToUriHasErrorThenMustReturnAnExceptionTest() {
		Path route = Mockito.mock(Path.class);
		
		Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> productController.getResource(route)
		);
		
		verify(route).toUri();
	}
}
