package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InfluencerRepository extends JpaRepository<Influencer, Integer>, JpaSpecificationExecutor<Influencer> {
    Influencer findByInstagramId(String instagramId);

    @Query("SELECT i FROM Influencer i LEFT JOIN i.reviews r GROUP BY i.id ORDER BY AVG(r.rating) DESC")
    List<Influencer> findAllOrderByAverageRatingDesc();

    @Query("""
           SELECT i 
           FROM Influencer i 
           LEFT JOIN i.influencerMediaTypes imt 
           GROUP BY i.id 
           ORDER BY MIN(imt.price) ASC
           """)
    List<Influencer> findAllOrderByLowestPriceAsc();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.influencer.id = :influencerId")
    Double findAverageRatingByInfluencerId(@Param("influencerId") Long influencerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.influencer.id = :influencerId")
    Integer findTotalReviewsByInfluencerId(@Param("influencerId") Long influencerId);

    Influencer findByUser(User user);
}
