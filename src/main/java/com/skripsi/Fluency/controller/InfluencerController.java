package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.InfluencerFilterRequestDto;
import com.skripsi.Fluency.model.dto.InfluencerFilterResponseDto;
import com.skripsi.Fluency.service.InfluencerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("influencer")
public class InfluencerController {

    @Autowired
    public InfluencerService influencerService;

    @PostMapping("filter")
    public ResponseEntity<?> filterInfluencer(@RequestBody InfluencerFilterRequestDto influencerFilterRequestDto) {
        try {
            System.out.println("ini masuk influencer controller");
            List<InfluencerFilterResponseDto> response = influencerService.filterInfluencer(influencerFilterRequestDto);
            return ResponseEntity.ok(response);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }



}
