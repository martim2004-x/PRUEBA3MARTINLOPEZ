package com.louaraya.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fairs")
public class Fair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String place;
    private String dateText;
    private String description;
    private Double lat;
    private Double lng;

    public Fair() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getDateText() { return dateText; }
    public void setDateText(String dateText) { this.dateText = dateText; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}
