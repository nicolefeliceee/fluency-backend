package com.skripsi.Fluency.model.entity;

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
public class TopComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_detail_id")
    private ProjectDetail projectDetail;

    @Column(length = 55)
    private String username;

    @Column(length = 255)
    private String comment;

    @Column
    private Integer likes;
    private LocalDateTime dateTime;
}
