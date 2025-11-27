package main.java.gui;

import main.java.model.Business;
import main.java.controller.SearchController;
import main.java.geo.PathFinder;
import main.java.geo.BusinessLinker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    // Declaration of all the components and controllers used in the GUI
    private JTextField searchTextField;
    private JButton searchButton;
    private JEditorPane resultsEditorPane;
    private SearchController searchController;
    private JComboBox<String> clusterComboBox;
    private JButton showClusterButton;
    private JComboBox<Business> startBusinessComboBox;
    private JComboBox<Business> endBusinessComboBox;
    private JButton findPathButton;
    private JButton reportConnectivityButton;
    private PathFinder pathFinder;
    private BusinessLinker businessLinker;

    // Constructor to init the GUI frame with necessary controllers and utilities
    public MainFrame(SearchController searchController, PathFinder pathFinder, BusinessLinker businessLinker) {
        this.searchController = searchController;
        this.pathFinder = pathFinder;
        this.businessLinker = businessLinker;
        createView();
        setTitle("Business Recommendation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
    }

    // Method to setup the main view of the GUI
    private void createView() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(mainPanel);

        setupCategoryPanel(mainPanel);
        setupPathfindingPanel(mainPanel);
        setupSearchPanel(mainPanel);
        setupReportPanel(mainPanel);
    }

    // Setup for the reporting connectivity panel
    private void setupReportPanel(JPanel mainPanel) {
        JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportConnectivityButton = new JButton("Report Connectivity");
        reportConnectivityButton.addActionListener(this::reportConnectivity);
        reportPanel.add(reportConnectivityButton);
        mainPanel.add(reportPanel, BorderLayout.EAST);
    }

    // Action handler for reporting connectivity
    private void reportConnectivity(ActionEvent e) {
        int numberOfComponents = businessLinker.getNumberOfConnectedComponents();
        JOptionPane.showMessageDialog(this, "Number of connected components: " + numberOfComponents);
    }

    // Setup for the category selection panel
    private void setupCategoryPanel(JPanel mainPanel) {
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("Select Category:"));

        clusterComboBox = new JComboBox<>();
        populateClusterComboBox();
        categoryPanel.add(clusterComboBox);

        showClusterButton = new JButton("Show Category");
        showClusterButton.addActionListener(this::onShowCluster);
        categoryPanel.add(showClusterButton);

        mainPanel.add(categoryPanel, BorderLayout.NORTH);
    }

    // Setup for the pathfinding panel
    private void setupPathfindingPanel(JPanel mainPanel) {
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathPanel.add(new JLabel("Start Business:"));

        startBusinessComboBox = new JComboBox<>(new Vector<>(searchController.getAllBusinesses()));
        startBusinessComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Business) {
                    setText(((Business) value).getName());
                }
                return this;
            }
        });

        pathPanel.add(startBusinessComboBox);
        pathPanel.add(new JLabel("End Business:"));

        endBusinessComboBox = new JComboBox<>(new Vector<>(searchController.getAllBusinesses()));
        endBusinessComboBox.setRenderer(startBusinessComboBox.getRenderer());
        pathPanel.add(endBusinessComboBox);

        findPathButton = new JButton("Find Shortest Path");
        findPathButton.addActionListener(this::onFindPath);
        pathPanel.add(findPathButton);

        mainPanel.add(pathPanel, BorderLayout.CENTER);
    }

    // Setup for the general search panel
    private void setupSearchPanel(JPanel mainPanel) {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchTextField = new JTextField();
        searchPanel.add(searchTextField, BorderLayout.CENTER);

        searchButton = new JButton("Find Similar Businesses");
        searchButton.addActionListener(this::onSearch);
        searchPanel.add(searchButton, BorderLayout.EAST);

        mainPanel.add(searchPanel, BorderLayout.SOUTH);

        resultsEditorPane = new JEditorPane();
        resultsEditorPane.setEditable(false);
        resultsEditorPane.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane(resultsEditorPane);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
    }

    // Handler for showing businesses in the selected category
    private void onShowCluster(ActionEvent e) {
        String selectedCluster = (String) clusterComboBox.getSelectedItem();
        if (selectedCluster != null && !selectedCluster.isEmpty()) {
            List<Business> businessesInCluster = searchController.getBusinessesInCluster(selectedCluster);
            displayResults(businessesInCluster, "Businesses in the category: " + selectedCluster);
        } else {
            resultsEditorPane.setText("<html><body style='font-size:10px;'><strong>Please select a category.</strong></body></html>");
        }
    }

    // Handler for finding businesses similar to the search term
    private void onSearch(ActionEvent e) {
        String searchTerm = searchTextField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Business> similarBusinesses = searchController.findSimilarBusinesses(searchTerm);
            displayResults(similarBusinesses, "Searching for businesses similar to: " + searchTerm);
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a search term.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to display results of the search or category selection
    private void displayResults(List<Business> businesses, String heading) {
        StringBuilder resultsBuilder = new StringBuilder("<html><body style='font-size:12px;'>");
        resultsBuilder.append("<p style='font-size:14px;font-weight:bold;'>").append(heading).append("</p>");

        if (businesses == null || businesses.isEmpty()) {
            resultsBuilder.append("<p>No businesses found.</p>");
        } else {
            for (Business business : businesses) {
                resultsBuilder.append("<p>")
                        .append(business.getName())
                        .append(" - ")
                        .append(business.getAddress())
                        .append("</p>");
            }
        }

        resultsBuilder.append("</body></html>");
        resultsEditorPane.setText(resultsBuilder.toString());
    }

    // Handler for finding the shortest path between selected businesses
    private void onFindPath(ActionEvent e) {
        Business start = (Business) startBusinessComboBox.getSelectedItem();
        Business end = (Business) endBusinessComboBox.getSelectedItem();
        if (start != null && end != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<List<Business>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Business> doInBackground() {
                    return pathFinder.findShortestPath(start, end, new ArrayList<>(searchController.getAllBusinesses()));
                }

                @Override
                protected void done() {
                    try {
                        List<Business> path = get();
                        displayPathResults(path);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Error finding path: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(this, "Please select both start and end businesses.", "Selection Needed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Displays the path found or a message if no path exists
    private void displayPathResults(List<Business> path) {
        if (path.isEmpty()) {
            resultsEditorPane.setText("<html><body><p>No path found between the selected businesses.</p></body></html>");
            return;
        }

        // Start the HTML with the "Best route" heading
        StringBuilder sb = new StringBuilder("<html><body><p><strong>Best route:</strong></p>");
        Business previous = null;
        double totalDistance = 0.0;
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                double distance = path.get(i-1).getNeighbors().get(path.get(i));
                totalDistance += distance;
                sb.append("<span style='color:green;'> -> (")
                        .append(String.format("%.2f km", distance))
                        .append(") </span>");
            }
            sb.append("<span style='color:green;'>").append(path.get(i).getName()).append("</span>");
        }

        // Add the total distance traveled at the end of the route
        sb.append("<p>Total Distance: ").append(String.format("%.2f km", totalDistance)).append("</p>");

        // Add a section for other connections
        sb.append("<p>Other connections to consider:</p>");
        for (Business business : path) {
            Map<Business, Double> neighbors = business.getNeighbors();
            List<Map.Entry<Business, Double>> sortedNeighbors = new ArrayList<>(neighbors.entrySet());
            sortedNeighbors.sort(Map.Entry.comparingByValue());
            int count = 0;
            for (Map.Entry<Business, Double> entry : sortedNeighbors) {
                if (!path.contains(entry.getKey()) && count < 5) { // Limit to 5 connections for clarity
                    sb.append("<p style='color:red;'>")
                            .append(business.getName())
                            .append(" -> ")
                            .append(entry.getKey().getName())
                            .append("</p>");
                    count++;
                }
            }
        }

        // Close the HTML tags and set the text in the editor pane
        sb.append("</body></html>");
        resultsEditorPane.setText(sb.toString());
        resultsEditorPane.revalidate();
        resultsEditorPane.repaint();
    }

    // Populates the category combo box with available categories
    private void populateClusterComboBox() {
        Set<String> clusters = searchController.getClusters();
        for (String cluster : clusters) {
            clusterComboBox.addItem(cluster);
        }
    }

}
