package com.louaraya.catalog;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("data"));
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
