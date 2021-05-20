package com.example.controller;

import com.example.dto.Center;
import com.example.service.FetchAndFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/slots")
public class Controller {

    @Autowired
    private FetchAndFilterService service;

    @GetMapping
    public Map<String, Map<String, List<Center>>> getAvailableCenters() {
        return service.getAvailableCenters();
    }
}
