package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.service.GenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("gender")
public class GenderController {
    @Autowired
    public GenderService genderService;

    @GetMapping
    public ResponseEntity<?> getAllGender() {

        try {
            List<Map<String, String>> response = genderService.getAllGender();

            return ResponseEntity.ok(response);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
