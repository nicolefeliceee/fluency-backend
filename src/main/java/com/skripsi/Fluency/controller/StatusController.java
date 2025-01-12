package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("status")
public class StatusController {

    @Autowired
    public StatusService service;

    @GetMapping()
    public ResponseEntity<?> getAllStatus() {
        List<Map<String, Object>> response =  service.getAllStatus();
        return ResponseEntity.ok(response);
    }
}
