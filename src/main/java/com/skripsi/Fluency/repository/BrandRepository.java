package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.Brand;
import com.skripsi.Fluency.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Brand findByUser(User user);
}
