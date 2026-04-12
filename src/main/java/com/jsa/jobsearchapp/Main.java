package com.jsa.jobsearchapp;

import com.jsa.jobsearchapp.admin.AdminService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("all")
public class Main {

    private final AdminService adminService;


    public Main(AdminService adminService) {
        this.adminService = adminService;
    }

    public static void main(String[] args) throws IOException {

        String url = "https://bulldogjob.pl/companies/jobs/176251-senior-devops-engineer-aws-warszawa-1dea";
        String testUrl = "https://bulldogjob.pl/companies/jobs/176586-zleceniobiorca-czyni-w-zespole-migration-leakage-prevention-warszawa-t-mobile-polska-s-a";
        String internUrl = "https://bulldogjob.pl/companies/jobs/175319-llm-engineer-internship-program-she-he-they-warsaw-accenture-polska";
        String testOfDifferents1 = "https://bulldogjob.pl/companies/jobs/176023-information-security-analyst-warsaw-tqlo-spolka-z-ograniczona-odpowiedzialnoscia";
        String testOfDifferents2 = "https://bulldogjob.pl/companies/jobs/176567-senior-application-support-specialist-efront-warsaw-gdansk-ergo-technology-services-s-a";
        String testOfDifferents3 = "https://bulldogjob.pl/companies/jobs/176541-tester-automatyzujacy-test-automation-engineer-b2b-warszawa-gdansk-bank-millennium";
        String twoLocTwoContracts = "https://bulldogjob.pl/companies/jobs/176303-senior-security-grc-specialist-katowice-szczecin-gdynia-warsaw-sapiens-software-solutions-poland-sp-z-o-o";
        String noSalaryInfo = "https://bulldogjob.pl/companies/jobs/176640-miles-developer-wroclaw-eviden";
//        Document document = Jsoup.connect(twoLocTwoContracts).get();

//        Document diff1 = Jsoup.connect(testOfDifferents1).get();
//        Document diff2 = Jsoup.connect(testOfDifferents2).get();
//        Document diff3 = Jsoup.connect(testOfDifferents3).get();

        Set<String> crawledUrls = new HashSet<>();
        Set<String> employmentTypes = new HashSet<>();
        Set<List<String>> salariesInfo = new HashSet<>();
        Set<String> periods = new HashSet<>();
        for (int page = 1; page < 20; page++) { //! CHANGE PAGE SIZE FOR BIGGER SITES
            System.out.println("Page:" + page + ", size:" + crawledUrls.size());
            String url1 = "https://it.pracuj.pl/praca?pn=" + page;
            String url2 = "https://bulldogjob.pl/companies/jobs/s/page," + page;

            Document document1 = Jsoup.connect(url1).get();
            Document document2 = Jsoup.connect(url2).get();
//            if (document1 == null || document2 == null) {
//                throw new NullDocumentException("Document is null");
//            }
            Elements allLinks = new Elements();
//            allLinks.addAll(document1.select("a[href*=pracuj.pl/praca/]"));
            allLinks.addAll(document2.select("a[href*=bulldogjob.pl/companies/jobs/]"));

            for (Element link : allLinks) {
                String linkUrl = link.absUrl("href");
//                System.out.println(linkUrl);
                if (linkUrl.startsWith("https://bulldogjob.pl/companies/jobs/") && !linkUrl.contains("page")) {
//!                    crawledUrls.add(linkUrl); //!!! WYłĄCZONE!!!
                    try {
                        Document documentX = Jsoup.connect(linkUrl).get();
                        Elements select = documentX.select("aside > div > div > p");
//                        Elements select = documentX.select("aside > div > div > div").not(":has(svg)");

//                        System.out.println("SIZE: " + select.size());
//                        System.out.println(linkUrl);

                        List<String> list = null;
//                        if (select.size() > 5) {
                        if (select.size() == 0) {
                        }

//                        System.out.println(select);
//                        System.out.println("–––");

                        list = new ArrayList<>();
                        for (Element element : select) {
                            list.add(element.text());
                        }
                        list.removeIf(String::isEmpty);
                        if (list.size() == 1) {
                            continue;
                        } else {
                            System.out.println("LINK:" + linkUrl);
                        }
//                        System.out.println(list);
                        salariesInfo.add(list);

                        //! TESTY PONIŻEJ:


//                        if (test.size() == 1) {
//                            Salary salary = new Salary(SalaryType.UNSPECIFIED, 0.0d, 0.0d);
////                            System.out.println("FOR SIZE 1: " + salary);
//                        } else {
//                            List<String> salaryValues = test.subList(0, test.size() - 2);
//                            System.out.println(salaryValues);
//                            String salaryCurrency = test.get(test.size() - 2);
//                            String salaryPeriod = test.get(test.size() - 1);
//                            periods.add(salaryPeriod);
//                            double salaryFrom = exchangeService.calculateExchange(Double.parseDouble(Collections.min(salaryValues)), salaryCurrency, "PLN", exchangeRates);
//                            double salaryTo = exchangeService.calculateExchange(Double.parseDouble(Collections.max(salaryValues)), salaryCurrency, "PLN", exchangeRates);
////                            switch (salaryValues.size()) {
////                                case 1:
////                            }
////                            ;
//                            Salary salary = new Salary(SalaryType.SPECIFIED, salaryFrom, salaryTo);
//
////                            System.out.println("FOR SIZE ELSE: " + salary);
//                        }
//                        String salaryCurrency = test.get(test.size() - 1);
//                        System.out.println("CLEANED: "+test);

//                            List<String> temp = new ArrayList<>();
//                            if (list.size() > 1) {
//                                String nThElement = "";
//                                String secondElement = list.get(1);
//                                for (int i = 0; i < list.size(); i += 2) {
//                                    nThElement = list.get(i);
//                                    temp.addAll(Stream.of(nThElement.substring(0, nThElement.length() - 3).split("-"))
//                                            .map(value -> value.replace("Up to ", "").replace("Do ", "").trim())
//                                            .toList());
//                                }
//                                list.clear();
//                                list.addAll(temp);
//                                String salaryCurrency = nThElement.substring(nThElement.length() - 3);
//                                list.add(salaryCurrency);
//                                list.add(secondElement.substring(secondElement.indexOf("/ ") + 1).trim());
//                            }
//                        }

                        /*
                        * (Stream.of(firstElement.substring(0, firstElement.length() - 3).split("-"))
                                    .map(value -> value.replace("Up to ", "").replace("Do ", "").trim())
                                    .toList())
                        */
//                        if (list.size() > 2) {
//                            String thirdElement = list.get(2);
//                            salaryValues.addAll(Stream.of(thirdElement.substring(0, thirdElement.length() - 3).split("-"))
//                                    .map(value -> value.replace("Up to ", "").replace("Do ", "").trim())
//                                    .collect(Collectors.toSet()));
//                        }

//                        for (int i = 0; i < list.size(); i += 2) {
//                            String nThElement = list.get(i);
//                            salaryValues.addAll(Stream.of(nThElement.substring(0, nThElement.length() - 3).split("-"))
//                                    .map(value -> value.replace("Up to ", "").replace("Do ", "").trim())
//                                    .toList());
//                        }
                        System.out.println("–•–•–•–•–•–");
                    } catch (IOException e) {
                        System.err.println("Failed to fetch URL: " + linkUrl);
                        System.err.println("Error: " + e.getMessage());
                        continue;
                    }



                    /*
        [10 000 - 16 000 PLN, + VAT (B2B) / mies.]
CLEANED: [10 000, 16 000, PLN, mies.]

                    String s = "Do 16 000, Do 14 000, PLN, mies.";
                    s = s.replaceAll(" ", "");
                    String[] split = s.split("Do ");
                    System.out.println(Arrays.stream(split).toList());

                    List<String> list = new ArrayList<>(List.of("Od 16 000 PLN", " + VAT (B2B) / mies.", "Od 14 000 PLN", " + VAT (UoP) / mies."));
                    List<String> temp = new ArrayList<>();
                    if (list.size() > 1) {
                        String nThElement = "";
                        String secondElement = list.get(1);
                        for (int i = 0; i < list.size(); i += 2) {
                            nThElement = list.get(i);
                            temp.addAll(Stream.of(nThElement.substring(0, nThElement.length() - 3).split("-"))
                                    .map(String::trim)
                                    .toList());
                        }
                        System.out.println(list);
                        list.clear();
                        list.addAll(temp);
                        System.out.println(list);
                        String salaryCurrency = nThElement.substring(nThElement.length() - 3);
                        list.add(salaryCurrency);
                        System.out.println(salaryCurrency);
                        String salaryPeriod = secondElement.substring(secondElement.indexOf("/ ") + 1).trim();
                        list.add(salaryPeriod);
                        System.out.println(salaryPeriod);
                        System.out.println("FINAL LIST: " + list);
                        List<String> temp2 = new ArrayList<>(list.subList(0, list.size() - 2));
                        temp2 = temp2.stream().map(s1 -> s1.replaceAll(" ", "")).toList();
                        System.out.println(temp2);
                    }
                    // end
                    */
                }
            }
            System.out.println(periods.size());
            System.out.println("–•–•–•–");
            periods.forEach(System.out::println);
        }
    }
}
