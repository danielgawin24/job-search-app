package com.jsa.jobsearchapp;

import com.jsa.jobsearchapp.exception.NullDocumentException;
import com.jsa.jobsearchapp.jobOffer.Salary;
import com.jsa.jobsearchapp.jobOffer.SalaryType;
import com.jsa.jobsearchapp.request.RequestService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MainPracujPl {

    private static final RequestService requestService = new RequestService();

    //! fuck, fetch not working, I don't know why,
    @SuppressWarnings("SpellCheckingInspection")
    public static void main(String[] args) throws IOException {
        String link1 = "https://www.pracuj.pl/praca/atlassian-engineer-warszawa-aleja-niepodleglosci-69,oferta,1004219680?s=1f7c2c91&searchId=MTc1MzQ2NDAwMDUxNC44NjA0";
        /*
Workplace:
Whole Poland (remote work)
B2B contract
full-time
specialist (Mid / Regular), senior specialist (Senior)
full office work, home office work, hybrid work
        */
        String link2 = "https://www.pracuj.pl/praca/technical-consultant-in-telecommunications-warszawa,oferta,1004196825?sug=oferta_bottom_bd_6_tname_252_tgroup_D";
        /*
Warszawa(Masovian)
contract of employment
full-time
specialist (Mid / Regular)
hybrid work
Запрошуємо працівників з України
        */

        String offerWithSalary = "https://www.pracuj.pl/praca/mlodszy-analityk-mlodsza-analityczka-warszawa-mokotowska-1,oferta,1004205858?sug=list_top_cr_bd_6_tname_252_tgroup_D&s=1f7c2c91&searchId=MTc1MzQ2NTExMDg1Ni4wMTE=";
        String offerSalary2 = "https://www.pracuj.pl/praca/senior-data-engineer-krakow-zablocie-43b,oferta,1004248637?sug=list_top_cr_bd_1_tname_252_tgroup_D_boosterAI_L0&s=1f7c2c91&searchId=MTc1MzQ2NTY1MTMwOC45MzQz";
        String offerSalary3 = "https://www.pracuj.pl/praca/specjalista-ds-it-balice-pow-krakowski-kapitana-mieczyslawa-medweckiego-1,oferta,1004206622?sug=list_top_cr_bd_2_tname_252_tgroup_D&s=1f7c2c91&searchId=MTc1MzQ2NTY1MTMwOC45MzQz";
        String multipleSalariesOffer = "https://www.pracuj.pl/praca/mlodszy-tester-oprogramowania-poznan-krysiewicza-9,oferta,1004214763?sug=list_bd_1_tname_252_tgroup_D_boosterAI_L0&s=a21252c9&searchId=MTc1MzUwOTQxNjAzOC40Mjcy";
        Document document = Jsoup.connect(multipleSalariesOffer).get();
        String salary = document.select("div > div > div > div:has(div):has(span:has(span))").get(4).text();
        System.out.println("START: " + salary);
        String salaryPeriodFirstLetter = String.valueOf(salary.substring(salary.indexOf('/') + 1).trim().charAt(0)).toUpperCase();
        System.out.println("LETTER: " + salaryPeriodFirstLetter);
        String salaryValuesAndCurrency = salary.substring(0, salary.indexOf("\n"));
        System.out.println("V&C: " + salaryValuesAndCurrency);
        String offerCurrency = salaryValuesAndCurrency.substring(salaryValuesAndCurrency.length() - 3).trim();
        String convertedOfferCurrency = offerCurrency.equals("zł") ? "PLN" : offerCurrency;
        System.out.println("CURR: " + convertedOfferCurrency);
        String salaryValue = salaryValuesAndCurrency.substring(0, salaryValuesAndCurrency.indexOf(offerCurrency)).trim()
                .replaceAll(" ", "").replaceAll(",", ".");
        System.out.println("VALUES: " + salaryValue);
        List<String> salaries = new ArrayList<>(
                List.of(
                        """
                                5 000 – 6 900 zł
                                brutto / mies. | umowa o pracę
                                6 630 – 9 050 zł
                                netto (+ VAT) / mies. | kontrakt B2B""",
                        """
                                3 490 – 4 000 zł
                                brutto / mies. | umowa o pracę
                                22,80 – 23,80 zł
                                brutto / godz. | umowa na zastępstwo"""
                )
        );
        boolean contains = salaries.contains(salaryValue);

        Set<String> crawledUrls = new HashSet<>();
        Set<String> contractTypes = new HashSet<>();
        for (int page = 1; page < 20; page++) {
            String url1 = "https://it.pracuj.pl/praca?pn=" + page;
//            System.out.println("SET SIZE: " + contractTypes.size());
//            contractTypes.forEach(System.out::println);
            Document document1 = requestService.requestConnection(url1);
            if (document1 == null) {
                throw new NullDocumentException("Document is null");
            }
            Elements allLinks = new Elements();
            allLinks.addAll(document1.select("a[href*=pracuj.pl/praca/]"));
            for (Element link : allLinks) {
                String linkUrl = link.absUrl("href");
                if (linkUrl.startsWith("https://www.pracuj.pl/praca/")) {
                    crawledUrls.add(linkUrl);
//                    Document document = AdminService.requestConnection(linkUrl);
//                    if (document != null) {
                    //* insert code for mass tests
//                        System.out.println(linkUrl);
//                        Elements elements = document.select("ul > li[data-test*=sections-benefit] > div > div");
//                        Map<String, String> stringStringMap = convertElementsToMap(elements);
//                        String s = stringStringMap.get("Contract type");
//                        contractTypes.add(s);
//                        System.out.println("–•–•–•–•–•–");
//                    }
                }

            }
        }
        int size = crawledUrls.size();
    }

    @SuppressWarnings("all")
    public static void convertInputToSalary(Document document) {
        String salary = document.select("div > div > div > div:has(div):has(span:has(span))").get(4).text();
        List<String> list = new ArrayList<>(List.of(salary.split("\n")));
        list.subList(0, 2).clear();
        list.removeIf(String::isEmpty);
        if (list.size() == 1) {

        }
        String regex = "\\d";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(salary);
        if (matcher.find()) {
            for (int i = 0; i < list.size(); i += 2) {
                String nThOddElement = list.get(i);
                String nThEvenElement = list.get(i + 1);
                salary = salary.replace(salary.substring(salary.indexOf('|')), ""); //? THIS IS THE TYPE OF CONTRACT FIELD
                String salaryPeriodFirstLetter = String.valueOf(salary.substring(salary.indexOf('/') + 1).trim().charAt(0)).toUpperCase();
                String salaryValuesAndCurrency = salary.substring(0, salary.indexOf("\n"));
                String offerCurrency = salaryValuesAndCurrency.substring(salaryValuesAndCurrency.length() - 3).trim();
                String convertedOfferCurrency = offerCurrency.equals("zł") ? "PLN" : offerCurrency;
                String salaryValue = salaryValuesAndCurrency.substring(0, salaryValuesAndCurrency.indexOf(offerCurrency)).trim()
                        .replaceAll(" ", "").replaceAll(",", ".");
                String[] split = salaryValue.split("–");
                List<Double> salaryValues = new ArrayList<>(Stream.of(split).map(value -> {
                    try {
                        return Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }).toList());
                salaryValues.removeIf(Objects::isNull);
                List<Double> convertedMonthlyValues = switch (salaryPeriodFirstLetter) {
                    case "H", "G" -> salaryValues.stream().map(value -> value * 160).toList();
                    case "D" -> salaryValues.stream().map(value -> value * 20).toList();
                    case "M" -> salaryValues;
                    case "Y", "R" -> salaryValues.stream().map(value -> value / 12).toList();
                    default ->
                            throw new IllegalArgumentException("Unexpected period value: " + salaryPeriodFirstLetter);
                };
                List<Double> salaryDoubleValues = new ArrayList<>(convertedMonthlyValues);
                Collections.sort(salaryDoubleValues);
                double minValue = salaryDoubleValues.get(0);
                double maxValue = salaryDoubleValues.get(salaryDoubleValues.size() - 1);
                Salary salary1 = new Salary(SalaryType.SPECIFIED, minValue, maxValue, convertedOfferCurrency, true);
                System.out.println(salary1);
            }
        }

    }
}
