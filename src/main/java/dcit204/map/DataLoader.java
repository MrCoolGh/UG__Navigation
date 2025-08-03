package dcit204.map;

import java.util.*;

public class DataLoader {
    private Map<String, Map<String, Double>> graph;
    private List<String> locations;
    private List<String> landmarks;

    public DataLoader() {
        this.graph = new HashMap<>();
        this.locations = new ArrayList<>();
        this.landmarks = new ArrayList<>();
    }

    public void loadData() {
        // Load hardcoded data since there's an issue with JSON library
        loadHardcodedData();
    }

    // Method to add a custom location to the graph
    public void addCustomLocation(String location) {
        if (!locations.contains(location)) {
            locations.add(location);
            graph.put(location, new HashMap<>());

            // Connect this location to the nearest three known locations
            // This is a simplification - in a real app, you'd use geographic coordinates
            connectToNearestLocations(location, 3);
        }
    }

    // Method to add a custom landmark
    public void addCustomLandmark(String landmark) {
        if (!landmarks.contains(landmark)) {
            landmarks.add(landmark);

            // If it's also a location, it's already in the graph
            // Otherwise, add it as a location too
            if (!locations.contains(landmark)) {
                addCustomLocation(landmark);
            }
        }
    }

    // Connect a new location to some existing locations
    private void connectToNearestLocations(String newLocation, int numberOfConnections) {
        // This is a simplified version - in a real app, you'd use geographic coordinates
        List<String> otherLocations = new ArrayList<>(locations);
        otherLocations.remove(newLocation);

        // Shuffle to randomize connections
        Collections.shuffle(otherLocations);

        // Connect to the first N locations
        for (int i = 0; i < Math.min(numberOfConnections, otherLocations.size()); i++) {
            String otherLocation = otherLocations.get(i);

            // Generate a random distance between 100 and 800 meters
            double distance = 100 + Math.random() * 700;

            // Add bidirectional connection
            if (!graph.containsKey(newLocation)) {
                graph.put(newLocation, new HashMap<>());
            }
            graph.get(newLocation).put(otherLocation, distance);

            if (!graph.containsKey(otherLocation)) {
                graph.put(otherLocation, new HashMap<>());
            }
            graph.get(otherLocation).put(newLocation, distance);
        }
    }

