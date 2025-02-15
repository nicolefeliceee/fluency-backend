package com.skripsi.Fluency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.Fluency.model.dto.*;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    public WalletHeaderRepository walletHeaderRepository;

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
                return LoginResponseDto.builder()
                        .id(null)
                        .name(null)
                        .instagramId(instagramId)
                        .build();
            }

//            Kalau ketemu update token nya dulu di database
            influencer.setToken(loginInfluencerRequestDto.getToken());
            influencerRepository.save(influencer);

//            Build responsenya
            LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                    .id(influencer.getUser().getId())
                    .name(influencer.getUser().getName())
                    .instagramId(instagramId)
                    .build();

            return loginResponseDto;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Transactional
    public ResponseEntity<?> signUpBrand(String requestString, MultipartFile profilePicture) throws IOException {
        SignupBrandRequestDto requestDto;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
             requestDto = objectMapper.readValue(requestString, SignupBrandRequestDto.class);

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

            byte[] profilePictureByte = null;
            String profilePictureType = null;
            String profilePictureName = null;
            if(profilePicture != null && !profilePicture.isEmpty()) {
                profilePictureByte = profilePicture.getBytes();
                profilePictureType = profilePicture.getContentType();
                profilePictureName = profilePicture.getName();
            }

//            System.out.println(Arrays.toString(profilePictureByte));
//            System.out.println(Arrays.toString(profilePicture.getBytes()));

            Brand newBrand = Brand.builder()
                    .password(requestDto.getPassword())
                    .profilePictureByte(profilePictureByte)
                    .profilePictureType(profilePictureType)
                    .profilePictureName(profilePictureName)
                    .user(savedUser)
                    .category(category)
                    .ages(targetAges)
                    .genders(targetGender)
                    .locations(targetLocation)
                    .build();

            Brand savedBrand = brandRepository.save(newBrand);

//            sambungin user ke brand
            savedUser.setBrand(savedBrand);
            userRepository.save(savedUser);

//            create wallet header
            WalletHeader walletHeader = WalletHeader.builder()
                    .balance(0)
                    .user(savedUser)
                    .build();

            walletHeaderRepository.save(walletHeader);

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }

        return ResponseEntity.ok(requestDto);

    }

    @Transactional
    public ResponseEntity<?> signUpInfluencer(SignupInfluencerRequestDto requestDto) {

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
                    .userType("influencer")
                    .build();

            User savedUser = userRepository.save(newUser);

//        habis itu create influencer
            Gender found = genderRepository.findById(Integer.valueOf(requestDto.getGender())).orElse(null);

            Influencer newInfluencer = Influencer.builder()
                    .user(savedUser)
                    .dob(LocalDate.parse(requestDto.getDob()))
                    .instagramId(requestDto.getInstagramId())
                    .token(requestDto.getToken())
                    .gender(found)
                    .isActive(false)
                    .build();

            Influencer savedInfluencer = influencerRepository.save(newInfluencer);


            //            sambungin user ke influencer
            savedUser.setInfluencer(savedInfluencer);
            userRepository.save(savedUser);

            //            create wallet header
            WalletHeader walletHeader = WalletHeader.builder()
                    .balance(0)
                    .user(savedUser)
                    .build();

            walletHeaderRepository.save(walletHeader);

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }

        return ResponseEntity.ok(requestDto);

    }



    public ResponseEntity<?> findEmail(String email) {
        User check = userRepository.findByEmail(email);

        if(check == null) {
            return ResponseEntity.ok("");
        }

        return ResponseEntity.ok( check.getEmail());
    }


    public ResponseEntity<?> getProfile(String userId) {
        try {
            User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);

            if(user == null) {
                return ResponseEntity.notFound().build();
            }

            if(user.getUserType().equalsIgnoreCase("brand")) {

                List<Map<String, String>> targetAge = new ArrayList<>();
                targetAge = user.getBrand().getAges().stream().map(
                        item -> {
                            Map<String, String> newMap = new HashMap<>();
                            newMap.put("id", String.valueOf(item.getId()));
                            newMap.put("label", item.getLabel());
                            return newMap;
                        }
                ).collect(Collectors.toList());

                List<Map<String, String>> targetLocation = new ArrayList<>();
                targetLocation = user.getBrand().getLocations().stream().map(
                        item -> {
                            Map<String, String> newMap = new HashMap<>();
                            newMap.put("id", String.valueOf(item.getId()));
                            newMap.put("label", item.getLabel());
                            return newMap;
                        }
                ).collect(Collectors.toList());

                List<Map<String, String>> targetGender = new ArrayList<>();
                targetGender = user.getBrand().getGenders().stream().map(
                        item -> {
                            Map<String, String> newMap = new HashMap<>();
                            newMap.put("id", String.valueOf(item.getId()));
                            newMap.put("label", item.getLabel());
                            return newMap;
                        }
                ).collect(Collectors.toList());

                BrandProfileDto brandProfileDto = BrandProfileDto.builder()
                        .name(user.getName())
                        .category(user.getBrand().getCategory().getLabel())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .location(user.getLocation().getLabel())
                        .targetAgeRange(targetAge)
                        .targetGender(targetGender)
                        .targetLocation(targetLocation)
                        .profilePictureByte(user.getBrand().getProfilePictureByte())
                        .profilePictureType(user.getBrand().getProfilePictureType())
                        .profilePictureName(user.getBrand().getProfilePictureName())
                        .build();

                return ResponseEntity.ok(brandProfileDto);

            } else if(user.getUserType().equalsIgnoreCase("influencer")) {
                List<Map<String, String>> categories = new ArrayList<>();
                categories = user.getInfluencer().getCategories().stream().map(
                        item -> {
                            Map<String, String> newMap = new HashMap<>();
                            newMap.put("id", String.valueOf(item.getId()));
                            newMap.put("label", item.getLabel());
                            return newMap;
                        }
                ).collect(Collectors.toList());

                String feedsPrice = user.getInfluencer().getInfluencerMediaTypes().stream().filter(
                        item -> item.getMediaType().getLabel().equalsIgnoreCase("feeds")
                ).toString();

                String reelsPrice = user.getInfluencer().getInfluencerMediaTypes().stream().filter(
                        item -> item.getMediaType().getLabel().equalsIgnoreCase("reels")
                ).toString();

                String storyPrice = user.getInfluencer().getInfluencerMediaTypes().stream().filter(
                        item -> item.getMediaType().getLabel().equalsIgnoreCase("story")
                ).toString();

                InfluencerProfileDto influencerProfileDto = InfluencerProfileDto.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .gender(user.getInfluencer().getGender().getLabel())
                        .instagramId(user.getInfluencer().getInstagramId())
                        .category(categories)
                        .token(user.getInfluencer().getToken())
                        .feedsPrice(feedsPrice)
                        .reelsPrice(reelsPrice)
                        .storyPrice(storyPrice)
                        .build();

                return ResponseEntity.ok(influencerProfileDto);
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }


        return null;
    }
}
