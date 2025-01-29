package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionTypeRepository extends JpaRepository<TransactionType, Integer> {
}
