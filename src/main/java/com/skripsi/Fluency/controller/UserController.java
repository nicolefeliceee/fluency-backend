package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.LoginResponseDto;
import com.skripsi.Fluency.model.dto.LoginBrandRequestDto;
import com.skripsi.Fluency.model.dto.LoginInfluencerRequestDto;
import com.skripsi.Fluency.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    public UserService userService;

//    untuk cek login brand
    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginBrandRequestDto loginBrandRequestDto) {
        try {
            LoginResponseDto user = userService.login(loginBrandRequestDto);
            if (user == null){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }

    @PostMapping("token")
    public ResponseEntity<?>  loginInfluencer(@RequestBody LoginInfluencerRequestDto loginInfluencerRequestDto){
        try {
            LoginResponseDto response = userService.loginInfluencer(loginInfluencerRequestDto);
            if (response == null){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }



}
