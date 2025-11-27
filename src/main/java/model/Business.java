package main.java.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;
import java.io.Serializable;


public class Business implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String business_id;
    private String name;
    private Map<Business, Double> neighbors;
    private String address;
    private String city;
    private String state;
    private String postal_code;
    private double latitude;
    private double longitude;
    private float stars;
    private int review_count;
    private int is_open;
    private Map<String, Boolean> attributes;
    private String categories;
    private Map<String, String> hours;
    private String phoneNumber;

    // Constructor
    public Business(String business_id, String name, String address, String city, String state,
                    String postal_code, double latitude, double longitude, float stars, int review_count,
                    int is_open, Map<String, Boolean> attributes, String categories, Map<String, String> hours, String phoneNumber) {
        this.business_id = business_id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postal_code = postal_code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stars = stars;
        this.review_count = review_count;
        this.is_open = is_open;
        this.attributes = attributes;
        this.categories = categories;
        this.hours = hours;
        this.phoneNumber = phoneNumber;
        this.neighbors = new HashMap<>();
    }

    // Setters and getters:

    public String getBusiness_id() {
        return business_id;
    }

    public void setBusiness_id(String business_id) {
        this.business_id = business_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    public int getReview_count() {
        return review_count;
    }

    public void setReview_count(int review_count) {
        this.review_count = review_count;
    }

    public boolean is_open() {
        return is_open == 1;
    }

    public void setIs_open(int is_open) {
        this.is_open = is_open;
    }

    public Map<String, Boolean> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Boolean> attributes) {
        this.attributes = attributes;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public List<String> getCategoriesList() {
        if (categories == null || categories.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(categories.split(",\\s*"));
    }

    public Map<String, String> getHours() {
        return hours;
    }

    public void setHours(Map<String, String> hours) {
        this.hours = hours;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void addNeighbor(Business neighbor, double distance) {
        if (this.neighbors == null) {
            System.out.println("Neighbor map is null for business: " + this.name);
            this.neighbors = new HashMap<>();
        }
        this.neighbors.put(neighbor, distance);
    }

    public Map<Business, Double> getNeighbors() {
        return this.neighbors;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (neighbors == null) {
            neighbors = new HashMap<>();
        }
    }
}