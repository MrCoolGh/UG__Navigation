package dcit204.map;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class NavigationController {
    @FXML
    private VBox navigationMenu;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label resourcesLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setOnMouseClicked(event -> handleNavigationClick("welcome"));
        resourcesLabel.setOnMouseClicked(event -> handleNavigationClick("resources"));
    }

    private void handleNavigationClick(String section) {
        // This would typically navigate to different sections or views
        System.out.println("Navigating to: " + section);

        // In a real application, this would change content in the main view
    }
}