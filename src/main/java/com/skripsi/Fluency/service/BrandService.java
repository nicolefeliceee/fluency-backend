package com.skripsi.Fluency.service;


import com.skripsi.Fluency.model.dto.BrandProfileDto;
import com.skripsi.Fluency.model.entity.Brand;
import com.skripsi.Fluency.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BrandService {

    @Autowired
    public BrandRepository brandRepository;

    public ResponseEntity<BrandProfileDto> getBrandById(String brandId) {
        Brand brand = brandRepository.findById(Integer.valueOf(brandId)).orElse(null);

        if(brand == null) {
            return ResponseEntity.notFound().build();
        }

        BrandProfileDto response = BrandProfileDto.builder()
                .name(brand.getUser().getName())
                .phone(brand.getUser().getPhone())
                .email(brand.getUser().getEmail())
                .location(capitalize(brand.getUser().getLocation().getLabel()))
                .profilePictureByte(brand.getProfilePictureByte())
                .profilePictureType(brand.getProfilePictureType())
                .profilePictureName(brand.getProfilePictureName())
                .category(brand.getCategory().getLabel())
                .build();

        return ResponseEntity.ok(response);
    }

    public String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }


}
