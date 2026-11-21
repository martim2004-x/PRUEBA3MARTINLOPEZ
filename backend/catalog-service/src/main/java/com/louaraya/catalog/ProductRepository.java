package com.louaraya.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByIdDesc();

    List<Product> findByActiveTrueOrderByIdDesc();

    Optional<Product> findByCodeIgnoreCase(String code);
}
