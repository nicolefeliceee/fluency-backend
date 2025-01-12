package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.dto.InfluencerFilterRequestDto;
import com.skripsi.Fluency.model.dto.InfluencerFilterResponseDto;
import com.skripsi.Fluency.model.dto.LoginBrandRequestDto;
import com.skripsi.Fluency.model.dto.LoginResponseDto;
import com.skripsi.Fluency.model.entity.Brand;
import com.skripsi.Fluency.model.entity.Influencer;
import com.skripsi.Fluency.model.entity.User;
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
import java.util.List;


@Service
public class InfluencerService {

    private Range<Integer> parseRange(String value) {
        value = value.toLowerCase(); // Ubah ke lowercase untuk konsistensi
        if (value.contains("-")) { // Format seperti "1k - 10k" atau "1M - 10M"
            String[] parts = value.replace("k", "000").replace("m", "000000").split("-");
            int lower = Integer.parseInt(parts[0].trim());
            int upper = Integer.parseInt(parts[1].trim());
            return Range.closed(lower, upper);
        } else if (value.startsWith(">")) { // Format seperti ">1k" atau ">1M"
            int lowerBound = Integer.parseInt(value.replace(">", "").replace("k", "000").replace("m", "000000").trim());
            return Range.rightOpen(lowerBound, Integer.MAX_VALUE); // (lowerBound, +∞)
        } else if (value.startsWith("<")) { // Format seperti "<1k" atau "<1M"
            int upperBound = Integer.parseInt(value.replace("<", "").replace("k", "000").replace("m", "000000").trim());
            return Range.leftOpen(Integer.MIN_VALUE, upperBound); // (-∞, upperBound)
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
                System.out.println("masuk di for");
                System.out.println("age per range: " + influencerFilterRequestDto.getAge());

                Range<Integer> ageRange = parseRange(range);
                System.out.println("ageRange: " + ageRange);

                // Mengambil nilai dari Optional dan mengonversinya menjadi Integer
                Integer lowerAge = ageRange.getLowerBound().getValue().orElseThrow();
                Integer upperAge = ageRange.getUpperBound().getValue().orElseThrow();

                System.out.println("lowerAge: " + lowerAge);
                System.out.println("upperAge: " + upperAge);

                // Menggunakan fungsi yang telah diperbarui dengan parameter rentang usia
                Predicate agePredicate = createAgePredicate(criteriaBuilder, root, lowerAge, upperAge);

                // Menambahkan predikat usia ke dalam daftar predikat
                agePredicates.add(agePredicate);
                System.out.println("agepredicates: " + agePredicates);

//                // Menggunakan nilai yang sudah dikeluarkan dari Optional untuk membuat predikat usia
//                Predicate lowerAgePredicate = createAgePredicate(criteriaBuilder, root, lowerAge);
//                Predicate upperAgePredicate = createAgePredicate(criteriaBuilder, root, upperAge);

//                // Membuat predikat untuk batas bawah usia
//                Predicate lowerAgePredicate = criteriaBuilder.greaterThanOrEqualTo(
//                        criteriaBuilder.function("YEAR", Integer.class, root.get("dob")),
//                        currentYear - upperAge
//                );
//
//                // Membuat predikat untuk batas atas usia
//                Predicate upperAgePredicate = criteriaBuilder.lessThanOrEqualTo(
//                        criteriaBuilder.function("YEAR", Integer.class, root.get("dob")),
//                        currentYear - lowerAge
//                );

//                System.out.println("lowerAgePredicate: " + lowerAgePredicate);
//                System.out.println("upperAgePredicate: " + upperAgePredicate);

//                Predicate lowerBoundPredicate = criteriaBuilder.greaterThanOrEqualTo(
//                        root.get("dob"),
//                        ageRange.getLowerBound().getValue().orElseThrow());
//                Predicate upperBoundPredicate = criteriaBuilder.lessThanOrEqualTo(
//                        root.get("dob"),
//                        ageRange.getUpperBound().getValue().orElseThrow());
//
//                System.out.println("lowerBoundPredicate: " + lowerBoundPredicate);
//                System.out.println("upperBoundPredicate: " + upperBoundPredicate);


                // Gabungkan rentang usia dengan AND
//                agePredicates.add(criteriaBuilder.and(lowerAgePredicate, upperAgePredicate));
//                System.out.println("agePredicates: " + agePredicates);
//                System.out.println("andPredicates: " + andPredicates);
//                System.out.println("orPredicates: " + orPredicates);

            }

            // Gabungkan predikat follower dengan OR (untuk rentang berbeda, gabungkan dengan OR)
            if (!agePredicates.isEmpty()) {
                System.out.println("kalau kosong");
                orPredicates.add(criteriaBuilder.or(agePredicates.toArray(new Predicate[0])));
                System.out.println("agePredicates: " + agePredicates);
                System.out.println("andPredicates: " + andPredicates);
                System.out.println("orPredicates: " + orPredicates);
            }
        }

//        // 1. Price Range
//        if (!influencerFilterRequestDto.getPrice().isEmpty()) {
//            List<Predicate> pricePredicates = new ArrayList<>();
//
//            // Loop untuk setiap range followers dan buat predikatnya
//            for (String range : influencerFilterRequestDto.getPrice()) {
//                System.out.println("masuk di for");
//                System.out.println("age per range: " + influencerFilterRequestDto.getPrice());
//
//                Range<Integer> priceRange = parseRange(range);
//                System.out.println("ageRange: " + priceRange);
//                System.out.println("root.get(followers): " + root.get("followers"));
//                Predicate lowerBoundPredicate = criteriaBuilder.greaterThanOrEqualTo(
//                        root.get("price"),
//                        priceRange.getLowerBound().getValue().orElseThrow());
//                Predicate upperBoundPredicate = criteriaBuilder.lessThanOrEqualTo(
//                        root.get("dob"),
//                        priceRange.getUpperBound().getValue().orElseThrow());
//
//                System.out.println("lowerBoundPredicate: " + lowerBoundPredicate);
//                System.out.println("upperBoundPredicate: " + upperBoundPredicate);
//
//                // Gabungkan range ini dengan AND (followers >= X AND followers <= Y)
//                pricePredicates.add(criteriaBuilder.and(lowerBoundPredicate, upperBoundPredicate));
//                System.out.println("followerPredicates: " + pricePredicates);
//                System.out.println("andPredicates: " + andPredicates);
//                System.out.println("orPredicates: " + orPredicates);
//
//            }
//
//            // Gabungkan predikat follower dengan OR (untuk rentang berbeda, gabungkan dengan OR)
//            if (!pricePredicates.isEmpty()) {
//                System.out.println("kalau kosong");
//                orPredicates.add(criteriaBuilder.or(pricePredicates.toArray(new Predicate[0])));
//                System.out.println("followerPredicates: " + pricePredicates);
//                System.out.println("andPredicates: " + andPredicates);
//                System.out.println("orPredicates: " + orPredicates);
//            }
//        }




//
//        // 2. Media Filter
//        if (!influencerFilterRequestDto.getMedia().isEmpty()) {
//            Predicate mediaPredicate = root.get("media").in(influencerFilterRequestDto.getMedia());
//            andPredicates.add(mediaPredicate); // Gabungkan dengan AND
//        }
//
//        // 3. Price Filter
//        if (!influencerFilterRequestDto.getPrice().isEmpty()) {
//            for (String range : influencerFilterRequestDto.getPrice()) {
//                Range<Integer> priceRange = parseRange(range);
//                Predicate lowerPricePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceRange.getLowerBound().getValue().orElseThrow());
//                Predicate upperPricePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceRange.getUpperBound().getValue().orElseThrow());
//                andPredicates.add(criteriaBuilder.and(lowerPricePredicate, upperPricePredicate));
//            }
//        }
//
//        // 4. Rating Filter
//        if (!influencerFilterRequestDto.getRating().isEmpty()) {
//            Predicate ratingPredicate = root.get("rating").in(influencerFilterRequestDto.getRating());
//            andPredicates.add(ratingPredicate); // Gabungkan dengan AND
//        }
//
//        // 5. Age Filter
//        if (!influencerFilterRequestDto.getAge().isEmpty()) {
//            for (String range : influencerFilterRequestDto.getAge()) {
//                Range<Integer> ageRange = parseRange(range);
//                Predicate lowerAgePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("age"), ageRange.getLowerBound().getValue().orElseThrow());
//                Predicate upperAgePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("age"), ageRange.getUpperBound().getValue().orElseThrow());
//                andPredicates.add(criteriaBuilder.and(lowerAgePredicate, upperAgePredicate));
//            }
//        }
//
//        // 6. Gender Filter
//        if (!influencerFilterRequestDto.getGender().isEmpty()) {
//            Predicate genderPredicate = root.get("gender").in(influencerFilterRequestDto.getGender());
//            andPredicates.add(genderPredicate); // Gabungkan dengan AND
//        }
//
//        // 7. Gender Audience Filter
//        if (!influencerFilterRequestDto.getGenderAudience().isEmpty()) {
//            Predicate genderAudiencePredicate = root.get("genderAudience").in(influencerFilterRequestDto.getGenderAudience());
//            andPredicates.add(genderAudiencePredicate); // Gabungkan dengan AND
//        }
//
//        // 8. Location Filter
//        if (!influencerFilterRequestDto.getLocation().isEmpty()) {
//            Predicate locationPredicate = root.get("location").in(influencerFilterRequestDto.getLocation());
//            andPredicates.add(locationPredicate); // Gabungkan dengan AND
//        }

