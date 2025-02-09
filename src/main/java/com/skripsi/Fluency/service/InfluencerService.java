package com.skripsi.Fluency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.Fluency.model.dto.*;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.BrandRepository;
import com.skripsi.Fluency.repository.InfluencerRepository;
import com.skripsi.Fluency.repository.UserRepository;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDate;

import static org.springframework.util.StringUtils.capitalize;


@Service
public class InfluencerService {

    public ResponseEntity<?> getInfluencer(String id) {

//        User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);

        Influencer influencer = this.influencerRepository.findById(Integer.valueOf(id)).orElse(null);

        if(influencer == null) {
            return ResponseEntity.notFound().build();
        }

        InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, null);
        return ResponseEntity.ok(influencerFilterResponseDto);
    }

//    ini parse range untuk age
    private Range<Integer> parseRange(String value) {
        value = value.toLowerCase(); // Ubah ke lowercase untuk konsistensi
        if (value.contains("-")) { // Format seperti "1k - 10k" atau "1M - 10M"
            String[] parts = value.replace("k", "000").replace("m", "000000").split("-");
            int lower = Integer.parseInt(parts[0].trim());
            int upper = Integer.parseInt(parts[1].trim());
            return Range.closed(lower, upper);
        } else if (value.startsWith(">")) { // Format seperti ">1k" atau ">1M"
            int lowerBound = Integer.parseInt(value.replace(">", "").replace("k", "000").replace("m", "000000").trim());
            return Range.rightOpen(lowerBound, 120); // (lowerBound, +∞)
        } else if (value.startsWith("<")) { // Format seperti "<1k" atau "<1M"
            int upperBound = Integer.parseInt(value.replace("<", "").replace("k", "000").replace("m", "000000").trim());
            return Range.leftOpen(1, upperBound); // (-∞, upperBound)
        }
        throw new IllegalArgumentException("Invalid range format: " + value);
    }

    //    ini parse range untuk price
    private Range<Integer> parseRangePrice(String value) {
        value = value.toLowerCase(); // Ubah ke lowercase untuk konsistensi
        if (value.contains("-")) { // Format seperti "1k - 10k" atau "1M - 10M"
            String[] parts = value.replace("k", "000").replace("m", "000000").split("-");
            int lower = Integer.parseInt(parts[0].trim());
            int upper = Integer.parseInt(parts[1].trim());
            return Range.closed(lower, upper);
        } else if (value.startsWith(">")) { // Format seperti ">1k" atau ">1M"
            int lowerBound = Integer.parseInt(value.replace(">", "").replace("k", "000").replace("m", "000000").trim());
            return Range.rightOpen(lowerBound, 999999999); // (lowerBound, +∞)
        } else if (value.startsWith("<")) { // Format seperti "<1k" atau "<1M"
            int upperBound = Integer.parseInt(value.replace("<", "").replace("k", "000").replace("m", "000000").trim());
            return Range.leftOpen(1, upperBound); // (-∞, upperBound)
        }
        throw new IllegalArgumentException("Invalid range format: " + value);
    }

    @Autowired
    public InfluencerRepository influencerRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public BrandRepository brandRepository;

    private Predicate createAgePredicate(CriteriaBuilder criteriaBuilder, Root<Influencer> root, Integer lowerAge, Integer upperAge) {
        LocalDate today = LocalDate.now();

        // Menghitung tahun kelahiran berdasarkan rentang usia
        LocalDate lowerBirthDate = today.minusYears(upperAge); // Tahun kelahiran untuk batas atas usia
        LocalDate upperBirthDate = today.minusYears(lowerAge); // Tahun kelahiran untuk batas atas usia

        // Mendapatkan dob dari root (field dalam database)
        Path<LocalDate> dobPath = root.get("dob");

        // Membandingkan dob dengan lowerBirthDate dan upperBirthDate
        Predicate lowerDatePredicate = criteriaBuilder.greaterThanOrEqualTo(dobPath, lowerBirthDate); // dob >= lowerBirthDate
        Predicate upperDatePredicate = criteriaBuilder.lessThanOrEqualTo(dobPath, upperBirthDate);   // dob <= upperBirthDate

        // Menggabungkan kedua predicate dengan AND
        return criteriaBuilder.and(lowerDatePredicate, upperDatePredicate);
    }

    private Predicate createPricePredicate(CriteriaBuilder criteriaBuilder, Join<Influencer, InfluencerMediaType> mediaTypeJoin, Integer lowerPrice, Integer upperPrice) {
        // Membandingkan harga dalam range
        Predicate lowerPricePredicate = criteriaBuilder.greaterThanOrEqualTo(mediaTypeJoin.get("price"), lowerPrice);
        Predicate upperPricePredicate = criteriaBuilder.lessThanOrEqualTo(mediaTypeJoin.get("price"), upperPrice);

        // Gabungkan predikat batas bawah dan atas dengan AND
        return criteriaBuilder.and(lowerPricePredicate, upperPricePredicate);
    }


//    public Predicate toPredicate(Root<Influencer> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    public Predicate toPredicate(Root<Influencer> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, InfluencerFilterRequestDto influencerFilterRequestDto) {
        System.out.println("ini masuk toPredicate");
        List<Predicate> andPredicates = new ArrayList<>();
        List<Predicate> orPredicates = new ArrayList<>();

        // 1. Age Range
        if (!influencerFilterRequestDto.getAge().isEmpty()) {
            List<Predicate> agePredicates = new ArrayList<>();

            // Loop untuk setiap range followers dan buat predikatnya
            for (String range : influencerFilterRequestDto.getAge()) {
                System.out.println("masuk di for age");
                System.out.println("age per range: " + influencerFilterRequestDto.getAge());

                Range<Integer> ageRange = parseRange(range);
                System.out.println("ageRange: " + ageRange);

                // Mengambil nilai dari Optional dan mengonversinya menjadi Integer
                Integer lowerAge = ageRange.getLowerBound().getValue().orElseThrow();
                Integer upperAge = ageRange.getUpperBound().getValue().orElseThrow();

                // Menggunakan fungsi yang telah diperbarui dengan parameter rentang usia
                Predicate agePredicate = createAgePredicate(criteriaBuilder, root, lowerAge, upperAge);

                // Menambahkan predikat usia ke dalam daftar predikat
                agePredicates.add(agePredicate);

            }

            // Gabungkan predikat follower dengan OR (untuk rentang berbeda, gabungkan dengan OR)
            if (!agePredicates.isEmpty()) {
                Predicate agePredicateGroup = criteriaBuilder.or(agePredicates.toArray(new Predicate[0]));
                andPredicates.add(agePredicateGroup);
            }
        }

        // Join dengan InfluencerMediaType
        Join<Influencer, InfluencerMediaType> mediaTypeJoin = root.join("influencerMediaTypes", JoinType.LEFT);

