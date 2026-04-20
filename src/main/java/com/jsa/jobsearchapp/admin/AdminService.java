package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.JobOffer;
import com.jsa.jobsearchapp.jobOffer.JobOfferRepository;
import com.jsa.jobsearchapp.jobOffer.OfferMatchProjection;
import com.jsa.jobsearchapp.jobOffer.WorkModes;
import com.jsa.jobsearchapp.mail.MailService;
import com.jsa.jobsearchapp.offer_history.History;
import com.jsa.jobsearchapp.offer_history.HistoryRepository;
import com.jsa.jobsearchapp.scraping.ScrapingService;
import com.jsa.jobsearchapp.user.User;
import com.jsa.jobsearchapp.user.UserRepository;
import com.jsa.jobsearchapp.userPref.UserPref;
import com.jsa.jobsearchapp.userPref.UserPrefRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final ScrapingService scrapingService;

    public AdminService(BulldogJobsServiceJSON bulldogJobsServiceJSON, HistoryRepository historyRepository, JobOfferRepository jobOfferRepository, JustJoinItService justJoinItService, MailService mailService, NoFluffJobsService noFluffJobsService, UserPrefRepository userPrefRepository, UserRepository userRepository, ScrapingService scrapingService) {
        this.bulldogJobsServiceJSON = bulldogJobsServiceJSON;
        this.historyRepository = historyRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.justJoinItService = justJoinItService;
        this.mailService = mailService;
        this.noFluffJobsService = noFluffJobsService;
        this.userPrefRepository = userPrefRepository;
        this.userRepository = userRepository;
        this.scrapingService = scrapingService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public String forceImportOffers() {
        CompletableFuture<List<JobOffer>> bulldogJobs = bulldogJobsServiceJSON.getJobOffers();
        CompletableFuture<List<JobOffer>> justJoinIt = justJoinItService.getJobOffers();
        CompletableFuture<List<JobOffer>> noFluffJobs = noFluffJobsService.getJobOffers();
        List<JobOffer> allOffers = jobOfferRepository.findAll();
        for (JobOffer offer : allOffers) {
            scrapingService.validateShouldOfferBeMarkedAsInactive(offer);
        }
        return "Successfully imported: "
                + "JustJoinIT: " + justJoinIt.join().size()
                + ". NoFluffJobs: " + noFluffJobs.join().size()
                + ". BulldogJobs: " + bulldogJobs.join().size();
    }

    @Scheduled(cron = "0 15 4 * * *")
    public String forceSendOffers() {
        List<User> allUsers = userRepository.findAll();
        List<String> usernamesEmailSent = new ArrayList<>();
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
            WorkModes workModes = userPref.getWorkModes();
            List<String> allOfferUrlsByUserPref = fetchOffersByUserPref(userPref, workModes, userId, maxScore);
            if (allOfferUrlsByUserPref.isEmpty()) {
                continue;
            }
            String emailContents = generateEmailContents(allOfferUrlsByUserPref);
            HttpResponse<String> response = mailService.sendSimpleMailAPI(
                    "Job offers for " + user.getUsername(),
                    emailContents
            );
            if (response.statusCode() == 200) {
                usernamesEmailSent.add(user.getUsername());
                List<History> historyList = generateHistoryList(allOfferUrlsByUserPref, user);
                historyRepository.saveAll(historyList);
            } else {
                return response.body();
            }
        }
        return "Offers sent for users: " + usernamesEmailSent;
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

    private String generateEmailContents(List<String> allOfferUrlsByUserPref) {
        String contents = "Hello,\\nHere are some offers we found for you:\\n";
        for (String url : allOfferUrlsByUserPref) {
            contents += url + "\\n";
        }
        contents += ("\\nThank you for using JSA (JobSearchApp).");
        return contents;
    }

    private List<History> generateHistoryList(List<String> urls, User user) {
        List<History> historyList = new ArrayList<>();
        for (String url : urls) {
            History history = new History();
            history.setUserId(user);
            history.setUrl(url);
            historyList.add(history);
        }
        return historyList;
    }
}
