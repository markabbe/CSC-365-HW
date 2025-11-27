package main.java.loader;

import main.java.Main;
import main.java.model.Business;
import main.java.model.Review;
import main.java.parser.JsonParseException;
import main.java.parser.JsonParser;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class DataLoader {
    // Paths for data storage.
    public static final String BUSINESS_DATA_PATH = "business_data/";
    public static final String BUSINESS_MAP_PATH = "business_map/business_mapping.ser";
    public static final String REVIEW_DATA_PATH = "review_data/";

    // Perform clustering of businesses based on the first category listed
    private static void performClustering(List<Business> businesses) throws IOException {
        Map<String, List<String>> clusters = new HashMap<>();
        for (Business business : businesses) {
            List<String> categories = business.getCategoriesList();
            if (!categories.isEmpty()) {
                // Use first category as the primary category
                String primaryCategory = categories.get(0);
                // Group businesses by their primary category
                clusters.computeIfAbsent(primaryCategory, k -> new ArrayList<>()).add(business.getBusiness_id());
            }
        }
        // Serialize the clusters for persistent storage
        serializeClusters(clusters);
    }

    // Serialize the clusters to a file
    private static void serializeClusters(Map<String, List<String>> clusters) throws IOException {
        File clustersFile = new File(BUSINESS_DATA_PATH + "clusters.ser");
        // Ensure the directory exists before writing the file
        File clustersDir = clustersFile.getParentFile();
        if (!clustersDir.exists() && !clustersDir.mkdirs()) {
            throw new IOException("Failed to create directory for clusters.");
        }
        // Write the clusters object to a file using serialization
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(clustersFile))) {
            oos.writeObject(clusters);
        }
    }

    // Load serialized clusters from file.
    public static Map<String, List<String>> loadClusters() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BUSINESS_DATA_PATH + "clusters.ser"))) {
            // Cast the deserialized object to the expected type
            return (Map<String, List<String>>) ois.readObject();
        }
    }

    // Load business data from JSON, serialize the businesses, create and serialize a business map, and perform clustering.
    public static void loadBusinessData() {
        JsonParser jsonParser = new JsonParser();
        List<Business> businesses = null;

        try (InputStream businessStream = Main.class.getClassLoader().getResourceAsStream("yelp_dataset/yelp_academic_dataset_business.json")) {
            if (businessStream == null) throw new FileNotFoundException("Business data file not found.");
            // Parse businesses from the JSON file
            businesses = jsonParser.parseBusinesses(new InputStreamReader(businessStream));
            // Serialize businesses to files
            serializeBusinesses(businesses);
            // Create a persistent hash table that mapping business names to IDs and serialize it
            createAndSerializeBusinessMap(businesses);
            // Cluster businesses and serialize clusters
            performClustering(businesses);
        } catch (IOException | JsonParseException e) {
            JOptionPane.showMessageDialog(null, "Error loading business data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        // Report the number of businesses loaded
        System.out.println("Loaded " + businesses.size() + " businesses.");
    }

    // Serialize a list of businesses to individual files
    public static void serializeBusinesses(List<Business> businesses) throws IOException {
        File businessDataDir = new File(BUSINESS_DATA_PATH);
        if (!businessDataDir.exists() && !businessDataDir.mkdirs()) {
            System.err.println("Failed to create business data directory at " + businessDataDir.getAbsolutePath());
            return;
        }
        for (Business business : businesses) {
            File businessFile = new File(businessDataDir, business.getBusiness_id() + ".ser");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(businessFile))) {
                oos.writeObject(business);
                System.out.println("Serialized business with neighbors: " + business.getName());
            } catch (IOException e) {
                System.err.println("Error serializing business " + business.getName() + ": " + e.getMessage());
            }
        }
    }


    // Deserialize businesses from files to a list
    public static List<Business> loadSerializedBusinessData() throws IOException, ClassNotFoundException {
        File businessDataDir = new File(BUSINESS_DATA_PATH);
        List<Business> businesses = new ArrayList<>();
        // Check if the directory exists
        if (businessDataDir.exists() && businessDataDir.isDirectory()) {
            // List all serialized files in the directory
            File[] files = businessDataDir.listFiles((dir, name) -> name.endsWith(".ser"));
            // Deserialize each file to a Business object and add to the list
            for (File file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    Business business = (Business) ois.readObject();
                    businesses.add(business);
                    System.out.println("Deserialized business: " + business.getName() + " with ID: " + business.getBusiness_id());
                } catch (Exception e) {
                    System.err.println("Error deserializing file " + file.getName() + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("Business data directory does not exist or is not a directory.");
        }
        return businesses;
    }

    // Create and serialize a hash table that maps business names to business IDs
    private static void createAndSerializeBusinessMap(List<Business> businesses) throws IOException {
        File businessMapFile = new File(BUSINESS_MAP_PATH);
        File businessMapDir = businessMapFile.getParentFile();
        if (!businessMapDir.exists() && !businessMapDir.mkdirs()) {
            System.err.println("Failed to create business map directory at " + businessMapDir.getAbsolutePath());
            return;
        }

        // Create the hash table and fill it with businesses
        PersistentHashTable businessMap = new PersistentHashTable(1024);
        for (Business business : businesses) {
            // Use the business name in lowercase as the key
            businessMap.put(business.getName().toLowerCase(), business.getBusiness_id());
            System.out.println("Mapped business: " + business.getName().toLowerCase() + " to ID: " + business.getBusiness_id());
        }

        // Serialize the hash table to a file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(businessMapFile))) {
            oos.writeObject(businessMap);
        }
    }

    // Serialize a list of reviews, checking against existing business IDs
    public static void serializeReviews(List<Review> reviews, List<Business> businesses) throws IOException {
        File reviewDataDir = new File(REVIEW_DATA_PATH);
        // Create the directory if it doesn't exist
        if (!reviewDataDir.exists() && !reviewDataDir.mkdirs()) {
            throw new IOException("Failed to create review data directory at " + reviewDataDir.getAbsolutePath());
        }

        // Keep track of reviews that failed to serialize on the first attempt
        List<Review> failedReviews = new ArrayList<>();
        // Collect business IDs for reference checking
        Set<String> businessIds = businesses.stream().map(Business::getBusiness_id).collect(Collectors.toSet());

        // Attempt to serialize each review
        for (Review review : reviews) {
            // Skip serialization if the review's business ID is not known (Probably not the best way to go about this...)
            if (!businessIds.contains(review.getBusiness_id())) {
                failedReviews.add(review);
                logSerializationError(review);
                continue;
            }
            try {
                // Serialize the review
                serializeReview(review, reviewDataDir);
            } catch (IOException e) {
                // Add to the list of failed reviews if an error occurs
                failedReviews.add(review);
            }
        }

        // Attempt to serialize failed reviews again......
        if (!failedReviews.isEmpty()) {
            System.out.println("Retrying serialization for failed reviews...");
            for (Review review : failedReviews) {
                try {
                    serializeReview(review, reviewDataDir);
                } catch (IOException e) {
                    logSerializationError(review);
                }
            }
        }
    }

    // Serialize an individual review to a file
    private static void serializeReview(Review review, File reviewDataDir) throws IOException {
        // File for storing the serialized review
        File reviewFile = new File(reviewDataDir, review.getReview_id() + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(reviewFile))) {
            // Write the review object to file
            oos.writeObject(review);
            System.out.println("Serialized review: " + review.getReview_id() + " for business ID: " + review.getBusiness_id());
        } catch (IOException e) {
            // Log an error if serialization fails and rethrow the exception
            System.err.println("Failed to serialize review " + review.getReview_id() + ": " + e.getMessage());
            throw e;
        }
    }

    // Log errors that occur during review serialization
    private static void logSerializationError(Review review) {
        String errorLogPath = "error_logs.log";
        String logEntry = new Date() + " - Failed to serialize review: " + review.getReview_id() + " with Business ID: " + review.getBusiness_id() + "\n";

        try {
            Files.write(Paths.get(errorLogPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("Logged serialization error for review ID: " + review.getReview_id());
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    // Deserialize review data from files into a list of Review objects
    public static List<Review> loadSerializedReviewData() throws IOException, ClassNotFoundException {
        File reviewDataDir = new File(REVIEW_DATA_PATH);
        List<Review> reviews = new ArrayList<>();
        if (reviewDataDir.exists() && reviewDataDir.isDirectory()) {
            File[] files = reviewDataDir.listFiles((dir, name) -> name.endsWith(".ser"));
            for (File file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    Review review = (Review) ois.readObject();
                    reviews.add(review);
                    if (reviews.size() < 10) {
                        System.out.println("Deserialized review: " + review.getReview_id() + " for business ID: " + review.getBusiness_id());
                    }
                }
            }
        } else {
            System.err.println("Review data directory does not exist or is not a directory.");
        }
        return reviews;
    }

    // Begin the process of loading and serializing review data (Methods arent in order because im lazy)
    public static void loadAndSerializeReviewData(List<Business> businesses) {
        System.out.println("Starting to load and serialize review data...");
        JsonParser jsonParser = new JsonParser();
        try (InputStream reviewStream = Main.class.getClassLoader().getResourceAsStream("yelp_dataset/yelp_academic_dataset_review.json")) {
            if (reviewStream == null) throw new FileNotFoundException("Review data file not found.");
            List<Review> reviews = jsonParser.parseReviews(new InputStreamReader(reviewStream));
            serializeReviews(reviews, businesses);
            System.out.println("Serialized " + reviews.size() + " reviews.");
        } catch (IOException | JsonParseException e) {
            JOptionPane.showMessageDialog(null, "Error loading review data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method to execute data loading
    public static void main(String[] args) {
        try {
            loadBusinessData();
            List<Business> businesses = loadSerializedBusinessData();
            List<Review> reviews = loadSerializedReviewData();

            validateDataIntegrity(businesses, reviews);
            testReviewMatching(businesses, reviews);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "IO Error occurred: " + e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Class Not Found Error occurred: " + e.getMessage(), "Class Not Found Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Checks each review to ensure it matches a known business
    private static void validateDataIntegrity(List<Business> businesses, List<Review> reviews) {
        System.out.println("Validating data integrity...");
        int unmatchedReviews = 0;
        for (Review review : reviews) {
            boolean matched = businesses.stream().anyMatch(b -> b.getBusiness_id().equals(review.getBusiness_id()));
            if (!matched) {
                unmatchedReviews++;
                System.out.println("Unmatched Review: " + review.getReview_id() + " for Business ID: " + review.getBusiness_id());
            }
        }
        System.out.println("Total unmatched reviews: " + unmatchedReviews);
        System.out.println("Total businesses: " + businesses.size());
        System.out.println("Total reviews: " + reviews.size());
    }

    // Tests the matching of reviews to businesses and reports the number of reviews each business has
    public static void testReviewMatching(List<Business> businesses, List<Review> reviews) {
        System.out.println("Testing review matching...");
        for (Business business : businesses) {
            List<Review> matchedReviews = reviews.stream()
                    .filter(r -> r.getBusiness_id().equals(business.getBusiness_id()))
                    .collect(Collectors.toList());
            System.out.println("Business: " + business.getName() + " (" + business.getBusiness_id() + ") - Reviews found: " + matchedReviews.size());
        }
    }
}