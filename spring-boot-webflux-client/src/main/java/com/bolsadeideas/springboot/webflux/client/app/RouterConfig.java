package com.bolsadeideas.springboot.webflux.client.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.bolsadeideas.springboot.webflux.client.app.handler.ProductoHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterConfig {

	@Bean
	public RouterFunction<ServerResponse> rutas(ProductoHandler handler) {
		return route(GET("/api/client"), handler::listar)
				.andRoute(GET("/api/client/{id}"), handler::ver);
	}
}