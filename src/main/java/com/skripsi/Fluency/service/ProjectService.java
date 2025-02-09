package com.skripsi.Fluency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.Fluency.model.dto.ProjectDetailDto;
import com.skripsi.Fluency.model.dto.ProjectHeaderDto;
import com.skripsi.Fluency.model.dto.VerifyLinkDto;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Autowired
    private RestTemplate restTemplate;

    @Value(value = "${base.url}")
    private String baseUrl;

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

    public ResponseEntity<?> getProjectById(String id) {
        ProjectHeader entities = projectHeaderRepository.findById(Integer.valueOf(id)).orElse(null);

        if(entities == null) {
            return ResponseEntity.notFound().build();
        }

        ProjectHeaderDto responseDto = ProjectHeaderDto.builder()
                .id(entities.getId().toString())
                .title(entities.getTitle())
                .description(entities.getDescription())
                .mention(entities.getMention())
                .hashtag(entities.getHashtag())
                .caption(entities.getCaption())
                .brandId(entities.getBrand().getId().toString())
                .influencerId(entities.getInfluencer() == null ? "" : entities.getInfluencer().getId().toString())
                .statusId(entities.getStatus().getId().toString())
                .referenceNumber(entities.getReferenceNumber())
                .projectDetails(
                        getProjectDetailsByHeaderId(entities.getId())
                )
                .build();


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
        System.out.println("========Edit project=======");
        System.out.println(requestDto);
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

//        projectDetailRepository.deleteAll(existing.getProjectDetails());

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

//                if detail id exist -> update, else -> create

                if(item.getId() == null || item.getId().equalsIgnoreCase("")) {
                    return ProjectDetail.builder()
                            .mediaType(mediaType)
                            .nominal(nominal)
                            .link(item.getLink())
                            .deadlineDate(dateDeadline)
                            .deadlineTime(timeDeadline)
                            .instagramMediaId(item.getInstagramMediaId())
                            .note(item.getNote())
                            .status(newStatus)
                            .projectHeader(existing)
                            .build();
                } else {
                    return ProjectDetail.builder()
                            .id(Integer.valueOf(item.getId()))
                            .mediaType(mediaType)
                            .nominal(nominal)
                            .link(item.getLink())
                            .deadlineDate(dateDeadline)
                            .deadlineTime(timeDeadline)
                            .instagramMediaId(item.getInstagramMediaId())
                            .note(item.getNote())
                            .status(newStatus)
                            .projectHeader(existing)
                            .build();
                }

            }
        ).toList();
        newDetails.stream().map(
                item -> {
                    System.out.print(item.getId());
                    return item.getId();
                }
        );
        List<ProjectDetail> savedDetails = projectDetailRepository.saveAll(newDetails);
        savedDetails.stream().map(
                item -> {
                    System.out.print(item.getId());
                    return item.getId();
                }
        );

        return ResponseEntity.ok(requestDto);
    }


    @Transactional
    public ResponseEntity<?> editProjectDetail(ProjectDetailDto request) {
        ProjectDetail existing = projectDetailRepository.findById(Integer.valueOf(request.getId())).orElse(null);

        Status doneStatus = statusRepository.findById(5).orElse(null);

        System.out.println("===========Edit project detail===========");
        System.out.println(request);
        existing.setStatus(doneStatus);
        existing.setLink(request.getLink());
        existing.setInstagramMediaId(request.getInstagramMediaId());

        projectDetailRepository.save(existing);

        return ResponseEntity.ok(request);
    }

//    get all details by header id
    public List<ProjectDetailDto> getProjectDetailsByHeaderId(Integer headerId) {
        ProjectHeader header = projectHeaderRepository.findById(headerId).orElse(null);

        List<ProjectDetail> entities = projectDetailRepository.findByProjectHeaderOrderByDeadlineDateAscDeadlineTimeAsc(header);

        List<ProjectDetailDto> details = entities.stream().map(
                entity -> {
                    ProjectDetailDto newDetail = ProjectDetailDto.builder()
                            .id(entity.getId().toString())
                            .mediatypeId(entity.getMediaType().getId().toString())
                            .note(entity.getNote())
                            .deadlineDate(entity.getDeadlineDate().toString())
                            .deadlineTime(entity.getDeadlineTime().toString())
                            .nominal(entity.getNominal().toString())
                            .link(entity.getLink())
                            .statusId(entity.getStatus().getId().toString())
                            .analyticsLastUpdated(entity.getAnalyticsLastUpdated())
                            .analyticsPicture(entity.getAnalyticsPicture())
                            .analyticsCaption(entity.getAnalyticsCaption())
                            .analyticsLikes(entity.getAnalyticsLikes())
                            .analyticsComments(entity.getAnalyticsComments())
                            .analyticsSaved(entity.getAnalyticsSaved())
                            .analyticsShared(entity.getAnalyticsShared())
                            .analyticsAccountsEngaged(entity.getAnalyticsAccountsEngaged())
                            .analyticsAccountsReached(entity.getAnalyticsAccountsReached())
                            .sentimentPositive(entity.getSentimentPositive())
                            .sentimentNegative(entity.getSentimentNegative())
                            .sentimentNeutral(entity.getSentimentNeutral())
                            .build();
                    return newDetail;
                }
        ).collect(Collectors.toList());

        return details;
    }

