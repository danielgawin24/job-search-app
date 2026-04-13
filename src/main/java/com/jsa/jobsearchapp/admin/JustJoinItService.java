package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.*;
import com.jsa.jobsearchapp.location.Location;
import com.jsa.jobsearchapp.location.LocationRepository;
import com.jsa.jobsearchapp.request.RequestService;
import com.jsa.jobsearchapp.scraping.ScrapingService;
import com.jsa.jobsearchapp.skill.Skill;
import com.jsa.jobsearchapp.skill.SkillRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class JustJoinItService {

    private final JobOfferRepository jobOfferRepository;
    private final LocationRepository locationRepository;
    private final RequestService requestService;
    private final ScrapingService scrapingService;
    private final SkillRepository skillRepository;
    private final ObjectMapper mapper;

    public JustJoinItService(JobOfferRepository jobOfferRepository, LocationRepository locationRepository, SkillRepository skillRepository, RequestService requestService, ScrapingService scrapingService, ObjectMapper mapper) {
        this.jobOfferRepository = jobOfferRepository;
        this.locationRepository = locationRepository;
        this.requestService = requestService;
        this.scrapingService = scrapingService;
        this.skillRepository = skillRepository;
        this.mapper = mapper;
    }

    @Async
    public CompletableFuture<List<JobOffer>> getJobOffers() {
        List<JobOffer> jobOffers = new ArrayList<>();
        int currentCursor = 0;
        while (true) {
            String justJoinItAPILink = "https://api.justjoin.it/v2/user-panel/offers/by-cursor?currency=pln&from=" + currentCursor + "&itemsCount=100&orderBy=DESC&sortBy=published";
            HttpResponse<String> response = requestService.fetchResponseBody(justJoinItAPILink);
            if (response.body() == null) {
                System.err.println("Response body is null for current cursor: " + currentCursor);
                continue;
            }
            ObjectNode objectNode = (ObjectNode) mapper.readTree(response.body());
            ArrayNode postings = (ArrayNode) objectNode.path("data");
            List<JobOffer> tempJobOffers = new ArrayList<>();
            for (int i = 0; i < postings.size(); i++) {
                JobOffer jobOffer = scrapeOffer(postings.get(i));
                tempJobOffers.add(jobOffer);
            }
            jobOffers.addAll(tempJobOffers);
            try {
                jobOfferRepository.saveAll(tempJobOffers);
                tempJobOffers.clear();
            } catch (Exception e) {
                System.err.println("Failed to save jobOffers list at cursor: " + currentCursor + " in JustJoinItService. Error: " + e.getMessage());
            }
            Object nextCursor = objectNode.path("meta").path("next").path("cursor");
            if (nextCursor == null) {
                break;
            }
            try {
                currentCursor = Integer.parseInt(String.valueOf(nextCursor));
            } catch (Exception e) {
                break;
            }
        }
        return CompletableFuture.completedFuture(jobOffers);
    }

    private JobOffer scrapeOffer(JsonNode offerJson) {
        offerJson.path("requiredSkills").getNodeType();
        String url = "https://www.justjoin.it/job-offer/" + offerJson.path("slug").asString();
        System.out.println("Scraping URL: " + url);
        Optional<JobOffer> jobOffer = jobOfferRepository.findByUrl(url);
        if (jobOffer.isPresent()) {
            return jobOffer.get();
        }
        JobOffer newJobOffer = new JobOffer();
        newJobOffer.setDateAdded(LocalDateTime.now());
        newJobOffer.setUrl(url);
        newJobOffer.setCategory(convertCategoryIdToCategoryName(offerJson.path("categoryId").asInt()));
        newJobOffer.setLocations(convertToLocations(offerJson.path("workplaceType").asString(), offerJson));
        newJobOffer.setSkills(convertToSkills(offerJson));
        newJobOffer.setEmployerName(offerJson.path("companyName").asString());
        newJobOffer.setSeniority(convertToSeniority(offerJson.path("experienceLevel").asString()));
        ArrayNode employmentTypesArray = (ArrayNode) offerJson.path("employmentTypes");
        newJobOffer.setSalary(convertToSalary(employmentTypesArray));
        newJobOffer.setEmploymentType(convertToEmploymentType(offerJson.path("workingTime").asString()));
        newJobOffer.setTypeOfContract(convertToTypeOfContract(employmentTypesArray));
        newJobOffer.setWorkModes(convertToWorkModes(offerJson.path("workplaceType").asString()));
        return newJobOffer;
    }

    private String convertCategoryIdToCategoryName(int categoryId) {
        return getCategoryIdNameMap().get(categoryId);
    }

    private Set<Location> convertToLocations(String workplaceType, JsonNode ObjectNode) {
        Set<Location> locations = new HashSet<>();
        if (workplaceType.equalsIgnoreCase("remote")) {
            Optional<Location> locationOpt = locationRepository.findByAliasName("fullremote");
            Location location = locationOpt.orElseThrow(() ->
                    new EntityNotFoundException("Location 'Full-Remote' not found."));
            locations.add(location);
        } else {
            ArrayNode multilocationArray = (ArrayNode) ObjectNode.path("multilocation");
            List<String> cities = new ArrayList<>();
            for (int i = 0; i < multilocationArray.size(); i++) {
                cities.add(multilocationArray.get(i).path("city").asString());
            }
            for (String city : cities) {
                if (Objects.equals(city, "")) {
                    continue;
                }
                scrapingService.createNewLocationIfNotFoundElseGet(city, locations);
            }
        }
        return locations;
    }

    private Set<Skill> convertToSkills(JsonNode offerJson) {
        Set<Skill> skills = new HashSet<>();
        JsonNode node = offerJson.path("requiredSkills");
        ArrayNode skillsArray = (node != null && node.isArray())
                ? (ArrayNode) node
                : mapper.createArrayNode();
        List<String> skillNames = new ArrayList<>();
        for (int i = 0; i < skillsArray.size(); i++) {
            skillNames.add(skillsArray.get(i).asString());
        }
        for (String skill : skillNames) {
            if (Objects.equals(skill, "") || scrapingService.isSkillInPolish(skill)) {
                continue;
            }
            scrapingService.createNewSkillIfNotFoundElseGet(skill, skills);
        }
        return skills;
    }

    private Seniority convertToSeniority(String experienceLevel) {
        if (experienceLevel == null || experienceLevel.isEmpty()) {
            return Seniority.UNSPECIFIED;
        }
        return scrapingService.convertTextToSeniority(experienceLevel);
    }

    private static Salary convertToSalary(ArrayNode ArrayNode) {
        Salary salary = new Salary(SalaryType.UNSPECIFIED, null, null, null, false);
        List<Boolean> isGrossValues = new ArrayList<>();
        List<Double> salaryValues = new ArrayList<>();
        for (int i = 0; i < ArrayNode.size(); i++) {
            ObjectNode ObjectNode = (ObjectNode) ArrayNode.path(i);
            String salaryPeriodFirstLetter = String.valueOf(ObjectNode.path("unit").asString().toUpperCase().charAt(0));
            List<Double> tempValues;
            try {
                double doubleFrom = ObjectNode.path("fromPln").asInt();
                double doubleTo = ObjectNode.path("toPln").asInt();
                tempValues = new ArrayList<>(List.of(doubleFrom, doubleTo));
            } catch (Exception e) {
                break;
            }
            switch (salaryPeriodFirstLetter) {
                case "H", "G" -> tempValues = tempValues.stream().map(v -> v * 160).toList();
                case "D" -> tempValues = tempValues.stream().map(v -> v * 20).toList();
                case "M" -> {
                }
                case "Y", "R" -> tempValues = tempValues.stream().map(v -> v / 12).toList();
                default ->
                        throw new IllegalArgumentException("Unexpected salary period value: " + salaryPeriodFirstLetter);
            }
            isGrossValues.add(ObjectNode.path("gross").asBoolean());
            salaryValues.addAll(tempValues);
        }
        if (!salaryValues.isEmpty()) {
            salaryValues.sort(Comparator.naturalOrder());
            salary.setType(SalaryType.SPECIFIED);
            salary.setFrom(salaryValues.get(0));
            salary.setTo(salaryValues.get(salaryValues.size() - 1));
            salary.setCurrency("PLN");
            salary.setIsGross(isGrossValues.contains(true));
        }
        return salary;
    }

    private EmploymentType convertToEmploymentType(String workingTime) {
        if (workingTime == null || workingTime.isEmpty()) {
            return EmploymentType.UNSPECIFIED;
        }
        return scrapingService.convertTextToEmploymentType(workingTime);
    }

    private TypeOfContract convertToTypeOfContract(ArrayNode ArrayNode) {
        if (ArrayNode.size() > 1) {
            return TypeOfContract.MULTIPLE;
        }
        String text = ArrayNode.path(0).path("type").asString();
        if (text == null || text.isEmpty()) {
            return TypeOfContract.UNSPECIFIED;
        }
        return scrapingService.convertTextToTypeOfContract(text);
    }

    private WorkModes convertToWorkModes(String workplaceType) {
        WorkModes workModes = new WorkModes();
        switch (workplaceType.toLowerCase()) {
            case "remote" -> workModes.setIsRemote(true);
            case "hybrid" -> workModes.setIsHybrid(true);
            case "office" -> workModes.setIsOnSite(true);
        }
        return workModes;
    }

    private Map<Integer, String> getCategoryIdNameMap() {
        Map<Integer, String> categoryIdNameMap = new HashMap<>();
        categoryIdNameMap.put(1, "JavaScript");
        categoryIdNameMap.put(2, "HTML");
        categoryIdNameMap.put(3, "PHP");
        categoryIdNameMap.put(4, "Ruby");
        categoryIdNameMap.put(5, "Python");
        categoryIdNameMap.put(6, "Java");
        categoryIdNameMap.put(7, "Net");
        categoryIdNameMap.put(8, "Scala");
        categoryIdNameMap.put(9, "C");
        categoryIdNameMap.put(10, "Mobile");
        categoryIdNameMap.put(11, "Testing");
        categoryIdNameMap.put(12, "DevOps");
        categoryIdNameMap.put(13, "Admin");
        categoryIdNameMap.put(14, "UX/UI");
        categoryIdNameMap.put(15, "PM");
        categoryIdNameMap.put(16, "Game");
        categoryIdNameMap.put(17, "Analytics");
        categoryIdNameMap.put(18, "Security");
        categoryIdNameMap.put(19, "Data");
        categoryIdNameMap.put(20, "Go");
        categoryIdNameMap.put(21, "Support");
        categoryIdNameMap.put(22, "ERP");
        categoryIdNameMap.put(23, "Architecture");
        categoryIdNameMap.put(24, "Other");
        categoryIdNameMap.put(25, "AI/ML");
        return categoryIdNameMap;
    }
}