        // Gabungkan semua predikat AND terlebih dahulu
        Predicate andPredicate = criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));

        System.out.println("ini udah di bagian bawah");
        System.out.println("andPredicates: " + andPredicates);
        System.out.println("orPredicates: " + orPredicates);

        // Gabungkan predikat AND dengan OR (untuk followers)
        if (!orPredicates.isEmpty()) {
            Predicate orPredicate = criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));

            System.out.println("or predicates kosong");
            System.out.println("andPredicates: " + andPredicates);
            System.out.println("orPredicates: " + orPredicates);
            System.out.println("yang dibalikin: " + criteriaBuilder.and(andPredicate, orPredicate));

            return criteriaBuilder.and(andPredicate, orPredicate); // Gabungkan AND dengan OR
        }
        System.out.println("yang dibalikin paling bawah: " + andPredicate);
        return andPredicate; // Jika tidak ada OR, cukup return AND
    }

    public InfluencerFilterResponseDto filterInfluencer(InfluencerFilterRequestDto influencerFilterRequestDto) {
        System.out.println(influencerFilterRequestDto);
        // Buat Predicate menggunakan toPredicate
        Specification<Influencer> spec = (root, query, criteriaBuilder) -> {
            System.out.println("masuk ke spec");
            System.out.println("toPre: " + toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto));
            return toPredicate(root, query, criteriaBuilder, influencerFilterRequestDto);
        };

        System.out.println("spec: " + spec);

        // Ambil influencer berdasarkan predikat yang telah dibuat
        List<Influencer> influencers = influencerRepository.findAll(spec);
//        return influencerRepository.findAll(spec);
        for (Influencer influencer: influencers){
            System.out.println("influencers: " + influencer.getUser().getName());
        }
        return null;
    }
}
