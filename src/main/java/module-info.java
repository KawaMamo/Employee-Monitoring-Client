module org.nestech.monitoring {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.nestech.monitoring to javafx.fxml;
    exports org.nestech.monitoring;
}