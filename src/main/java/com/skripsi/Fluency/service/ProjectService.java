package com.skripsi.Fluency.service;

import com.skripsi.Fluency.model.dto.ProjectDetailDto;
import com.skripsi.Fluency.model.dto.ProjectHeaderDto;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    public ProjectHeaderRepository projectHeaderRepository;

    @Autowired
    public ProjectDetailRepository projectDetailRepository;

    @Autowired
    public StatusRepository statusRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public BrandRepository brandRepository;

    @Autowired
    public InfluencerRepository influencerRepository;

    @Autowired
    public MediaTypeRepository mediaTypeRepository;

    public ResponseEntity<?> getProject(String statusId, String userId) {
        List<ProjectHeader> entities = new ArrayList<>();
        if(statusId == null) {
            entities = projectHeaderRepository.findAll();

        } else {
            Status status =  statusRepository.findById(Integer.valueOf(statusId)).orElse(null);
            User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);
            Brand brand;
            Influencer influencer;
            if(user.getUserType().equalsIgnoreCase("brand")) {
                brand = brandRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByStatusAndBrand(status, brand);
            } else if(user.getUserType().equalsIgnoreCase(("influencer"))) {
                influencer = influencerRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByStatusAndInfluencer(status, influencer);
            }
        }

        List<ProjectHeaderDto> responseDto = entities.stream().map(
                item -> ProjectHeaderDto.builder()
                        .id(item.getId().toString())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .mention(item.getMention())
                        .hashtag(item.getHashtag())
                        .caption(item.getCaption())
                        .brandId(item.getBrand().getId().toString())
                        .influencerId(item.getInfluencer() == null ? "" : item.getInfluencer().getId().toString())
                        .statusId(item.getStatus().getId().toString())
                        .projectDetails(
                                item.getProjectDetails().stream().map(
                                        itemDetail -> ProjectDetailDto.builder()
                                                .mediatypeId(itemDetail.getMediaType().getId().toString())
                                                .deadlineDate(itemDetail.getDateDeadline() == null ? "" : itemDetail.getDateDeadline().toString())
                                                .deadlineTime(itemDetail.getTimeDeadline() == null ? "" : itemDetail.getTimeDeadline().toString())
                                                .note(itemDetail.getNote())
                                                .build()
                                ).collect(Collectors.toList())
                        )
                        .build()
        ).toList();


        return ResponseEntity.ok(responseDto);

    }

    @Transactional
    public ResponseEntity<?> createProject(ProjectHeaderDto request) {

        User user = userRepository.findById(Integer.valueOf(request.getUserId())).orElse(null);

        Brand brand = brandRepository.findById(user.getBrand().getId()).orElse(null);
        Influencer influencer = null;
        if(request.getInfluencerId() != null && !request.getInfluencerId().isBlank()) {
            influencer = influencerRepository.findById(Integer.valueOf(request.getInfluencerId())).orElse(null);
        }
        Status status =  statusRepository.findById(Integer.valueOf(request.getStatusId())).orElse(null);

        ProjectHeader entity = ProjectHeader.builder()
                .brand(brand)
                .influencer(influencer)
                .status(status)
                .title(request.getTitle())
                .description(request.getDescription())
                .caption(request.getCaption())
                .hashtag(request.getHashtag())
                .mention(request.getMention())
                .build();

        ProjectHeader savedProjectHeader = projectHeaderRepository.save(entity);

        List<ProjectDetail> details = request.getProjectDetails().stream().map(
                item -> {
                    MediaType mediaType = mediaTypeRepository.findById(Integer.valueOf(item.getMediatypeId())).orElse(null);

                    Double nominal = item.getNominal() == null? 0 : Double.parseDouble(item.getNominal());
                    LocalDate dateDeadline = LocalDate.parse(item.getDeadlineDate());
                    LocalTime timeDeadline = LocalTime.parse(item.getDeadlineTime());

                    return ProjectDetail.builder()
                            .mediaType(mediaType)
                            .nominal(nominal)
                            .link(item.getLink())
                            .dateDeadline(dateDeadline)
                            .timeDeadline(timeDeadline)
                            .note(item.getNote())
                            .projectHeader(savedProjectHeader)
                            .build();
                }
        ).toList();

        List<ProjectDetail> savedDetails = projectDetailRepository.saveAll(details);

        return ResponseEntity.ok(request);
    }

}
