package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    public BrandService brandService;

    @GetMapping("{id}")
    public ResponseEntity<?> getBrandById(@PathVariable("id") String brandId) {
        return this.brandService.getBrandById(brandId);
    }


}
