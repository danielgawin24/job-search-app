package com.jsa.jobsearchapp.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsa.jobsearchapp.jobOffer.*;
import com.jsa.jobsearchapp.location.Location;
import com.jsa.jobsearchapp.location.LocationRepository;
import com.jsa.jobsearchapp.skill.Skill;
import com.jsa.jobsearchapp.skill.SkillRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapingService {

    private final JobOfferRepository jobOfferRepository;
    private final LocationRepository locationRepository;
    private final SkillRepository skillRepository;
    private final ObjectMapper mapper;

    public ScrapingService(JobOfferRepository jobOfferRepository, LocationRepository locationRepository, SkillRepository skillRepository, ObjectMapper mapper) {
        this.jobOfferRepository = jobOfferRepository;
        this.locationRepository = locationRepository;
        this.skillRepository = skillRepository;
        this.mapper = mapper;
    }

    public Seniority convertTextToSeniority(String text) {
        return switch (text.toLowerCase()) {
            case "internship", "intern" -> Seniority.INTERN;
            case "junior", "trainee" -> Seniority.JUNIOR;
            case "mid/regular", "mid", "regular", "medium" -> Seniority.MID;
            case "senior" -> Seniority.SENIOR;
            case "lead", "principal", "expert", "lead / principal" -> Seniority.LEAD_PRINCIPAL;
            case "manager", "c-level", "manager / c-level" -> Seniority.C_LEVEL;
            default -> Seniority.OTHER;
        };
    }

    public EmploymentType convertTextToEmploymentType(String text) {
        return switch (text.toLowerCase()) {
            case "contractor", "kontrakt" -> EmploymentType.B2B;
            case "full-time", "full_time", "pełny etat" -> EmploymentType.FULL_TIME;
            case "part-time", "part_time", "część etatu" -> EmploymentType.PART_TIME;
            case "practice / internship" -> EmploymentType.INTERNSHIP;
            case "freelance" -> EmploymentType.FREELANCE;
            default -> EmploymentType.UNSPECIFIED;
        };
    }

    public TypeOfContract convertTextToTypeOfContract(String text) {
        return switch (text.toLowerCase()) {
            case "kontrakt b2b", "b2b contract", "b2b" -> TypeOfContract.B2B;
            case "umowa o pracę", "employment contract", "permanent" -> TypeOfContract.PERMANENT;
            case "internship" -> TypeOfContract.INTERNSHIP;
            case "mandate contract", "zlecenie" -> TypeOfContract.MANDATE;
            case "specific-task contract" -> TypeOfContract.FREELANCE;
            default -> TypeOfContract.OTHER;
        };
    }

    public List<JobOffer> sendJobOffersInSmallerBatches(List<JobOffer> jobOffers) {
        List<JobOffer> sentJobOffers = new ArrayList<>();
        for (int i = 0; i < jobOffers.size(); i += 100) {
            List<JobOffer> batchSubList = jobOffers.subList(i, Math.min(i + 100, jobOffers.size()));
            try {
                jobOfferRepository.saveAll(batchSubList);
                sentJobOffers.addAll(batchSubList);
            } catch (Exception e) {
                System.err.println("Failed to send NoFluff job offers at batch " + i);
            }
        }
        return sentJobOffers;
    }

    public void createNewLocationIfNotFoundElseGet(String city, Set<Location> locations) {
        String aliasKey = normalizeCityAliasName(city);
        Optional<Location> location = locationRepository.findByAliasName(aliasKey);
        if (location.isPresent()) {
            locations.add(location.get());
        } else {
            Location newLocation = new Location();
            newLocation.setDisplayName(normalizeCityDisplayName(city));
            newLocation.setAliasName(aliasKey);
            try {
                locationRepository.save(newLocation);
                locations.add(newLocation);
            } catch (Exception ignored) {
            }
        }
    }

    public void createNewSkillIfNotFoundElseGet(String skillName, Set<Skill> skills) {
        if (skillName == null || skillName.isEmpty()) {
            return;
        }
        skillName = skillName.replaceAll("[&,/)(\\[\\]↔]|( or )|( and )|( lub )|(?<!\\+)\\+(?!\\+)", "|");
        String[] split = skillName.split("\\|");

        for (String s : split) {
            try {
                String jsonContents = Files.readString(Paths.get("src/main/resources/files/canonical_skills_patterns.json"));
                JsonNode canonicalSkillsPatterns = mapper.readTree(jsonContents);

                String finalSkillName = getSkillCanonicalNameIfMatchesPattern(canonicalSkillsPatterns, normalizeSkillAliasName(s.trim()));

                Optional<Skill> skill = skillRepository.findByAliasName(finalSkillName);
                if (skill.isPresent()) {
                    skills.add(skill.get());
                } else {
                    Skill newSkill = new Skill();
                    newSkill.setDisplayName(normalizeSkillDisplayName(finalSkillName));
                    newSkill.setAliasName(finalSkillName);
                    try {
                        skillRepository.save(newSkill);
                        skills.add(newSkill);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error reading canonical skills patterns json file: " + e.getMessage());
                return;
            }
        }
    }

    public String getSkillCanonicalNameIfMatchesPattern(JsonNode canonicalSkillsPatterns, String skillName) {
        for (JsonNode pattern : canonicalSkillsPatterns) {
            String correctName = pattern.get("name").asText();
            JsonNode patterns = pattern.get("patterns");
            for (JsonNode jsonNode : patterns) {
                Pattern skillPattern = Pattern.compile(jsonNode.asText(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = skillPattern.matcher(skillName);
                if (matcher.find()) {
                    return correctName;
                }
            }
        }
        return skillName;
    }

    public String normalizeCityAliasName(String cityName) {
        String cityNameLowerTrimReplace = cityName.toLowerCase().trim().replaceAll("(hybrid)|(remote)|(onsite)|(office)", "");
        String cityNameSplitted = cityNameLowerTrimReplace.split(",")[0];
        return cityNameSplitted
                .replaceAll("[àáâäãåāăąǎȁȃ]", "a")
                .replaceAll("[æǽ]", "ae")
                .replaceAll("[ƀɓ]", "b")
                .replaceAll("[çćčĉċ]", "c")
                .replaceAll("[ďđð]", "d")
                .replaceAll("[èéêëēĕėęěȅȇ]", "e")
                .replaceAll("[ğĝġģ]", "g")
                .replaceAll("[ĥħ]", "h")
                .replaceAll("[ìíîïĩīĭįǐȉȋ]", "i")
                .replaceAll("ĵ", "j")
                .replaceAll("ķ", "k")
                .replaceAll("[łľļĺŀ]", "l")
                .replaceAll("[ñńňņŋ]", "n")
                .replaceAll("[òóôöõøōŏőǒȍȏ]", "o")
                .replaceAll("œ", "oe")
                .replaceAll("[ŕřŗ]", "r")
                .replaceAll("[śšşŝ]", "s")
                .replaceAll("ß", "ss")
                .replaceAll("[ťţŧ]", "t")
                .replaceAll("[ùúûüũūŭůűųǔȕȗ]", "u")
                .replaceAll("ŵ", "w")
                .replaceAll("[ýÿŷ]", "y")
                .replaceAll("[źžż]", "z")
                .replaceAll("[^A-Za-z0-9]", "");
    }

    public String normalizeCityDisplayName(String cityName) {
        String cityNameReplaced = cityName.replaceAll("(hybrid)|(remote)|(onsite)|(office)", "");
        return convertAnyStringToCamelCase(cityNameReplaced.split(",")[0]);
    }

    public String normalizeSkillAliasName(String displayName) {
        if (displayName.isEmpty()) {
            return "";
        }
        String aliasKey = displayName.toLowerCase();
        aliasKey = aliasKey
                .replaceAll("[•\"]|(\\.(?!\\w))|(:(?!\\w))", "")
                .replaceAll("\\s+", " ").trim();

        List<String> bannedKeywords = Arrays.asList(
                "for", "must-have", "nice to have", "required", "yrs", "years", "of", "to", "in", "advanced", "strong", "building", "various",
                "project", "experience", "exp", "industry", "knowledge", "background", "any", "academic", "modern", "types",
                "based", "development", "management", "systems", "design", "architecture", "frameworks", "support", "platform", "services"
        );

        String[] split = aliasKey.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            if (bannedKeywords.contains(s.toLowerCase()) || s.matches("\\d+") || (s.length() < 2 && s.matches("[^CR]"))) {
                continue;
            }
            sb.append(s).append(" ");
        }
        if (sb.toString().isEmpty() || sb.toString().matches("[^a-zA-Z]+")) {
            return "";
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    public String normalizeSkillDisplayName(String skillName) {
        return convertAnyStringToCamelCase(skillName);
    }

    public String convertAnyStringToCamelCase(String text) {
        try {
            StringBuilder sb = new StringBuilder();
            text = text.replaceAll("\\s+", " ");
            for (String s : text.split(" ")) {
                s = s.toLowerCase().trim();
                char c = s.charAt(0);
                String wordWithoutFirstLetter = s.substring(1).toLowerCase();
                sb.append(String.valueOf(c).toUpperCase()).append(wordWithoutFirstLetter).append(" ");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            System.err.println("ERROR with skill: " + text);
            return "error";
        }
    }

    public boolean isSkillInPolish(String skill) {
        if (skill == null || skill.isEmpty()) {
            return false;
        }
        String cleanedSkill = skill.toLowerCase().trim();
        Pattern polishLetters = Pattern.compile("[ąćęłńóśźż]");
        Matcher matcher = polishLetters.matcher(cleanedSkill);
        if (matcher.find()) {
            return true;
        }
        List<String> polishSkillNames = getTypicalPolishSkillNames();
        for (String s : cleanedSkill.split(" ")) {
            if (polishSkillNames.contains(s.toLowerCase())) {
                return true;
            }
        }
        List<String> polishSkillEndings = getTypicalPolishSkillNameEndings();
        return polishSkillEndings.stream().anyMatch(s -> cleanedSkill.endsWith(s.toLowerCase()));
    }

    private List<String> getTypicalPolishSkillNames() {
        return List.of(
                "programowanie",
                "testowanie",
                "automatyzacja",
                "zarządzanie",
                "analiza",
                "analizy",
                "analityka",
                "projektowanie",
                "wdrażanie",
                "wdrożenia",
                "utrzymanie",
                "rozwój",
                "optymalizacja",
                "integracja",
                "dokumentacja",
                "bezpieczeństwo",
                "administracja",
                "konfiguracja",
                "monitorowanie",
                "diagnozowanie",
                "wsparcie",
                "komunikacja",
                "organizacja",
                "planowanie",
                "koordynacja",
                "raportowanie",
                "modelowanie",
                "architektura",
                "skalowanie",
                "standaryzacja",
                "weryfikacja",
                "walidacja",
                "debugowanie",
                "refaktoryzacja",
                "kontrola",
                "jakość",
                "wydajność",
                "dostępność",
                "niezawodność",
                "utrzymywalność",
                "dokumentowanie",
                "szkolenia",
                "mentoring",
                "konsulting",
                "wdrażalność",
                "optymalizowanie",
                "cyberbezpieczeństwo",
                "audyt",
                "testy",
                "analizowanie",
                "projektowanie",
                "zarządzanie",
                "utrzymywanie",
                "aplikacje",
                "arkusz",
                "arkusze",
                "uprawnienia",
                "ubezpieczenia",
                "transmisja",
                "systemy",
                "symfonia"
        );
    }

    private List<String> getTypicalPolishSkillNameEndings() {
        return List.of(
                "anie",
                "enie",
                "owanie",
                "ywanie",
                "izacja",
                "yzacja",
                "acja",
                "cja",
                "sja",
                "zja",
                "ność",
                "alność",
                "owość",
                "liwość",
                "izacja",
                "ator",
                "atorów",
                "ista",
                "ystyka",
                "owanie",
                "izacja",
                "izacja",
                "owanie",
                "acja",
                "owanie",
                "izacja"
        );
    }
}
