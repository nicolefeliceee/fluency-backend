package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.WalletHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletHeaderRepository extends JpaRepository<WalletHeader, Integer> {
    Optional<WalletHeader> findByUserId(Integer userId);
}
