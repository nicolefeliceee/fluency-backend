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

    // Query untuk mendapatkan influencer berdasarkan jumlah status ID 3 dan 4 terbanyak
    @Query("SELECT i, COUNT(p) AS statusCount " +
            "FROM Influencer i " +
            "JOIN i.projectHeaders p " +
            "JOIN p.status s " +
            "WHERE s.id IN (3, 4) " +
            "GROUP BY i " +
            "ORDER BY statusCount DESC")
    List<Influencer> findTopInfluencers();

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Brand b JOIN b.influencers i " +
            "WHERE b.id = :brandId AND i.id = :influencerId")
    boolean isInfluencerSaved(@Param("brandId") Integer brandId, @Param("influencerId") Integer influencerId);

    Influencer findByUser(User user);

    @Query("SELECT b.influencers FROM Brand b WHERE b.id = :brandId")
    List<Influencer> findSavedInfluencersByBrandId(@Param("brandId") Integer brandId);

}
