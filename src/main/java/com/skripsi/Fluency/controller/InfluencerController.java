package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.InfluencerFilterRequestDto;
import com.skripsi.Fluency.model.dto.InfluencerFilterResponseDto;
import com.skripsi.Fluency.model.dto.SortFilterDto;
import com.skripsi.Fluency.model.dto.SortRequestDto;
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

        @PostMapping("sort")
        public ResponseEntity<?> sortInfluencer(@RequestBody SortRequestDto sortRequestDto) {
            try {
                System.out.println("ini masuk influencer sort controller");
                System.out.println("sortRequestDto" + sortRequestDto);
                InfluencerFilterRequestDto dto = new InfluencerFilterRequestDto();
                dto.setFollowers(sortRequestDto.getFollowers2());
                dto.setMedia(sortRequestDto.getMedia2());
                dto.setGender(sortRequestDto.getGender2());
                dto.setAge(sortRequestDto.getAge2());
                dto.setPrice(sortRequestDto.getPrice2());
                dto.setRating(sortRequestDto.getRating2());
                dto.setLocation(sortRequestDto.getLocation2());
                dto.setGenderAudience(sortRequestDto.getGenderAudience2());
                dto.setAgeAudience(sortRequestDto.getAgeAudience2());


                List<InfluencerFilterResponseDto> sortedInfluencers = influencerService.sortInfluencer(sortRequestDto.getSort(), dto);
                return ResponseEntity.ok(sortedInfluencers);
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        }



}
