package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Category;
import com.skripsi.Fluency.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {


    @Autowired
    public CategoryRepository categoryRepository;

    public List<Map<String, String>> getAllCategory() {
        List<Category> list = categoryRepository.findAll();

        List<Map<String, String>> listMap = new ArrayList<>();

        for(Category item: list) {
            Map<String, String> newMap = new HashMap<>();
            newMap.put("id", item.getId().toString());
            newMap.put("label", item.getLabel());
            newMap.put("logo", item.getLogo());
            newMap.put("active_logo", item.getActiveLogo());
            listMap.add(newMap);
        }

        return listMap;
    }
}