package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.service.AgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("age")
public class AgeController {

    @Autowired
    public AgeService ageService;

    @GetMapping
    public ResponseEntity<?> getAllAge() {

        try {
            List<Map<String, String>> response = ageService.getAllAge();

            return ResponseEntity.ok(response);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
