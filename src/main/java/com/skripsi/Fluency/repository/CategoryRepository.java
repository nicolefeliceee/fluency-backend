package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
