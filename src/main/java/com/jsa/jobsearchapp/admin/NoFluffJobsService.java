package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.*;
import com.jsa.jobsearchapp.location.Location;
import com.jsa.jobsearchapp.location.LocationRepository;
import com.jsa.jobsearchapp.location.LocationService;
import com.jsa.jobsearchapp.request.RequestService;
import com.jsa.jobsearchapp.scraping.ScrapingService;
import com.jsa.jobsearchapp.skill.Skill;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class NoFluffJobsService {

    private final JobOfferRepository jobOfferRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final RequestService requestService;
    private final ScrapingService scrapingService;
    private final ObjectMapper mapper;

    public NoFluffJobsService(JobOfferRepository jobOfferRepository, LocationRepository locationRepository, LocationService locationService, RequestService requestService, ScrapingService scrapingService, ObjectMapper mapper) {
        this.jobOfferRepository = jobOfferRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.requestService = requestService;
        this.scrapingService = scrapingService;
        this.mapper = mapper;
    }

    @Async
    public CompletableFuture<List<JobOffer>> getJobOffers() {
        List<JobOffer> jobOffers = new ArrayList<>();
        String link = "https://nofluffjobs.com/api/joboffers/main?pageTo=1&pageSize=1000000&salaryCurrency=PLN&salaryPeriod=month&region=pl&language=pl-PL";
        HttpResponse<String> response = requestService.fetchResponseBody(link);
        ObjectNode jsonObject = (ObjectNode) mapper.readTree(response.body());
        ArrayNode postings = (ArrayNode) jsonObject.path("postings");
        Map<String, ObjectNode> postingsByReferenceMap = new LinkedHashMap<>();
        for (int i = 0; i < postings.size(); i++) {
            ObjectNode postingOffer = (ObjectNode) postings.get(i);
            String reference = postingOffer.path("reference").asString();
            postingsByReferenceMap.putIfAbsent(reference, postingOffer);
        }
        for (ObjectNode offerJson : postingsByReferenceMap.values()) {
            String url = "https://nofluffjobs.com/pl/job/" + offerJson.path("url").asString();
            Optional<JobOffer> offerByUrl = jobOfferRepository.findByUrl(url);
            if (url.contains("zabke")) {
                continue;
            }
            if (offerByUrl.isPresent()) {
                JobOffer jobOffer = offerByUrl.get();
                jobOffer.setDateLastSeen(Instant.now());
                jobOfferRepository.save(jobOffer);
            } else {
                JobOffer jobOffer = scrapeOffer(offerJson);
                jobOffers.add(jobOffer);
            }
        }
        try {
            jobOfferRepository.saveAll(jobOffers);
        } catch (Exception e) {
            System.err.println("Failed to save jobOffers list in NoFluffJobsService. Error: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(jobOffers);
    }

    private JobOffer scrapeOffer(ObjectNode offerJson) {
        JobOffer newJobOffer = new JobOffer();
        String url = "https://nofluffjobs.com/pl/job/" + offerJson.path("url").asString();
//        System.out.println("Scraping URL: " + url);
        Instant instant = Instant.now();
        newJobOffer.setDateAdded(instant);
        newJobOffer.setUrl(url);
        newJobOffer.setCategory(offerJson.path("category").asString());
        newJobOffer.setLocations(convertToLocations((ObjectNode) offerJson.path("location")));
        newJobOffer.setSkills(convertToSkills(offerJson.path("tiles")));
        newJobOffer.setSeniority(convertToSeniority((ArrayNode) offerJson.path("seniority")));
        newJobOffer.setSalary(convertToSalary((ObjectNode) offerJson.path("salary")));
        newJobOffer.setEmployerName(offerJson.path("name").asString());
        newJobOffer.setEmploymentType(EmploymentType.UNSPECIFIED);
        newJobOffer.setTypeOfContract(convertToTypeOfContract(offerJson.path("salary").path("type").asString()));
        newJobOffer.setWorkModes(convertToWorkModes(offerJson, url));
        newJobOffer.setDateLastSeen(instant);
        return newJobOffer;
    }

    private Set<Location> convertToLocations(ObjectNode locationJson) {
        Set<Location> locations = new HashSet<>();
        if (locationJson.path("fullyRemote").asBoolean()) {
            Optional<Location> locationOpt = locationRepository.findByAliasName("fullremote");
            Location location = locationOpt.orElseThrow(() ->
                    new EntityNotFoundException("Location 'Full-Remote' not found."));
            locations.add(location);
        } else {
            ArrayNode locationPlaces = (ArrayNode) locationJson.path("places");
            for (int i = 0; i < locationPlaces.size(); i++) {
                List<String> voivodeships = locationService.getAllPolishVoivodeships();
                String city = locationPlaces.get(i).path("city").asString();
                if (voivodeships.contains(city) || Objects.equals(city, "")) {
                    continue;
                }
                scrapingService.createNewLocationIfNotFoundElseGet(city, locations);
            }
        }
        return locations;
    }

    private Set<Skill> convertToSkills(JsonNode tiles) {
        Set<Skill> skills = new HashSet<>();
        ArrayNode valuesArray = (ArrayNode) tiles.path("values");
        List<String> skillNames = new ArrayList<>();
        for (JsonNode pair : valuesArray) {
            String value = pair.path("value").asString();
            String type = pair.path("type").asString();
            if (type.equalsIgnoreCase("requirement")) {
                skillNames.add(value);
            }
        }
        for (String skill : skillNames) {
            if (Objects.equals(skill, "") || scrapingService.isSkillInPolish(skill)) {
                continue;
            }
            scrapingService.createNewSkillIfNotFoundElseGet(skill, skills);
        }
        return skills;
    }

    private Seniority convertToSeniority(ArrayNode seniorityArray) {
        return scrapingService.convertTextToSeniority(seniorityArray.get(0).asString());
    }

    private Salary convertToSalary(ObjectNode salaryObject) {
        Salary salary = new Salary();
        salary.setType(SalaryType.SPECIFIED);
        try {
            salary.setFrom(salaryObject.path("from").asDouble());
        } catch (Exception e) {
            salary.setFrom(null);
        }
        try {
            salary.setTo(salaryObject.path("to").asDouble());
        } catch (Exception e) {
            salary.setTo(null);
        }
        salary.setCurrency(salaryObject.path("currency").asString());
        salary.setIsGross(!salaryObject.path("type").asString().equalsIgnoreCase("b2b"));
        return salary;
    }

    private TypeOfContract convertToTypeOfContract(String text) {
        if (text == null || text.isEmpty()) {
            return TypeOfContract.UNSPECIFIED;
        }
        return scrapingService.convertTextToTypeOfContract(text.toLowerCase());
    }

    private WorkModes convertToWorkModes(ObjectNode node, String url) {
        WorkModes wm = new WorkModes();
        if (node.path("fullyRemote").asBoolean(false) || url.toLowerCase().contains("remote")) {
            return wm.setExclusiveMode("remote");
        }
        return wm.setExclusiveMode("hybrid");
    }
}
