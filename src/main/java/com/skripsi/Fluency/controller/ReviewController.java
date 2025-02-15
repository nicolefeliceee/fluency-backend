package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.CreateReviewDto;
import com.skripsi.Fluency.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("review")
public class ReviewController {

    @Autowired
    public ReviewService reviewService;

    @GetMapping("influencer")
    public ResponseEntity<?> getReviewByInfluencer(@RequestParam(name = "influencer-id") String influencerId) {
        return this.reviewService.getReviewByInfluencer(influencerId);
    }

    @PostMapping()
    public ResponseEntity<?> createReview(@RequestBody CreateReviewDto requestDto) {
        return reviewService.createReview(requestDto);
    }

    @GetMapping("project-header")
    public ResponseEntity<?> getReviewByProjectHeader(@RequestParam(name = "project-header-id") String projectId) {
        return this.reviewService.getReviewByProjectHeader(projectId);
    }
}
