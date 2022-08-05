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

	@Test
	void setPhotoWithFormattedNameWhenParamsAreNullMustSetPhotoWithOnlyHyphenTest() {
		
		product.setPhotoWithFormattedName(null, null);
		
		Assert.assertEquals("-", product.getPhoto());
	}

	@Test
	void setPhotoWithFormattedNameWhenParamsAreStringEmptyMustSetPhotoWithOnlyHyphenTest() {
		String format = "";
		String name = "";
		
		product.setPhotoWithFormattedName(format, name);
		
		Assert.assertEquals("-", product.getPhoto());
	}

	@Test
	void setPhotoWithFormattedNameWhenNameHasSpaceBlankMustSetPhotoWithoutSpaceBlankTest() {
		String format = "uuid";
		String name = "name of a photo";
		
		product.setPhotoWithFormattedName(format, name);
		
		Assert.assertEquals("uuid-nameofaphoto", product.getPhoto());
	}

	@Test
	void setPhotoWithFormattedNameWhenNameHasColonMustSetPhotoWithoutColonTest() {
		String format = "uuid";
		String name = "name: photo";
		
		product.setPhotoWithFormattedName(format, name);
		
		Assert.assertEquals("uuid-namephoto", product.getPhoto());
	}

	@Test
	void setPhotoWithFormattedNameWhenNameHasBackslashMustSetPhotoWhitoutBackslashTest() {
		String format = "uuid";
		String name = "name\\photo";
		
		product.setPhotoWithFormattedName(format, name);
		
		Assert.assertEquals("uuid-namephoto", product.getPhoto());
	}

	@Test
	void setPhotoWithFormattedNameWhenNameHasNotSpaceBlankOrColonOrBackslashMustSetPhotoWhitoutThatTest() {
		String format = "uuid";
		String name = "motorbike";
		
		product.setPhotoWithFormattedName(format, name);
		
		Assert.assertEquals("uuid-motorbike", product.getPhoto());
	}

	@Test
	void setPhotoWithFormattedNameWhenNameHasSpaceBlankAndColonAndBackslashMustSetPhotoWhitoutThatTest() {
		String format = "uuid";
		String name = "name: name\\photo";
		
		product.setPhotoWithFormattedName(format, name);
		
		Assert.assertEquals("uuid-namenamephoto", product.getPhoto());
	}
}
