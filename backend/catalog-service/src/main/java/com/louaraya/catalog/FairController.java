package com.louaraya.catalog;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FairController {

    private final FairRepository repository;

    public FairController(FairRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/fairs")
    public List<Fair> all() {
        return repository.findAllByOrderByIdAsc();
    }
}
