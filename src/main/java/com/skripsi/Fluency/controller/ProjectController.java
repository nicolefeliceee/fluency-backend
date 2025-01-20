package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.ProjectHeaderDto;
import com.skripsi.Fluency.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("project")
public class ProjectController {

    @Autowired
    public ProjectService projectService;


    @GetMapping("{user_id}")
    public ResponseEntity<?> getProject(@PathVariable(name = "user_id") String userId,
                                        @RequestParam(name = "status", required = false) String status) {

        return projectService.getProject(status, userId);

    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectHeaderDto request) {
        System.out.println(request);
        return projectService.createProject(request);
    }

    @PutMapping
    public ResponseEntity<?> editProject(@RequestBody ProjectHeaderDto request) {
        System.out.println(request);
        return projectService.editProject(request);
    }
}
