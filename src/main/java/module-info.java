module dcit204.map {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;



    opens dcit204.map to javafx.fxml;
    exports dcit204.map;
}