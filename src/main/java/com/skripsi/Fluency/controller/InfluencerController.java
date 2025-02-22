package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.*;
import com.skripsi.Fluency.service.InfluencerService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            dto.setCategoryChosen(sortRequestDto.getCategoryChosen2());


            List<InfluencerFilterResponseDto> sortedInfluencers = influencerService.sortInfluencer(sortRequestDto.getSort(), dto, brandId);
            return ResponseEntity.ok(sortedInfluencers);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("filter/saved/{user-id}")
    public ResponseEntity<?> filterInfluencerSaved(@RequestBody InfluencerFilterRequestDto influencerFilterRequestDto, @PathVariable(name = "user-id") Integer brandId) {
        try {
            System.out.println("ini masuk influencer controller");
            List<InfluencerFilterResponseDto> response = influencerService.filterInfluencerSaved(influencerFilterRequestDto, brandId);

            return ResponseEntity.ok(response);
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("sort/saved/{user-id}")
    public ResponseEntity<?> sortInfluencerSaved(@RequestBody SortRequestDto sortRequestDto, @PathVariable(name = "user-id") Integer brandId) {
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
            dto.setCategoryChosen(sortRequestDto.getCategoryChosen2());


            List<InfluencerFilterResponseDto> sortedInfluencers = influencerService.sortInfluencerSaved(sortRequestDto.getSort(), dto, brandId);
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

    @GetMapping("/search/{user-id}")
    public ResponseEntity<?> searchInfluencers(@PathVariable(name = "user-id") Integer userId, @RequestParam String query) {
        List<InfluencerFilterResponseDto> searchInfluencers = influencerService.searchInfluencers(query, String.valueOf(userId));
        return ResponseEntity.ok(searchInfluencers);
    }

    @GetMapping("/saved/search/{user-id}")
    public ResponseEntity<?> searchInfluencersSaved(@PathVariable(name = "user-id") Integer userId, @RequestParam String query) {
        List<InfluencerFilterResponseDto> searchInfluencersSaved = influencerService.searchInfluencersSaved(query, String.valueOf(userId));
        return ResponseEntity.ok(searchInfluencersSaved);
    }

    @GetMapping("/detail/{influencer-id}/{user-id}")
    public ResponseEntity<?> detailInfluencer(@PathVariable(name = "influencer-id") Integer influencerId, @PathVariable(name = "user-id") Integer userId) {
        InfluencerDetailResponseDto detailInfluencer = influencerService.detailInfluencer(influencerId, userId);
        return ResponseEntity.ok(detailInfluencer);
    }

//    ini untuk home influencer
    @GetMapping("/home/{user-id}")
    public ResponseEntity<?> detailHomeInfluencer(@PathVariable(name = "user-id") Integer userId) {
        InfluencerHomeDto detailHomeInfluencer = influencerService.detailHomeInfluencer(userId);
        return ResponseEntity.ok(detailHomeInfluencer);
    }

    @PostMapping("/update-status/{influencer-id}")
    public ResponseEntity<?> updateInfluencerStatus(@PathVariable(name = "influencer-id") Integer influencerId, @RequestBody Map<String, Boolean> requestBody) {
        boolean isActive = requestBody.getOrDefault("isactive", false);

        try {
            influencerService.updateStatus(influencerId, isActive);
            return ResponseEntity.ok(Collections.singletonMap("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/check-profile/{influencer-id}")
    public ResponseEntity<?> checkProfileCompletion(@PathVariable(name = "influencer-id") Integer influencerId) {
        boolean isProfileComplete = influencerService.isProfileComplete(influencerId);
        return ResponseEntity.ok(Collections.singletonMap("profileCompleted", isProfileComplete));
    }

    @GetMapping("/check-project/{influencer-id}")
    public ResponseEntity<?> checkProjectCompletion(@PathVariable(name = "influencer-id") Integer influencerId) {
        boolean isProjectComplete = influencerService.isProjectComplete(influencerId);
        return ResponseEntity.ok(Collections.singletonMap("projectCompleted", isProjectComplete));
    }

    @PostMapping("/get-project/{influencer-id}")
    public ResponseEntity<?> getProjectInfluencer(@PathVariable(name = "influencer-id") Integer influencerId, @RequestBody String dateString) {
        try {
            // Parse dateString yang berbentuk JSON {"date":"2025-02-19T09:59:19.422Z"}
            JSONObject jsonObject = new JSONObject(dateString);
            String dateFromJson = jsonObject.getString("date");

            // Konversi string tanggal ke LocalDate
            LocalDate date = LocalDate.parse(dateFromJson, DateTimeFormatter.ISO_DATE_TIME);

            List<ProjectInfluencerDto> projectInfluencerDto = influencerService.getProjectInfluencer(influencerId, date);

            return ResponseEntity.ok(projectInfluencerDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

}
