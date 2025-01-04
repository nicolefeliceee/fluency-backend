package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.dto.BrandDto;
import com.skripsi.Fluency.model.dto.LoginDto;
import com.skripsi.Fluency.model.entity.Brand;
import com.skripsi.Fluency.model.entity.Category;
import com.skripsi.Fluency.model.entity.User;
import com.skripsi.Fluency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;

    public BrandDto login(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail());
        if (user == null){
            return null;
        }
        Brand brand = user.getBrand();
        if (loginDto.getPassword().equals(brand.getPassword())){

            BrandDto brandDto = BrandDto.builder()
                    .id(brand.getUser().getId())
                    .email(brand.getUser().getEmail())
                    .name(brand.getUser().getName())
                    .build();

            return brandDto;
        }else{
            return null;
        }

    }
}
