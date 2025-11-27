package main.java.geo;

import main.java.model.Business;

import java.util.*;

public class PathFinder {
    // Method to find the shortest path between two businesses using Dijkstra's algorithm
    public List<Business> findShortestPath(Business start, Business end, List<Business> businesses) {
        // Maps to store the previous business in the shortest path and the shortest distances to each business
        Map<Business, Business> previous = new HashMap<>();
        Map<Business, Double> shortestDistances = new HashMap<>();
        // Priority queue to manage exploration based on shortest known distances.
        PriorityQueue<Business> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(b -> shortestDistances.getOrDefault(b, Double.MAX_VALUE)));

        // Initialize distances and add all businesses to the priority queue
        for (Business business : businesses) {
            if (business.equals(start)) {
                shortestDistances.put(business, 0.0);
            } else {
                shortestDistances.put(business, Double.MAX_VALUE);
            }
            priorityQueue.add(business);
        }

        // Process each business in the priority queue
        while (!priorityQueue.isEmpty()) {
            Business current = priorityQueue.poll();
            if (current.equals(end)) {
                return reconstructPath(previous, end);
            }

            // Explore neighbors of the current business
            Map<Business, Double> neighbors = current.getNeighbors();
            if (neighbors != null) {
                for (Map.Entry<Business, Double> neighborEntry : neighbors.entrySet()) {
                    Business neighbor = neighborEntry.getKey();
                    double distance = neighborEntry.getValue();
                    double newDist = shortestDistances.get(current) + distance;
                    if (newDist < shortestDistances.get(neighbor)) {
                        shortestDistances.put(neighbor, newDist);
                        previous.put(neighbor, current);
                        priorityQueue.add(neighbor);
                    }
                }
            } else {
                System.out.println("No neighbors found for business: " + current.getName());
            }
        }
        return Collections.emptyList();
    }

    // Helper method to reconstruct the path from start to end
    private List<Business> reconstructPath(Map<Business, Business> previous, Business end) {
        List<Business> path = new ArrayList<>();
        // Traverse from end to start using the previous map
        for (Business at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
