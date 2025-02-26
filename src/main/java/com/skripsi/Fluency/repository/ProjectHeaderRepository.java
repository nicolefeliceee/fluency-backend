package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectHeaderRepository extends JpaRepository<ProjectHeader, Integer> {

    List<ProjectHeader> findAllByStatusAndBrandOrderByIdDesc(Status status, Brand brand);
    List<ProjectHeader> findAllByStatusAndInfluencerOrderByIdDesc(Status status, Influencer influencer);
    List<ProjectHeader> findByTitleContaining(String title);
    List<ProjectHeader> findAllByInfluencerAndTitleContainingIgnoreCaseOrderByIdDesc(Influencer influencer, String title);
    List<ProjectHeader> findAllByBrandAndTitleContainingIgnoreCaseOrderByIdDesc(Brand brand, String title);
    List<ProjectHeader> findAllByTitleContainingIgnoreCaseOrderByIdDesc(String title);
    List<ProjectHeader> findByInfluencerId(Integer influencerId);
    List<ProjectHeader> findByInfluencerIdAndStatusIdIn(Integer influencerId, List<Integer> statusIds);
    boolean existsByInfluencerIdAndStatusIdIn(Integer influencerId, List<Integer> statusIds);

    @Query(value = "SELECT * \n" +
            "FROM project_header ph\n" +
            "WHERE ph.finished_date IS NOT NULL \n" +
            "AND ph.finished_date <= NOW() - interval '7 days'\n" +
            "and ph.status_id = 5;", nativeQuery = true)
    List<ProjectHeader> findFinishSevenDaysAgoProjects();
}
