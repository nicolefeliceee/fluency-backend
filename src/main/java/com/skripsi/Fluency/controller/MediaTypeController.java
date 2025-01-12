package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.service.AgeService;
import com.skripsi.Fluency.service.MediaTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("media-type")
public class MediaTypeController {
    @Autowired
    public MediaTypeService mediaTypeService;

    @GetMapping
    public ResponseEntity<?> getAllMediaType() {

        try {
            List<Map<String, String>> response = mediaTypeService.getAllMediaType();

            return ResponseEntity.ok(response);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
