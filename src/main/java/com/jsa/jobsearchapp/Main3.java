package com.jsa.jobsearchapp;

import com.jsa.jobsearchapp.mail.MailService;
import com.jsa.jobsearchapp.request.RequestService;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@SuppressWarnings("All")
public class Main3 {

    private static final RequestService requestService = new RequestService();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MailService mailService = new MailService();

    //    public static void main(String[] args) {
//        String justJoinItAPILink = "https://api.justjoin.it/v2/user-panel/offers/by-cursor?currency=pln&from=" + 0 + "&itemsCount=100&orderBy=DESC&sortBy=published";
//        HttpResponse<String> response = requestService.fetchResponseBody(justJoinItAPILink);
//        ObjectNode node = (ObjectNode) mapper.readTree(response.body());
//        ArrayNode postings = (ArrayNode) node.get("data");
//        ObjectNode offer = (ObjectNode) postings.get(1);
//        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offer));
//        ArrayNode skillsArray = (ArrayNode) offer.get("requiredSkills");
//        System.out.println(skillsArray);
//    }

    public static void main(String[] args) throws IOException {
        List<String> allOfferUrlsByUserPref = List.of("dupa1", "dupa2", "dupa3", "dupa4", "dupa5", "dupa6");
        String contents = "";
        contents += ("Hello,\\nHere are some offers we found for you:\\n");
        for (String url : allOfferUrlsByUserPref) {
            contents += url + "\\n";
        }
        contents += ("\\nThank you for using JSA (JobSearchApp).");

        HttpResponse<String> stringHttpResponse = mailService.sendSimpleMailAPI("dupa prodTest2", contents);
        System.out.println(stringHttpResponse.body());
    }
//
//    public static void main(String[] args) {
//        System.out.println(Arrays.toString("Azure / Dupa / Git / Chuj".split("/")));
//        Set<String> setToCheckValues = new HashSet<>();
//        Set<String> senioritiesSet = new HashSet<>();
//        Set<String> employmentTypesSet = new HashSet<>();
//        int count = 1;
//        for (int i = 0; true; i++) {
//            try {
//                Document document = requestService.requestConnection("https://bulldogjob.pl/companies/jobs/s/page," + i);
//                Elements select = document.select("#__NEXT_DATA__");
//                String data = select.get(0).toString();
//                String jsonString = data.substring(
//                        data.indexOf("{\"props\""),
//                        data.lastIndexOf("</script>")
//                );
//                ObjectNode node = (ObjectNode) mapper.readTree(jsonString);
//                ArrayNode jobs = (ArrayNode) node.get("props").get("pageProps").get("jobs");
//                if (jobs.isEmpty()) {
//                    break;
//                }
//                for (JsonNode offerJson : jobs) {
//                    String url = "https://bulldogjob.pl/companies/jobs/" + offerJson.get("id").asString();
////                    Document document1 = requestService.requestConnection(url);
////                    Elements select1 = document1.select("aside > div > div > div").not(":has(svg)");
////                    Map<String, Set<String>> scrapedSideDetails = getScrapedSideDetails(select1);
////                    Set<String> workModesSet = scrapedSideDetails.get("work mode");
////                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString()
//
////                    System.out.println(url);
////                    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerJson));
////                    System.out.println("======================================================");
//
////                    System.out.println(scrapedSideDetails.get("Work mode").toArray()[0]);
//                    if (Objects.equals(url, "https://bulldogjob.pl/companies/jobs/226744-senior-consultant-sap-success-factors-warszawa-krakow-katowice-wroclaw-poznan-gdansk-lodz-kpmg-w-polsce")) {
//                        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerJson));
//                        break;
//                    }
//                    senioritiesSet.add(offerJson.get("experienceLevel").asString());
//                    employmentTypesSet.add(offerJson.get("employmentType").asString());
//                    count++;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//
//

    ////            jobOffers.add(scrapeOffer((ObjectNode) offerJson));
//
//        System.out.println("Seniorities: " + senioritiesSet);
//        System.out.println("EmploymentTypes: " + employmentTypesSet);
//    }
    private static Map<String, Set<String>> getScrapedSideDetails(Elements select) {
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

    private static String translateLabelIfPolish(String text) {
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
  