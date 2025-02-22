package com.skripsi.Fluency.repository;

import com.skripsi.Fluency.model.entity.WalletDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WalletDetailRepository extends JpaRepository<WalletDetail, Integer> {
    @Query("SELECT COALESCE(SUM(wd.nominal), 0) " +
            "FROM WalletDetail wd " +
            "WHERE wd.walletHeader.id = :walletHeaderId " +
            "AND wd.transactionType.id = 4")
    Integer getTotalRevenueByInfluencer(@Param("walletHeaderId") Integer walletHeaderId);

    List<WalletDetail> findByWalletHeaderIdAndTransactionTypeIdAndDateTimeBetween(
            Integer walletHeaderId, Integer transactionTypeId, LocalDateTime startDate, LocalDateTime endDate
    );
}
