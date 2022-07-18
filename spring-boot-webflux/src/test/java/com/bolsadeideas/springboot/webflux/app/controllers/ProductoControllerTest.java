package com.bolsadeideas.springboot.webflux.app.controllers;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.net.MalformedURLException;
import java.nio.file.Path;

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
import org.springframework.ui.Model;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.IProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ComponentScan(basePackages = "com.bolsadeideas.springboot.webflux.app.controllers")
public class ProductoControllerTest {

	@Spy
	@InjectMocks
	private ProductoController productoController;
	@Mock
	private IProductoService service;

	@BeforeEach
	void init() {
		productoController = new ProductoController();
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void indexWhenFindAllReturnZeroElementsThenMustReturnFluxEmptyTest() {
		doReturn(Flux.empty()).when(service).findAllCategoria();

		StepVerifier.create(productoController.cagetorias())
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

		StepVerifier.create(productoController.cagetorias())
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

		StepVerifier.create(productoController.cagetorias())
		.expectNextMatches(categoryExpected -> "Deporte".equals(categoryExpected.getNombre()))
		.expectNextMatches(categoryExpected -> "Muebles".equals(categoryExpected.getNombre()))
		.expectComplete()
		.verify();

		verify(service).findAllCategoria();
	}

	@Test
	public void viewPhotoWhenResourceThrowAnExceptionThenMustReturnMalformedURLExceptionTest() throws MalformedURLException {
		Path rute = Mockito.mock(Path.class);
		String photoName = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2-SonyCamaraHDDigital";
		doReturn(rute).when(productoController).getPath();
		doReturn(rute).when(rute).resolve(photoName);
		doReturn(rute).when(rute).toAbsolutePath();
		doThrow(new MalformedURLException()).when(productoController).getResource(rute);
		
		Assertions.assertThrows(
				MalformedURLException.class,
	           () -> productoController.verFoto(photoName)
	    );

		verify(productoController).getPath();
		verify(rute).resolve(photoName);
		verify(rute).toAbsolutePath();
		verify(productoController).getResource(rute);
	}

	@Test
	public void viewPhotoWhenResourceIsValidThenMustReturnResponseEntityOKWithImageInBodyTest() throws MalformedURLException {
		Path rute = Mockito.mock(Path.class);
		String photoName = "bce26c12-553e-4b20-a593-6dbc9d8dfdd2-SonyCamaraHDDigital";
		Resource image = Mockito.mock(Resource.class);
		ResponseEntity<Resource> resourceExpected = ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\"")
				.body(image);
		doReturn(rute).when(productoController).getPath();
		doReturn(rute).when(rute).resolve(photoName);
		doReturn(rute).when(rute).toAbsolutePath();
		doReturn(image).when(productoController).getResource(rute);
		
		StepVerifier.create(productoController.verFoto(photoName))
		.expectNext(resourceExpected)
		.expectComplete()
		.verify();

		verify(productoController).getPath();
		verify(rute).resolve(photoName);
		verify(rute).toAbsolutePath();
		verify(productoController).getResource(rute);
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

		StepVerifier.create(productoController.ver(model, id))
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

		StepVerifier.create(productoController.ver(model, id))
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

		String response = productoController.listar(model);

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

		String response = productoController.listar(model);

		Assert.assertEquals("listar", response);
		verify(service).findAllConNombreUpperCase();
		verify(model).addAttribute("productos", products);
		verify(model).addAttribute("titulo", "Listado de productos");
	}

	@Test
	public void createMustSetNewProductAndTitleAndNameButtonAndReturnStringTest() {
		Model model = Mockito.mock(Model.class);
		doReturn(model).when(model).addAttribute(Mockito.eq("producto"), Mockito.any());
		doReturn(model).when(model).addAttribute("titulo", "Formulario de producto");
		doReturn(model).when(model).addAttribute("boton", "Crear");

		StepVerifier.create(productoController.crear(model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(model).addAttribute(Mockito.eq("producto"), Mockito.any());
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

		StepVerifier.create(productoController.editar(id, model))
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

		StepVerifier.create(productoController.editar(id, model))
		.expectNextMatches(expected -> "form".equals(expected))
		.expectComplete()
		.verify();

		verify(service).findById(id);
		verify(model).addAttribute(Mockito.eq("producto"), Mockito.any()); // Return MonoDefaultIfEmpty
		verify(model).addAttribute("titulo", "Editar Producto");
		verify(model).addAttribute("boton", "Editar");
	}
}
