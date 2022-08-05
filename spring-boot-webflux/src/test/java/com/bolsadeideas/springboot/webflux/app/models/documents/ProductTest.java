package com.bolsadeideas.springboot.webflux.app.models.documents;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductTest {
	
	Product product;

	@BeforeEach
	void init() {
		product = new Product();
	}

	@Test
	void setNameToUpperCaseWhenMethodIsInvokedMustReturnProductWithNameToUpperCaseTest() {
		product.setName("Sony Notebook");
		
		Product productResult = product.setNameToUpperCase();
		
		Assert.assertEquals("SONY NOTEBOOK", productResult.getName());
	}
}
