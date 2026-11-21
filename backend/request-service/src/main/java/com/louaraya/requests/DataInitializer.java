package com.louaraya.requests;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final QuoteRequestRepository repository;

    public DataInitializer(QuoteRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        QuoteRequest request = new QuoteRequest();
        request.setUserId(2L);
        request.setClientName("Valentina Rojas");
        request.setProductId(1L);
        request.setProductName("Arco Ceremonial Aliento");
        request.setEventType("Matrimonio");
        request.setWidthCm(220);
        request.setHeightCm(180);
        request.setMaterial("Algodón natural");
        request.setColors("Crudo,Salvia");
        request.setRequiredDate(System.currentTimeMillis() + 75L * 24 * 60 * 60 * 1000);
        request.setNotes("Ceremonia en jardín, necesito que sea desarmable.");
        request.setStatus("Pendiente");
        repository.save(request);
    }
}
