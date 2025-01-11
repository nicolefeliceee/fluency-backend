package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenderRepository extends JpaRepository<Gender, Integer> {
}
