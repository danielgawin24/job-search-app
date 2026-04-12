package com.jsa.jobsearchapp;

import com.jsa.jobsearchapp.jobOffer.JobOffer;
import com.jsa.jobsearchapp.request.RequestService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;
import java.util.*;

public class Main2 {

    private static final RequestService requestService = new RequestService();
    private static final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("all")
    public static void main(String[] args) {
        Set<String> crawledUrls = new HashSet<>();
        List<JobOffer> jobOffers = new ArrayList<>();
        int count = 0;
        while (true) {
            System.out.println("COUNT: " + count);
            String link = "https://nofluffjobs.com/api/joboffers/main?pageTo=" + count + "&pageSize=50&salaryCurrency=PLN&salaryPeriod=month&region=pl&language=pl-PL";
            HttpResponse<String> response = requestService.fetchResponseBody(link);
            ObjectNode ObjectNode = (ObjectNode) mapper.readTree(response.body());
            ArrayNode postings = (ArrayNode) ObjectNode.get("postings");
            if (postings.isEmpty()) {
                break;
            }
            Map<String, ObjectNode> postingsByReferenceMap = new LinkedHashMap<>();
            for (int i = 0; i < postings.size(); i++) {
                ObjectNode postingOffer = (ObjectNode) postings.get(i);
                String reference = postingOffer.get("reference").asString();
                postingsByReferenceMap.putIfAbsent(reference, postingOffer);
            }
            for (Map.Entry<String, ObjectNode> stringObjectNodeEntry : postingsByReferenceMap.entrySet()) {
                ObjectNode offerJson = stringObjectNodeEntry.getValue();
                String url = "https://nofluffjobs.com/pl/job/" + offerJson.get("url").asString();
                System.out.println("LINK: " + url);
                try {
                    System.out.println("CITY: " + offerJson.get("location").get("places").get(0).get("city").asString());
                } catch (Exception e) {
                    System.out.println("Possibly full remote, check on page");
                }
                System.out.println("–•–•–•–•–•–•–•–");
            }
            count++;
        }
    }
}