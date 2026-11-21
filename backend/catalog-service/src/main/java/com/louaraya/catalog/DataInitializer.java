package com.louaraya.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository repository;
    private final FairRepository fairRepository;

    public DataInitializer(ProductRepository repository, FairRepository fairRepository) {
        this.repository = repository;
        this.fairRepository = fairRepository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.save(product("LAM-0001", "Arco Ceremonial Aliento", "Matrimonios",
                    "Algodón natural", 220, 180, 189000, "Arco para ceremonias al aire libre."));
            repository.save(product("LAM-0002", "Tapiz Mural Sol Andino", "Interiores",
                    "Algodón natural", 120, 90, 99000, "Tapiz decorativo con motivos andinos."));
            repository.save(product("LAM-0003", "Colgante de Plantas Quillay", "Interiores",
                    "Yute", 40, 120, 34500, "Soporte de fibras naturales para plantas."));
            repository.save(product("LAM-0004", "Mandalas de Festival", "Festivales",
                    "Algodón reciclado", 90, 90, 52000, "Set de piezas para festivales."));
        }
        if (fairRepository.count() == 0) {
            fairRepository.save(fair("Feria Artesanal Parque Bicentenario", "Feria",
                    "Parque Bicentenario, Vitacura", "22 y 23 de noviembre",
                    "Piezas de macramé y fibras naturales.", -33.3830, -70.6100));
            fairRepository.save(fair("Showroom Lou Araya", "Showroom",
                    "Providencia, Santiago", "Todos los sábados",
                    "Visita guiada al taller.", -33.4260, -70.6140));
        }
    }

    private Fair fair(String name, String type, String place, String dateText,
                      String description, double lat, double lng) {
        Fair fair = new Fair();
        fair.setName(name);
        fair.setType(type);
        fair.setPlace(place);
        fair.setDateText(dateText);
        fair.setDescription(description);
        fair.setLat(lat);
        fair.setLng(lng);
        return fair;
    }

    private Product product(String code, String name, String category, String material,
                            int width, int height, int price, String description) {
        Product p = new Product();
        p.setCode(code);
        p.setName(name);
        p.setCategory(category);
        p.setMaterial(material);
        p.setWidthCm(width);
        p.setHeightCm(height);
        p.setPriceClp(price);
        p.setStock(10);
        p.setDescription(description);
        p.setActive(true);
        p.setMadeToOrder(false);
        return p;
    }
}
