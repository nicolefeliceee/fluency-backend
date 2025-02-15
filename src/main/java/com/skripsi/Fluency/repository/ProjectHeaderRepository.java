package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectHeaderRepository extends JpaRepository<ProjectHeader, Integer> {

    List<ProjectHeader> findAllByStatusAndBrandOrderByIdDesc(Status status, Brand brand);
    List<ProjectHeader> findAllByStatusAndInfluencerOrderByIdDesc(Status status, Influencer influencer);
    List<ProjectHeader> findByTitleContaining(String title);
    List<ProjectHeader> findAllByInfluencerAndTitleContainingIgnoreCaseOrderByIdDesc(Influencer influencer, String title);
    List<ProjectHeader> findAllByBrandAndTitleContainingIgnoreCaseOrderByIdDesc(Brand brand, String title);
}
