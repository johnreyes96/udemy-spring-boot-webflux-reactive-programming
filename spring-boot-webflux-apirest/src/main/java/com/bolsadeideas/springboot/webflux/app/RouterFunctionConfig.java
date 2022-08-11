package com.bolsadeideas.springboot.webflux.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.bolsadeideas.springboot.webflux.app.constants.RouteEnum;
import com.bolsadeideas.springboot.webflux.app.handler.ProductHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route(GET(RouteEnum.API_V2_BASE_PRODUCTS.getRoute())
				.or(GET(RouteEnum.API_V3_BASE_PRODUCTS.getRoute())), handler::listar)
				.andRoute(GET(RouteEnum.API_V2_PRODUCTS.getRoute() + "{id}"), handler::ver)
				.andRoute(POST(RouteEnum.API_V2_BASE_PRODUCTS.getRoute()), handler::crear)
				.andRoute(PUT(RouteEnum.API_V2_PRODUCTS.getRoute() + "{id}"), handler::editar)
				.andRoute(DELETE(RouteEnum.API_V2_PRODUCTS.getRoute() + "{id}"), handler::eliminar)
				.andRoute(POST(RouteEnum.API_V2_PRODUCTS.getRoute() + "upload/{id}"), handler::upload)
				.andRoute(POST(RouteEnum.API_V2_PRODUCTS.getRoute() + "crear"), handler::crearConFoto);
	}
}
