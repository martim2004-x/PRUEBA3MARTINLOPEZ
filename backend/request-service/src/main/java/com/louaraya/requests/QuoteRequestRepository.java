package com.louaraya.requests;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    List<QuoteRequest> findAllByOrderByCreatedAtDesc();

    List<QuoteRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<QuoteRequest> findByStatusOrderByCreatedAtDesc(String status);
}
