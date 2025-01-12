package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.MediaType;
import com.skripsi.Fluency.repository.MediaTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MediaTypeService {
    @Autowired
    public MediaTypeRepository mediaTypeRepository;

    public List<Map<String, String>> getAllMediaType() {
        List<MediaType> list = mediaTypeRepository.findAll();
        List<Map<String, String>> response = new ArrayList<>();

        for(MediaType item : list) {
            Map<String, String> newMap = new HashMap<>();
            newMap.put("id", item.getId().toString());
            newMap.put("label", item.getLabel());

            response.add(newMap);
        }

        return response;
    }
}
