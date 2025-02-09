package com.skripsi.Fluency.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SignupBrandRequestDto {
    private String userId;
    private String name;
    private String email;
    private Integer location;
    private String phone;
    private String password;
    private String[] category;
    private String[] targetAgeRange;
    private String[] targetGender;
    private String[] targetLocation;
    private String userType;
    private MultipartFile profilePicture;
}
