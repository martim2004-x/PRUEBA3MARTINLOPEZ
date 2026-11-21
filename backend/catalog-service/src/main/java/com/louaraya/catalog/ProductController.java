package com.louaraya.catalog;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "catalog-service", "status", "UP", "products",
                String.valueOf(repository.count()));
    }

    @GetMapping("/products")
    public List<Product> all() {
        return repository.findAllByOrderByIdDesc();
    }

    @GetMapping("/products/active")
    public List<Product> active() {
        return repository.findByActiveTrueOrderByIdDesc();
    }

    @GetMapping("/products/code/{code}")
    public Product byCode(@PathVariable String code) {
        return repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @GetMapping("/products/{id}")
    public Product byId(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @PostMapping("/products")
    public Product create(@RequestBody Product product) {
        product.setId(null);
        if (product.getCode() == null || product.getCode().isBlank()) {
            long next = repository.count() + 1;
            product.setCode(String.format("LAM-%04d", next));
        }
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(System.currentTimeMillis());
        }
        return repository.save(product);
    }

    @PutMapping("/products/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        product.setId(id);
        return repository.save(product);
    }

    @DeleteMapping("/products/{id}")
    public Map<String, Boolean> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        repository.deleteById(id);
        return Map.of("deleted", true);
    }
}
