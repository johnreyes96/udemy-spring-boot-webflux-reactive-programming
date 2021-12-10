package com.bolsadeideas.springboot.webflux.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.webflux.app.models.dao.IProductoDao;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private IProductoDao productoDao;
	private static final Logger logger = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89),
					new Producto("Sony Camara HD Digital", 177.89),
					new Producto("Apple iPod", 46.89),
					new Producto("Sony Notebook", 846.89),
					new Producto("Hewlett Packard Multifuncional", 200.89),
					new Producto("Bianchi Bicicleta", 70.89),
					new Producto("HP Notebook Omen 17", 2500.89),
					new Producto("Mica Cómoda 5 Cajones", 150.89),
					new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89)
				)
			.flatMap(producto -> productoDao.save(producto))
			.subscribe(producto -> logger.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}
}