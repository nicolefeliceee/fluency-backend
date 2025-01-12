package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Gender;
import com.skripsi.Fluency.model.entity.Status;
import com.skripsi.Fluency.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatusService {

    @Autowired
    public StatusRepository statusRepository;

    public List<Map<String, Object>> getAllStatus() {

        List<Status> list = statusRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for(Status item : list) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("id", item.getId().toString());
            newMap.put("label", item.getLabel());
            newMap.put("for_brand", item.getForBrand());
            newMap.put("for_influencer", item.getForInfluencer());

            response.add(newMap);
        }

        return response;
    }
}
