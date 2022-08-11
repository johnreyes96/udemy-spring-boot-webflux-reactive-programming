package com.bolsadeideas.springboot.webflux.app.constants;

public enum RouteEnum {

    API_PRODUCTS("/api/productos/"),
    API_V2_BASE_PRODUCTS("/api/v2/productos"),
    API_V2_PRODUCTS("/api/v2/productos/"),
    API_V3_BASE_PRODUCTS("/api/v3/productos");

    private final String route;

    RouteEnum(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}
