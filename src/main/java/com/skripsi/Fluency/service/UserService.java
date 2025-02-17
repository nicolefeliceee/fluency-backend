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
    @Autowired
    public MediaTypeRepository mediaTypeRepository;
    @Autowired
    public InfluencerMediaTypeRepository influencerMediaTypeRepository;

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
    public ResponseEntity<?> editProfileBrand(String userId, String requestString, MultipartFile profilePicture) throws IOException {
        SignupBrandRequestDto requestDto;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            requestDto = objectMapper.readValue(requestString, SignupBrandRequestDto.class);

//            check email exist
            User existing = userRepository.findById(Integer.valueOf(userId)).orElse(null);

            if(existing == null) {
                return ResponseEntity.notFound().build();
            }


//        save user dulu
            Location location = locationRepository.findById(requestDto.getLocation()).orElse(null);

            existing.setName(requestDto.getName());
            existing.setEmail(requestDto.getEmail());
            existing.setPhone(requestDto.getPhone());
            existing.setLocation(location);

            userRepository.save(existing);

//        habis itu save brand
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

            Brand currBrand = existing.getBrand();

            currBrand.setProfilePictureByte(profilePictureByte);
            currBrand.setProfilePictureType(profilePictureType);
            currBrand.setProfilePictureName(profilePictureName);
            currBrand.setCategory(category);
            currBrand.setAges(targetAges);
            currBrand.setGenders(targetGender);
            currBrand.setLocations(targetLocation);

            brandRepository.save(currBrand);

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }

        return ResponseEntity.ok(requestDto);

    }

    @Transactional(rollbackOn = Exception.class)
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

//            save category
            List<Category> categories = new ArrayList<>();
            for(String id: requestDto.getCategory()) {
                Category newCategory = categoryRepository.findById(Integer.valueOf(id)).orElse(null);
                categories.add(newCategory);
            }

            Influencer newInfluencer = Influencer.builder()
                    .user(savedUser)
                    .dob(LocalDate.parse(requestDto.getDob()))
                    .instagramId(requestDto.getInstagramId())
                    .token(requestDto.getToken())
                    .gender(found)
                    .categories(categories)
                    .isActive(false)
                    .build();

            Influencer savedInfluencer = influencerRepository.save(newInfluencer);

            //            sambungin user ke influencer
            savedUser.setInfluencer(savedInfluencer);
            userRepository.save(savedUser);

//            save mediatypes
            List<InfluencerMediaType> mediatypes = new ArrayList<>();
            if(requestDto.getStoryPrice()!= null && !requestDto.getStoryPrice().isEmpty()) {
                InfluencerMediaType story = InfluencerMediaType.builder()
                        .price(Integer.valueOf(requestDto.getStoryPrice()))
                        .influencer(savedInfluencer)
                        .mediaType(mediaTypeRepository.findById(1).orElse(null))
                        .build();

                influencerMediaTypeRepository.save(story);
            }
            if(requestDto.getFeedsPrice()!= null && !requestDto.getFeedsPrice().isEmpty()) {
                InfluencerMediaType feeds = InfluencerMediaType.builder()
                        .price(Integer.valueOf(requestDto.getFeedsPrice()))
                        .influencer(savedInfluencer)
                        .mediaType(mediaTypeRepository.findById(2).orElse(null))
                        .build();

                influencerMediaTypeRepository.save(feeds);
            }
            if(requestDto.getReelsPrice()!= null && !requestDto.getReelsPrice().isEmpty()) {
                InfluencerMediaType reels = InfluencerMediaType.builder()
                        .price(Integer.valueOf(requestDto.getReelsPrice()))
                        .influencer(savedInfluencer)
                        .mediaType(mediaTypeRepository.findById(3).orElse(null))
                        .build();

                influencerMediaTypeRepository.save(reels);
            }

            //            create wallet header
            WalletHeader walletHeader = WalletHeader.builder()
                    .balance(0)
                    .user(savedUser)
                    .build();

            walletHeaderRepository.save(walletHeader);

        } catch(Exception ex) {
            throw new RuntimeException(ex);
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
                            newMap.put("label", capitalizeWords(item.getLabel()));
                            return newMap;
                        }
                ).collect(Collectors.toList());

                List<Map<String, String>> targetGender = new ArrayList<>();
                targetGender = user.getBrand().getGenders().stream().map(
                        item -> {
                            Map<String, String> newMap = new HashMap<>();
                            newMap.put("id", String.valueOf(item.getId()));
                            newMap.put("label", item.getLabel());
                            newMap.put("logo", item.getLogo());
                            newMap.put("active_logo", item.getActiveLogo());
                            return newMap;
                        }
                ).collect(Collectors.toList());

                HashMap<String, String> locationMap = new HashMap<>();
                locationMap.put("id", user.getLocation().getId().toString());
                locationMap.put("label", capitalizeWords(user.getLocation().getLabel()));

                HashMap<String, String> categoryMap = new HashMap<>();
                categoryMap.put("id", user.getBrand().getCategory().getId().toString());
                categoryMap.put("label", capitalizeWords(user.getBrand().getCategory().getLabel()));

                BrandProfileDto brandProfileDto = BrandProfileDto.builder()
                        .name(user.getName())
                        .category(user.getBrand().getCategory().getLabel())
                        .categoryMap(categoryMap)
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .locationMap(locationMap)
                        .location(capitalizeWords(user.getLocation().getLabel()))
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
                            newMap.put("label", capitalizeWords(item.getLabel()));
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
                        .location(capitalizeWords(user.getLocation().getLabel()))
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

    public String capitalizeWords(String input) {
        String[] words = input.split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}
