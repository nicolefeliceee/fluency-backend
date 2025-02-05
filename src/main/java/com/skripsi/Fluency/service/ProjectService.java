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
                entities = projectHeaderRepository.findAllByStatusAndBrandOrderByIdDesc(status, brand);
            } else if(user.getUserType().equalsIgnoreCase(("influencer"))) {
                influencer = influencerRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByStatusAndInfluencerOrderByIdDesc(status, influencer);
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
                        .referenceNumber(item.getReferenceNumber())
                        .projectDetails(
                                item.getProjectDetails().stream().map(
                                        itemDetail -> ProjectDetailDto.builder()
                                                .mediatypeId(itemDetail.getMediaType().getId().toString())
                                                .deadlineDate(itemDetail.getDeadlineDate() == null ? "" : itemDetail.getDeadlineDate().toString())
                                                .deadlineTime(itemDetail.getDeadlineTime() == null ? "" : itemDetail.getDeadlineTime().toString())
                                                .note(itemDetail.getNote())
                                                .link(itemDetail.getLink())
                                                .statusId(itemDetail.getStatus().getId().toString())
                                                .id(itemDetail.getId().toString())
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
                .referenceNumber(request.getReferenceNumber())
                .build();

        ProjectHeader savedProjectHeader = projectHeaderRepository.save(entity);

        List<ProjectDetail> details = request.getProjectDetails().stream().map(
                item -> {
                    MediaType mediaType = mediaTypeRepository.findById(Integer.valueOf(item.getMediatypeId())).orElse(null);

                    Double nominal = item.getNominal() == null? 0 : Double.parseDouble(item.getNominal());
                    LocalDate dateDeadline = LocalDate.parse(item.getDeadlineDate());
                    LocalTime timeDeadline = LocalTime.parse(item.getDeadlineTime());
//                    Status waitingStatus = statusRepository.findById(3).orElse(null);

                    return ProjectDetail.builder()
                            .mediaType(mediaType)
                            .nominal(nominal)
                            .link(item.getLink())
                            .deadlineDate(dateDeadline)
                            .deadlineTime(timeDeadline)
                            .note(item.getNote())
                            .status(status)
                            .projectHeader(savedProjectHeader)
                            .build();
                }
        ).toList();

        List<ProjectDetail> savedDetails = projectDetailRepository.saveAll(details);

        return ResponseEntity.ok(request);
    }

    @Transactional
    public ResponseEntity<?> editProject(ProjectHeaderDto requestDto) {
        ProjectHeader existing = projectHeaderRepository.findById(Integer.valueOf(requestDto.getId())).orElse(null);

        if(existing == null) {
            return ResponseEntity.notFound().build();
        }

        Influencer influencer = null;
        if(requestDto.getInfluencerId() != null && !requestDto.getInfluencerId().equalsIgnoreCase("")) {
            influencer = influencerRepository.findById(Integer.valueOf(requestDto.getInfluencerId())).orElse(null);
        }

        Status newStatus = statusRepository.findById(Integer.valueOf(requestDto.getStatusId())).orElse(null);

        existing.setTitle(requestDto.getTitle());
        existing.setDescription(requestDto.getDescription());
        existing.setCaption(requestDto.getCaption());
        existing.setHashtag(requestDto.getHashtag());
        existing.setMention(requestDto.getMention());
        existing.setInfluencer(influencer);
        existing.setStatus(newStatus);
        existing.setReferenceNumber(requestDto.getReferenceNumber());

        projectHeaderRepository.save(existing);

        projectDetailRepository.deleteAll(existing.getProjectDetails());

        List<ProjectDetail> newDetails = requestDto.getProjectDetails().stream().map(
            item -> {
                MediaType mediaType = mediaTypeRepository.findById(Integer.valueOf(item.getMediatypeId())).orElse(null);

                Double nominal = item.getNominal() == null || item.getNominal().equalsIgnoreCase("")? 0 : Double.parseDouble(item.getNominal());


                LocalDate dateDeadline = null;
                LocalTime timeDeadline = null;
                if(item.getDeadlineDate() != null && !item.getDeadlineDate().equalsIgnoreCase("")) {
                    dateDeadline = LocalDate.parse(item.getDeadlineDate());
                }
                if(item.getDeadlineTime() != null && !item.getDeadlineTime().equalsIgnoreCase("")) {
                    timeDeadline = LocalTime.parse(item.getDeadlineTime());
                }

                return ProjectDetail.builder()
                        .mediaType(mediaType)
                        .nominal(nominal)
                        .link(item.getLink())
                        .deadlineDate(dateDeadline)
                        .deadlineTime(timeDeadline)
                        .note(item.getNote())
                        .status(newStatus)
                        .projectHeader(existing)
                        .build();
            }
        ).toList();
        List<ProjectDetail> savedDetails = projectDetailRepository.saveAll(newDetails);

        return ResponseEntity.ok(requestDto);
    }


    @Transactional
    public ResponseEntity<?> editProjectDetail(ProjectDetailDto request) {
        ProjectDetail existing = projectDetailRepository.findById(Integer.valueOf(request.getId())).orElse(null);

        Status doneStatus = statusRepository.findById(5).orElse(null);

        existing.setStatus(doneStatus);
        existing.setLink(request.getLink());

        projectDetailRepository.save(existing);

        return ResponseEntity.ok(request);
    }

}
