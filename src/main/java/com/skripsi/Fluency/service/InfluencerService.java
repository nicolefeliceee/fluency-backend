package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.dto.InfluencerFilterRequestDto;
import com.skripsi.Fluency.model.dto.InfluencerFilterResponseDto;
import com.skripsi.Fluency.model.dto.LoginBrandRequestDto;
import com.skripsi.Fluency.model.dto.LoginResponseDto;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.InfluencerRepository;
import com.skripsi.Fluency.repository.UserRepository;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class InfluencerService {

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

        public List<InfluencerFilterResponseDto> filterInfluencer(InfluencerFilterRequestDto influencerFilterRequestDto) {
        System.out.println(influencerFilterRequestDto);
        // Buat Predicate menggunakan toPredicate
        Specification<Influencer> spec = (root, query, criteriaBuilder) -> {
            return toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto);
        };

        System.out.println("spec: " + spec);

        // Ambil influencer berdasarkan predikat yang telah dibuat
        List<Influencer> influencers = influencerRepository.findAll(spec);
        List<InfluencerFilterResponseDto> response = new ArrayList<>();
//        return influencerRepository.findAll(spec);
        for (Influencer influencer: influencers){
            System.out.println("influencers: " + influencer.getUser().getName());

            List<Category> categories = influencer.getCategories();
            List<Map<String,Object>> categoryDto = new ArrayList<>();

            for (Category category: categories){
                Map<String,Object> newMap = new HashMap<>();
                newMap.put("id", category.getId());
                newMap.put("label", category.getLabel());
                categoryDto.add(newMap);
            }

//            List<Map<String,Object>> categoryDtos = categories.stream().map(
//                    item -> {
//                        Map<String,Object> newMap = new HashMap<>();
//                        newMap.put("id", item.getId());
//                        newMap.put("label", item.getLabel());
//                        categoryDto.add(newMap);
//                        return newMap;
//                    }
//            ).toList();

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

            //      Build responsenya
            InfluencerFilterResponseDto influencerFilterResponseDto = InfluencerFilterResponseDto.builder()
                    .id(influencer.getUser().getId())
                    .name(influencer.getUser().getName())
                    .email(influencer.getUser().getEmail())
                    .location(influencer.getUser().getLocation().getLabel())
                    .phone(influencer.getUser().getPhone())
                    .gender(influencer.getGender().getLabel())
                    .dob(influencer.getDob().toString())
                    .feedsPrice(feedsPrice)
                    .reelsPrice(reelsPrice)
                    .storyPrice(storyPrice)
                    .category(categoryDto)
                    .userType(influencer.getUser().getUserType())
                    .instagramId(influencer.getInstagramId())
                    .isActive(influencer.getIsActive())
                    .token(influencer.getToken())
                    .build();

            response.add(influencerFilterResponseDto);
        }

        return response;
    }
}
