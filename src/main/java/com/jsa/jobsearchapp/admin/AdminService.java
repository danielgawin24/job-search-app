package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.*;
import com.jsa.jobsearchapp.mail.MailService;
import com.jsa.jobsearchapp.offer_history.History;
import com.jsa.jobsearchapp.offer_history.HistoryRepository;
import com.jsa.jobsearchapp.user.User;
import com.jsa.jobsearchapp.user.UserRepository;
import com.jsa.jobsearchapp.userPref.UserPref;
import com.jsa.jobsearchapp.userPref.UserPrefRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AdminService {

    private final BulldogJobsServiceJSON bulldogJobsServiceJSON;
    private final HistoryRepository historyRepository;
    private final JobOfferRepository jobOfferRepository;
    private final JustJoinItService justJoinItService;
    private final MailService mailService;
    private final NoFluffJobsService noFluffJobsService;
    private final UserPrefRepository userPrefRepository;
    private final UserRepository userRepository;

    public AdminService(BulldogJobsServiceJSON bulldogJobsServiceJSON, HistoryRepository historyRepository, JobOfferRepository jobOfferRepository, JustJoinItService justJoinItService, MailService mailService, NoFluffJobsService noFluffJobsService, UserPrefRepository userPrefRepository, UserRepository userRepository) {
        this.bulldogJobsServiceJSON = bulldogJobsServiceJSON;
        this.historyRepository = historyRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.justJoinItService = justJoinItService;
        this.mailService = mailService;
        this.noFluffJobsService = noFluffJobsService;
        this.userPrefRepository = userPrefRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "00 30 01 * * *")
    public String forceImportOffers() {
        CompletableFuture<List<JobOffer>> noFluffJobs = noFluffJobsService.getJobOffers();
        CompletableFuture<List<JobOffer>> bulldogJobs = bulldogJobsServiceJSON.getJobOffers();
        CompletableFuture<List<JobOffer>> justJoinIt = justJoinItService.getJobOffers();
        return "Successfully imported: "
                + "JustJoinIT: " + justJoinIt.join().size()
                + ". NoFluffJobs: " + noFluffJobs.join().size()
                + ". BulldogJobs: " + bulldogJobs.join().size();
    }

    @Scheduled(cron = "00 00 03 * * *")
    public String forceSendOffers() {
        /*
         Input: allUsers
         TASK A: get user preferences and validate they are not null/empty.
         input: one user
         output: userPref object/null
         if null, we skip the user completely
         if exists, continue pipeline
         TASK B: get maxScore from separate method which already checks for wrong values.
         input: userPref ID
         output: integer of max score
         if null/0 we skip the user completely
         if bigger than 0, continue pipeline
         TASK C: get offers filtered by maxScore and user preferences.
         input: userPref, maxScore
         output: List of Offers
         if size() == 0 we don't go further
         if size >= 1, continue pipeline
         TASK D: write email contents with offer URLs
         input: List of Offers
         output: string containing message
         if ==null/empty/"" we don't send the message
         else, send message, continue pipeline
         TASK E: if email is sent, add offers to history
         input: statusCode of email response
         output: saved history list
         if save is error i don't know

         */
        List<User> allUsers = userRepository.findAll();
        List<String> errorUsernames = new ArrayList<>();
        for (User user : allUsers) {
            System.out.println("================================");
            Optional<UserPref> userPrefOptional = userPrefRepository.findByUser(user);
            if (userPrefOptional.isPresent()) {
                System.out.println("USER_PREF PRESENT: " + user.getUsername());
                UserPref userPref = userPrefOptional.get();
                Integer maxScore = userPrefRepository.calculateMaxScoreByUserPref(userPref.getId());
                System.out.println("MAX_SCORE: " + maxScore + ". USERNAME: " + user.getUsername());
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


                System.out.println("ALL_OFFERS: " + allOffersByUserPref + ". USERNAME: " + user.getUsername());
                List<History> historyList = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                sb.append("Hello,\nHere are some offers we found for you:\n");
                for (String url : allOffersByUserPref) {
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
                } else {
                    System.out.println("ERROR: MAIL_NOT_SENT: " + response.body());
                }
            } else {
                errorUsernames.add(user.getUsername());
            }
        }
        return "Successfully sent: " + (allUsers.size() - errorUsernames.size()) + ". Not sent by error: " + errorUsernames;
    }

    public String forceSendOffers2() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            Integer userId = user.getId();
            Optional<UserPref> userPrefOptional = userPrefRepository.findByUser(user);
            if (userPrefOptional.isEmpty()) {
                continue;
            }

            UserPref userPref = userPrefOptional.get();
            Integer maxScore = userPrefRepository.calculateMaxScoreByUserPref(userPref.getId());
            if (maxScore == null || maxScore == 0) {
                continue;
            }
            System.out.println("MAX_SCORE: " + maxScore + ". USERNAME: " + user.getUsername());

            WorkModes workModes = userPref.getWorkModes();
            List<String> allOfferUrlsByUserPref = fetchOffersByUserPref(userPref, workModes, userId, maxScore);
            if (allOfferUrlsByUserPref.isEmpty()) {
                System.err.println("EMPTY FOR USERNAME: " + user.getUsername());
                continue;
            }
            System.out.println("ALL_OFFERS: " + allOfferUrlsByUserPref + ". USERNAME: " + user.getUsername());

            String emailContents = generateEmailContents(allOfferUrlsByUserPref, userId);
            if (emailContents.isEmpty()) {
                System.err.println("EMAIL_EMPTY for user: " + user.getUsername());
                continue;
            }

            HttpResponse<String> response = mailService.sendSimpleMailAPI(
                    "Job offers for " + user.getUsername(),
                    emailContents
            );
            if (response.statusCode() == 200) {
                historyRepository.saveAll(generateHistoryList(allOfferUrlsByUserPref, userId));
            } else {
                System.out.println("ERROR: MAIL_NOT_SENT: " + response.body());
            }
        }
        return "no stack overflow";
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

    private List<String> fetchOffersByUserPref(UserPref userPref, WorkModes workModes, Integer userId, Integer maxScore) {
        return jobOfferRepository.findAllUrlsByUserPref(
                        userPref.getId(),
                        userPref.getIsNoLocationPref(),
                        userPref.getCity(),
                        userPref.getSeniority().name(),
                        userPref.getSalaryFrom(),
                        workModes.getIsRemote(),
                        workModes.getIsHybrid(),
                        workModes.getIsOnSite(),
                        userId,
                        maxScore
                ).stream()
                .map(OfferMatchProjection::getUrl)
                .toList();
    }

    private String generateEmailContents(List<String> allOfferUrlsByUserPref, Integer userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello,\nHere are some offers we found for you:\n");
        for (String url : allOfferUrlsByUserPref) {
            sb.append(url).append("\n");
        }
        sb.append("Thank you for using JSA (JobSearchApp).");
        return sb.toString();
    }

    private List<History> generateHistoryList(List<String> urls, Integer userId) {
        List<History> historyList = new ArrayList<>();
        for (String url : urls) {
            History history = new History();
            history.setId(userId);
            history.setUrl(url);
            historyList.add(history);
        }
        return historyList;
    }
}
