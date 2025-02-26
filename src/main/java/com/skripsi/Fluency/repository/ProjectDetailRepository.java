package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.ProjectDetail;
import com.skripsi.Fluency.model.entity.ProjectHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectDetailRepository extends JpaRepository<ProjectDetail, Integer> {
    List<ProjectDetail> findByProjectHeaderOrderByDeadlineDateAscDeadlineTimeAsc(ProjectHeader projectHeader);

    List<ProjectDetail> findByProjectHeaderInAndDeadlineDateBetween(List<ProjectHeader> projectHeaders, LocalDate startDate, LocalDate endDate);

    // Ambil semua ProjectDetail based on influencer_id dari ProjectHeader
    @Query("SELECT pd FROM ProjectDetail pd WHERE pd.projectHeader.influencer.id = :influencerId")
    List<ProjectDetail> findByInfluencerId(@Param("influencerId") Integer influencerId);
}
