package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@Data
public class WalletHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Double balance;

    //    tambahan
    @OneToMany(mappedBy = "walletHeader")
    private List<WalletDetail> walletDetails;

}