package com.louaraya.requests;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
public class QuoteRequestController {

    private final QuoteRequestRepository repository;

    public QuoteRequestController(QuoteRequestRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "request-service", "status", "UP", "requests",
                String.valueOf(repository.count()));
    }

    @GetMapping("/requests")
    public List<QuoteRequest> all() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/requests/user/{userId}")
    public List<QuoteRequest> byUser(@PathVariable Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/requests/status/{status}")
    public List<QuoteRequest> byStatus(@PathVariable String status) {
        return repository.findByStatusOrderByCreatedAtDesc(status);
    }

    @GetMapping("/requests/{id}")
    public QuoteRequest byId(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    @PostMapping("/requests")
    public QuoteRequest create(@RequestBody QuoteRequest request) {
        request.setId(null);
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            request.setStatus("Pendiente");
        }
        if (request.getCreatedAt() == null) {
            request.setCreatedAt(System.currentTimeMillis());
        }
        return repository.save(request);
    }

    @PutMapping("/requests/{id}")
    public QuoteRequest update(@PathVariable Long id, @RequestBody QuoteRequest request) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada");
        }
        request.setId(id);
        return repository.save(request);
    }

    @PatchMapping("/requests/{id}/status")
    public QuoteRequest updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        QuoteRequest request = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        String status = body.get("status");
        if (status != null) {
            request.setStatus(status);
        }
        return repository.save(request);
    }

    @DeleteMapping("/requests/{id}")
    public Map<String, Boolean> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada");
        }
        repository.deleteById(id);
        return Map.of("deleted", true);
    }
}
