package com.louaraya.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FairRepository extends JpaRepository<Fair, Long> {
    List<Fair> findAllByOrderByIdAsc();
}
