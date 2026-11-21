package com.louaraya.requests;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quote_requests")
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String clientName;
    private Long productId;
    private String productName;
    private String eventType;
    private Integer widthCm;
    private Integer heightCm;
    private String material;
    private String colors;
    private Long requiredDate;
    private String notes;
    private String status;
    private Long createdAt = System.currentTimeMillis();

    public QuoteRequest() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Integer getWidthCm() { return widthCm; }
    public void setWidthCm(Integer widthCm) { this.widthCm = widthCm; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getColors() { return colors; }
    public void setColors(String colors) { this.colors = colors; }
    public Long getRequiredDate() { return requiredDate; }
    public void setRequiredDate(Long requiredDate) { this.requiredDate = requiredDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
