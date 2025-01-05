package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfluencerRepository extends JpaRepository<Influencer, Integer> {
    Influencer findByInstagramId(String instagramId);
}
