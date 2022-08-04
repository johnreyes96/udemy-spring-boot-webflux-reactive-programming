package com.bolsadeideas.springboot.webflux.app.models.documents;

import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection="productos")
public class Product {
	
	@Id
	private String id;
	
	@NotEmpty
	private String name;
	
	@NotNull
	private Double price;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createAt;
	
	@Valid
	@NotNull
	private Category category;
	
	private String photo;

	public Product() {}

	public Product(String name, Double price) {
		this.name = name;
		this.price = price;
	}
	
	public Product(String name, Double price, Category category) {
		this(name, price);
		this.category = category;
	}
	
	public Product setNameToUpperCase() {
		this.setName(this.getName().toUpperCase());
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Double getPrice() {
		return price;
	}
	
	public void setPrice(Double price) {
		this.price = price;
	}
	
	public Date getCreateAt() {
		return createAt;
	}
	
	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public String getPhoto() {
		return photo;
	}
	
	public void setPhoto(String photo) {
		this.photo = photo;
	}
}
