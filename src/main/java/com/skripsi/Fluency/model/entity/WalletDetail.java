package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table
@Data
public class WalletDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_header_id")
    private WalletHeader walletHeader;

    @ManyToOne
    @JoinColumn(name = "transacion_type_id")
    private TransactionType transactionType;

    @Column
    private Double nominal;
    private LocalDateTime dateTime;


}
