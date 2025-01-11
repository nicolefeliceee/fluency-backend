package com.skripsi.Fluency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.Fluency.model.dto.LoginInfluencerRequestDto;
import com.skripsi.Fluency.model.dto.LoginResponseDto;
import com.skripsi.Fluency.model.dto.LoginBrandRequestDto;
import com.skripsi.Fluency.model.dto.SignupBrandRequestDto;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public BrandRepository brandRepository;

    @Autowired
    public LocationRepository locationRepository;

    @Autowired
    public GenderRepository genderRepository;

    @Autowired
    public AgeRepository ageRepository;

    @Autowired
    public CategoryRepository categoryRepository;

    public LoginResponseDto login(LoginBrandRequestDto loginBrandRequestDto) {
        User user = userRepository.findByEmail(loginBrandRequestDto.getEmail());
        if (user == null){
            return null;
        }
        Brand brand = user.getBrand();
        if (loginBrandRequestDto.getPassword().equals(brand.getPassword())){

            LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                    .id(brand.getUser().getId())
                    .name(brand.getUser().getName())
                    .build();

            return loginResponseDto;
        }else{
            return null;
        }

    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public InfluencerRepository influencerRepository;

    @Value(value = "${base.url}")
    private String baseUrl;

    public LoginResponseDto loginInfluencer(LoginInfluencerRequestDto loginInfluencerRequestDto){
        try{
//            Hit URL API Instagram
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/me/accounts")
                    .queryParam("fields", "connected_instagram_account")
                    .queryParam("access_token", loginInfluencerRequestDto.getToken());

//            Ambil response
            ResponseEntity<?> response = restTemplate.getForEntity(builder.toUriString(), String.class);

//            Ubah response kedalam bentuk JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(String.valueOf(response.getBody()));

//            Ambil data saja
            JsonNode data = jsonNode.get("data").get(0);

//            Ambil per field
            String instagramId = data.get("connected_instagram_account").get("id").asText();

//            Cari influencer based on ig id nya
            Influencer influencer = influencerRepository.findByInstagramId(instagramId);

//            Kalau ga ketemu, return null
            if (influencer == null){
                return null;
            }

//            Kalau ketemu update token nya dulu di database
            influencer.setToken(loginInfluencerRequestDto.getToken());
            influencerRepository.save(influencer);

//            Build responsenya
            LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                    .id(influencer.getUser().getId())
                    .name(influencer.getUser().getName())
                    .build();

            return loginResponseDto;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Transactional
    public ResponseEntity<?> signUpBrand(SignupBrandRequestDto requestDto) {

        try {

//            check email exist
            User check = userRepository.findByEmail(requestDto.getEmail());

            if(check != null) {
                return ResponseEntity.ok("Email already exists");
            }


//        create user dulu
            Location location = locationRepository.findById(requestDto.getLocation()).orElse(null);

            User newUser = User.builder()
                    .name(requestDto.getName())
                    .email(requestDto.getEmail())
                    .phone(requestDto.getPhone())
                    .location(location)
                    .userType("brand")
                    .build();

            User savedUser = userRepository.save(newUser);

//        habis itu create brand
            List<Age> targetAges = new ArrayList<>();
            List<Gender> targetGender = new ArrayList<>();
            List<Location> targetLocation = new ArrayList<>();
            Category category = categoryRepository.findById(Integer.valueOf(requestDto.getCategory()[0])).orElse(null);

            for(String item: requestDto.getTargetAgeRange()) {
                Age found = ageRepository.findById(Integer.valueOf(item)).orElse(null);
                targetAges.add(found);
            }

            for(String item: requestDto.getTargetGender()) {
                Gender found = genderRepository.findById(Integer.valueOf(item)).orElse(null);
                targetGender.add(found);
            }

            for(String item: requestDto.getTargetLocation()) {
                Location found = locationRepository.findById(Integer.valueOf(item)).orElse(null);
                targetLocation.add(found);
            }

            Brand newBrand = Brand.builder()
                    .password(requestDto.getPassword())
                    .profilePicture("")
                    .user(savedUser)
                    .category(category)
                    .ages(targetAges)
                    .genders(targetGender)
                    .locations(targetLocation)
                    .build();

            Brand savedBrand = brandRepository.save(newBrand);

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }

        return ResponseEntity.ok(requestDto);

    }


    public ResponseEntity<?> findEmail(String email) {
        User check = userRepository.findByEmail(email);

        return ResponseEntity.ok(check.getEmail());
    }
}
