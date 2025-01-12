package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Age;
import com.skripsi.Fluency.model.entity.Gender;
import com.skripsi.Fluency.repository.GenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenderService {

    @Autowired
    public GenderRepository genderRepository;

    public List<Map<String, String>> getAllGender() {
        List<Gender> list = genderRepository.findAll();
        List<Map<String, String>> response = new ArrayList<>();

        for(Gender item : list) {
            Map<String, String> newMap = new HashMap<>();
            newMap.put("id", item.getId().toString());
            newMap.put("label", item.getLabel());
            newMap.put("logo", item.getLogo());
            newMap.put("active_logo", item.getActiveLogo());

            response.add(newMap);
        }

        return response;
    }
}
