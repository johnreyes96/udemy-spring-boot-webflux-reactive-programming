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

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.IProductoService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.controllers")
public class ProductoControllerTest {

	@Spy
	@InjectMocks
	private ProductoController productController;
	@Mock
	private IProductoService service;

	@BeforeEach
	void init() {
		productController = new ProductoController();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void indexWhenFindAllReturnZeroElementsThenMustReturnFluxEmptyTest() {
		doReturn(Flux.empty()).when(service).findAllCategoria();

		StepVerifier.create(productController.cagetorias())
		.expectNextCount(0)
		.expectComplete()
		.verify();

		verify(service).findAllCategoria();
	}

	@Test
	public void indexWhenFindAllReturnOneElementThenMustReturnFluxWithOneCategoryTest() {
		Categoria category = new Categoria();
		category.setNombre("Deporte");
		doReturn(Flux.just(category)).when(service).findAllCategoria();

		StepVerifier.create(productController.cagetorias())
		.expectNextMatches(categoryExpected -> "Deporte".equals(categoryExpected.getNombre()))
		.expectComplete()
		.verify();

		verify(service).findAllCategoria();
	}

	@Test
	public void indexWhenFindAllReturnElementsThenMustReturnFluxWithCategoriesTest() {
		Categoria sport = new Categoria();
		sport.setNombre("Deporte");
		Categoria furniture = new Categoria();
		furniture.setNombre("Muebles");
		doReturn(Flux.just(sport, furniture)).when(service).findAllCategoria();

		StepVerifier.create(productController.cagetorias())
		.expectNextMatches(categoryExpected -> "Deporte".equals(categoryExpected.getNombre()))
		.expectNextMatches(categoryExpected -> "Muebles".equals(categoryExpected.getNombre()))
		.expectComplete()
		.verify();

		verify(service).findAllCategoria();
	}

	@Test
	public void viewPhotoWhenResourceThrowAnExceptionThenMustReturnMalformedURLExceptionTest() throws MalformedURLException {
		Path route = Mockito.mock(Path.class);
		String photoName = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2-SonyCamaraHDDigital";
		doReturn(route).when(productController).getPath();
		doReturn(route).when(route).resolve(photoName);
		doReturn(route).when(route).toAbsolutePath();
		doThrow(new MalformedURLException()).when(productController).getResource(route);
		
		Assertions.assertThrows(
				MalformedURLException.class,
	           () -> productController.verFoto(photoName)
	    );

		verify(productController).getPath();
		verify(route).resolve(photoName);
		verify(route).toAbsolutePath();
		verify(productController).getResource(route);
	}

	@Test
	public void viewPhotoWhenResourceIsValidThenMustReturnResponseEntityOKWithImageInBodyTest() throws MalformedURLException {
		Path route = Mockito.mock(Path.class);
		String photoName = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2-SonyCamaraHDDigital";
		Resource image = Mockito.mock(Resource.class);
		ResponseEntity<Resource> resourceExpected = ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"")
				.body(image);
		doReturn(route).when(productController).getPath();
		doReturn(route).when(route).resolve(photoName);
		doReturn(route).when(route).toAbsolutePath();
		doReturn(image).when(productController).getResource(route);
		
		StepVerifier.create(productController.verFoto(photoName))
		.expectNext(resourceExpected)
		.expectComplete()
		.verify();

		verify(productController).getPath();
		verify(route).resolve(photoName);
		verify(route).toAbsolutePath();
		verify(productController).getResource(route);
	}

	@Test
	public void viewWhenFindProductByIdThenMustSetProductAndTitleAndReturnMonoStringTest() {
		Producto product = new Producto();
		product.setId("id");
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.just(product)).when(service).findById(id);
		doReturn(model).when(model).addAttribute("producto", product);
		doReturn(model).when(model).addAttribute("titulo", "Detalle Producto");

		StepVerifier.create(productController.ver(model, id))
		.expectNextMatches(expected -> "ver".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute("producto", product);
		verify(model).addAttribute("titulo", "Detalle Producto");
	}

