package com.jsa.jobsearchapp.admin;

import com.jsa.jobsearchapp.jobOffer.*;
import com.jsa.jobsearchapp.location.Location;
import com.jsa.jobsearchapp.location.LocationRepository;
import com.jsa.jobsearchapp.request.RequestService;
import com.jsa.jobsearchapp.scraping.ScrapingService;
import com.jsa.jobsearchapp.skill.Skill;
import jakarta.persistence.EntityNotFoundException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class BulldogJobsServiceJSON {

    private final JobOfferRepository jobOfferRepository;
    private final LocationRepository locationRepository;
    private final RequestService requestService;
    private final ScrapingService scrapingService;
    private final ObjectMapper mapper;

    public BulldogJobsServiceJSON(JobOfferRepository jobOfferRepository, LocationRepository locationRepository, RequestService requestService, ScrapingService scrapingService, ObjectMapper mapper) {
        this.jobOfferRepository = jobOfferRepository;
        this.locationRepository = locationRepository;
        this.requestService = requestService;
        this.scrapingService = scrapingService;
        this.mapper = mapper;
    }

    @Async
    public CompletableFuture<List<JobOffer>> getJobOffers() {
        List<JobOffer> jobOffers = new ArrayList<>();
        for (int i = 0; true; i++) {
            try {
                Document document = requestService.requestConnection("https://bulldogjob.pl/companies/jobs/s/page," + i);
                Elements select = document.select("#__NEXT_DATA__");
                String data = select.get(0).toString();
                String jsonString = data.substring(
                        data.indexOf("{\"props\""),
                        data.lastIndexOf("</script>")
                );
                ObjectNode node = (ObjectNode) mapper.readTree(jsonString);
                ArrayNode jobs = (ArrayNode) node.get("props").get("pageProps").get("jobs");
                if (jobs.isEmpty()) {
                    break;
                }
                for (JsonNode offerJson : jobs) {
                    JobOffer jobOffer = scrapeOffer((ObjectNode) offerJson);
                    jobOfferRepository.save(jobOffer);
                    jobOffers.add(jobOffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return CompletableFuture.completedFuture(jobOffers);
    }

    private JobOffer scrapeOffer(ObjectNode offerJson) {
        String url = "https://bulldogjob.pl/companies/jobs/" + offerJson.get("id").asString();
        System.out.println("Scraping URL: " + url);
        Optional<JobOffer> jobOffer = jobOfferRepository.findByUrl(url);
        if (jobOffer.isPresent()) {
            return jobOffer.get();
        }
        JobOffer newJobOffer = new JobOffer();
        newJobOffer.setDateAdded(LocalDateTime.now());
        newJobOffer.setUrl(url);
        newJobOffer.setCategory("");


        newJobOffer.setLocations(convertToLocations(offerJson));


        newJobOffer.setSkills(convertToSkills(offerJson));


        newJobOffer.setEmployerName(offerJson.get("company").get("name").asString());
        newJobOffer.setSeniority(scrapingService.convertTextToSeniority(offerJson.get("experienceLevel").asString()));
        newJobOffer.setEmploymentType(scrapingService.convertTextToEmploymentType(offerJson.get("employmentType").asString()));


        newJobOffer.setSalary(convertInputToSalary(offerJson.get("denominatedSalaryLong")));


        newJobOffer.setTypeOfContract(convertInputToTypeOfContract(offerJson));
//        newJobOffer.setWorkModes(convertInputToWorkModes(url));
        newJobOffer.setWorkModes(new WorkModes());
        return newJobOffer;
    }

    private Set<Location> convertToLocations(ObjectNode offerJson) {
        Set<Location> locations = new HashSet<>();
        if (offerJson.get("remote").asBoolean()) {
            Optional<Location> locationOpt = locationRepository.findByAliasName("fullremote");
            Location location = locationOpt.orElseThrow(() ->
                    new EntityNotFoundException("Location 'Full-Remote' not found."));
            locations.add(location);
        } else {
            String cityJsonString = offerJson.get("city").asString();
            List<String> citiesList = Arrays.stream(cityJsonString.split(","))
                    .map(String::trim)
                    .toList();
            for (String city : citiesList) {
                if (Objects.equals(city, "")) {
                    continue;
                }
                scrapingService.createNewLocationIfNotFoundElseGet(city, locations);
            }
        }
        return locations;
    }

    private Set<Skill> convertToSkills(ObjectNode offerJson) {
        JsonNode technologyTags = offerJson.get("technologyTags");
        if (technologyTags.getNodeType().toString().equalsIgnoreCase("null")) {
            return Collections.emptySet();
        }
        Set<Skill> skills = new HashSet<>();
        ArrayNode skillsArray = (ArrayNode) technologyTags;
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

    private Salary convertInputToSalary(JsonNode salaryJson) {
        String money = salaryJson.get("money").asString();
        String salaryCurrency = salaryJson.get("currency").asString();
        Salary salary = new Salary();
        if (money == null || salaryCurrency == null || salaryCurrency.isEmpty()) {
            return salary;
        }
        money = money.replaceAll(" ", "");
        salary.setType(SalaryType.SPECIFIED);
        salary.setCurrency(salaryCurrency);
        salary.setIsGross(null);
        if (money.contains("-")) {
            String[] split = money.split("-");
            String from = split[0];
            String to = split[1];
            salary.setFrom(Double.parseDouble(from));
            salary.setTo(Double.parseDouble(to));
        } else if (money.contains("From")) {
            String[] split = money.split("From");
            String to = split[1];
            salary.setFrom(null);
            salary.setTo(Double.parseDouble(to));
        } else if (money.contains("Od")) {
            String[] split = money.split("Od");
            String to = split[1];
            salary.setFrom(null);
            salary.setTo(Double.parseDouble(to));
        } else {
            String[] split = money.split("Upto");
            String to = split[1];
            salary.setFrom(null);
            salary.setTo(Double.parseDouble(to));
        }
        return salary;
    }

    private TypeOfContract convertInputToTypeOfContract(ObjectNode offerJson) {
        boolean isContractB2b = offerJson.get("contractB2b").asBoolean();
        boolean isContractPermanent = offerJson.get("contractEmployment").asBoolean();
        if (isContractB2b) {
            return TypeOfContract.B2B;
        } else if (isContractPermanent) {
            return TypeOfContract.PERMANENT;
        } else {
            return TypeOfContract.OTHER;
        }
    }

    private WorkModes convertInputToWorkModes(String url) {
        Document document1 = requestService.requestConnection(url);
        Elements select1 = document1.select("aside > div > div > div").not(":has(svg)");
        Map<String, Set<String>> scrapedSideDetails = getScrapedSideDetails(select1);
        Set<String> offerWorkModes = scrapedSideDetails.get("Work mode");
        if (offerWorkModes == null) {
            return new WorkModes(false, false, false);
        }
        WorkModes workModes = new WorkModes();
        for (String text : offerWorkModes) {
            if (text == null || text.isEmpty()) {
                continue;
            }
            switch (text.toLowerCase()) {
                case "hybrid", "hybrydowy" -> workModes.setIsHybrid(true);
                case "full-remote", "zdalny" -> workModes.setIsRemote(true);
                case "on-site", "stacjonarny" -> workModes.setIsOnSite(true);
            }
        }
        return workModes;
    }

    private Map<String, Set<String>> getScrapedSideDetails(Elements select) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Element element : select) {
            element.select("span").remove();
            Elements paragraphs = element.select("div > p");
            String label = translateLabelIfPolish(paragraphs.get(0).text());

            Elements values = new Elements(paragraphs.subList(1, paragraphs.size()));
            if (values.size() == 1) {
                map.put(label, Set.of(values.get(0).text()));
            } else {
                Set<String> valuesList = new HashSet<>();
                for (Element value : values) {
                    valuesList.add(value.text());
                }
                map.put(label, valuesList);
            }
        }
        return map;
    }

    private String translateLabelIfPolish(String text) {
        if (text == null || text.isEmpty()) return text;

        Map<String, String> translations = new HashMap<>();
        translations.put("Ważna jeszcze", "Valid for");
        translations.put("Doświadczenie", "Experience");
        translations.put("Typ współpracy", "Employment Type");
        translations.put("Rodzaj umowy", "Contract type");
        translations.put("Płatny urlop", "Paid holidays");
        translations.put("Tryb pracy", "Work mode");
        translations.put("Lokalizacja", "Location");
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (text.startsWith(entry.getKey())) {
                return text.replaceFirst(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }
}
