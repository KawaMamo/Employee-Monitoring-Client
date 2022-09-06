module org.nestech.monitoring {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.net.http;
    requires org.json;

    opens org.nestech.monitoring to javafx.fxml;
    exports org.nestech.monitoring;
}