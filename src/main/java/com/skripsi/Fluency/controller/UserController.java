package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.BrandDto;
import com.skripsi.Fluency.model.dto.LoginDto;
import com.skripsi.Fluency.model.entity.User;
import com.skripsi.Fluency.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    public UserService userService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            BrandDto user = userService.login(loginDto);
            if (user == null){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

    }
}
