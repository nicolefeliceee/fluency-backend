package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InfluencerRepository extends JpaRepository<Influencer, Integer>, JpaSpecificationExecutor<Influencer> {
    Influencer findByInstagramId(String instagramId);
}
