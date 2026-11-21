package com.louaraya.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String category;
    private Integer widthCm;
    private Integer heightCm;
    private String material;
    private Integer priceClp;
    private Integer stock;
    private Boolean madeToOrder = false;
    private Boolean active = true;
    private String imageUri;
    private Long createdAt = System.currentTimeMillis();

    public Product() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getWidthCm() { return widthCm; }
    public void setWidthCm(Integer widthCm) { this.widthCm = widthCm; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public Integer getPriceClp() { return priceClp; }
    public void setPriceClp(Integer priceClp) { this.priceClp = priceClp; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Boolean getMadeToOrder() { return madeToOrder; }
    public void setMadeToOrder(Boolean madeToOrder) { this.madeToOrder = madeToOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