//    get single detail by Id
    public ProjectDetailDto getProjectDetail(String detailId) {
        ProjectDetail entity = projectDetailRepository.findById(Integer.valueOf(detailId)).orElse(null);

        if(entity == null) {
            return null;
        }

        String instagramMediaId = entity.getInstagramMediaId();



        ProjectDetailDto responseDto = ProjectDetailDto.builder()
                .id(detailId)
                .mediatypeId(entity.getMediaType().getId().toString())
                .note(entity.getNote())
                .deadlineDate(entity.getDeadlineDate().toString())
                .deadlineTime(entity.getDeadlineTime().toString())
                .nominal(entity.getNominal().toString())
                .link(entity.getLink())
                .statusId(entity.getStatus().getId().toString())
                .instagramMediaId(entity.getInstagramMediaId())
                .analyticsLastUpdated(entity.getAnalyticsLastUpdated())
                .analyticsPicture(entity.getAnalyticsPicture())
                .analyticsCaption(entity.getAnalyticsCaption())
                .analyticsLikes(entity.getAnalyticsLikes())
                .analyticsComments(entity.getAnalyticsComments())
                .analyticsSaved(entity.getAnalyticsSaved())
                .analyticsShared(entity.getAnalyticsShared())
                .analyticsAccountsEngaged(entity.getAnalyticsAccountsEngaged())
                .analyticsAccountsReached(entity.getAnalyticsAccountsReached())
                .sentimentPositive(entity.getSentimentPositive())
                .sentimentNegative(entity.getSentimentNegative())
                .sentimentNeutral(entity.getSentimentNeutral())
                .build();


        return responseDto;
    }

    public VerifyLinkDto findMediaidByLink(String influencerId, String requestLink) {
        Influencer influencer = influencerRepository.findById(Integer.valueOf(influencerId)).orElse(null);

        String trimmedLink = requestLink;
        if(requestLink.endsWith("/")) {
            trimmedLink = requestLink.substring(0, requestLink.length()-1);
        }

        try {
            //            Hit URL API Instagram
            UriComponentsBuilder getMediaUrl = UriComponentsBuilder.fromUriString(baseUrl + "/" + influencer.getInstagramId())
                    .queryParam("fields", "media")
                    .queryParam("access_token", influencer.getToken());

            //            Ambil response
            ResponseEntity<?> mediaResponse = restTemplate.getForEntity(getMediaUrl.toUriString(), String.class);

            //            Ubah response kedalam bentuk JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(String.valueOf(mediaResponse.getBody()));

            //            Ambil data saja
            JsonNode media = jsonNode.get("media");
            JsonNode data = media.get("data");

            for(var item: data) {
// hit ig buat dapetin permalink dari masing2 media id
                System.out.println(item.get("id").asText());
                UriComponentsBuilder getPermalinkUrl = UriComponentsBuilder.fromUriString(baseUrl + "/" + item.get("id").asText())
                        .queryParam("fields", "permalink")
                        .queryParam("access_token", influencer.getToken());

                //            Ambil response
                ResponseEntity<?> permalinkResponse = restTemplate.getForEntity(getPermalinkUrl.toUriString(), String.class);
                JsonNode permalinkJson = mapper.readTree(String.valueOf(permalinkResponse.getBody()));
                System.out.println(permalinkResponse);

                //            Ambil permalink
                String permalink = permalinkJson.get("permalink").asText();

                System.out.println(permalink);

                if(requestLink.contains(trimmedLink)) {
                    return VerifyLinkDto.builder()
                            .mediaId(item.get("id").asText())
                            .link(requestLink)
                            .build();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return VerifyLinkDto.builder()
                .mediaId("")
                .link(requestLink)
                .build();

    }


}
