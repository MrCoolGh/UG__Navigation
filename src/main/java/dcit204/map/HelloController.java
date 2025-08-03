package dcit204.map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelloController {
    @FXML
    private ComboBox<String> startLocationComboBox;

    @FXML
    private ComboBox<String> destinationComboBox;

    @FXML
    private ComboBox<String> landmarkComboBox;

    @FXML
    private WebView mapWebView;

    // Keep the reference even though it's removed from FXML
    @FXML
    private TableView<RouteOption> routesTableView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button findRouteButton;

    @FXML
    private VBox routeDetailsBox;

    @FXML
    private VBox landmarkRoutesBox;

    @FXML
    private CheckBox useLandmarkCheckBox;

    @FXML
    private HBox landmarkSelectionBox;

    @FXML
    private ListView<String> selectedLandmarksListView;

    @FXML
    private Button addLandmarkButton;

    @FXML
    private Button removeLandmarkButton;

    @FXML
    private RadioButton shortestDistanceRadio;

    @FXML
    private RadioButton optimalTimeRadio;

    private final DataLoader dataLoader = new DataLoader();
    private final RouteFinder routeFinder = new RouteFinder();
    private final DistanceCalculator distanceCalculator = new DistanceCalculator();
    private final SearchAndLandmarks searchAndLandmarks = new SearchAndLandmarks();
    private final SortingAlgorithms sortingAlgorithms = new SortingAlgorithms();
    private final TrafficSimulator trafficSimulator = new TrafficSimulator();

    private ObservableList<String> selectedLandmarks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load campus data
        dataLoader.loadData();

        // Populate location dropdowns
        List<String> locations = dataLoader.getLocations();
        ObservableList<String> observableLocations = FXCollections.observableArrayList(locations);
        startLocationComboBox.setItems(observableLocations);
        destinationComboBox.setItems(observableLocations);

        // Make comboboxes editable to allow typing
        startLocationComboBox.setEditable(true);
        destinationComboBox.setEditable(true);

        // Populate landmark dropdown
        List<String> landmarks = dataLoader.getLandmarks();
        ObservableList<String> observableLandmarks = FXCollections.observableArrayList(landmarks);
        landmarkComboBox.setItems(observableLandmarks);
        landmarkComboBox.setEditable(true);

        // Initialize the map
        initializeMap();

        // Initialize table columns - only if routesTableView exists
        if (routesTableView != null) {
            initializeRoutesTable();
        }

        // Setup landmark selection
        setupLandmarkSelection();

        // Set button action
        findRouteButton.setOnAction(event -> findRoutes());

        // Setup route criteria toggle group
        ToggleGroup routeCriteriaGroup = new ToggleGroup();
        shortestDistanceRadio.setToggleGroup(routeCriteriaGroup);
        optimalTimeRadio.setToggleGroup(routeCriteriaGroup);
        optimalTimeRadio.setSelected(true);

        // Set landmark option visibility listener
        useLandmarkCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            landmarkSelectionBox.setVisible(newVal);
            landmarkSelectionBox.setManaged(newVal);
        });

        // Initialize with landmarks section hidden
        landmarkSelectionBox.setVisible(false);
        landmarkSelectionBox.setManaged(false);

        // Setup selected landmarks list
        selectedLandmarksListView.setItems(selectedLandmarks);

        // Setup add/remove landmark buttons
        setupLandmarkButtons();

        // Setup keyboard handlers for entering custom locations
        setupKeyboardHandlers();
    }

    private void setupKeyboardHandlers() {
        // Handler for start location
        startLocationComboBox.getEditor().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = startLocationComboBox.getEditor().getText();
                if (!text.isEmpty() && !startLocationComboBox.getItems().contains(text)) {
                    startLocationComboBox.getItems().add(text);
                    startLocationComboBox.setValue(text);
                }
            }
        });

        // Handler for destination
        destinationComboBox.getEditor().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = destinationComboBox.getEditor().getText();
                if (!text.isEmpty() && !destinationComboBox.getItems().contains(text)) {
                    destinationComboBox.getItems().add(text);
                    destinationComboBox.setValue(text);
                }
            }
        });

        // Handler for landmark
        landmarkComboBox.getEditor().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = landmarkComboBox.getEditor().getText();
                if (!text.isEmpty() && !landmarkComboBox.getItems().contains(text)) {
                    landmarkComboBox.getItems().add(text);
                    landmarkComboBox.setValue(text);

                    // Auto-add to selected landmarks if the box is checked
                    if (useLandmarkCheckBox.isSelected() && !selectedLandmarks.contains(text)) {
                        selectedLandmarks.add(text);
                    }
                }
            }
        });
    }

    private void setupLandmarkButtons() {
        addLandmarkButton.setOnAction(event -> {
            String selectedLandmark = landmarkComboBox.getValue();
            if (selectedLandmark != null && !selectedLandmarks.contains(selectedLandmark)) {
                selectedLandmarks.add(selectedLandmark);

                // Add to graph if it's a custom landmark
                if (!dataLoader.getLandmarks().contains(selectedLandmark)) {
                    dataLoader.addCustomLandmark(selectedLandmark);
                }
            }
        });

        removeLandmarkButton.setOnAction(event -> {
            String selectedItem = selectedLandmarksListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                selectedLandmarks.remove(selectedItem);
            }
        });
    }

    private void setupLandmarkSelection() {
        // This sets up the landmark selection UI components
        if (landmarkSelectionBox == null) {
            landmarkSelectionBox = new HBox(10);
            landmarkSelectionBox.setAlignment(Pos.CENTER_LEFT);
            landmarkSelectionBox.setPadding(new Insets(10));
        }
    }

    private void initializeMap() {
        // Load a simple map initially
        mapWebView.getEngine().loadContent(
                "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<h2 style='text-align:center;'>University of Ghana Campus Map</h2>" +
                        "<p style='text-align:center;'>Select locations to see routes</p>" +
                        "<div style='text-align:center; margin-top: 20px;'>" +
                        "<img src='https://www.ug.edu.gh/sites/default/files/ug_map.jpg' " +
                        "alt='UG Campus Map' style='max-width:100%; max-height:400px; border:1px solid #ccc;'>" +
                        "</div>" +
                        "</body></html>"
        );
    }

    private void initializeRoutesTable() {
        // Only initialize if the table exists
        if (routesTableView == null) return;

        TableColumn<RouteOption, String> routeColumn = new TableColumn<>("Route");
        routeColumn.setCellValueFactory(cellData -> {
            String path = String.join(" → ", cellData.getValue().getPath());
            return new SimpleStringProperty(path);
        });

        TableColumn<RouteOption, Double> distanceColumn = new TableColumn<>("Distance (m)");
        distanceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getDistance()).asObject());

        TableColumn<RouteOption, Integer> timeColumn = new TableColumn<>("Time (min)");
        timeColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTime()).asObject());

        TableColumn<RouteOption, String> landmarksColumn = new TableColumn<>("Landmarks");
        landmarksColumn.setCellValueFactory(cellData -> {
            String landmarks = String.join(", ", cellData.getValue().getLandmarks());
            return new SimpleStringProperty(landmarks);
        });

        routesTableView.getColumns().addAll(routeColumn, distanceColumn, timeColumn, landmarksColumn);
    }

    private void findRoutes() {
        String start = startLocationComboBox.getValue();
        String destination = destinationComboBox.getValue();

        // Check for custom locations and add them to the graph if needed
        if (!dataLoader.getLocations().contains(start)) {
            dataLoader.addCustomLocation(start);
        }

        if (!dataLoader.getLocations().contains(destination)) {
            dataLoader.addCustomLocation(destination);
        }

        if (start == null || destination == null) {
            statusLabel.setText("Please select both start and destination locations");
            return;
        }

        if (start.equals(destination)) {
            statusLabel.setText("Start and destination cannot be the same");
            return;
        }

        statusLabel.setText("Finding routes from " + start + " to " + destination + "...");

        List<RouteOption> routes;

        // Check if user wants to use landmarks
        if (useLandmarkCheckBox.isSelected() && !selectedLandmarks.isEmpty()) {
            // Find routes with required landmarks
            routes = searchAndLandmarks.findRoutesWithMultipleLandmarks(
                    start,
                    destination,
                    new ArrayList<>(selectedLandmarks),
                    dataLoader.getGraph()
            );
        } else {
            // Find direct routes
            routes = routeFinder.findRoutes(start, destination, dataLoader.getGraph());
        }

        // Apply traffic simulation
        routes = trafficSimulator.applyTrafficConditions(routes);

        // Sort routes based on selected criteria
        if (shortestDistanceRadio.isSelected()) {
            routes = sortingAlgorithms.mergeSort(routes); // Sort by distance
        } else {
            routes = sortingAlgorithms.quickSort(routes); // Sort by time (default)
        }

        // Update UI with routes
        displayRoutes(routes);

        // Update table view if it exists
        if (routesTableView != null) {
            updateRoutesTable(routes);
        }

        // Display the best route on the map
        if (!routes.isEmpty()) {
            displayRouteOnMap(routes.get(0));
        }
    }

    private void findRoutesByLandmark() {
        String landmark = landmarkComboBox.getValue();

        if (landmark == null) {
            return;
        }

        // Add to landmark list if it's custom
        if (!dataLoader.getLandmarks().contains(landmark)) {
            dataLoader.addCustomLandmark(landmark);
        }

        // Find routes that pass through this landmark
        List<RouteOption> landmarkRoutes = searchAndLandmarks.findRoutesByLandmark(landmark, dataLoader.getGraph());

        // Display landmark-based routes
        landmarkRoutesBox.getChildren().clear();

        Label landmarkHeader = new Label("Routes via " + landmark);
        landmarkHeader.getStyleClass().add("section-header");
        landmarkRoutesBox.getChildren().add(landmarkHeader);

        if (landmarkRoutes.isEmpty()) {
            landmarkRoutesBox.getChildren().add(new Label("No routes found through this landmark"));
        } else {
            for (RouteOption route : landmarkRoutes) {
                VBox routeBox = createRouteInfoBox(route);
                landmarkRoutesBox.getChildren().add(routeBox);
            }
        }
    }

    private void displayRoutes(List<RouteOption> routes) {
        routeDetailsBox.getChildren().clear();

        if (routes.isEmpty()) {
            routeDetailsBox.getChildren().add(new Label("No routes found"));
            return;
        }

        Label header = new Label("Available Routes:");
        header.getStyleClass().add("section-header");
        routeDetailsBox.getChildren().add(header);

        for (int i = 0; i < Math.min(3, routes.size()); i++) {
            RouteOption route = routes.get(i);
            VBox routeBox = createRouteInfoBox(route);

            if (i == 0) {
                Label bestRouteLabel = new Label("BEST ROUTE");
                bestRouteLabel.getStyleClass().add("best-route-label");
                routeBox.getChildren().add(0, bestRouteLabel);
            }

            routeDetailsBox.getChildren().add(routeBox);
        }

        statusLabel.setText("Found " + routes.size() + " routes");
    }

    private void updateRoutesTable(List<RouteOption> routes) {
        // Only update if the table exists
        if (routesTableView == null) return;

        ObservableList<RouteOption> observableRoutes = FXCollections.observableArrayList(routes);
        routesTableView.setItems(observableRoutes);

        // Make table visible if it wasn't already
        routesTableView.setVisible(true);
        routesTableView.setManaged(true);
    }

    private VBox createRouteInfoBox(RouteOption route) {
        VBox routeBox = new VBox(5);
        routeBox.getStyleClass().add("route-box");

        Label routePathLabel = new Label("Path: " + String.join(" → ", route.getPath()));
        Label distanceLabel = new Label(String.format("Distance: %.1f meters", route.getDistance()));
        Label timeLabel = new Label(String.format("Estimated time: %d minutes", route.getTime()));
        Label landmarksLabel = new Label("Landmarks: " +
                (route.getLandmarks().isEmpty() ? "None" : String.join(", ", route.getLandmarks())));

        String trafficLevel = trafficSimulator.getTrafficLevel(route.getPath().get(0));
        Label trafficLabel = new Label("Traffic: " + trafficLevel);
        trafficLabel.getStyleClass().add("traffic-" + trafficLevel.toLowerCase());

        routeBox.getChildren().addAll(routePathLabel, distanceLabel, timeLabel, landmarksLabel, trafficLabel);

        // Add button to show this route on Google Maps
        Button showOnMapButton = new Button("Show on Google Maps");
        showOnMapButton.setOnAction(event -> showOnGoogleMaps(route));
        routeBox.getChildren().add(showOnMapButton);

        return routeBox;
    }

    private void displayRouteOnMap(RouteOption route) {
        // Load Google Maps to show the route
        showOnGoogleMaps(route);
    }

    private void showOnGoogleMaps(RouteOption route) {
        // Add ", University of Ghana" to each location for better geocoding by Google Maps
        String origin = route.getPath().get(0) + ", University of Ghana";
        String destination = route.getPath().get(route.getPath().size() - 1) + ", University of Ghana";

        // Create waypoints from the path for a more accurate route display
        StringBuilder waypoints = new StringBuilder();
        for (int i = 1; i < route.getPath().size() - 1; i++) {
            if (waypoints.length() > 0) {
                waypoints.append("|");
            }
            // Add context to waypoints as well
            waypoints.append(route.getPath().get(i).replace(" ", "+")).append(",+University+of+Ghana");
        }

        // Build a standard Google Maps URL which does not require an API key for basic directions.
        // This will load the full Google Maps website within the WebView.
        String googleMapsUrl = "https://www.google.com/maps/dir/?api=1" +
                "&origin=" + origin.replace(" ", "+") +
                "&destination=" + destination.replace(" ", "+") +
                (waypoints.length() > 0 ? "&waypoints=" + waypoints.toString() : "") +
                "&travelmode=walking";

        mapWebView.getEngine().load(googleMapsUrl);
    }
}