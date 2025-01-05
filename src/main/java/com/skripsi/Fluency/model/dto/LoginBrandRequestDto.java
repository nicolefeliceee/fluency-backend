package com.skripsi.Fluency.model.dto;

import lombok.Data;

@Data
public class LoginBrandRequestDto {
    // kiriman login as brand dari front end
    private String email;
    private String password;
}
