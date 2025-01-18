package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Brand;
import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.ProjectHeader;
import com.skripsi.Fluency.model.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectHeaderRepository extends JpaRepository<ProjectHeader, Integer> {

    List<ProjectHeader> findAllByStatusAndBrand(Status status, Brand brand);
    List<ProjectHeader> findAllByStatusAndInfluencer(Status status, Influencer influencer);
}
