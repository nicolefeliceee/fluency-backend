package com.skripsi.Fluency.model.dto;

import com.skripsi.Fluency.model.entity.Gender;
import com.skripsi.Fluency.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InfluencerFilterResponseDto {

    private Integer id;
    private String name;
}
