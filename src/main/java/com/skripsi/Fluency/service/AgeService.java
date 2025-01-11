package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Age;
import com.skripsi.Fluency.repository.AgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgeService {

    @Autowired
    public AgeRepository ageRepository;

    public List<Map<String, String>> getAllAge() {
        List<Age> list = ageRepository.findAll();
        List<Map<String, String>> response = new ArrayList<>();

        for(Age item : list) {
            Map<String, String> newMap = new HashMap<>();
            newMap.put("id", item.getId().toString());
            newMap.put("label", item.getLabel());

            response.add(newMap);
        }

        return response;
    }
}
