package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.*;
import com.skripsi.Fluency.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
//
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }


    @PostMapping(value = "brand/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> signupBrand(
            @RequestPart("data") String requestDto,
            @RequestPart(value = "profile_picture", required = false) MultipartFile profilePicture
            ) {

        try {
            ResponseEntity<?> response = userService.signUpBrand(requestDto, profilePicture);

            return response;
        } catch(Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("influencer/signup")
    public ResponseEntity<?> signupInfluencer(@RequestBody SignupInfluencerRequestDto requestDto) {
        System.out.println(requestDto);

        try {
            ResponseEntity<?> response = userService.signUpInfluencer(requestDto);
            return response;
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("validation/email/{email}")
    public ResponseEntity<?> validateEmail(@PathVariable String email) {
        return userService.findEmail(email);
    }

    @GetMapping("profile/{user-id}")
    public ResponseEntity<?>  getProfile(@PathVariable(name = "user-id") String userId) {

        try {
            return userService.getProfile(userId);
        } catch(Exception ex) {
            return ResponseEntity.internalServerError().build() ;
        }
    }

    @PutMapping(value = "brand/profile/{user-id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> editProfileBrand(
            @PathVariable(name = "user-id") String userId,
            @RequestPart("data") String requestDto,
            @RequestPart(value = "profile_picture", required = false) MultipartFile profilePicture
    ) {
        try {
            ResponseEntity<?> response = userService.editProfileBrand(userId, requestDto, profilePicture);

            return response;
        } catch(Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("influencer/profile/{user-id}")
    public ResponseEntity<?> editProfileInfluencer(@PathVariable(name = "user-id") String userId, @RequestBody SignupInfluencerRequestDto requestDto) {
        System.out.println(requestDto);
        try {
            ResponseEntity<?> response = userService.editProfileInfluencer(userId, requestDto);
            return response;
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("all")
    public ResponseEntity<?> getAllUser() {
        try {
            List<UserDto> userDto = userService.getAllUser();
            return ResponseEntity.ok(userDto);
        } catch(Exception ex) {
            return ResponseEntity.internalServerError().build() ;
        }
    }
    
    @PutMapping("/block/{id}")
    public ResponseEntity<?> toggleBlockStatus(@PathVariable(name = "id") Integer id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String result = userService.toggleBlockStatus(id, status);

        if ("User not found".equals(result)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("status", result));
    }
}
