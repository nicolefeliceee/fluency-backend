package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Location;
import com.skripsi.Fluency.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationService {

    @Autowired
    public LocationRepository locationRepository;

    public List<Map<String, String>> getAllLocations() {
        List<Location> list = locationRepository.findAll();

        List<Map<String, String>> response = new ArrayList<>();

        for(Location item: list) {
            Map<String, String> map = new HashMap<>();
            map.put("id", item.getId().toString());
            map.put("label", capitalizeWords(item.getLabel()));

            response.add(map);
        }

        return response;
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
