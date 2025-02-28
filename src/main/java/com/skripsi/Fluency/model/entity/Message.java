package com.skripsi.Fluency.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnoreProperties("messages") // ✅ Prevents infinite recursion
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(length = 255)
    private String text;

    @Column
    private LocalDateTime dateTime;

    @Column(length = 55)
    private String messageType;

    @Column(length = 255)
    private String url;

}
