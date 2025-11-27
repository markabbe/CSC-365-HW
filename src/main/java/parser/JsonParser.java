package main.java.parser;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import main.java.model.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;

public class JsonParser {
    private final Gson gson = new Gson(); // Create a Gson instance
    private final int MAX_RECORDS = 10000;

    // Parse the businesses
    public List<Business> parseBusinesses(Reader reader) throws JsonParseException {
        List<Business> businessList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            // Read each line of JSON data until either hit end of file or the maximum record limit is met
            while ((line = bufferedReader.readLine()) != null && businessList.size() < MAX_RECORDS) {
                Business business = gson.fromJson(line, Business.class); // Convert the JSON line to a Business object
                businessList.add(business);// Add the parsed business object to the list
            }
        } catch (IOException e) {
            // Throw JsonParseException if an IO error occurs during reading or parsing.
            throw new JsonParseException("Failed to parse businesses from JSON", e);
        }
        return businessList;
    }

    //Parse the reviews
    public List<Review> parseReviews(Reader reader) throws JsonParseException {
        List<Review> reviewList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            // Read each line of JSON data until either hit end of file or the maximum record limit is met
            while ((line = bufferedReader.readLine()) != null && reviewList.size() < MAX_RECORDS) {
                Review review = gson.fromJson(line, Review.class); // Convert the JSON line to a Review object
                reviewList.add(review); // Add the parsed review object to the list
            }
        } catch (IOException e) {
            // Throw JsonParseException if an IO error occurs during reading or parsing.
            throw new JsonParseException("Failed to parse reviews from JSON", e);
        }
        return reviewList;
    }
}