package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.dto.CreateReviewDto;
import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.ProjectHeader;
import com.skripsi.Fluency.model.entity.Review;
import com.skripsi.Fluency.repository.BrandRepository;
import com.skripsi.Fluency.repository.InfluencerRepository;
import com.skripsi.Fluency.repository.ProjectHeaderRepository;
import com.skripsi.Fluency.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    public ReviewRepository reviewRepository;

    @Autowired
    public InfluencerRepository influencerRepository;

    @Autowired
    public BrandRepository brandRepository;

    @Autowired
    public ProjectHeaderRepository projectHeaderRepository;

    public ResponseEntity<?> getReviewByInfluencer(String influencerId) {

        Influencer influencer = influencerRepository.findById(Integer.valueOf(influencerId)).orElse(null);

        if(influencer == null) {
            return ResponseEntity.notFound().build();
        }

        List<Review> reviews = reviewRepository.findAllByInfluencer(influencer);

        List<CreateReviewDto> responseDto = reviews.stream().map(
                item -> {
                    return CreateReviewDto.builder()
                            .review(item.getReview())
                            .rating(item.getRating())
                            .brandId(item.getBrand().getId().toString())
                            .influencerId(item.getInfluencer().getId().toString())
                            .projectHeaderId(item.getProjectHeader().getId().toString())
                            .build();
                }
        ).toList();

        return ResponseEntity.ok(responseDto);
    }

    public  ResponseEntity<?> createReview(CreateReviewDto requestDto) {


        ProjectHeader projectHeader = projectHeaderRepository.findById(Integer.valueOf(requestDto.getProjectHeaderId())).orElse(null);
//        Influencer influencer = influencerRepository.findById(Integer.valueOf(requestDto.getInfluencerId())).orElse(null);
//        Brand brand = brandRepository.findById(Integer.valueOf(requestDto.getBrandId())).orElse(null);

//        validate by project
        Review existing = reviewRepository.findByProjectHeader(projectHeader);

        if(existing!= null) {
            return ResponseEntity.badRequest().build();
        }

        Review entity = Review.builder()
                .review(requestDto.getReview())
                .rating(requestDto.getRating())
                .influencer(projectHeader.getInfluencer())
                .brand(projectHeader.getBrand())
                .projectHeader(projectHeader)
                .dateTime(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(entity);

        return ResponseEntity.ok(requestDto);
    }

    public ResponseEntity<?> getReviewByProjectHeader(String projectHeaderId) {
        ProjectHeader project = projectHeaderRepository.findById(Integer.valueOf(projectHeaderId)).orElse(null);

        if(project == null) {
            return ResponseEntity.notFound().build();
        }

        Review review = reviewRepository.findByProjectHeader(project);

        if(review == null) {
            return ResponseEntity.notFound().build();
        }

        CreateReviewDto responseDto = CreateReviewDto.builder()
                            .review(review.getReview())
                            .rating(review.getRating())
                            .brandId(review.getBrand().getId().toString())
                            .influencerId(review.getInfluencer().getId().toString())
                            .projectHeaderId(review.getProjectHeader().getId().toString())
                            .build();

        return ResponseEntity.ok(responseDto);
    }
}
