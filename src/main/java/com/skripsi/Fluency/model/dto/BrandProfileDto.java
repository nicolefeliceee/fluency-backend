package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandProfileDto {
    private String name;
    private String email;
    private String location;
    private String phone;
    private String password;
    private String category;
    private List<?> targetAgeRange;
    private List<?> targetGender;
    private List<?> targetLocation;
    private String userType;
    private String profilePicture;
}
