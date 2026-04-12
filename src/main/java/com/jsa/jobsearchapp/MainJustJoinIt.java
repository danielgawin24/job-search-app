package com.jsa.jobsearchapp;

import com.jsa.jobsearchapp.request.RequestService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;

@SuppressWarnings("DuplicatedCode")
public class MainJustJoinIt {

    private static final RequestService requestService = new RequestService();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        // 3700, 5700, 3900, 3200, 3500

        // after improving noFluff
        // 5600, 5600, 3700, 3700, 5400, 4800, 2900, 4500, 3200, 3500
        int currentCursor = 0;
        while (true) {
//            if (currentCursor == 1000) {
//                break;
//            }
//            System.out.println(currentCursor / 100);
            String justJoinItAPI = "https://api.justjoin.it/v2/user-panel/offers/by-cursor?currency=pln&from=" + currentCursor + "&itemsCount=100&orderBy=DESC&sortBy=published";
            HttpResponse<String> response = requestService.fetchResponseBody(justJoinItAPI);
            ObjectNode ObjectNode = (ObjectNode) mapper.readTree(response.body());
            ArrayNode postings = (ArrayNode) ObjectNode.get("data");
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postings));
            System.out.println("LENGTH: " + postings.size());
//            for (int i = 0; i < postings.length(); i++) {
//                ObjectNode offerJson = postings.getObjectNode(i);
//                String url = "https://www.justjoin.it/job-offer/" + offerJson.getString("slug");
//                System.out.println(url);
//                System.out.println(offerJson.toString(2));
//                System.out.println("–•–•–•–•–•–");
//
//                System.out.println(employmentTypes.toString(2));
//                scrapedStuffFromJson.add(string);
//            System.out.println("LINK: " + offerUrl);
//            System.out.println(offerJson.toString(2));
//            }
            Object nextCursor = ObjectNode.get("meta").get("next").get("cursor");
            if (nextCursor != null) {
                try {
                    currentCursor = (int) nextCursor;
                } catch (Exception e) {
                    break;
                }
            } else {
                break;
            }
        }
//        System.out.println(scrapedStuffFromJson);
    }
}