package main.java.controller;

import main.java.loader.DataLoader;
import main.java.loader.PersistentHashTable;
import main.java.model.Business;
import main.java.model.Review;
import main.java.service.SimilarityCalculator;

import java.util.*;
import java.util.stream.Collectors;

public class SearchController {
    private SimilarityCalculator similarityCalculator;
    private List<Business> allBusinesses;
    private List<Review> allReviews;
    private Map<String, List<String>> clusters;
    private PersistentHashTable businessMap;

    // Constructor
    public SearchController(List<Business> businesses, List<Review> reviews, Map<String, List<String>> clusters, PersistentHashTable businessMap) {
        this.similarityCalculator = new SimilarityCalculator();
        this.allBusinesses = businesses;
        this.allReviews = reviews;
        this.clusters = clusters;
        this.businessMap = businessMap;
    }

    // Finds and returns a list of businesses similar to the given business name
    public List<Business> findSimilarBusinesses(String searchName) {
        String searchNameLower = searchName.toLowerCase();
        String businessId = businessMap.get(searchNameLower);
        System.out.println("Retrieving business ID for '" + searchNameLower + "': " + businessId);
        // If business ID is not found, return an empty list
        if (businessId == null) {
            System.out.println("No businesses found with the name: " + searchNameLower);
            return List.of();
        }

        // Retrieve matched businesses based on ID
        List<Business> matchedBusinesses = allBusinesses.stream()
                .filter(business -> business.getBusiness_id().equals(businessId))
                .collect(Collectors.toList());

        System.out.println("Matched businesses count: " + matchedBusinesses.size());
        matchedBusinesses.forEach(business -> {
            List<Review> reviewsForBusiness = allReviews.stream()
                    .filter(review -> review.getBusiness_id().equals(business.getBusiness_id()))
                    .collect(Collectors.toList());
            System.out.println("Found " + reviewsForBusiness.size() + " reviews for business ID: " + business.getBusiness_id());
        });

        // Return a list of businesses that are similar to the matched businesses
        return matchedBusinesses.stream()
                .flatMap(business -> similarityCalculator.findSimilarBusinesses(business, allBusinesses, allReviews).stream())
                .filter(similarBusiness -> !similarBusiness.getName().toLowerCase().equals(searchNameLower))
                .distinct()
                .peek(business -> System.out.println("Found similar business: " + business.getName()))
                .collect(Collectors.toList());
    }

    // Returns a set of all cluster names
    public Set<String> getClusters() {
        return clusters.keySet();
    }

    // Returns a list of businesses that belong to the specified cluster.
    public List<Business> getBusinessesInCluster(String cluster) {
        List<String> businessIds = clusters.getOrDefault(cluster, Collections.emptyList());
        return allBusinesses.stream()
                .filter(business -> businessIds.contains(business.getBusiness_id()))
                .collect(Collectors.toList());
    }
    public List<Business> getAllBusinesses() {
        return new ArrayList<>(allBusinesses);  // Return a copy of the list to prevent external modifications
    }
}
