package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String name;
    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(length = 20)
    private String userType;

//    tambahan
    @OneToMany(mappedBy = "user")
    private List<WalletDetail> walletDetails;

    @OneToOne
    @JoinColumn(name = "wallet_header_id")
    private WalletHeader walletHeader;

    @OneToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToOne
    @JoinColumn(name = "influencer_id")
    private Influencer influencer;

    @OneToMany(mappedBy = "user1")
    private List<Chat> chats1;

    @OneToMany(mappedBy = "user2")
    private List<Chat> chats2;

    @OneToMany(mappedBy = "user")
    private List<Message> messages;
}
