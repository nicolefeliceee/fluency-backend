package com.skripsi.Fluency.controller;

import com.skripsi.Fluency.model.dto.ProjectDetailDto;
import com.skripsi.Fluency.model.dto.ProjectHeaderDto;
import com.skripsi.Fluency.model.dto.SentimentAnalysisDto;
import com.skripsi.Fluency.model.dto.VerifyLinkDto;
import com.skripsi.Fluency.service.ProjectService;
import org.apache.coyote.Response;
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
                                        @RequestParam(name = "status", required = false) String status,
                                        @RequestParam(name = "title", required = false) String title
    ) {
        return projectService.getProject(status, userId, title);

    }

    @GetMapping("header-id/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable(name = "id") String id,
                                              @RequestParam(name = "status", required = false) String status) {

        return projectService.getProjectById(id);

    }

    @GetMapping("detail/detail-id/{id}")
    public ResponseEntity<?> getProjectDetailById(@PathVariable(name = "id") String id) {

        ProjectDetailDto response = projectService.getProjectDetail(id);
        if(response == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectHeaderDto request) {
        return projectService.createProject(request);
    }

    @PutMapping
    public ResponseEntity<?> editProject(@RequestBody ProjectHeaderDto request) {
        return projectService.editProject(request);
    }

    @PutMapping("detail")
    public ResponseEntity<?> editProjectDetail(@RequestBody ProjectDetailDto request) {
        return projectService.editProjectDetail(request);
    }

    @GetMapping("verify-link")
    public ResponseEntity<?> verifyLink(
            @RequestParam("link") String link,
            @RequestParam("influencer-id") String influencerId
    ) {
        VerifyLinkDto response = projectService.findMediaidByLink(influencerId, link);
        if(response == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteProjectHeader(@PathVariable(name = "id") String projectHeaderId) {
        return projectService.deleteProjectHeader(projectHeaderId);
    }

    @GetMapping("performance-analytics/{id}")
    public ResponseEntity<?> getPerformanceAnalytics(@PathVariable(name = "id") String id) {

        ProjectDetailDto response = projectService.getPerformanceAnalytics(id);
        if(response == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(response);
        }
    }


    @GetMapping("sentiment-analysis/{id}")
    public ResponseEntity<?> getSentimentAnalysis(@PathVariable(name = "id") String id) {
        ResponseEntity<?> response = projectService.getSentimentAnalysis(id);
        return response;
    }

    @GetMapping("report/create/{id}")
    public ResponseEntity<?> createReportProject(@PathVariable(name = "id") String projectHeaderId) {
        ResponseEntity<?> response = projectService.createTicket(projectHeaderId);
        return response;
    }

    @GetMapping("report")
    public ResponseEntity<?> getTickets(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "query", required = false) String query
    ) {
        return projectService.getTicket(status, query);
    }

    @GetMapping("report/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable(name = "id") String id,
                                            @RequestParam(name = "status", required = false) String status
    ) {
        return projectService.getTicketById(id);
    }

    @PutMapping("report/edit/{id}")
    public ResponseEntity<?> editTicketStatus(@PathVariable(name = "id") String id,
                                              @RequestParam(name = "status") String status) {
        System.out.println(status);
        return projectService.editTicket(id, status);
    }

}
