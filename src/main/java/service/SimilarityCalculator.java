package main.java.service;

import main.java.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class SimilarityCalculator {

    // Define a node for the custom frequency table, holding information about a term.
    private static final class FrequencyNode {
        String term;
        int termCount;
        Set<String> documents;
        FrequencyNode next;

        // Constructor
        FrequencyNode(String term, FrequencyNode next) {
            this.term = term;
            this.termCount = 1;
            this.documents = new HashSet<>();
            this.next = next;
        }
    }

    // The table itself, initially sized at 8.
    private FrequencyNode[] frequencyTable = new FrequencyNode[8];
    private int documentCount = 0;

    // Updates the frequency count for a term or creates a new node if the term isnt found
    private void addOrUpdateFrequency(String term, String documentId) {
        int index = getIndex(term);
        FrequencyNode node = findOrCreateNode(term, index);
        node.termCount++;
        node.documents.add(documentId);
    }

    // Computes the index for a term in the frequency table using its hash code
    private int getIndex(String term) {
        return term.hashCode() & (frequencyTable.length - 1);
    }

    // Finds an existing node for the term or creates a new one if not found
    private FrequencyNode findOrCreateNode(String term, int index) {
        for (FrequencyNode current = frequencyTable[index]; current != null; current = current.next) {
            if (current.term.equals(term)) {
                return current;
            }
        }
        FrequencyNode newNode = new FrequencyNode(term, frequencyTable[index]);
        frequencyTable[index] = newNode;
        return newNode;
    }

    // Calculate TF-IDF values for terms in the documents related to a business
    public Map<String, Double> calculateTfIdf(Business business, List<Review> reviews) {
        resetFrequencyTable();
        documentCount = reviews.size();

        // Process each document (review) for the given business
        for (Review review : reviews) {
            String documentId = review.getReview_id();
            processDocument(review, documentId, business);
        }

        return calculateTfIdfValues();
    }

    // Resets the frequency table to its initial state
    private void resetFrequencyTable() {
        frequencyTable = new FrequencyNode[8];
    }

    // Processes a single document and update frequency counts for each term
    private void processDocument(Review review, String documentId, Business business) {
        String preprocessedText = preprocessReviewText(review.getText());
        String enhancedText = enhanceReviewText(business, preprocessedText);
        String[] terms = enhancedText.toLowerCase().split("\\s+");

        // Update frequency counts for each term in this document
        for (String term : terms) {
            addOrUpdateFrequency(term, documentId);
        }
    }

    // Calculates TF-IDF values using the custom frequency table
    private Map<String, Double> calculateTfIdfValues() {
        Map<String, Double> tfIdf = new HashMap<>();
        // Iterate over each bucket in the frequency table
        for (FrequencyNode bucket : frequencyTable) {
            // Iterate over each node in the bucket
            for (FrequencyNode node = bucket; node != null; node = node.next) {
                double tf = (double) node.termCount / documentCount;
                double idf = Math.log((double) documentCount / node.documents.size());
                tfIdf.put(node.term, tf * idf);
            }
        }
        return tfIdf;
    }

    // Calculate the cosine similarity between two TF-IDF vectors
    public double calculateCosineSimilarity(Map<String, Double> tfIdfA, Map<String, Double> tfIdfB) {
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(tfIdfA.keySet());
        allTerms.addAll(tfIdfB.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        // Compute dot product and norms for cosine similarity calculation
        for (String term : allTerms) {
            double tfIdfValueA = tfIdfA.getOrDefault(term, 0.0);
            double tfIdfValueB = tfIdfB.getOrDefault(term, 0.0);

            dotProduct += tfIdfValueA * tfIdfValueB;
            normA += Math.pow(tfIdfValueA, 2);
            normB += Math.pow(tfIdfValueB, 2);
        }

        // Avoid division by zero
        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        // Return the cosine similarity score
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Finds businesses similar to a target business based on text and category similarity
    public List<Business> findSimilarBusinesses(Business targetBusiness, List<Business> allBusinesses, List<Review> allReviews) {
        if (targetBusiness == null) {
            System.out.println("Target business is null.");
            return Collections.emptyList();
        }

        List<Review> targetReviews = getReviewsForBusiness(targetBusiness, allReviews);
        if (targetReviews == null || targetReviews.isEmpty()) {
            System.out.println("No reviews found for target business: " + targetBusiness.getName());
            return Collections.emptyList();
        }
        Map<String, Double> targetTfIdf = calculateTfIdf(targetBusiness, getReviewsForBusiness(targetBusiness, allReviews));
        Map<Business, Double> similarityScores = new HashMap<>();

        // Compare the target business to each business in the list.
        for (Business business : allBusinesses) {
            if (!business.equals(targetBusiness)) {
                // Calculate TF-IDF for the compared business
                List<Review> businessReviews = getReviewsForBusiness(business, allReviews);
                Map<String, Double> businessTfIdf = calculateTfIdf(business, businessReviews);

                // Calculate text and category similarity
                double textSimilarity = calculateCosineSimilarity(targetTfIdf, businessTfIdf);
                double categorySimilarity = calculateCategorySimilarity(targetBusiness, business);

                // Combine text and category similarities into a final score
                double finalScore = textSimilarity * 0.3 + categorySimilarity * 0.7;
                similarityScores.put(business, finalScore);

                System.out.println("Similarity with " + business.getName() + ": Text=" + textSimilarity + ", Category=" + categorySimilarity + ", Final=" + finalScore);
            }
        }

        // Sort businesses by similarity score and return the top 10
        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }

    // Calculate the similarity between two businesses based on the categories they belong to
    private double calculateCategorySimilarity(Business businessA, Business businessB) {
        // Retrieve the list of categories for each business
        List<String> categoriesA = businessA.getCategoriesList();
        List<String> categoriesB = businessB.getCategoriesList();

        // Count the number of common categories between the two businesses
        long commonCategories = categoriesA.stream().filter(categoriesB::contains).count();

        // Calculate and return the category similarity score
        return (double) commonCategories / (categoriesA.size() + categoriesB.size() - commonCategories);
    }

    // Gets all reviews associated with a business
    private List<Review> getReviewsForBusiness(Business business, List<Review> reviews) {
        List<Review> businessReviews = new ArrayList<>();
        System.out.println("Searching for reviews for business ID: " + business.getBusiness_id());
        int reviewCount = 0;
        for (Review review : reviews) {
            if (review.getBusiness_id().equals(business.getBusiness_id())) {
                businessReviews.add(review);
                reviewCount++;
            }
        }
        System.out.println("Found " + reviewCount + " reviews for business ID: " + business.getBusiness_id());
        return businessReviews;
    }

    // Enhance the text of a review by appending categories and attributes of the business
    private String enhanceReviewText(Business business, String reviewText) {
        StringBuilder enhancedText = new StringBuilder(reviewText);

        // Append categories to the review text
        if (business.getCategories() != null) {
            for (String category : business.getCategoriesList()) {
                enhancedText.append(" ").append(category);
            }
        }

        // Append attributes to the review text if they are present/true
        if (business.getAttributes() != null) {
            business.getAttributes().forEach((key, value) -> {
                if (value) {
                    enhancedText.append(" ").append(key);
                }
            });
        }

        return enhancedText.toString();
    }

    // Preprocesses the review text by converting it to lowercase, and removing punctuation, stopwords, and extra spaces
    private String preprocessReviewText(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        text = removeStopWords(text);
        text = text.trim().replaceAll("\\s+", " ");
        return text;
    }

    // Removes common English stopwords from the text
    private String removeStopWords(String text) {
        // Typical list of stopWords that i found online
        Set<String> stopWords = Set.of(
                "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
                "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers",
                "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves",
                "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are",
                "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does",
                "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until",
                "while", "of", "at", "by", "for", "with", "about", "against", "between", "into",
                "through", "during", "before", "after", "above", "below", "to", "from", "up", "down",
                "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here",
                "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more",
                "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
                "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"
        );

        return Arrays.stream(text.split("\\s+"))
                .filter(term -> !stopWords.contains(term))
                .collect(Collectors.joining(" "));
    }

    // Prints the top similar businesses based on cosine similarity scores
    // Remove this, probably dont need anymore
    private void printTopSimilarBusinesses(Map<Business, Double> scores) {
        System.out.println("Top similar businesses based on cosine similarity:");
        scores.entrySet().stream()
                .sorted(Map.Entry.<Business, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> System.out.println(entry.getKey().getName() + " - Score: " + entry.getValue()));
    }
}