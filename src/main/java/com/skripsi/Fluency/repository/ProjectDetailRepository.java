package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.ProjectDetail;
import com.skripsi.Fluency.model.entity.ProjectHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectDetailRepository extends JpaRepository<ProjectDetail, Integer> {
    List<ProjectDetail> findByProjectHeaderOrderByDeadlineDateAscDeadlineTimeAsc(ProjectHeader projectHeader);
}