    private void loadHardcodedData() {
        // Hardcoded data for UG campus locations and distances
        locations = Arrays.asList(
                "Main Gate",
                "Commonwealth Hall",
                "Legon Hall",
                "Akuafo Hall",
                "Balme Library",
                "JQB Building",
                "Mathematics Department",
                "Computer Science Department",
                "School of Engineering",
                "Business School",
                "UGCS Bank",
                "Great Hall",
                "Athletic Oval",
                "Night Market",
                "Diaspora",
                "International Students Hostel",
                "Valco Trust Hostel",
                "TF Hostel"
        );

        landmarks = Arrays.asList(
                "Balme Library",
                "Great Hall",
                "UGCS Bank",
                "Night Market",
                "JQB Building",
                "Athletic Oval",
                "Commonwealth Hall",
                "Business School"
        );

        // Create the graph with distances (in meters)

        // Main Gate connections
        Map<String, Double> mainGateNeighbors = new HashMap<>();
        mainGateNeighbors.put("Great Hall", 400.0);
        mainGateNeighbors.put("UGCS Bank", 600.0);
        graph.put("Main Gate", mainGateNeighbors);

        // Great Hall connections
        Map<String, Double> greatHallNeighbors = new HashMap<>();
        greatHallNeighbors.put("Main Gate", 400.0);
        greatHallNeighbors.put("Commonwealth Hall", 300.0);
        greatHallNeighbors.put("Business School", 500.0);
        graph.put("Great Hall", greatHallNeighbors);

        // Commonwealth Hall connections
        Map<String, Double> commonwealthNeighbors = new HashMap<>();
        commonwealthNeighbors.put("Great Hall", 300.0);
        commonwealthNeighbors.put("Legon Hall", 250.0);
        commonwealthNeighbors.put("Balme Library", 400.0);
        commonwealthNeighbors.put("Athletic Oval", 350.0);
        graph.put("Commonwealth Hall", commonwealthNeighbors);

        // Legon Hall connections
        Map<String, Double> legonHallNeighbors = new HashMap<>();
        legonHallNeighbors.put("Commonwealth Hall", 250.0);
        legonHallNeighbors.put("Akuafo Hall", 200.0);
        legonHallNeighbors.put("Night Market", 450.0);
        graph.put("Legon Hall", legonHallNeighbors);

        // Akuafo Hall connections
        Map<String, Double> akuafoHallNeighbors = new HashMap<>();
        akuafoHallNeighbors.put("Legon Hall", 200.0);
        akuafoHallNeighbors.put("Balme Library", 350.0);
        akuafoHallNeighbors.put("Night Market", 300.0);
        graph.put("Akuafo Hall", akuafoHallNeighbors);

        // Balme Library connections
        Map<String, Double> balmeLibraryNeighbors = new HashMap<>();
        balmeLibraryNeighbors.put("Commonwealth Hall", 400.0);
        balmeLibraryNeighbors.put("Akuafo Hall", 350.0);
        balmeLibraryNeighbors.put("JQB Building", 200.0);
        balmeLibraryNeighbors.put("Mathematics Department", 250.0);
        graph.put("Balme Library", balmeLibraryNeighbors);

        // JQB Building connections
        Map<String, Double> jqbBuildingNeighbors = new HashMap<>();
        jqbBuildingNeighbors.put("Balme Library", 200.0);
        jqbBuildingNeighbors.put("Mathematics Department", 150.0);
        jqbBuildingNeighbors.put("Computer Science Department", 200.0);
        graph.put("JQB Building", jqbBuildingNeighbors);

        // Mathematics Department connections
        Map<String, Double> mathDeptNeighbors = new HashMap<>();
        mathDeptNeighbors.put("Balme Library", 250.0);
        mathDeptNeighbors.put("JQB Building", 150.0);
        mathDeptNeighbors.put("Computer Science Department", 150.0);
        mathDeptNeighbors.put("School of Engineering", 300.0);
        graph.put("Mathematics Department", mathDeptNeighbors);

        // Computer Science Department connections
        Map<String, Double> csDeptNeighbors = new HashMap<>();
        csDeptNeighbors.put("JQB Building", 200.0);
        csDeptNeighbors.put("Mathematics Department", 150.0);
        csDeptNeighbors.put("School of Engineering", 200.0);
        graph.put("Computer Science Department", csDeptNeighbors);

        // School of Engineering connections
        Map<String, Double> engineeringNeighbors = new HashMap<>();
        engineeringNeighbors.put("Mathematics Department", 300.0);
        engineeringNeighbors.put("Computer Science Department", 200.0);
        engineeringNeighbors.put("Business School", 400.0);
        graph.put("School of Engineering", engineeringNeighbors);

        // Business School connections
        Map<String, Double> businessSchoolNeighbors = new HashMap<>();
        businessSchoolNeighbors.put("Great Hall", 500.0);
        businessSchoolNeighbors.put("School of Engineering", 400.0);
        businessSchoolNeighbors.put("UGCS Bank", 250.0);
        graph.put("Business School", businessSchoolNeighbors);

        // UGCS Bank connections
        Map<String, Double> bankNeighbors = new HashMap<>();
        bankNeighbors.put("Main Gate", 600.0);
        bankNeighbors.put("Business School", 250.0);
        graph.put("UGCS Bank", bankNeighbors);

        // Athletic Oval connections
        Map<String, Double> athleticOvalNeighbors = new HashMap<>();
        athleticOvalNeighbors.put("Commonwealth Hall", 350.0);
        athleticOvalNeighbors.put("Night Market", 500.0);
        athleticOvalNeighbors.put("Diaspora", 550.0);
        graph.put("Athletic Oval", athleticOvalNeighbors);

        // Night Market connections
        Map<String, Double> nightMarketNeighbors = new HashMap<>();
        nightMarketNeighbors.put("Legon Hall", 450.0);
        nightMarketNeighbors.put("Akuafo Hall", 300.0);
        nightMarketNeighbors.put("Athletic Oval", 500.0);
        nightMarketNeighbors.put("Diaspora", 400.0);
        graph.put("Night Market", nightMarketNeighbors);

        // Diaspora connections
        Map<String, Double> diasporaNeighbors = new HashMap<>();
        diasporaNeighbors.put("Athletic Oval", 550.0);
        diasporaNeighbors.put("Night Market", 400.0);
        diasporaNeighbors.put("International Students Hostel", 300.0);
        diasporaNeighbors.put("TF Hostel", 450.0);
        graph.put("Diaspora", diasporaNeighbors);

        // International Students Hostel connections
        Map<String, Double> ishNeighbors = new HashMap<>();
        ishNeighbors.put("Diaspora", 300.0);
        ishNeighbors.put("Valco Trust Hostel", 400.0);
        graph.put("International Students Hostel", ishNeighbors);

        // Valco Trust Hostel connections
        Map<String, Double> valcoNeighbors = new HashMap<>();
        valcoNeighbors.put("International Students Hostel", 400.0);
        valcoNeighbors.put("TF Hostel", 300.0);
        graph.put("Valco Trust Hostel", valcoNeighbors);

        // TF Hostel connections
        Map<String, Double> tfHostelNeighbors = new HashMap<>();
        tfHostelNeighbors.put("Diaspora", 450.0);
        tfHostelNeighbors.put("Valco Trust Hostel", 300.0);
        graph.put("TF Hostel", tfHostelNeighbors);
    }

    public Map<String, Map<String, Double>> getGraph() {
        return graph;
    }

    public List<String> getLocations() {
        return new ArrayList<>(locations);  // Return a copy to prevent modification
    }

    public List<String> getLandmarks() {
        return new ArrayList<>(landmarks);  // Return a copy to prevent modification
    }
}