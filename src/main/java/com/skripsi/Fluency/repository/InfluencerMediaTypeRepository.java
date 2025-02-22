package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.InfluencerMediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfluencerMediaTypeRepository extends JpaRepository<InfluencerMediaType, Integer> {
    List<InfluencerMediaType> findByInfluencer(Influencer influencer);

    boolean existsByInfluencer_Id(Integer influencerId);
}
