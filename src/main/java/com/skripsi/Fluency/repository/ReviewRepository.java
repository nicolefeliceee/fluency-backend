package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByInfluencerId(Integer influencerId);
}
