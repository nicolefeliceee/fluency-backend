package com.skripsi.Fluency.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate reportedDate;
    private LocalDate resolvedDate;
    private String status;

    @OneToOne
    @JoinColumn(name = "project_header_id")
    private ProjectHeader projectHeader;
}
