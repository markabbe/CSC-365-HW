package main.java;

import main.java.gui.MainFrame;
import main.java.controller.SearchController;
import main.java.loader.DataLoader;
import main.java.model.Business;
import main.java.loader.PersistentHashTable;
import main.java.model.Review;
import main.java.geo.PathFinder;
import main.java.geo.BusinessLinker;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            File businessMapFile = new File(DataLoader.BUSINESS_MAP_PATH);
            System.out.println("Looking for business map file at: " + businessMapFile.getAbsolutePath());
            PersistentHashTable businessMap = null;
            List<Business> businesses;
            Map<String, List<String>> clusters;

            // Load business data
            DataLoader.loadBusinessData();

            // Load serialized businesses
            businesses = DataLoader.loadSerializedBusinessData();
            if (businesses.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No businesses loaded. Exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Link businesses with their geographical neighbors
            BusinessLinker linker = new BusinessLinker();
            linker.linkBusinesses(businesses);  // This adds neighbors to each business

            // Load and serialize review data, passing the list of businesses
            DataLoader.loadAndSerializeReviewData(businesses);

            // Check and load the business map
            if (businessMapFile.exists()) {
                businessMap = PersistentHashTable.deserializeFromFile(DataLoader.BUSINESS_MAP_PATH);
            } else {
                JOptionPane.showMessageDialog(null, "Business map file does not exist. Exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (businessMap == null) {
                JOptionPane.showMessageDialog(null, "Business map could not be loaded. Exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Load clusters
            clusters = DataLoader.loadClusters();

            // Load serialized review data again to ensure all are up to date
            List<Review> reviews = DataLoader.loadSerializedReviewData();

            if (reviews.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No reviews loaded. Exiting.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PathFinder pathFinder = new PathFinder();

            // Setup and start the GUI
            SearchController searchController = new SearchController(businesses, reviews, clusters, businessMap);
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(searchController, pathFinder, linker);  // Include BusinessLinker instance
                mainFrame.setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to initialize application: " + e.getMessage(), "Initialization Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