//      2. Price Range
        if (!influencerFilterRequestDto.getPrice().isEmpty()) {
            List<Predicate> pricePredicates = new ArrayList<>();

            for (String range : influencerFilterRequestDto.getPrice()) {
                System.out.println("masuk di for age");
                System.out.println("Processing price range: " + range);
                Range<Integer> priceRange = parseRangePrice(range); // Menggunakan fungsi parseRange yang sudah ada
                System.out.println("price range: " + priceRange);
                Integer lowerPrice = priceRange.getLowerBound().getValue().orElseThrow();
                Integer upperPrice = priceRange.getUpperBound().getValue().orElseThrow();

                // Untuk setiap media type (feeds, reels, story), buat predicate
                Predicate feedsPredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(mediaTypeJoin.get("mediaType").get("label"), "Feeds"),
                        createPricePredicate(criteriaBuilder, mediaTypeJoin, lowerPrice, upperPrice)
                );

                Predicate reelsPredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(mediaTypeJoin.get("mediaType").get("label"), "Reels"),
                        createPricePredicate(criteriaBuilder, mediaTypeJoin, lowerPrice, upperPrice)
                );

                Predicate storyPredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(mediaTypeJoin.get("mediaType").get("label"), "Story"),
                        createPricePredicate(criteriaBuilder, mediaTypeJoin, lowerPrice, upperPrice)
                );

                // Gabungkan predikat price untuk setiap media type dengan OR di dalam kurung
                Predicate mediaTypePredicateGroup = criteriaBuilder.or(feedsPredicate, reelsPredicate, storyPredicate);
                pricePredicates.add(mediaTypePredicateGroup);
            }

            // Gabungkan semua predikat price dengan OR di dalam kurung
            if (!pricePredicates.isEmpty()) {
                Predicate pricePredicateGroup = criteriaBuilder.or(pricePredicates.toArray(new Predicate[0]));
                andPredicates.add(pricePredicateGroup);
            }
        }

        // 3. Gender Filter
        if (!influencerFilterRequestDto.getGender().isEmpty()) {
            List<Predicate> genderPredicates = new ArrayList<>();

            // Loop untuk setiap gender yang dipilih
            for (Integer genderId : influencerFilterRequestDto.getGender()) {
                // Tambahkan predikat untuk gender
                Predicate genderPredicate = criteriaBuilder.equal(root.get("gender").get("id"), genderId);
                genderPredicates.add(genderPredicate);
            }

            // Gabungkan semua predikat gender dengan OR (karena sifatnya multiple select)
            if (!genderPredicates.isEmpty()) {
                Predicate genderPredicateGroup = criteriaBuilder.or(genderPredicates.toArray(new Predicate[0]));
                andPredicates.add(genderPredicateGroup);
            }
        }

        // 4. Media Type Filter
        if (!influencerFilterRequestDto.getMedia().isEmpty()) {
            List<Predicate> mediaTypePredicates = new ArrayList<>();

            // Loop untuk setiap media type yang dipilih
            for (Integer mediaTypeId : influencerFilterRequestDto.getMedia()) {
                // Tambahkan predikat untuk media type
                Predicate mediaTypePredicate = criteriaBuilder.equal(
                        root.join("influencerMediaTypes").get("mediaType").get("id"),
                        mediaTypeId
                );
                mediaTypePredicates.add(mediaTypePredicate);
            }

            // Gabungkan semua predikat media type dengan OR (karena sifatnya multiple select)
            if (!mediaTypePredicates.isEmpty()) {
                Predicate mediaTypePredicateGroup = criteriaBuilder.or(mediaTypePredicates.toArray(new Predicate[0]));
                andPredicates.add(mediaTypePredicateGroup);
            }
        }

        // 5. Location Filter
        if (!influencerFilterRequestDto.getLocation().isEmpty()) {
            List<Predicate> locationPredicates = new ArrayList<>();

            // Loop untuk setiap location ID yang dipilih
            for (Integer locationId : influencerFilterRequestDto.getLocation()) {
                // Tambahkan predikat untuk location
                Predicate locationPredicate = criteriaBuilder.equal(
                        root.join("user").join("location").get("id"),
                        locationId
                );
                locationPredicates.add(locationPredicate);
            }

            // Gabungkan semua predikat location dengan OR (karena sifatnya multiple select)
            if (!locationPredicates.isEmpty()) {
                Predicate locationPredicateGroup = criteriaBuilder.or(locationPredicates.toArray(new Predicate[0]));
                andPredicates.add(locationPredicateGroup);
            }
        }

        // 6. Rating Filter
        if (!influencerFilterRequestDto.getRating().isEmpty()) {
            List<Predicate> ratingPredicates = new ArrayList<>();

            // Loop untuk setiap rating yang dipilih
            for (String ratingInput : influencerFilterRequestDto.getRating()) {
                try {
                    // Ambil angka rating dari input (misalnya "1 star" menjadi 1)
                    Integer ratingValue = Integer.parseInt(ratingInput.split(" ")[0]);

                    // Hitung rentang rating
                    Double minRating = ratingValue.doubleValue();
                    Double maxRating = minRating + 0.99;

                    // Subquery untuk menghitung rata-rata rating influencer
                    Subquery<Double> avgRatingSubquery = query.subquery(Double.class);
                    Root<Review> reviewRoot = avgRatingSubquery.from(Review.class);
                    avgRatingSubquery.select(criteriaBuilder.avg(reviewRoot.get("rating")))
                            .where(criteriaBuilder.equal(reviewRoot.get("influencer").get("id"), root.get("id")));

                    // Predikat untuk rentang rating
                    Predicate ratingPredicate = criteriaBuilder.between(avgRatingSubquery, minRating, maxRating);
                    ratingPredicates.add(ratingPredicate);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid rating format: " + ratingInput);
                }
            }

            // Gabungkan semua predikat rating dengan OR (karena sifatnya multiple select)
            if (!ratingPredicates.isEmpty()) {
                Predicate ratingPredicateGroup = criteriaBuilder.or(ratingPredicates.toArray(new Predicate[0]));
                andPredicates.add(ratingPredicateGroup);
            }
        }


        // Gabungkan semua predikat AND terlebih dahulu
        Predicate andPredicate = criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));

        // Gabungkan semua predikat OR dalam kurung
        Predicate orPredicateGroup = orPredicates.isEmpty() ? null : criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));

        // Gabungkan keduanya (AND dan OR)
        if (orPredicateGroup != null) {
            return criteriaBuilder.and(andPredicate, orPredicateGroup);
        }
        return andPredicate; // Jika OR kosong, hanya kembalikan AND

    }

