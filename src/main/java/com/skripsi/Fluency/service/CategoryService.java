package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Category;
import com.skripsi.Fluency.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {


    @Autowired
    public CategoryRepository categoryRepository;

    public Map<String, String> getAllCategory() {
        List<Category> list = categoryRepository.findAll();

        Map<String, String> labels = new HashMap<>();

        for(Category item: list) {
            labels.put(item.getId().toString(), item.getLabel());
        }

        return labels;
    }
}
