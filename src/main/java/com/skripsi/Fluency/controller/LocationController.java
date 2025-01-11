package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("location")
public class LocationController {

    @Autowired
    public LocationService locationService;

    @GetMapping
    public ResponseEntity<?> findAll() {
        try {
            List<Map<String, String>> list = this.locationService.getAllLocations();

            return ResponseEntity.ok(list);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
