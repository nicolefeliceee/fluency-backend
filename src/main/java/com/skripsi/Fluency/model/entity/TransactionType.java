package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class TransactionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

//    tambahan
    @OneToMany(mappedBy = "transactionType")
    private List<WalletDetail> walletDetails;
}
