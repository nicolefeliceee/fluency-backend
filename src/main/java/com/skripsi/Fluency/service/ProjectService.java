package com.skripsi.Fluency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skripsi.Fluency.model.dto.ProjectDetailDto;
import com.skripsi.Fluency.model.dto.ProjectHeaderDto;
import com.skripsi.Fluency.model.dto.SentimentAnalysisDto;
import com.skripsi.Fluency.model.dto.VerifyLinkDto;
import com.skripsi.Fluency.model.entity.*;
import com.skripsi.Fluency.repository.*;
import jakarta.transaction.Transactional;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value(value = "${sentiment.analysis.url}")
    private String sentimentUrl;

    @Value(value = "${sentiment.analysis.comments.limit}")
    private String commentLimit;

    public ResponseEntity<?> getProject(String statusId, String userId, String title) {
        if(title != null) {
            title = title.trim();
        }

        List<ProjectHeader> entities = new ArrayList<>();
        User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);
        if((statusId == null || statusId.isEmpty()) && (title == null || title.isEmpty())) {
            entities = projectHeaderRepository.findAll();
        } else if(title == null || title.isEmpty()){
            Status status =  statusRepository.findById(Integer.valueOf(statusId)).orElse(null);
            Brand brand;
            Influencer influencer;
            if(user.getUserType().equalsIgnoreCase("brand")) {
                brand = brandRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByStatusAndBrandOrderByIdDesc(status, brand);
            } else if(user.getUserType().equalsIgnoreCase(("influencer"))) {
                influencer = influencerRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByStatusAndInfluencerOrderByIdDesc(status, influencer);
            }
        } else if(statusId == null || statusId.isEmpty()) {
            Brand brand;
            Influencer influencer;
            if(user.getUserType().equalsIgnoreCase("brand")) {
                brand = brandRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByBrandAndTitleContainingIgnoreCaseOrderByIdDesc(brand, title);
            } else if(user.getUserType().equalsIgnoreCase(("influencer"))) {
                influencer = influencerRepository.findByUser(user);
                entities = projectHeaderRepository.findAllByInfluencerAndTitleContainingIgnoreCaseOrderByIdDesc(influencer, title);
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
                                                .instagramMediaId(itemDetail.getInstagramMediaId())
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
                    return item.getId();
                }
        );
        List<ProjectDetail> savedDetails = projectDetailRepository.saveAll(newDetails);
        savedDetails.stream().map(
                item -> {
                    return item.getId();
                }
        );

        return ResponseEntity.ok(requestDto);
    }


    @Transactional
    public ResponseEntity<?> editProjectDetail(ProjectDetailDto request) {
        ProjectDetail existing = projectDetailRepository.findById(Integer.valueOf(request.getId())).orElse(null);

        Status doneStatus = statusRepository.findById(5).orElse(null);
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
                            .instagramMediaId(entity.getInstagramMediaId())
                            .mediatypeId(entity.getMediaType().getId().toString())
                            .note(entity.getNote())
                            .deadlineDate(entity.getDeadlineDate().toString())
                            .deadlineTime(entity.getDeadlineTime().toString())
                            .nominal(entity.getNominal().toString())
                            .link(entity.getLink())
                            .statusId(entity.getStatus().getId().toString())
                            .analyticsLastUpdated(entity.getAnalyticsLastUpdated())
                            .analyticsMediaUrl(entity.getMediaUrl())
                            .analyticsCaption(entity.getAnalyticsCaption())
                            .analyticsLikes(formatFollowers(entity.getAnalyticsLikes() == null? 0 : entity.getAnalyticsLikes()))
                            .analyticsComments(formatFollowers(entity.getAnalyticsComments() == null? 0 : entity.getAnalyticsComments()))
                            .analyticsSaved(formatFollowers(entity.getAnalyticsSaved() == null? 0 : entity.getAnalyticsSaved()))
                            .analyticsShared(formatFollowers(entity.getAnalyticsShared() == null? 0 : entity.getAnalyticsShared()))
                            .analyticsAccountsEngaged(formatFollowers(entity.getAnalyticsAccountsEngaged() == null? 0 : entity.getAnalyticsAccountsEngaged()))
                            .analyticsAccountsReached(formatFollowers(entity.getAnalyticsAccountsReached() == null? 0 : entity.getAnalyticsAccountsReached()))
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
                .analyticsMediaUrl(entity.getMediaUrl())
                .analyticsCaption(entity.getAnalyticsCaption())
                .analyticsLikes(formatFollowers(entity.getAnalyticsLikes()))
                .analyticsComments(formatFollowers(entity.getAnalyticsComments()))
                .analyticsSaved(formatFollowers(entity.getAnalyticsSaved()))
                .analyticsShared(formatFollowers(entity.getAnalyticsShared()))
                .analyticsAccountsEngaged(formatFollowers(entity.getAnalyticsAccountsEngaged()))
                .analyticsAccountsReached(formatFollowers(entity.getAnalyticsAccountsReached()))
                .sentimentPositive(entity.getSentimentPositive())
                .sentimentNegative(entity.getSentimentNegative())
                .sentimentNeutral(entity.getSentimentNeutral())
                .build();



        return responseDto;
    }

    public VerifyLinkDto findMediaidByLink(String influencerId, String requestLink) {
        Influencer influencer = influencerRepository.findById(Integer.valueOf(influencerId)).orElse(null);

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
                UriComponentsBuilder getPermalinkUrl = UriComponentsBuilder.fromUriString(baseUrl + "/" + item.get("id").asText())
                        .queryParam("fields", "permalink")
                        .queryParam("access_token", influencer.getToken());

                //            Ambil response
                ResponseEntity<?> permalinkResponse = restTemplate.getForEntity(getPermalinkUrl.toUriString(), String.class);
                JsonNode permalinkJson = mapper.readTree(String.valueOf(permalinkResponse.getBody()));

                //            Ambil permalink
                String permalink = permalinkJson.get("permalink").asText();

                String trimmedLink = permalink;
                if(permalink.endsWith("/")) {
                    trimmedLink = permalink.substring(0, permalink.length()-1);
                }

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

    public ResponseEntity<?> deleteProjectHeader(String id) {
        try {
            projectHeaderRepository.deleteById(Integer.valueOf(id));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(id);
    }

    @Transactional
    public ProjectDetailDto getPerformanceAnalytics(String detailId) {

        ProjectDetailDto responseDto = null;
        try {
            ProjectDetail entity = projectDetailRepository.findById(Integer.valueOf(detailId)).orElse(null);

            if(entity == null) {
                return null;
            }

            Influencer influencer = entity.getProjectHeader().getInfluencer();

            //        hit ig
            String instagramMediaId = entity.getInstagramMediaId();

            ObjectMapper mapper = new ObjectMapper();

            //            Hit URL API Instagram
            UriComponentsBuilder getMediaUrl = UriComponentsBuilder.fromUriString(baseUrl + "/" + instagramMediaId)
                    .queryParam("fields", "media_url,caption,comments_count")
                    .queryParam("access_token", influencer.getToken());

            //            Ambil response
            ResponseEntity<?> mediaResponse = restTemplate.getForEntity(getMediaUrl.toUriString(), String.class);

            //            Hit URL API Instagram
            UriComponentsBuilder getInsightUrl = UriComponentsBuilder.fromUriString(baseUrl + "/" + instagramMediaId + "/insights")
                    .queryParam("metric", "likes,shares,saved,reach,views")
                    .queryParam("access_token", influencer.getToken());

            //            Ambil response
            ResponseEntity<?> insightResponse = restTemplate.getForEntity(getInsightUrl.toUriString(), String.class);

            //        untuk get username influencer
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/" + influencer.getInstagramId())
                    .queryParam("fields", "username")
                    .queryParam("access_token", influencer.getToken());

//            Ambil response
            ResponseEntity<?> response = restTemplate.getForEntity(builder.toUriString(), String.class);

            JsonNode influencerJson = mapper.readTree(String.valueOf(response.getBody()));
            String username = influencerJson.get("username").asText();

//            declare field2 analytics
//            from media
            String mediaUrl;
            String caption;
            String commentsCount;
            JsonNode comments;
            String positive;
            String negative;
            String neutral;

//            from insight
            String likes = "";
            String shared = "";
            String saved = "";
            String reach = "";
            String acctEngaged = "";
            String views = "";

            if(mediaResponse.getStatusCode().is2xxSuccessful() && insightResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode mediaJson = mapper.readTree(String.valueOf(mediaResponse.getBody()));
                JsonNode insightJson = mapper.readTree(String.valueOf(insightResponse.getBody())).get("data");

//                ambil data
                mediaUrl = mediaJson.get("media_url").asText();
                caption = mediaJson.get("caption").asText();
                commentsCount = mediaJson.get("comments_count").asText();

//                for insight
                for (JsonNode item:insightJson) {
                    if(item.get("name").asText().equalsIgnoreCase("likes")) {
                        likes = item.get("values").get(0).get("value").asText();
                    } else if(item.get("name").asText().equalsIgnoreCase("shares")) {
                        shared = item.get("values").get(0).get("value").asText();
                    } else if(item.get("name").asText().equalsIgnoreCase("saved")) {
                        saved = item.get("values").get(0).get("value").asText();
                    } else if(item.get("name").asText().equalsIgnoreCase("views")) {
                        views = item.get("values").get(0).get("value").asText();
                    } else if(item.get("name").asText().equalsIgnoreCase("reach")) {
                        reach = item.get("values").get(0).get("value").asText();
                    }
                }

//               temp
                acctEngaged = views;

//                simpan ke db
                entity.setAnalyticsCaption(caption);
                entity.setMediaUrl(mediaUrl);
                entity.setAnalyticsComments(Integer.valueOf(commentsCount.isEmpty()? "0" : commentsCount));
                entity.setAnalyticsLikes(Integer.valueOf(likes.isEmpty()? "0" : likes));
                entity.setAnalyticsShared(Integer.valueOf(shared.isEmpty()? "0" : shared));
                entity.setAnalyticsSaved(Integer.valueOf(saved.isEmpty()? "0" : saved));
                entity.setAnalyticsAccountsReached(Integer.valueOf(reach.isEmpty()? "0" : reach));
                entity.setAnalyticsAccountsEngaged(Integer.valueOf(acctEngaged .isEmpty()? "0" :acctEngaged));

                projectDetailRepository.save(entity);

            } else {
//                balikin dari db lgsg

                mediaUrl = entity.getMediaUrl();
                caption = entity.getAnalyticsCaption();
                likes = entity.getAnalyticsLikes().toString();
                shared = entity.getAnalyticsShared().toString();
                saved = entity.getAnalyticsSaved().toString();
                commentsCount = entity.getAnalyticsComments().toString();
                acctEngaged = entity.getAnalyticsAccountsEngaged().toString();
                reach = entity.getAnalyticsAccountsReached().toString();
            }

//            terakhir baru balikin
            responseDto = ProjectDetailDto.builder()
                    .id(detailId)
                    .mediatypeId(entity.getMediaType().getId().toString())
                    .note(entity.getNote())
                    .deadlineDate(entity.getDeadlineDate().toString())
                    .deadlineTime(entity.getDeadlineTime().toString())
                    .nominal(entity.getNominal().toString())
                    .link(entity.getLink())
                    .statusId(entity.getStatus().getId().toString())
                    .instagramMediaId(entity.getInstagramMediaId())
                    .influencerUsername(username)
                    .analyticsLastUpdated(entity.getAnalyticsLastUpdated())
                    .analyticsMediaUrl(mediaUrl)
                    .analyticsCaption(caption)
                    .analyticsLikes(formatFollowers(Integer.valueOf(likes)))
                    .analyticsComments(formatFollowers(Integer.valueOf(commentsCount)))
                    .analyticsSaved(formatFollowers(Integer.valueOf(saved)))
                    .analyticsShared(formatFollowers(Integer.valueOf(shared)))
                    .analyticsAccountsEngaged(formatFollowers(Integer.valueOf(acctEngaged)))
                    .analyticsAccountsReached(formatFollowers(Integer.valueOf(reach)))
                    .sentimentPositive(entity.getSentimentPositive())
                    .sentimentNegative(entity.getSentimentNegative())
                    .sentimentNeutral(entity.getSentimentNeutral())
                    .build();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        return responseDto;
    }

    public ResponseEntity<?> getSentimentAnalysis(String projectDetailId) {
        ProjectDetail entity = projectDetailRepository.findById(Integer.valueOf(projectDetailId)).orElse(null);

        SentimentAnalysisDto responseDto;

        if(entity == null) {
            return ResponseEntity.notFound().build();
        }

        Influencer influencer = influencerRepository.findById(entity.getProjectHeader().getInfluencer().getId()).orElse(null);

        ObjectMapper mapper = new ObjectMapper();

        try {
            //            Hit URL API Instagram
            UriComponentsBuilder getCommentsUrl = UriComponentsBuilder.fromUriString(baseUrl + "/" + entity.getInstagramMediaId() + "/comments")
                    .queryParam("fields", "text,username,like_count,timestamp")
                    .queryParam("limit", commentLimit)
                    .queryParam("access_token", influencer.getToken());

            //            Ambil response
            ResponseEntity<?> commmentResponse = restTemplate.getForEntity(getCommentsUrl.toUriString(), String.class);

            JsonNode commentJson = mapper.readTree(String.valueOf(commmentResponse.getBody()));
            JsonNode comments = commentJson.get("data");

            responseDto = calculateSentimentAnalysis(comments);
        } catch(Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(responseDto);
    }

    public SentimentAnalysisDto calculateSentimentAnalysis(JsonNode comments) {

        Integer commentsCount = 0;
        Integer sentimentPositiveCount = 0;
        Integer sentimentNegativeCount = 0;
        Integer sentimentNeutralCount = 0;
        List<HashMap<String, String>> topComments = new ArrayList<>();

        Integer max1 = Integer.MIN_VALUE;
        Integer max2 = Integer.MIN_VALUE;
        Integer max3 = Integer.MIN_VALUE;

        HashMap<String, String> comment1 = new HashMap<>();
        HashMap<String, String> comment2 = new HashMap<>();
        HashMap<String, String> comment3 = new HashMap<>();

        List<String> commentsText = new ArrayList<>();

//        bikin array of comments & get top comments
        for(JsonNode comment: comments) {
            commentsText.add(comment.get("text").asText());
            commentsCount++;

            if(comment.get("like_count").asInt() > max1) {
                max1 = comment.get("like_count").asInt();
                comment1.clear();
                comment1.put("username", comment.get("username").asText());
                comment1.put("text", comment.get("text").asText());
                comment1.put("like_count", comment.get("like_count").asText());
                String commmentTime = getCommentTime(comment.get("timestamp").asText());
                comment1.put("comment_time", commmentTime);
            } else if(comment.get("like_count").asInt() > max2) {
                max2 = comment.get("like_count").asInt();
                comment2.clear();
                comment2.put("username", comment.get("username").asText());
                comment2.put("text", comment.get("text").asText());
                comment2.put("like_count", comment.get("like_count").asText());
                String commmentTime = getCommentTime(comment.get("timestamp").asText());
                comment2.put("comment_time", commmentTime);
            } else if(comment.get("like_count").asInt() > max3) {
                max3 = comment.get("like_count").asInt();
                comment3.clear();
                comment3.put("username", comment.get("username").asText());
                comment3.put("text", comment.get("text").asText());
                comment3.put("like_count", comment.get("like_count").asText());
                String commmentTime = getCommentTime(comment.get("timestamp").asText());
                comment3.put("comment_time", commmentTime);
            }
        }

        try {

//        hit api
            Map<String, List<String>> request = new HashMap<>();
            request.put("comments", commentsText);

            ObjectMapper mapper = new ObjectMapper();
            ResponseEntity<?> response = restTemplate.postForEntity(sentimentUrl, request, String.class);
            JsonNode sentimentJson = mapper.readTree(String.valueOf(response.getBody()));
            JsonNode sentiments = sentimentJson.get("sentiments");

//        loop itung total
            for(JsonNode sentiment: sentiments) {
                if(sentiment.get("label").asText().contains("5") || sentiment.get("label").asText().contains("4")) {
                    sentimentPositiveCount++;
                } else if(sentiment.get("label").asText().contains("3")) {
                    sentimentNeutralCount++;
                } else if(sentiment.get("label").asText().contains("2") || sentiment.get("label").asText().contains("2")) {
                    sentimentNegativeCount++;
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

        if(!comment1.isEmpty()) {
            topComments.add(comment1);
        }
        if(!comment2.isEmpty()) {
            topComments.add(comment2);
        }
        if(!comment3.isEmpty()) {
            topComments.add(comment3);
        }

        SentimentAnalysisDto responseDto = SentimentAnalysisDto.builder()
                .sentimentNegative(sentimentNegativeCount.toString())
                .sentimentNeutral(sentimentNeutralCount.toString())
                .sentimentPositive(sentimentPositiveCount.toString())
                .topComments(topComments)
                .build();

        return responseDto;
    }

    public static String formatFollowers(int followers) {
        if (followers >= 1_000_000) {
            double value = followers / 1_000_000.0;
            return (value % 1 == 0) ? ((int) value + "M") : String.format("%.1fM", value);
        } else if (followers >= 1_000) {
            double value = followers / 1_000.0;
            return (value % 1 == 0) ? ((int) value + "k") : String.format("%.1fk", value);
        }
        return String.valueOf(followers);
    }

    public static String getCommentTime(String timestamp) {
        if (timestamp.endsWith("+0000") || timestamp.endsWith("-0000")) {
            timestamp = timestamp.substring(0, timestamp.length() - 5) + "+00:00";
        }

        OffsetDateTime pastTime = OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime now = OffsetDateTime.now();
        Duration duration = Duration.between(pastTime, now);

        long seconds = duration.getSeconds();
        if (seconds < 60) return seconds + "s";

        long minutes = duration.toMinutes();
        if (minutes < 60) return minutes + "m";

        long hours = duration.toHours();
        if (hours < 24) return hours + "h";

        long days = duration.toDays();
        if (days < 7) return days + "d";

        long weeks = days / 7;
        return weeks + "w";
    }


}
