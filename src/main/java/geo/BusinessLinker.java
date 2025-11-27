package main.java.geo;

import main.java.model.Business;
import main.java.geo.GeoUtils;
import java.util.*;

public class BusinessLinker {
    // UnionFind data structure to track connected components
    private UnionFind unionFind;
    private List<Business> businessList;

    // Method to link each business with its 4 closest geographic neighbors
    public void linkBusinesses(List<Business> businesses) {
        this.businessList = businesses;
        this.unionFind = new UnionFind(businesses.size());

        System.out.println("Linking each business with its 4 closest neighbors...");
        // Map to hold priority queues for each business, which store the closest neighbors.
        Map<Business, PriorityQueue<BusinessDistance>> businessNeighbors = new HashMap<>();

        // Initialize priority queues for each business
        for (Business business : businesses) {
            businessNeighbors.put(business, new PriorityQueue<>(4, Comparator.comparingDouble(bd -> bd.distance)));
        }

        // Calculate distances between all pairs of businesses to populate the priority queues
        for (Business current : businesses) {
            for (Business other : businesses) {
                if (!current.equals(other)) {
                    double distance = GeoUtils.haversine(current.getLatitude(), current.getLongitude(),
                            other.getLatitude(), other.getLongitude());
                    PriorityQueue<BusinessDistance> currentNeighbors = businessNeighbors.get(current);
                    PriorityQueue<BusinessDistance> otherNeighbors = businessNeighbors.get(other);

                    // Add new neighbor or replace the farthest one if 4 closer neighbors already exist
                    if (currentNeighbors.size() < 4) {
                        currentNeighbors.add(new BusinessDistance(other, distance));
                    } else if (distance < currentNeighbors.peek().distance) {
                        currentNeighbors.poll();
                        currentNeighbors.add(new BusinessDistance(other, distance));
                    }

                    if (otherNeighbors.size() < 4) {
                        otherNeighbors.add(new BusinessDistance(current, distance));
                    } else if (distance < otherNeighbors.peek().distance) {
                        otherNeighbors.poll();
                        otherNeighbors.add(new BusinessDistance(current, distance));
                    }
                }
            }
        }

        // Link each business with its neighbors in a bidirectional manner
        for (Business business : businesses) {
            PriorityQueue<BusinessDistance> neighbors = businessNeighbors.get(business);
            while (!neighbors.isEmpty()) {
                BusinessDistance bd = neighbors.poll();
                business.addNeighbor(bd.business, bd.distance);
                bd.business.addNeighbor(business, bd.distance);
                // Inside the loop where you link neighbors
                System.out.print(business.getName() + " is linked with: " + bd.business.getName() + ", ");
            }
            System.out.println();
        }

        // Update UnionFind structure for each connection
        for (int i = 0; i < businesses.size(); i++) {
            Business current = businesses.get(i);
            for (Business neighbor : current.getNeighbors().keySet()) {
                int j = businesses.indexOf(neighbor);
                unionFind.union(i, j);
            }
        }
    }

    // Helper class to store business and distance pair
    static class BusinessDistance {
        Business business;
        double distance;

        BusinessDistance(Business business, double distance) {
            this.business = business;
            this.distance = distance;
        }
    }

    // Method to retrieve the number of connected components
    public int getNumberOfConnectedComponents() {
        return unionFind.getCount();
    }
}
