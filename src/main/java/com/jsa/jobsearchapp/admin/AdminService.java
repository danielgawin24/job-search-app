package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.*;
import com.jsa.jobsearchapp.location.LocationRepository;
import com.jsa.jobsearchapp.mail.MailService;
import com.jsa.jobsearchapp.offer_history.History;
import com.jsa.jobsearchapp.offer_history.HistoryRepository;
import com.jsa.jobsearchapp.request.RequestService;
import com.jsa.jobsearchapp.skill.SkillRepository;
import com.jsa.jobsearchapp.user.User;
import com.jsa.jobsearchapp.user.UserRepository;
import com.jsa.jobsearchapp.userPref.UserPref;
import com.jsa.jobsearchapp.userPref.UserPrefRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AdminService {

    private final BulldogJobsServiceJSON bulldogJobsServiceJSON;
    private final HistoryRepository historyRepository;
    private final JobOfferRepository jobOfferRepository;
    private final JustJoinItService justJoinItService;
    private final LocationRepository locationRepository;
    private final MailService mailService;
    private final NoFluffJobsService noFluffJobsService;
    private final RequestService requestService;
    private final SkillRepository skillRepository;
    private final UserPrefRepository userPrefRepository;
    private final UserRepository userRepository;

    public AdminService(BulldogJobsServiceJSON bulldogJobsServiceJSON, HistoryRepository historyRepository, JobOfferRepository jobOfferRepository, JustJoinItService justJoinItService, LocationRepository locationRepository, MailService mailService, NoFluffJobsService noFluffJobsService, RequestService requestService, SkillRepository skillRepository, UserPrefRepository userPrefRepository, UserRepository userRepository) {
        this.bulldogJobsServiceJSON = bulldogJobsServiceJSON;
        this.historyRepository = historyRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.justJoinItService = justJoinItService;
        this.locationRepository = locationRepository;
        this.mailService = mailService;
        this.noFluffJobsService = noFluffJobsService;
        this.requestService = requestService;
        this.skillRepository = skillRepository;
        this.userPrefRepository = userPrefRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "00 30 01 * * *")
    public String forceImportOffers() {
        //* 20:38:02

        // delete all offers every single time, set AUTO_INCREMENT default 1? (SQL query in the console_1)

        //TODO

        CompletableFuture<List<JobOffer>> noFluffJobs = noFluffJobsService.getJobOffers();
        CompletableFuture<List<JobOffer>> bulldogJobs = bulldogJobsServiceJSON.getJobOffers();
        CompletableFuture<List<JobOffer>> justJoinIt = justJoinItService.getJobOffers();

        return "Succesfully imported: "
                + "JustJoinIT: " + justJoinIt.join().size()
                + ". NoFluffJobs: " + noFluffJobs.join().size()
                + ". BulldogJobs: " + bulldogJobs.join().size();
    }

    @Scheduled(cron = "00 00 03 * * *")
    public String forceSendOffers() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            UserPref userPref = userPrefRepository.findByUser(user)
                    .orElseThrow(() -> new EntityNotFoundException("User pref not found: " + user.getUsername()));

            Integer maxScore = userPrefRepository.calculateMaxScoreByUserPref(userPref.getId());
            if (maxScore == null) {
                throw new EntityNotFoundException("User pref not found");
            }

            WorkModes userPrefWorkModes = userPref.getWorkModes();
            List<String> allOffersByUserPref = jobOfferRepository.findAllUrlsByUserPref(
                            userPref.getId(),
                            userPref.getIsNoLocationPref(),
                            userPref.getCity(),
                            userPref.getSeniority().name(),
                            userPref.getSalaryFrom(),
                            userPrefWorkModes.getIsRemote(),
                            userPrefWorkModes.getIsHybrid(),
                            userPrefWorkModes.getIsOnSite(),
                            user.getId(),
                            maxScore
                    ).stream()
                    .map(OfferMatchProjection::getUrl)
                    .toList();

            List<History> historyList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("Hello,\nHere are some offers we found for you:\n");
            for (
                    String url : allOffersByUserPref) {
                History history = new History();
                history.setUserId(user);
                history.setUrl(url);

                historyList.add(history);
                sb.append(url).append("\n");
            }
            sb.append("Thank you for using job search app!");

            HttpResponse<String> response = mailService.sendSimpleMailAPI(
                    "Job offers for " + user.getUsername(),
                    sb.toString()
            );

            if (response.statusCode() == 200) {
                historyRepository.saveAll(historyList);
            }
        }
        return "All emails sent correctly.";
    }

    public Map<String, Integer> findAllByUserPrefOld(UserPref userPref) {
        WorkModes workModes = userPref.getWorkModes();
        List<OfferMatchProjectionOld> allByUserPref = jobOfferRepository.findAllByUserPrefOld(
                userPref.getSeniority().name(),
                userPref.getSalaryFrom(),
                userPref.getSalaryTo(),
                userPref.getEmploymentType().name(),
                userPref.getTypeOfContract().name(),
                workModes.getIsRemote(),
                workModes.getIsHybrid(),
                workModes.getIsOnSite(),
                userPref.getCity()
        );
        Map<String, Integer> offerUrlToMatchingCountMap = new HashMap<>();
        for (OfferMatchProjectionOld matchProjection : allByUserPref) {
            Integer matchCount = matchProjection.getMatchCount();
            if (matchCount >= 3) {
                offerUrlToMatchingCountMap.put(matchProjection.getUrl(), matchCount);
            }
        }
        return offerUrlToMatchingCountMap;
    }

    public List<String> findAllUrlsByUserPref(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserPref userPref = userPrefRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("User pref not found: " + user.getUsername()));
        Integer maxScore = userPrefRepository.calculateMaxScoreByUserPref(userPref.getId());
        if (maxScore == null) {
            throw new EntityNotFoundException("User pref not found");
        }
        WorkModes userPrefWorkModes = userPref.getWorkModes();
        return jobOfferRepository.findAllUrlsByUserPref(
                        userPref.getId(),
                        userPref.getIsNoLocationPref(),
                        userPref.getCity(),
                        userPref.getSeniority().name(),
                        userPref.getSalaryFrom(),
                        userPrefWorkModes.getIsRemote(),
                        userPrefWorkModes.getIsHybrid(),
                        userPrefWorkModes.getIsOnSite(),
                        user.getId(),
                        maxScore
                ).stream()
                .map(OfferMatchProjection::getUrl)
                .toList();
    }

//    public Set<String> getNoFluffUnsavedSKills() {
//        Set<String> unsavedSkills = new LinkedHashSet<>();
//        Map<String, JSONObject> postingsByReferenceMap = noFluffJobsService.getReferencePostingMap();
//        int count = 1;
//        for (Map.Entry<String, JSONObject> stringJSONObjectEntry : postingsByReferenceMap.entrySet()) {
//            JSONObject offerJson = stringJSONObjectEntry.getValue();
//            String url = "https://nofluffjobs.com/pl/job/" + offerJson.getString("url");
//            unsavedSkills.addAll(scrapingService.noFluffGetUnsavedSkills(url));
//            count++;
//        }
//        return unsavedSkills;

//    }
//
//    public Set<String> getJustJoinItUnsavedSKills() {
//        return new LinkedHashSet<>(justJoinItService.getOfferUrls());
//    }
}