//    ini untuk filter by followers (API IG)
    public List<Influencer> filterInfluencersByFollowers(List<Influencer> filteredInfluencers, List<String> followerRanges) {
        List<Influencer> result = new ArrayList<>();

        for (Influencer influencer : filteredInfluencers) {
            String token = influencer.getToken(); // Token Instagram
            String igid = influencer.getInstagramId();
            if (token == null || token.isEmpty()) {
                continue;
            }
            if (igid == null || igid.isEmpty()) {
                continue;
            }

            // Panggil API Instagram untuk mendapatkan jumlah followers
            int followers = getFollowersFromInstagramApi(token,igid);
            System.out.println("followers: " + followers);

            // Filter berdasarkan range followers
            boolean match = false;
            for (String range : followerRanges) {
                System.out.println("range: " + range);
                if (isFollowersInRange(followers, range)) {
                    match = true;
                    System.out.println("matchkah?" + match);
                    break;
                }
            }

            if (match) {
                result.add(influencer);
            }
        }
        return result;
    }

    // Fungsi untuk memeriksa apakah jumlah followers masuk dalam range
    private boolean isFollowersInRange(int followers, String range) {
        range = range.trim();
        if (range.startsWith(">")) {
            // Contoh "> 1000k"
            int min = parseFollowers(range.substring(1).trim());
            return followers > min;
        } else if (range.contains("-")) {
            // Contoh "10k - 100k"
            String[] parts = range.split("-");
            int min = parseFollowers(parts[0].trim());
            int max = parseFollowers(parts[1].trim());
            return followers >= min && followers <= max;
        } else {
            // Jika format tidak valid
            throw new IllegalArgumentException("Invalid range format: " + range);
        }
    }

    // Fungsi untuk mengonversi format followers ke angka
    private int parseFollowers(String value) {
        value = value.toLowerCase();
        if (value.endsWith("k")) {
            return Integer.parseInt(value.replace("k", "")) * 1000;
        } else if (value.endsWith("m")) {
            return Integer.parseInt(value.replace("m", "")) * 1000000;
        } else {
            return Integer.parseInt(value);
        }
    }

    @Autowired
    private RestTemplate restTemplate;

    @Value(value = "${base.url}")
    private String baseUrl;

    // Get followers dari API Instagram
    private int getFollowersFromInstagramApi(String token, String igid) {
        try{
//            Hit URL API Instagram
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/" + igid)
                    .queryParam("fields", "followers_count")
                    .queryParam("access_token", token);

//            Ambil response
            ResponseEntity<?> response = restTemplate.getForEntity(builder.toUriString(), String.class);

//            Ubah response kedalam bentuk JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(String.valueOf(response.getBody()));

//            Ambil data saja
            String data = jsonNode.get("followers_count").toString();

            Integer foll = Integer.parseInt(data);

            return foll;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    // Get followers dari API Instagram
    private String getProfilePicture(String token, String igid) {
        try{
//            Hit URL API Instagram
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/" + igid)
                    .queryParam("fields", "profile_picture_url")
                    .queryParam("access_token", token);

//            Ambil response
            ResponseEntity<?> response = restTemplate.getForEntity(builder.toUriString(), String.class);

//            Ubah response kedalam bentuk JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(String.valueOf(response.getBody()));

//            Ambil data saja
            String data = jsonNode.get("profile_picture_url").toString();

            return data;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }


    // Filter influencer berdasarkan age range yang dipilih
    public List<Influencer> filterByAudienceAge(List<Influencer> influencers, List<String> selectedAgeRanges) {
        List<Influencer> filteredInfluencers = new ArrayList<>();
        for (Influencer influencer : influencers) {
            System.out.println("influencer yang masuk looping: " + influencer.getUser().getName());
            // Ambil data demographics followers dari Instagram API (dari token influencer)
            List<AudienceAgeDto> demographics = getAudienceDemographics(influencer.getToken(),influencer.getInstagramId());
            System.out.println("demo: " + demographics);

            // Hitung total followers dari semua age range
            int totalFollowers = demographics.stream()
                    .mapToInt(AudienceAgeDto::getValue)
                    .sum();
//            totalFollowers = 30;
            System.out.println("totalFollowers: " + totalFollowers);

            // Hitung rata-rata followers dari semua age range
            double averageFollowersPerRange = totalFollowers / (double) demographics.size();
            System.out.println("averageFollowersPerRange: " + averageFollowersPerRange);

            System.out.println("selected age range " + selectedAgeRanges);
            boolean hasValidRange = demographics.stream()
                    .filter(demographic -> {
                        // Format ulang selectedAgeRanges agar sesuai dengan demographic.getAgeRange
                        List<String> formattedSelectedAgeRanges = selectedAgeRanges.stream()
                                .map(range -> {
                                    if (range.startsWith(">")) {
                                        return range.replace("> ", "") + "+";
                                    } else {
                                        return range.replace(" ", "");
                                    }
                                })
                                .collect(Collectors.toList());

                        return formattedSelectedAgeRanges.contains(demographic.getAgeRange());
                    })
                    .anyMatch(demographic -> demographic.getValue() >= averageFollowersPerRange);
            System.out.println("hasValidRange: " + hasValidRange);

            // Tambahkan influencer ke hasil jika ada age range yang memenuhi kriteria
            if (hasValidRange) {
                System.out.println("masuk valid range");
                System.out.println(influencer.getUser().getName());
                filteredInfluencers.add(influencer);
            }
        }

        return filteredInfluencers;
    }

    // Ambil data demographics audience age dari Instagram API
    private List<AudienceAgeDto> getAudienceDemographics(String token, String igid) {

        try{
            System.out.println("masuk ke hit api ig");
//            Hit URL API Instagram
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/" + igid + "/insights")
                    .queryParam("metric", "follower_demographics")
                    .queryParam("period", "lifetime")
                    .queryParam("metric_type", "total_value")
                    .queryParam("breakdown", "age")
                    .queryParam("access_token", token);

            System.out.println("builder: " + builder.toUriString());

//            Ambil response
            ResponseEntity<?> response = restTemplate.getForEntity(builder.toUriString(), String.class);
            System.out.println("response: " + response);

            String responseBody = (String) response.getBody();
            System.out.println("response body: " + responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            System.out.println("root: " + root);

            // Navigasi ke data demographics
            JsonNode breakdowns = root.path("data").get(0)
                    .path("total_value")
                    .path("breakdowns").get(0)
                    .path("results");

            System.out.println("breakdowns: " + breakdowns);

            List<AudienceAgeDto> audienceAgeDtos = new ArrayList<>();

            // Iterasi melalui setiap hasil
            for (JsonNode result : breakdowns) {
                String ageRange = result.path("dimension_values").get(0).asText();
                System.out.println("ageRange: " + ageRange);
                int value = result.path("value").asInt();
                System.out.println("value: " + value);

                // Map ke objek AudienceAge
                audienceAgeDtos.add(AudienceAgeDto.builder()
                        .ageRange(ageRange)
                        .value(value)
                        .build());
            }
            System.out.println("audience age list: " + audienceAgeDtos);
            return audienceAgeDtos;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Filter influencer berdasarkan gender yang dipilih
    public List<Influencer> filterByGenderAudience(List<Influencer> influencers, List<Integer> selectedGenderAudiences) {
        List<Influencer> filteredByGenderAudience = new ArrayList<>();

        for (Influencer influencer : influencers) {
            // Panggil fungsi untuk mendapatkan data followers berdasarkan gender
            System.out.println("influencer yang masuk looping gender: " + influencer.getUser().getName());
            List<AudienceGenderDto> genderFollowerData = getGenderFollowerData(influencer.getToken(), influencer.getInstagramId());
            System.out.println("genderFollowerData: " + genderFollowerData);

            // Cek apakah salah satu gender yang dipilih memenuhi kriteria >= 50% dari total followers
            boolean isValid = isGenderAudienceValid(genderFollowerData, selectedGenderAudiences);
            System.out.println("isValid: " + isValid);

            // Tambahkan influencer ke hasil jika valid
            if (isValid) {
                System.out.println(influencer.getUser().getName() + " masuk valid range");
                filteredByGenderAudience.add(influencer);
            }
        }

        return filteredByGenderAudience;
    }

    private List<AudienceGenderDto> getGenderFollowerData(String token, String igid) {
        // Panggil API Instagram
        try{
            System.out.println("masuk ke hit api ig untuk gender");
//            Hit URL API Instagram
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/" + igid + "/insights")
                    .queryParam("metric", "follower_demographics")
                    .queryParam("period", "lifetime")
                    .queryParam("metric_type", "total_value")
                    .queryParam("breakdown", "gender")
                    .queryParam("access_token", token);

            System.out.println("builder: " + builder.toUriString());

//            Ambil response
            ResponseEntity<?> response = restTemplate.getForEntity(builder.toUriString(), String.class);
            System.out.println("response: " + response);

            String responseBody = (String) response.getBody();
            System.out.println("response body: " + responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            System.out.println("root: " + root);

            // Navigasi ke data demographics
            JsonNode breakdowns = root.path("data").get(0)
                    .path("total_value")
                    .path("breakdowns").get(0)
                    .path("results");

            System.out.println("breakdowns: " + breakdowns);

            List<AudienceGenderDto> audienceGenderDtos = new ArrayList<>();

            // Iterasi melalui setiap hasil
            for (JsonNode result : breakdowns) {
                String gender = result.path("dimension_values").get(0).asText();
                System.out.println("gender: " + gender);
                int value = result.path("value").asInt();
                System.out.println("value: " + value);

                // Map ke objek AudienceGender
                audienceGenderDtos.add(AudienceGenderDto.builder()
                        .gender(gender)
                        .value(value)
                        .build());
            }
            System.out.println("audience gender list: " + audienceGenderDtos);
            return audienceGenderDtos;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private boolean isGenderAudienceValid(
            List<AudienceGenderDto> genderFollowerData,
            List<Integer> selectedGenderAudiences) {

        // Map angka ke string gender
        Map<Integer, String> genderMap = Map.of(
                1, "M",  // Male
                2, "F"   // Female
        );

        // Hitung total followers yang valid (hanya "F" dan "M")
        int validTotalFollowers = genderFollowerData.stream()
                .filter(genderData -> genderData.getGender().equalsIgnoreCase("F") ||
                        genderData.getGender().equalsIgnoreCase("M"))
                .mapToInt(AudienceGenderDto::getValue)
                .sum();

        System.out.println("validTotalFollowers: " + validTotalFollowers);

        if (validTotalFollowers == 0) {
            return false; // Jika validTotalFollowers 0, otomatis tidak valid
        }

        for (Integer selectedGender : selectedGenderAudiences) {
            String genderKey = genderMap.get(selectedGender);

            if (genderKey == null) {
                continue; // Gender tidak valid, skip
            }

            // Cari nilai followers untuk gender yang dipilih
            int genderValue = genderFollowerData.stream()
                    .filter(genderData -> genderData.getGender().equalsIgnoreCase(genderKey))
                    .mapToInt(AudienceGenderDto::getValue)
                    .sum();

            // Periksa apakah nilai followers >= 40% dari total followers yang valid
            if (genderValue >= (validTotalFollowers * 0.4)) {
                return true; // Salah satu gender memenuhi kriteria
            }
        }

        return false; // Tidak ada gender yang memenuhi kriteria
    }

    public boolean hasAnyFilter(InfluencerFilterRequestDto dto) {
        return !isListEmpty(dto.getMedia()) ||
                !isListEmpty(dto.getGender()) ||
                !isListEmpty(dto.getAge()) ||
                !isListEmpty(dto.getPrice()) ||
                !isListEmpty(dto.getRating()) ||
                !isListEmpty(dto.getLocation());
    }

    private boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static String formatPrice(String price) {
//        System.out.println("ini udah masuk di formatprice");
        if (price.isEmpty()) {
            return "";
        }
        // Format angka dengan titik (locale Indonesia)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
        return formatter.format(Long.parseLong(price));
    }

    public static String formatFollowers(int followers) {
//        System.out.println("ini lagi di format followers");
        if (followers >= 1_000_000) {
            return followers / 1_000_000 + "M";
        } else if (followers >= 1_000) {
            return followers / 1_000 + "k";
        }
        return String.valueOf(followers);
    }

    public static Double formatRating(double rating) {
//        System.out.println("ini lagi di format rating");
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.valueOf(decimalFormat.format(rating));
    }

    public static Double formatRatingDetail(double rating) {
//        System.out.println("ini lagi di format rating");
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return Double.valueOf(decimalFormat.format(rating));
    }

    public List<Influencer> filterInfluencersByCategory(List<Influencer> influencers, Integer categoryChosen) {
        List<Influencer> filteredInfluencers = new ArrayList<>();
        System.out.println("ini masuk di filterinfluencersbycategory");
        System.out.println("catChosen: " + categoryChosen);

        for (Influencer influencer : influencers) {
            System.out.println("infId: "+ influencer.getId());
            if (influencer.getCategories() != null) {
                for (Category category : influencer.getCategories()) {
                    System.out.println("category id: " + category.getId());
                    if (category.getId().equals(categoryChosen)) {
                        System.out.println("influencer ke " + influencer.getId() + " di add");
                        filteredInfluencers.add(influencer);
                        break; // Jika sudah cocok, hentikan loop kategori untuk influencer ini
                    }
                }
            }
        }

        return filteredInfluencers;
    }

    public List<InfluencerFilterResponseDto> filterInfluencer(InfluencerFilterRequestDto influencerFilterRequestDto, Integer brandId) {
        System.out.println(influencerFilterRequestDto);

        boolean hasFilter = hasAnyFilter(influencerFilterRequestDto);
        List<Influencer> influencers1 = new ArrayList<>();

        if(hasFilter){
            // Buat Predicate menggunakan toPredicate
            Specification<Influencer> spec = (root, query, criteriaBuilder) -> {
                return toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto);
            };

            // Ambil influencer berdasarkan predikat yang telah dibuat
            influencers1 = influencerRepository.findAll(spec);

            System.out.println("spec: " + spec);
        } else{
            influencers1 = influencerRepository.findAll();
        }

        System.out.println("foll: " + influencerFilterRequestDto.getFollowers());
        System.out.println("age aud: " + influencerFilterRequestDto.getAgeAudience());

        boolean isFollowersEmpty = influencerFilterRequestDto.getFollowers() == null
                || influencerFilterRequestDto.getFollowers().isEmpty();

        List<Influencer> influencers2 = new ArrayList<>();
        influencers2 = influencers1;
//        Untuk filter by followers
        if (!isFollowersEmpty){
            System.out.println("masuk filter by foll");
            influencers2 = filterInfluencersByFollowers(influencers1, influencerFilterRequestDto.getFollowers());
        }

        boolean isAudienceAgeEmpty = influencerFilterRequestDto.getAgeAudience() == null
                || influencerFilterRequestDto.getAgeAudience().isEmpty();

        List<Influencer> influencers3 = new ArrayList<>();
        influencers3 = influencers2;
//        Untuk filter by audience age
        if (!isAudienceAgeEmpty){
            System.out.println("masuk filter by age aud");
            influencers3 = filterByAudienceAge(influencers2, influencerFilterRequestDto.getAgeAudience());
        }

        boolean isAudienceGenderEmpty = influencerFilterRequestDto.getGenderAudience() == null
                || influencerFilterRequestDto.getGenderAudience().isEmpty();

        List<Influencer> influencers4 = new ArrayList<>();
        influencers4 = influencers3;
//        Untuk filter by audience gender
        if (!isAudienceGenderEmpty){
            System.out.println("masuk filter by gender aud");
            influencers4 = filterByGenderAudience(influencers3, influencerFilterRequestDto.getGenderAudience());
        }

//        Section ini untuk bagian tab category
        List<Influencer> influencers = new ArrayList<>();
        if (!influencerFilterRequestDto.getCategoryChosen().isEmpty()){
            Integer categoryChosen = influencerFilterRequestDto.getCategoryChosen().get(0);
            System.out.println("INI MASUK KE TAB CATEGORY");
            if(categoryChosen == 0){
                influencers = influencers4;
            }
            else{
                influencers = filterInfluencersByCategory(influencers4, categoryChosen);
            }
        }else {
            influencers = influencers4;
        }


        List<InfluencerFilterResponseDto> response = new ArrayList<>();
//        return influencerRepository.findAll(spec);
        for (Influencer influencer: influencers){
            System.out.println("influencers: " + influencer.getUser().getName());

            User userBrand = userRepository.findById(brandId).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI FILTER INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(brandId));
            System.out.println("influencer: " + influencer.getId());
            InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

            // Tambahkan influencer ke response list
            response.add(influencerFilterResponseDto);
        }

        return response;
    }

    public String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }


    public List<InfluencerFilterResponseDto> sortInfluencer(List<Integer> sort, InfluencerFilterRequestDto influencerFilterRequestDto, Integer brandId) {
        System.out.println("influencerFilterRequestDto" + influencerFilterRequestDto);
        System.out.println("sort" + sort);

        boolean hasFilter = hasAnyFilter(influencerFilterRequestDto);
        List<Influencer> influencers1 = new ArrayList<>();

        if(hasFilter){
            // Buat Predicate menggunakan toPredicate
            Specification<Influencer> spec = (root, query, criteriaBuilder) -> {
                return toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto);
            };

            // Ambil influencer berdasarkan predikat yang telah dibuat
            influencers1 = influencerRepository.findAll(spec);

            System.out.println("spec: " + spec);
        } else{
            influencers1 = influencerRepository.findAll();
        }

        System.out.println("foll: " + influencerFilterRequestDto.getFollowers());
        System.out.println("age aud: " + influencerFilterRequestDto.getAgeAudience());

        boolean isFollowersEmpty = influencerFilterRequestDto.getFollowers() == null
                || influencerFilterRequestDto.getFollowers().isEmpty();

        List<Influencer> influencers2 = new ArrayList<>();
        influencers2 = influencers1;
//        Untuk filter by followers
        if (!isFollowersEmpty){
            System.out.println("masuk filter by foll");
            influencers2 = filterInfluencersByFollowers(influencers1, influencerFilterRequestDto.getFollowers());
        }

        boolean isAudienceAgeEmpty = influencerFilterRequestDto.getAgeAudience() == null
                || influencerFilterRequestDto.getAgeAudience().isEmpty();

        List<Influencer> influencers3 = new ArrayList<>();
        influencers3 = influencers2;
//        Untuk filter by audience age
        if (!isAudienceAgeEmpty){
            System.out.println("masuk filter by age aud");
            influencers3 = filterByAudienceAge(influencers2, influencerFilterRequestDto.getAgeAudience());
        }

        boolean isAudienceGenderEmpty = influencerFilterRequestDto.getGenderAudience() == null
                || influencerFilterRequestDto.getGenderAudience().isEmpty();

        List<Influencer> influencers4 = new ArrayList<>();
        influencers4 = influencers3;
//        Untuk filter by audience gender
        if (!isAudienceGenderEmpty){
            System.out.println("masuk filter by gender aud");
            influencers4 = filterByGenderAudience(influencers3, influencerFilterRequestDto.getGenderAudience());
        }

//        Section ini untuk bagian tab category
        List<Influencer> influencers = new ArrayList<>();
        if (!influencerFilterRequestDto.getCategoryChosen().isEmpty()){
            Integer categoryChosen = influencerFilterRequestDto.getCategoryChosen().get(0);
            System.out.println("INI MASUK KE TAB CATEGORY");
            if(categoryChosen == 0){
                influencers = influencers4;
            }
            else{
                influencers = filterInfluencersByCategory(influencers4, categoryChosen);
            }
        }else {
            influencers = influencers4;
        }
//        ini untuk sort
        // Sort berdasarkan parameter
        if ( sort.isEmpty() || sort.get(0) == 1 ) {
            System.out.println("INI MASUK KE SORT POPULAR");
            influencers.sort(Comparator.comparing(influencer -> influencer.getProjectHeaders().size(), Comparator.reverseOrder()));
        } else if (sort.get(0) == 3) { // Sort by rating
            influencers.sort(Comparator.comparing(
                    influencer -> {
                        // Panggil repository untuk mendapatkan average rating
                        Double avgRating = influencerRepository.findAverageRatingByInfluencerId(Long.valueOf(influencer.getId()));
                        return avgRating == null ? 0.0 : avgRating; // Default nilai 0.0 jika null
                    }, Comparator.reverseOrder()));
//            influencers = influencerRepository.findAllOrderByAverageRatingDesc();
        } else if (sort.get(0) == 2) { // Sort by price
//            influencers = influencerRepository.findAllOrderByLowestPriceAsc();
            influencers.sort(Comparator.comparing(
                    influencer -> influencer.getInfluencerMediaTypes().stream()
                            .mapToDouble(mediaType -> mediaType.getPrice() != null ? mediaType.getPrice() : Double.MAX_VALUE)
                            .min()
                            .orElse(Double.MAX_VALUE)
            ));
        }

//      Mapping response
        List<InfluencerFilterResponseDto> response = new ArrayList<>();
//        return influencerRepository.findAll(spec);
        for (Influencer influencer: influencers){
            System.out.println("influencers: " + influencer.getUser().getName());


            User userBrand = userRepository.findById(brandId).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
//            Boolean isSaved = isInfluencerSavedByBrand(Integer.valueOf(brandId), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI SORT INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(brandId));
            System.out.println("influencer: " + influencer.getId());
            InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

            // Tambahkan influencer ke response list
            response.add(influencerFilterResponseDto);
        }

        return response;
    }

    public List<InfluencerFilterResponseDto> searchInfluencers(String query, String userId) {
        System.out.println("ini masuk ke service search inf");
//        List<Influencer> influencers = influencerRepository.searchInfluencers(query.toLowerCase());
        System.out.println("query: " + query);
        System.out.println("userid: " + userId);
        List<Influencer> influencers = influencerRepository.findByUser_NameContainingIgnoreCase(query);

        // Buat list untuk menampung response
        List<InfluencerFilterResponseDto> response = new ArrayList<>();

        // Looping melalui influencer dan mapping ke DTO
        for (Influencer influencer : influencers) {
            System.out.println("influencers: " + influencer.getUser().getName());

            User userBrand = userRepository.findById(Integer.valueOf(userId)).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
//            Boolean isSaved = isInfluencerSavedByBrand(Integer.valueOf(userId), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI SEARCH INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(userId));
            System.out.println("influencer: " + influencer.getId());

            InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

            // Tambahkan influencer ke response list
            response.add(influencerFilterResponseDto);
        }

        System.out.println("ini udah selesai build response");

        return response;
    }

    public List<InfluencerFilterResponseDto> searchInfluencersSaved(String query, String userId) {
        System.out.println("ini masuk ke service search inf");
//        List<Influencer> influencers = influencerRepository.searchInfluencers(query.toLowerCase());
        System.out.println("query: " + query);
        System.out.println("userid: " + userId);
        List<Influencer> influencers = influencerRepository.findByUser_NameContainingIgnoreCase(query);

        // Buat list untuk menampung response
        List<InfluencerFilterResponseDto> response = new ArrayList<>();

        // Looping melalui influencer dan mapping ke DTO
        for (Influencer influencer : influencers) {
            System.out.println("influencers: " + influencer.getUser().getName());

            User userBrand = userRepository.findById(Integer.valueOf(userId)).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
//            Boolean isSaved = isInfluencerSavedByBrand(Integer.valueOf(userId), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI SEARCH INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(userId));
            System.out.println("influencer: " + influencer.getId());

            if(isSaved){
                InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

                // Tambahkan influencer ke response list
                response.add(influencerFilterResponseDto);
            }
        }

        System.out.println("ini udah selesai build response");

        return response;
    }


//    Dari sini masuk ke get influencer untuk page home

    public List<InfluencerFilterResponseDto> getTopInfluencer(String userId) {
        System.out.println("masuk ke get top influencer");

        // Ambil data influencer teratas, di-limiting menjadi 8 influencer
        List<Influencer> influencerData = influencerRepository.findTopInfluencers();

        // Ambil 8 influencer teratas menggunakan limit
        List<Influencer> influencers = influencerData.stream()
                .limit(8) // Membatasi hanya 8 influencer
                .collect(Collectors.toList());

        // Buat list untuk menampung response
        List<InfluencerFilterResponseDto> response = new ArrayList<>();

        // Looping melalui influencer dan mapping ke DTO
        for (Influencer influencer : influencers) {
            System.out.println("influencers: " + influencer.getUser().getName());

            User userBrand = userRepository.findById(Integer.valueOf(userId)).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
//            Boolean isSaved = isInfluencerSavedByBrand(Integer.valueOf(userId), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI GET TOP INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(userId));
            System.out.println("influencer: " + influencer.getId());

            InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

            // Tambahkan influencer ke response list
            response.add(influencerFilterResponseDto);
        }

        System.out.println("ini udah selesai build response");

        return response;
    }

    public List<InfluencerFilterResponseDto> getRecommendation(String userId) {
        System.out.println("masuk ke get recommendation");

        // Ambil data influencer teratas, di-limiting menjadi 8 influencer
        List<InfluencerFilterResponseDto> influencerData = filterInfluencer(generateRecommendationRequest(userId), Integer.valueOf(userId));
        System.out.println("ini generate request nya: " + generateRecommendationRequest(userId));
        System.out.println("ini udah selesai hit filter influencer, balikannya: " + influencerData);

        // Ambil 8 influencer pertama (jika jumlahnya lebih dari 8, ambil maksimal 8)
        List<InfluencerFilterResponseDto> top8Influencers = influencerData.stream()
                .limit(8) // Ambil 8 influencer pertama
                .collect(Collectors.toList());

        System.out.println("SELESAI GET RECOMMENDATION");
        System.out.println("OUTPUT: " + top8Influencers);

        return top8Influencers;
    }

    public InfluencerFilterRequestDto generateRecommendationRequest(String userId) {
        System.out.println("ini lagi di generate recom req untuk user " + userId);
        // Ambil User berdasarkan userId
        User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);

        if (user == null || user.getBrand() == null) {
            // Jika user tidak ditemukan atau brand tidak terhubung
            return null;
        }

        // Ambil brand yang terkait dengan user
        Brand brand = user.getBrand();

        // Inisialisasi DTO yang akan dikembalikan
        InfluencerFilterRequestDto filterRequestDto = new InfluencerFilterRequestDto();

        // Inisialisasi dengan list kosong jika null
        filterRequestDto.setFollowers(filterRequestDto.getFollowers() != null ? filterRequestDto.getFollowers() : new ArrayList<>());
        filterRequestDto.setMedia(filterRequestDto.getMedia() != null ? filterRequestDto.getMedia() : new ArrayList<>());
        filterRequestDto.setGender(filterRequestDto.getGender() != null ? filterRequestDto.getGender() : new ArrayList<>());
        filterRequestDto.setAge(filterRequestDto.getAge() != null ? filterRequestDto.getAge() : new ArrayList<>());
        filterRequestDto.setPrice(filterRequestDto.getPrice() != null ? filterRequestDto.getPrice() : new ArrayList<>());
        filterRequestDto.setRating(filterRequestDto.getRating() != null ? filterRequestDto.getRating() : new ArrayList<>());
        filterRequestDto.setLocation(filterRequestDto.getLocation() != null ? filterRequestDto.getLocation() : new ArrayList<>());
        filterRequestDto.setGenderAudience(filterRequestDto.getGenderAudience() != null ? filterRequestDto.getGenderAudience() : new ArrayList<>());
        filterRequestDto.setAgeAudience(filterRequestDto.getAgeAudience() != null ? filterRequestDto.getAgeAudience() : new ArrayList<>());
        filterRequestDto.setCategoryChosen(filterRequestDto.getCategoryChosen() != null ? filterRequestDto.getCategoryChosen() : new ArrayList<>());

        // Mengisi location (id location dari brand)
        List<Integer> locationIds = brand.getLocations().stream()
                .map(Location::getId) // Ambil ID dari masing-masing location
                .collect(Collectors.toList());
        filterRequestDto.setLocation(locationIds);

        // Mengisi genderAudience (id gender dari brand)
        List<Integer> genderAudienceIds = brand.getGenders().stream()
                .map(Gender::getId) // Ambil ID dari masing-masing gender
                .collect(Collectors.toList());
        filterRequestDto.setGenderAudience(genderAudienceIds);

        // Mengisi ageAudience (label dari age yang ditargetkan oleh brand)
        List<String> ageAudienceLabels = brand.getAges().stream()
                .map(Age::getLabel) // Ambil label dari masing-masing age
                .collect(Collectors.toList());
        filterRequestDto.setAgeAudience(ageAudienceLabels);

        System.out.println("filterRequestDto" + filterRequestDto);

        // Kembalikan filterRequestDto yang sudah terisi
        return filterRequestDto;
    }

//    public List<InfluencerFilterResponseDto> filterInfluencersByCategoryId(Integer categoryId, List<InfluencerFilterResponseDto> influencers){
//        // Filter influencer berdasarkan kategori
//        return influencers.stream()
//                .filter(influencer -> influencer.getCategory() != null // Validasi null untuk category
//                        && influencer.getCategory().stream()
//                        .anyMatch(category -> {
//                            if (category instanceof Map<?, ?> categoryMap) { // Casting ke Map
//                                Object id = categoryMap.get("id");
//                                return id != null && id.equals(categoryId); // Bandingkan ID
//                            }
//                            return false;
//                        }))
//                .toList();
//    }

    public InfluencerFilterResponseDto buildResponse(Influencer influencer, Boolean isSaved){

        Double averageRating = influencerRepository.findAverageRatingByInfluencerId(Long.valueOf(influencer.getId()));
        Integer totalReviews = influencerRepository.findTotalReviewsByInfluencerId(Long.valueOf(influencer.getId()));


        if (averageRating == null) {
            averageRating = 0.0; // Default jika tidak ada review
        }

        if (totalReviews == null) {
            totalReviews = 0; // Default jika tidak ada review
        }

        List<Category> categories = influencer.getCategories();
        List<Map<String,Object>> categoryDto = new ArrayList<>();

        String feedsPrice = "";
        String reelsPrice = "";
        String storyPrice = "";
        List<InfluencerMediaType> mediaTypes = influencer.getInfluencerMediaTypes();
        for(InfluencerMediaType mediaType: mediaTypes){
            if(mediaType.getMediaType().getLabel().equalsIgnoreCase("feeds")){
                feedsPrice = mediaType.getPrice().toString();
            }else if(mediaType.getMediaType().getLabel().equalsIgnoreCase("reels")){
                reelsPrice = mediaType.getPrice().toString();
            }else if(mediaType.getMediaType().getLabel().equalsIgnoreCase("story")){
                storyPrice = mediaType.getPrice().toString();
            }
        }

        // Cari harga termurah
        int minPrice = Integer.MAX_VALUE; // Nilai awal sebagai infinity

        if (!feedsPrice.isEmpty()) {
            minPrice = Math.min(minPrice, Integer.parseInt(feedsPrice));
        }
        if (!reelsPrice.isEmpty()) {
            minPrice = Math.min(minPrice, Integer.parseInt(reelsPrice));
        }
        if (!storyPrice.isEmpty()) {
            minPrice = Math.min(minPrice, Integer.parseInt(storyPrice));
        }

        // Validasi jika tidak ada harga yang ditemukan
        if (minPrice == Integer.MAX_VALUE) {
            minPrice = 0; // Tidak ada harga yang ditemukan
        }

        System.out.println("Harga termurah: " + minPrice);

        for (Category category: categories){
            System.out.println("ini di bagian category");
//                System.out.println("category: " + category);
            Map<String,Object> newMap = new HashMap<>();
            newMap.put("id", category.getId());
            newMap.put("label", category.getLabel());
            categoryDto.add(newMap);
        }

        // Bangun InfluencerFilterResponseDto untuk setiap influencer
        InfluencerFilterResponseDto influencerFilterResponseDto = InfluencerFilterResponseDto.builder()
                .id(influencer.getUser().getId())
                .influencerId(influencer.getId())
                .name(influencer.getUser().getName())
                .email(influencer.getUser().getEmail())
                .location(capitalize(influencer.getUser().getLocation().getLabel()))
                .phone(influencer.getUser().getPhone())
                .gender(influencer.getGender().getLabel())
                .dob(influencer.getDob().toString())
                .feedsprice(formatPrice(feedsPrice)) // Pastikan feedsPrice sudah didefinisikan
                .reelsprice(formatPrice(reelsPrice)) // Pastikan reelsPrice sudah didefinisikan
                .storyprice(formatPrice(storyPrice)) // Pastikan storyPrice sudah didefinisikan
                .category(categoryDto) // Pastikan categoryDto sudah didefinisikan
                .usertype(influencer.getUser().getUserType())
                .instagramid(influencer.getInstagramId())
                .isactive(influencer.getIsActive())
                .token(influencer.getToken())
                .followers(formatFollowers(getFollowersFromInstagramApi(influencer.getToken(), influencer.getInstagramId())))
                .rating(formatRating(averageRating)) // Pastikan averageRating sudah didefinisikan
                .minprice(formatPrice(String.valueOf(minPrice))) // Pastikan minPrice sudah didefinisikan
                .totalreview(formatFollowers(totalReviews)) // Pastikan totalReviews sudah didefinisikan
                .profilepicture(getProfilePicture(influencer.getToken(), influencer.getInstagramId()))
                .issaved(isSaved)
                .build();

        return influencerFilterResponseDto;
    }

    public String saveInfluencer(Integer brandUserId, Integer influencerUserId){
        // Ambil brand berdasarkan brandUserId
        User userBrand = userRepository.findById(brandUserId).orElse(null);
        Brand brand = userBrand.getBrand();
        if (userBrand == null) {
            return "Brand user id not found";
        }
        System.out.println("brand: " + brand.getUser().getName());

        // Ambil influencer berdasarkan influencerUserId
        User userInfluencer = userRepository.findById(influencerUserId).orElse(null);
        Influencer influencer = userInfluencer.getInfluencer();
        if (userInfluencer == null) {
            return "Influencer user id not found";
        }
        System.out.println("influencer: " + influencer.getUser().getName());

        // Cek apakah influencer sudah ada dalam list brand
        if (!brand.getInfluencers().contains(influencer)) {
//            ini masuk ke if gaada di list
            brand.getInfluencers().add(influencer);

            // Simpan perubahan ke database
            brandRepository.save(brand);

            return "Influencer saved successfully";
        } else {
            return "Influencer already saved";
        }
    }

    public String unsaveInfluencer(Integer brandUserId, Integer influencerUserId){
        // Ambil brand berdasarkan brandUserId
        User userBrand = userRepository.findById(brandUserId).orElse(null);
        Brand brand = userBrand.getBrand();
        if (userBrand == null) {
            return "Brand user id not found";
        }
        System.out.println("brand: " + brand.getUser().getName());

        // Ambil influencer berdasarkan influencerUserId
        User userInfluencer = userRepository.findById(influencerUserId).orElse(null);
        Influencer influencer = userInfluencer.getInfluencer();
        if (userInfluencer == null) {
            return "Influencer user id not found";
        }
        System.out.println("influencer: " + influencer.getUser().getName());


        // Cek apakah influencer ada dalam list brand
        if (brand.getInfluencers().contains(influencer)) {
            // Hapus influencer dari list
            brand.getInfluencers().remove(influencer);

            // Simpan perubahan ke database
            brandRepository.save(brand);

            return "Influencer unsaved successfully";
        } else {
            return "Influencer is not in saved list";
        }
    }

    public boolean isInfluencerSavedByBrand(Integer brandId, Integer influencerId) {
        return influencerRepository.isInfluencerSaved(brandId, influencerId);
    }

    public List<InfluencerFilterResponseDto> filterInfluencerSaved(InfluencerFilterRequestDto influencerFilterRequestDto, Integer brandId) {
        System.out.println(influencerFilterRequestDto);

        boolean hasFilter = hasAnyFilter(influencerFilterRequestDto);
        List<Influencer> influencers1 = new ArrayList<>();

        if(hasFilter){
            // Buat Predicate menggunakan toPredicate
            Specification<Influencer> spec = (root, query, criteriaBuilder) -> {
                return toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto);
            };

            // Ambil influencer berdasarkan predikat yang telah dibuat
            influencers1 = influencerRepository.findAll(spec);

            System.out.println("spec: " + spec);
        } else{
            influencers1 = influencerRepository.findAll();
        }

        System.out.println("foll: " + influencerFilterRequestDto.getFollowers());
        System.out.println("age aud: " + influencerFilterRequestDto.getAgeAudience());

        boolean isFollowersEmpty = influencerFilterRequestDto.getFollowers() == null
                || influencerFilterRequestDto.getFollowers().isEmpty();

        List<Influencer> influencers2 = new ArrayList<>();
        influencers2 = influencers1;
//        Untuk filter by followers
        if (!isFollowersEmpty){
            System.out.println("masuk filter by foll");
            influencers2 = filterInfluencersByFollowers(influencers1, influencerFilterRequestDto.getFollowers());
        }

        boolean isAudienceAgeEmpty = influencerFilterRequestDto.getAgeAudience() == null
                || influencerFilterRequestDto.getAgeAudience().isEmpty();

        List<Influencer> influencers3 = new ArrayList<>();
        influencers3 = influencers2;
//        Untuk filter by audience age
        if (!isAudienceAgeEmpty){
            System.out.println("masuk filter by age aud");
            influencers3 = filterByAudienceAge(influencers2, influencerFilterRequestDto.getAgeAudience());
        }

        boolean isAudienceGenderEmpty = influencerFilterRequestDto.getGenderAudience() == null
                || influencerFilterRequestDto.getGenderAudience().isEmpty();

//        List<Influencer> influencers = new ArrayList<>();
//        influencers = influencers3;
////        Untuk filter by audience gender
//        if (!isAudienceGenderEmpty){
//            System.out.println("masuk filter by gender aud");
//            influencers = filterByGenderAudience(influencers3, influencerFilterRequestDto.getGenderAudience());
//        }

        List<Influencer> influencers4 = new ArrayList<>();
        influencers4 = influencers3;
//        Untuk filter by audience gender
        if (!isAudienceGenderEmpty){
            System.out.println("masuk filter by gender aud");
            influencers4 = filterByGenderAudience(influencers3, influencerFilterRequestDto.getGenderAudience());
        }

//        Section ini untuk bagian tab category
        List<Influencer> influencers = new ArrayList<>();
        if (!influencerFilterRequestDto.getCategoryChosen().isEmpty()){
            Integer categoryChosen = influencerFilterRequestDto.getCategoryChosen().get(0);
            System.out.println("INI MASUK KE TAB CATEGORY");
            if(categoryChosen == 0){
                influencers = influencers4;
            }
            else{
                influencers = filterInfluencersByCategory(influencers4, categoryChosen);
            }
        }else {
            influencers = influencers4;
        }


        List<InfluencerFilterResponseDto> response = new ArrayList<>();
//        return influencerRepository.findAll(spec);
        for (Influencer influencer: influencers){
            System.out.println("influencers: " + influencer.getUser().getName());

            User userBrand = userRepository.findById(brandId).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI FILTER INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(brandId));
            System.out.println("influencer: " + influencer.getId());

            if(isSaved){
                InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

                // Tambahkan influencer ke response list
                response.add(influencerFilterResponseDto);
            }

        }

        return response;
    }

    public List<InfluencerFilterResponseDto> sortInfluencerSaved(List<Integer> sort, InfluencerFilterRequestDto influencerFilterRequestDto, Integer brandId) {
        System.out.println("influencerFilterRequestDto" + influencerFilterRequestDto);
        System.out.println("sort" + sort);

        boolean hasFilter = hasAnyFilter(influencerFilterRequestDto);
        List<Influencer> influencers1 = new ArrayList<>();

        if(hasFilter){
            // Buat Predicate menggunakan toPredicate
            Specification<Influencer> spec = (root, query, criteriaBuilder) -> {
                return toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto);
            };

            // Ambil influencer berdasarkan predikat yang telah dibuat
            influencers1 = influencerRepository.findAll(spec);

            System.out.println("spec: " + spec);
        } else{
            influencers1 = influencerRepository.findAll();
        }

        System.out.println("foll: " + influencerFilterRequestDto.getFollowers());
        System.out.println("age aud: " + influencerFilterRequestDto.getAgeAudience());

        boolean isFollowersEmpty = influencerFilterRequestDto.getFollowers() == null
                || influencerFilterRequestDto.getFollowers().isEmpty();

        List<Influencer> influencers2 = new ArrayList<>();
        influencers2 = influencers1;
//        Untuk filter by followers
        if (!isFollowersEmpty){
            System.out.println("masuk filter by foll");
            influencers2 = filterInfluencersByFollowers(influencers1, influencerFilterRequestDto.getFollowers());
        }

        boolean isAudienceAgeEmpty = influencerFilterRequestDto.getAgeAudience() == null
                || influencerFilterRequestDto.getAgeAudience().isEmpty();

        List<Influencer> influencers3 = new ArrayList<>();
        influencers3 = influencers2;
//        Untuk filter by audience age
        if (!isAudienceAgeEmpty){
            System.out.println("masuk filter by age aud");
            influencers3 = filterByAudienceAge(influencers2, influencerFilterRequestDto.getAgeAudience());
        }

        boolean isAudienceGenderEmpty = influencerFilterRequestDto.getGenderAudience() == null
                || influencerFilterRequestDto.getGenderAudience().isEmpty();

//        List<Influencer> influencers = new ArrayList<>();
//        influencers = influencers3;
////        Untuk filter by audience gender
//        if (!isAudienceGenderEmpty){
//            System.out.println("masuk filter by gender aud");
//            influencers = filterByGenderAudience(influencers3, influencerFilterRequestDto.getGenderAudience());
//        }

        List<Influencer> influencers4 = new ArrayList<>();
        influencers4 = influencers3;
//        Untuk filter by audience gender
        if (!isAudienceGenderEmpty){
            System.out.println("masuk filter by gender aud");
            influencers4 = filterByGenderAudience(influencers3, influencerFilterRequestDto.getGenderAudience());
        }

//        Section ini untuk bagian tab category
        List<Influencer> influencers = new ArrayList<>();
        if (!influencerFilterRequestDto.getCategoryChosen().isEmpty()){
            Integer categoryChosen = influencerFilterRequestDto.getCategoryChosen().get(0);
            System.out.println("INI MASUK KE TAB CATEGORY");
            if(categoryChosen == 0){
                influencers = influencers4;
            }
            else{
                influencers = filterInfluencersByCategory(influencers4, categoryChosen);
            }
        }else {
            influencers = influencers4;
        }


//        ini untuk sort
        // Sort berdasarkan parameter
//        if (sort.isEmpty() || sort.get(0) == 1 ) {
//            System.out.println("INI MASUK KE SORT POPULAR");
//            influencers.sort(Comparator.comparing(influencer -> influencer.getProjectHeaders().size(), Comparator.reverseOrder()));
//        } else if (sort.get(0) == 3) { // Sort by rating
//            influencers = influencerRepository.findAllOrderByAverageRatingDesc();
//        } else if (sort.get(0) == 2) { // Sort by price
//            influencers = influencerRepository.findAllOrderByLowestPriceAsc();
//        }
        // Sort berdasarkan parameter
        if ( sort.isEmpty() || sort.get(0) == 1 ) {
            System.out.println("INI MASUK KE SORT POPULAR");
            influencers.sort(Comparator.comparing(influencer -> influencer.getProjectHeaders().size(), Comparator.reverseOrder()));
        } else if (sort.get(0) == 3) { // Sort by rating
            influencers.sort(Comparator.comparing(
                    influencer -> {
                        // Panggil repository untuk mendapatkan average rating
                        Double avgRating = influencerRepository.findAverageRatingByInfluencerId(Long.valueOf(influencer.getId()));
                        return avgRating == null ? 0.0 : avgRating; // Default nilai 0.0 jika null
                    }, Comparator.reverseOrder()));
//            influencers = influencerRepository.findAllOrderByAverageRatingDesc();
        } else if (sort.get(0) == 2) { // Sort by price
//            influencers = influencerRepository.findAllOrderByLowestPriceAsc();
            influencers.sort(Comparator.comparing(
                    influencer -> influencer.getInfluencerMediaTypes().stream()
                            .mapToDouble(mediaType -> mediaType.getPrice() != null ? mediaType.getPrice() : Double.MAX_VALUE)
                            .min()
                            .orElse(Double.MAX_VALUE)
            ));
        }

//      Mapping response
        List<InfluencerFilterResponseDto> response = new ArrayList<>();
//        return influencerRepository.findAll(spec);
        for (Influencer influencer: influencers){
            System.out.println("influencers: " + influencer.getUser().getName());


            User userBrand = userRepository.findById(brandId).orElse(null);
            Brand brand = userBrand.getBrand();

            Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());
//            Boolean isSaved = isInfluencerSavedByBrand(Integer.valueOf(brandId), influencer.getId());
            System.out.println("INI LAGI LIAT IS SAVED DI SORT INFLUENCER");
            System.out.println("boolean: " + isSaved);
            System.out.println("brandId: " + Integer.valueOf(brandId));
            System.out.println("influencer: " + influencer.getId());

            if(isSaved){
                InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

                // Tambahkan influencer ke response list
                response.add(influencerFilterResponseDto);
            }
        }
        return response;
    }

    public InfluencerDetailResponseDto detailInfluencer(Integer influencerId, Integer userId) {
        Influencer influencer = influencerRepository.findById(influencerId).orElse(null);
        System.out.println("influencer: " + influencer.getUser().getName());

        User userBrand = userRepository.findById(userId).orElse(null);
        Brand brand = userBrand.getBrand();

        Boolean isSaved = isInfluencerSavedByBrand(brand.getId(), influencer.getId());


        InfluencerFilterResponseDto influencerFilterResponseDto = buildResponse(influencer, isSaved);

        return null;
    }

    public InfluencerFilterResponseDto buildDetailResponse(Influencer influencer, Boolean isSaved){

        Double averageRating = influencerRepository.findAverageRatingByInfluencerId(Long.valueOf(influencer.getId()));
        Integer totalReviews = influencerRepository.findTotalReviewsByInfluencerId(Long.valueOf(influencer.getId()));


        if (averageRating == null) {
            averageRating = 0.0; // Default jika tidak ada review
        }

        if (totalReviews == null) {
            totalReviews = 0; // Default jika tidak ada review
        }

        List<Category> categories = influencer.getCategories();
        List<Map<String,Object>> categoryDto = new ArrayList<>();

        String feedsPrice = "";
        String reelsPrice = "";
        String storyPrice = "";
        List<InfluencerMediaType> mediaTypes = influencer.getInfluencerMediaTypes();
        for(InfluencerMediaType mediaType: mediaTypes){
            if(mediaType.getMediaType().getLabel().equalsIgnoreCase("feeds")){
                feedsPrice = mediaType.getPrice().toString();
            }else if(mediaType.getMediaType().getLabel().equalsIgnoreCase("reels")){
                reelsPrice = mediaType.getPrice().toString();
            }else if(mediaType.getMediaType().getLabel().equalsIgnoreCase("story")){
                storyPrice = mediaType.getPrice().toString();
            }
        }

        for (Category category: categories){
            Map<String,Object> newMap = new HashMap<>();
            newMap.put("id", category.getId());
            newMap.put("label", category.getLabel());
            categoryDto.add(newMap);
        }

        // Bangun InfluencerFilterResponseDto untuk setiap influencer
        InfluencerFilterResponseDto influencerFilterResponseDto = InfluencerFilterResponseDto.builder()
                .id(influencer.getUser().getId())
                .influencerId(influencer.getId())
                .name(influencer.getUser().getName())
                .email(influencer.getUser().getEmail())
                .location(capitalize(influencer.getUser().getLocation().getLabel()))
                .phone(influencer.getUser().getPhone())
                .gender(influencer.getGender().getLabel())
                .dob(influencer.getDob().toString())
                .feedsprice(formatPrice(feedsPrice)) // Pastikan feedsPrice sudah didefinisikan
                .reelsprice(formatPrice(reelsPrice)) // Pastikan reelsPrice sudah didefinisikan
                .storyprice(formatPrice(storyPrice)) // Pastikan storyPrice sudah didefinisikan
                .category(categoryDto) // Pastikan categoryDto sudah didefinisikan
                .usertype(influencer.getUser().getUserType())
                .instagramid(influencer.getInstagramId())
                .isactive(influencer.getIsActive())
                .token(influencer.getToken())
                .followers(formatFollowers(getFollowersFromInstagramApi(influencer.getToken(), influencer.getInstagramId())))
                .rating(formatRatingDetail(averageRating)) // Pastikan averageRating sudah didefinisikan
                .totalreview(formatFollowers(totalReviews)) // Pastikan totalReviews sudah didefinisikan
                .profilepicture(getProfilePicture(influencer.getToken(), influencer.getInstagramId()))
                .issaved(isSaved)
                .build();

        return influencerFilterResponseDto;
    }

}
