package com.jsa.jobsearchapp;

import com.jsa.jobsearchapp.jobOffer.JobOffer;
import com.jsa.jobsearchapp.request.RequestService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;
import java.util.*;

public class MainNoFluff {

    private static final RequestService requestService = new RequestService();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        // 13300, 16600, 18500, 19800, 12600, 9900, 17900

        // after removing filtering by reference to another map
        // 18000, 15200, 14500, 20100, 11200, 24800, 11000, 15600
        Set<String> crawledUrls = new HashSet<>();
        List<JobOffer> jobOffers = new ArrayList<>();
        String link = "https://nofluffjobs.com/api/joboffers/main?pageTo=1&pageSize=1000000&salaryCurrency=PLN&salaryPeriod=month&region=pl&language=pl-PL";
        HttpResponse<String> response = requestService.fetchResponseBody(link);
        ObjectNode ObjectNode = (ObjectNode) mapper.readTree(response.body());
        ArrayNode postings = (ArrayNode) ObjectNode.get("postings");
        if (postings.isEmpty()) {
            crawledUrls.add("Error: No postings found");
            return;
        }
        Map<String, ObjectNode> postingsByReferenceMap = new LinkedHashMap<>();
        System.out.println("LENGTH: " + postings.size());
        for (int i = 0; i < postings.size(); i++) {
            ObjectNode postingOffer = (ObjectNode) postings.get(i);
            postingOffer.remove("logo");
            String reference = postingOffer.get("reference").asString();
            postingsByReferenceMap.putIfAbsent(reference, postingOffer);
        }
        System.out.println("MAP LENGTH: " + postingsByReferenceMap.size());
        for (String s : postingsByReferenceMap.keySet()) {
            ObjectNode offerJson = postingsByReferenceMap.get(s);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerJson));
        }
    }
}
