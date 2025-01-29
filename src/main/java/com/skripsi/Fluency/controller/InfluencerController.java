package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.InfluencerFilterRequestDto;
import com.skripsi.Fluency.model.dto.InfluencerFilterResponseDto;
import com.skripsi.Fluency.model.dto.SortFilterDto;
import com.skripsi.Fluency.model.dto.SortRequestDto;
import com.skripsi.Fluency.service.InfluencerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("influencer")
public class InfluencerController {

    @Autowired
    public InfluencerService influencerService;

    @PostMapping("filter/{user-id}")
    public ResponseEntity<?> filterInfluencer(@RequestBody InfluencerFilterRequestDto influencerFilterRequestDto, @PathVariable(name = "user-id") Integer brandId) {
        try {
            System.out.println("ini masuk influencer controller");
            List<InfluencerFilterResponseDto> response = influencerService.filterInfluencer(influencerFilterRequestDto, brandId);

            return ResponseEntity.ok(response);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("sort/{user-id}")
    public ResponseEntity<?> sortInfluencer(@RequestBody SortRequestDto sortRequestDto, @PathVariable(name = "user-id") Integer brandId) {
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


            List<InfluencerFilterResponseDto> sortedInfluencers = influencerService.sortInfluencer(sortRequestDto.getSort(), dto, brandId);
            return ResponseEntity.ok(sortedInfluencers);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("top-influencer/{user-id}")
    public ResponseEntity<?> getTopInfluencer(@PathVariable(name = "user-id") String userId) {

        try {
            return ResponseEntity.ok(influencerService.getTopInfluencer(userId));
        } catch(Exception ex) {
            return ResponseEntity.internalServerError().build() ;
        }
    }

    @GetMapping("recommendation/{user-id}")
    public ResponseEntity<?> getRecommendation(@PathVariable(name = "user-id") String userId) {

        try {
            return ResponseEntity.ok(influencerService.getRecommendation(userId));
        } catch(Exception ex) {
            return ResponseEntity.internalServerError().build() ;
        }
    }

    @PostMapping("save/{user-id}")
    public ResponseEntity<?> saveInfluencer(@PathVariable(name = "user-id") Integer brandUserId, @RequestBody HashMap<String,Integer> influencerUserIdMap) {
        try {
            System.out.println("ini masuk controller save influencer");

            Integer influencerUserId = influencerUserIdMap.get("influencer_user_id");

            System.out.println("brandUserId: " + brandUserId);
            System.out.println("influencerUserId: " + influencerUserId);
            // Memanggil service untuk menyimpan influencer
            String result = influencerService.saveInfluencer(brandUserId, influencerUserId);

            if ("Influencer saved successfully".equals(result)) {
                return ResponseEntity.ok(influencerUserIdMap);
            } else if ("Brand not found".equals(result) || "Influencer not found".equals(result)) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
                return ResponseEntity.notFound().build() ;
            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                return ResponseEntity.badRequest().build() ;
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build() ;
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving influencer: " + e.getMessage());
        }
    }

    @PostMapping("unsave/{user-id}")
    public ResponseEntity<?> unsaveInfluencer(@PathVariable(name = "user-id") Integer brandUserId, @RequestBody HashMap<String,Integer> influencerUserIdMap) {
        try {
            System.out.println("ini masuk controller unsave influencer");

            Integer influencerUserId = influencerUserIdMap.get("influencer_user_id");

            System.out.println("brandUserId: " + brandUserId);
            System.out.println("influencerUserId: " + influencerUserId);
            // Memanggil service untuk menyimpan influencer
            String result = influencerService.unsaveInfluencer(brandUserId, influencerUserId);

            if ("Influencer unsaved successfully".equals(result)) {
                return ResponseEntity.ok(influencerUserIdMap);
            } else if ("Brand not found".equals(result) || "Influencer not found".equals(result)) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
                return ResponseEntity.notFound().build() ;
            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                return ResponseEntity.badRequest().build() ;
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build() ;
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving influencer: " + e.getMessage());
        }
    }

        @GetMapping("{influencer-id}")
        public ResponseEntity<?> getInfluencer(@PathVariable(name = "influencer-id") String id) {
            return this.influencerService.getInfluencer(id);
        }

}
