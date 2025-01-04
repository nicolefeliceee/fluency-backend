package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.entity.Category;
import com.skripsi.Fluency.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {


    @Autowired
    public CategoryRepository categoryRepository;

    public List<Category> getAllCategory() {
        List<Category> list = categoryRepository.findAll();

        System.out.print(list);

        return list;
    }
}
