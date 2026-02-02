<|editable_region_start|>
package com.example.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.model.PageContent;
import com.example.model.StreamingServicePlan;

@Service
public class CsvService {

    private static final String CSV_FILE_PATH = "src/main/resources/StreamingServices.csv";
    private List<StreamingServicePlan> plans;

    public CsvService() {
        // Load the plans on startup
        this.plans = readCsv();
    }

    // Helper method to remove dollar signs from price fields
    private String removeDollarSymbol(String value) {
        try {
            return value.replace("$", "").trim();
        } catch (Exception e) {
            System.err.println("Error removing dollar symbol from value: " + value);
            return value; // Return original value if error occurs
        }
    }

    // Get plans filtered by the platform (service name)
    public List<StreamingServicePlan> getPlans(String platform) {
        try {
            return plans.stream()
                    .filter(plan -> plan.getServiceName().equalsIgnoreCase(platform))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error filtering plans for platform: " + platform);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    // Search plans based on a query (case-insensitive search in serviceName,
    // planName, and features)
    public List<StreamingServicePlan> searchPlans(String query) {
        try {
            String lowerCaseQuery = query != null ? query.toLowerCase() : "";
            return plans.stream()
                    .filter(plan -> plan.getServiceName().toLowerCase().contains(lowerCaseQuery) ||
                            plan.getPlanName().toLowerCase().contains(lowerCaseQuery) ||
                            plan.getFeatures().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching plans for query: " + query);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Load page content from CSV file
    private List<PageContent> loadPageContentFromCsv() {
        List<PageContent> webPages = new ArrayList<>();
        Set<String> uniqueUrls = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Crawled_Website_Data.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(",", 2);
                    if (values.length == 2) {
                        String title = values[0].trim();
                        String content = values[1].trim();

                        if (uniqueUrls.add(title)) {
                            webPages.add(new PageContent(title, content));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing row: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Crawled_Website_Data.csv");
            e.printStackTrace();
        }

        return webPages;
    }

    // Boyer-Moore algorithm for counting occurrences of a keyword in a text
    public int countOccurrences(String text, String pattern) {
        try {
            Map<Character, Integer> badCharTable = buildBadCharTable(pattern);
            int occurrences = 0;
            int m = pattern.length();
            int n = text.length();

            int shift = 0;
            while (shift <= (n - m)) {
                int j = m - 1;
                while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                    j--;
                }

                if (j < 0) {
                    occurrences++;
                    shift += (shift + m < n) ? m - badCharTable.getOrDefault(text.charAt(shift + m), -1) : 1;
                } else {
                    shift += Math.max(1, j - badCharTable.getOrDefault(text.charAt(shift + j), -1));
                }
            }

            return occurrences;
        } catch (Exception e) {
            System.err.println("Error counting occurrences of pattern: " + pattern);
            e.printStackTrace();
            return 0;
        }
    }

    // Helper method to build the bad character table for the Boyer-Moore algorithm
    public Map<Character, Integer> buildBadCharTable(String pattern) {
        Map<Character, Integer> badCharTable = new HashMap<>();
        try {
            for (int i = 0; i < pattern.length(); i++) {
                badCharTable.put(pattern.charAt(i), i);
            }
        } catch (Exception e) {
            System.err.println("Error building bad character table for pattern: " + pattern);
            e.printStackTrace();
        }
        return badCharTable;
    }
}



public class CsvService {

<|editable_region_end|>

### Response:
<|editable_region_start|>
package com.example.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.model.PageContent;
import com.example.model.StreamingServicePlan;

@Service
public class CsvService {

    private static final String CSV_FILE_PATH = "src/main/resources/StreamingServices.csv";
    private List<StreamingServicePlan> plans;

    public CsvService() {
        // Load the plans on startup
        this.plans = readCsv();
    }

    // Helper method to remove dollar signs from price fields
    private String removeDollarSymbol(String value) {
        try {
            return value.replace("$", "").trim();
        } catch (Exception e) {
            System.err.println("Error removing dollar symbol from value: " + value);
            return value; // Return original value if error occurs
        }
    }

    // Get plans filtered by the platform (service name)
    public List<StreamingServicePlan> getPlans(String platform) {
        try {
            return plans.stream()
                    .filter(plan -> plan.getServiceName().equalsIgnoreCase(platform))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error filtering plans for platform: " + platform);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    // Search plans based on a query (case-insensitive search in serviceName,
    // planName, and features)
    public List<StreamingServicePlan> searchPlans(String query) {
        try {
            String lowerCaseQuery = query != null ? query.toLowerCase() : "";
            return plans.stream()
                    .filter(plan -> plan.getServiceName().toLowerCase().contains(lowerCaseQuery) ||
                            plan.getPlanName().toLowerCase().contains(lowerCaseQuery) ||
                            plan.getFeatures().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching plans for query: " + query);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Load page content from CSV file
    private List<PageContent> loadPageContentFromCsv() {
        List<PageContent> webPages = new ArrayList<>();
        Set<String> uniqueUrls = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Crawled_Website_Data.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(",", 2);
                    if (values.length == 2) {
                        String title = values[0].trim();
                        String content = values[1].trim();

                        if (uniqueUrls.add(title)) {
                            webPages.add(new PageContent(title, content));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing row: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Crawled_Website_Data.csv");
            e.printStackTrace();
        }

        return webPages;
    }

    // Boyer-Moore algorithm for counting occurrences of a keyword in a text
    public int countOccurrences(String text, String pattern) {
        try {
            Map<Character, Integer> badCharTable = buildBadCharTable(pattern);
            int occurrences = 0;
            int m = pattern.length();
            int n = text.length();

            int shift = 0;
            while (shift <= (n - m)) {
                int j = m - 1;
                while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                    j--;
                }

                if (j < 0) {
                    occurrences++;
                    shift += (shift + m < n) ? m - badCharTable.getOrDefault(text.charAt(shift + m), -1) : 1;
                } else {
                    shift += Math.max(1, j - badCharTable.getOrDefault(text.charAt(shift + j), -1));
                }
            }

            return occurrences;
        } catch (Exception e) {
            System.err.println("Error counting occurrences of pattern: " + pattern);
            e.printStackTrace();
            return 0;
        }
    }

    // Helper method to build the bad character table for the Boyer-Moore algorithm
    public Map<Character, Integer> buildBadCharTable(String pattern) {
        Map<Character, Integer> badCharTable = new HashMap<>();
        try {
            for (int i = 0; i < pattern.length(); i++) {
                bad