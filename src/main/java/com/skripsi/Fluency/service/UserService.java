package com.skripsi.Fluency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.Fluency.model.dto.LoginInfluencerRequestDto;
import com.skripsi.Fluency.model.dto.LoginResponseDto;
import com.skripsi.Fluency.model.dto.LoginBrandRequestDto;
import com.skripsi.Fluency.model.entity.Brand;
import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.User;
import com.skripsi.Fluency.repository.InfluencerRepository;
import com.skripsi.Fluency.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;

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
}