	@Test
	public void viewWhenFindProductByIdDoesNotExistsThenMustCreateNewProductAndThrowExceptionAndReturnMonoWithARedirectTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.empty()).when(service).findById(id);

		StepVerifier.create(productController.ver(model, id))
		.expectNextMatches(redirectExpected -> "redirect:/listar?error=no+existe+el+producto".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model, Mockito.never()).addAttribute(Mockito.anyString(), Mockito.any());
	}

	@Test
	public void listWhenFindAllWithNameUpperCaseReturnEmptyThenMustSetFluxEmptyAndTitleAndReturnStringTest() {
		Flux<Producto> products = Flux.empty();
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllConNombreUpperCase();
		doReturn(model).when(model).addAttribute("productos", products);
		doReturn(model).when(model).addAttribute("titulo", "Detalle Producto");

		String response = productController.listar(model);

		Assert.assertEquals("listar", response);
		verify(service).findAllConNombreUpperCase();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void listWhenFindAllWithNameUpperCaseReturnElementsThenMustSetProductsAndTitleAndReturnStringTest() {
		Producto laptop = new Producto();
		laptop.setNombre("Sony Notebook");
		Producto smartphone = new Producto();
		smartphone.setNombre("Apple iPod");
		Flux<Producto> products = Flux.just(laptop, smartphone);
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllConNombreUpperCase();
		doReturn(model).when(model).addAttribute("productos", products);
		doReturn(model).when(model).addAttribute("titulo", "Detalle Producto");

		String response = productController.listar(model);

		Assert.assertEquals("listar", response);
		verify(service).findAllConNombreUpperCase();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void createMustSetNewProductAndTitleAndNameButtonAndReturnStringTest() {
		Model model = Mockito.mock(Model.class);
		doReturn(model).when(model).addAttribute(Mockito.eq("producto"), Mockito.any(Producto.class));
		doReturn(model).when(model).addAttribute("titulo", "Formulario de producto");
		doReturn(model).when(model).addAttribute("boton", "Crear");

		StepVerifier.create(productController.crear(model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(model).addAttribute(Mockito.eq("producto"), Mockito.any(Producto.class));
		verify(model).addAttribute("titulo", "Formulario de producto");
		verify(model).addAttribute("boton", "Crear");
	}

	@Test
	public void editWhenFindProductByIdThenMustSetProductAndTitleAndButtonAndReturnMonoStringTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		Producto producto = new Producto();
		Mono<Producto> product = Mono.just(producto);
		doReturn(product).when(service).findById(id);
		doReturn(model).when(model).addAttribute("producto", product);
		doReturn(model).when(model).addAttribute("titulo", "Editar Producto");
		doReturn(model).when(model).addAttribute("boton", "Editar");

		StepVerifier.create(productController.editar(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute(Mockito.eq("producto"), Mockito.any()); // Return MonoDefaultIfEmpty
		verify(model).addAttribute("titulo", "Editar Producto");
		verify(model).addAttribute("boton", "Editar");
	}

	@Test
	public void editWhenFindProductByIdDoesNotExistsThenMustSetNewProductAndTitleAndButtonAndReturnMonoStringTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		Mono<Producto> product = Mono.empty();
		doReturn(product).when(service).findById(id);
		doReturn(model).when(model).addAttribute("producto", product);
		doReturn(model).when(model).addAttribute("titulo", "Editar Producto");
		doReturn(model).when(model).addAttribute("boton", "Editar");

		StepVerifier.create(productController.editar(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute(Mockito.eq("producto"), Mockito.any()); // Return MonoDefaultIfEmpty
		verify(model).addAttribute("titulo", "Editar Producto");
		verify(model).addAttribute("boton", "Editar");
	}

	@Test
	public void editV2WhenFindProductByIdThenMustSetProductAndTitleAndButtonAndReturnMonoStringTest() {
		Producto product = new Producto();
		product.setId("id");
		product.setNombre("TV Panasonic Pantalla LCD");
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.just(product)).when(service).findById(id);
		doReturn(model).when(model).addAttribute("producto", product);
		doReturn(model).when(model).addAttribute("titulo", "Editar Producto");
		doReturn(model).when(model).addAttribute("boton", "Editar");

		StepVerifier.create(productController.editarV2(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute("producto", product);
		verify(model).addAttribute("titulo", "Editar Producto");
		verify(model).addAttribute("boton", "Editar");
	}

	@Test
	public void editV2WhenFindProductByIdDoesNotExistsThenMustSetNewProductAndTitleAndButtonAndReturnMonoStringTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		Model model = Mockito.mock(Model.class);
		doReturn(Mono.empty()).when(service).findById(id);

		StepVerifier.create(productController.editarV2(id, model))
		.expectNextMatches(redirectExpected -> "redirect:/listar?error=no+existe+el+producto".equals(redirectExpected))
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
		doReturn(model).when(model).addAttribute("titulo", "Errores en formulario producto");
		doReturn(model).when(model).addAttribute("botton", "Guardar");
		
		StepVerifier.create(productController.guardar(null, result, model, null, null))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(result).hasErrors();
		verify(model).addAttribute("titulo", "Errores en formulario producto");
		verify(model).addAttribute("botton", "Guardar");	
		verify(result).hasErrors();
		verify(status, Mockito.never()).setComplete();
		verify(service, Mockito.never()).findCategoriaById(Mockito.anyString());
		verify(file, Mockito.never()).filename();
		verify(service, Mockito.never()).save(Mockito.any(Producto.class));
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
	}

	@Test
	public void saveWhenResultHasNotErrorsAndFindCategoriaByIdHasNotElementThenMustReturnRedirectTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Producto product = new Producto();
		Categoria category = new Categoria();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		category.setId(id);
		product.setCategoria(category);
		FilePart file = Mockito.mock(FilePart.class);
		Model model = Mockito.mock(Model.class);
		doReturn(false).when(result).hasErrors();
		doNothing().when(status).setComplete();
		doReturn(Mono.empty()).when(service).findCategoriaById(id);
		
		StepVerifier.create(productController.guardar(product, result, model, file, status))
		.expectNextMatches(redirectExpected -> "redirect:/listar?success=producto+guardado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(result).hasErrors();
		verify(status).setComplete();
		verify(service).findCategoriaById(id);
		verify(file, Mockito.never()).filename();
		verify(service, Mockito.never()).save(Mockito.any(Producto.class));
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
	}

	@Test
	public void saveWhenResultHasNotErrorsAndFindCategoriaByIdHasElementAndProductCreateAtIsNullThenMustReturnRedirectWithProductCreateAtTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Producto product = new Producto();
		Categoria category = new Categoria();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		category.setId(id);
		category.setNombre("Deporte");
		product.setCategoria(category);
		product.setId("bce26c12-553e-4b20-a593-6dbc9d8dfdd2");
		product.setNombre("Sony Camara HD Digital");
		FilePart file = Mockito.mock(FilePart.class);
		Model model = Mockito.mock(Model.class);
		Date dateSmaller = new Date();
		doReturn(false).when(result).hasErrors();
		doNothing().when(status).setComplete();
		doReturn(Mono.just(category)).when(service).findCategoriaById(id);
		doReturn("").when(file).filename();
		doReturn(Mono.just(product)).when(service).save(product);
		
		StepVerifier.create(productController.guardar(product, result, model, file, status))
		.expectNextMatches(redirectExpected -> "redirect:/listar?success=producto+guardado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		Assertions.assertNotNull(product.getCreateAt());
		Assertions.assertTrue(dateSmaller.getTime() < product.getCreateAt().getTime());
		Assertions.assertTrue(product.getCreateAt().getTime() < DateUtil.now().getTime());
		verify(result).hasErrors();
		verify(status).setComplete();
		verify(service).findCategoriaById(id);
		verify(file, Mockito.atLeastOnce()).filename();
		verify(service).save(product);
		verify(file, Mockito.never()).transferTo(Mockito.any(File.class));
	}

	@Test
	public void saveWhenResultHasNotErrorsAndFindCategoriaByIdHasElementAndFilenameIsNotEmptyThenMustReturnRedirectWithPhotoSettedTest() {
		BindingResult result = Mockito.mock(BindingResult.class);
		SessionStatus status = Mockito.mock(SessionStatus.class);
		Producto product = new Producto();
		Categoria category = new Categoria();
		Date createAt = new Date();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		category.setId(id);
		category.setNombre("Deporte");
		product.setCategoria(category);
		product.setId("bce26c12-553e-4b20-a593-6dbc9d8dfdd2");
		product.setNombre("Sony Camara HD Digital");
		product.setCreateAt(createAt);
		FilePart file = Mockito.mock(FilePart.class);
		Model model = Mockito.mock(Model.class);
		doReturn(false).when(result).hasErrors();
		doNothing().when(status).setComplete();
		doReturn(Mono.just(category)).when(service).findCategoriaById(id);
		doReturn("filename").when(file).filename();
		doReturn(Mono.just(product)).when(service).save(product);
		doReturn(Mono.empty()).when(file).transferTo(Mockito.any(File.class));
		
		StepVerifier.create(productController.guardar(product, result, model, file, status))
		.expectNextMatches(redirectExpected -> "redirect:/listar?success=producto+guardado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		Assertions.assertNotNull(product.getCreateAt());
		Assertions.assertEquals(createAt, product.getCreateAt());
		Assertions.assertTrue(product.getFoto().contains("-filename"));
		verify(result).hasErrors();
		verify(status).setComplete();
		verify(service).findCategoriaById(id);
		verify(file, Mockito.atLeastOnce()).filename();
		verify(service).save(product);
		verify(file).transferTo(Mockito.any(File.class));
	}

	@Test
	public void deleteWhenFindByIdHasNotElementThenMustNotDeleteProductAndReturnRedirectTest() {
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		doReturn(Mono.empty()).when(service).findById(id);
		
		StepVerifier.create(productController.eliminar(id))
		.expectNextMatches(redirectExpected -> "redirect:/listar?error=no+existe+el+producto+a+eliminar".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(service, Mockito.never()).delete(Mockito.any(Producto.class));
	}

	@Test
	public void deleteWhenFindByIdHasElementThenMustDeleteProductAndReturnRedirectTest() {
		Producto product = new Producto();
		String id = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2";
		product.setId(id);
		product.setNombre("Hewlett Packard Multifuncional");
		doReturn(Mono.just(product)).when(service).findById(id);
		doReturn(Mono.empty()).when(service).delete(product);
		
		StepVerifier.create(productController.eliminar(id))
		.expectNextMatches(redirectExpected -> "redirect:/listar?success=producto+eliminado+con+exito".equals(redirectExpected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(service).delete(product);
	}

	@Test
	public void listDataDriverWhenFindAllWithNameUpperCaseHasNotElementsThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Model model = Mockito.mock(Model.class);
		doReturn(Flux.empty()).when(service).findAllConNombreUpperCase();
		doReturn(model).when(model).addAttribute(Mockito.eq("productos"), Mockito.any(ReactiveDataDriverContextVariable.class));
		doReturn(model).when(model).addAttribute("titulo", "Listado de productos");
		
		String result = productController.listarDataDriver(model);

		Assertions.assertEquals("listar", result);
		verify(service).findAllConNombreUpperCase();
		verify(model).addAttribute(Mockito.eq("productos"), Mockito.any(ReactiveDataDriverContextVariable.class));
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void listDataDriverWhenFindAllWithNameUpperCaseThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Producto product = new Producto();
		Model model = Mockito.mock(Model.class);
		doReturn(Flux.just(product)).when(service).findAllConNombreUpperCase();
		doReturn(model).when(model).addAttribute(Mockito.eq("productos"), Mockito.any(ReactiveDataDriverContextVariable.class));
		doReturn(model).when(model).addAttribute("titulo", "Listado de productos");
		
		String result = productController.listarDataDriver(model);

		Assertions.assertEquals("listar", result);
		verify(service).findAllConNombreUpperCase();
		verify(model).addAttribute(Mockito.eq("productos"), Mockito.any(ReactiveDataDriverContextVariable.class));
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void listFullWhenFindAllWithNameUpperCaseRepeatHasNotElementsThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Flux<Producto> products = Flux.empty();
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllConNombreUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("productos", products);
		doReturn(model).when(model).addAttribute("titulo", "Listado de productos");
		
		String result = productController.listarFull(model);

		Assertions.assertEquals("listar", result);
		verify(service).findAllConNombreUpperCaseRepeat();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void listFullWhenFindAllWithNameUpperCaseRepeatThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Producto product = new Producto();
		Flux<Producto> products = Flux.just(product);
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllConNombreUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("productos", products);
		doReturn(model).when(model).addAttribute("titulo", "Listado de productos");
		
		String result = productController.listarFull(model);

		Assertions.assertEquals("listar", result);
		verify(service).findAllConNombreUpperCaseRepeat();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void listChunkedWhenFindAllWithNameUpperCaseRepeatHasNotElementsThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Flux<Producto> products = Flux.empty();
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllConNombreUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("productos", products);
		doReturn(model).when(model).addAttribute("titulo", "Listado de productos");
		
		String result = productController.listarChunked(model);

		Assertions.assertEquals("listar-chunked", result);
		verify(service).findAllConNombreUpperCaseRepeat();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void listChunkedWhenFindAllWithNameUpperCaseRepeatThenMustSetProductsAndTitleAndReturnMonoStringTest() {
		Producto product = new Producto();
		Flux<Producto> products = Flux.just(product);
		Model model = Mockito.mock(Model.class);
		doReturn(products).when(service).findAllConNombreUpperCaseRepeat();
		doReturn(model).when(model).addAttribute("productos", products);
		doReturn(model).when(model).addAttribute("titulo", "Listado de productos");
		
		String result = productController.listarChunked(model);

		Assertions.assertEquals("listar-chunked", result);
		verify(service).findAllConNombreUpperCaseRepeat();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
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
