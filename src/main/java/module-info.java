module org.nestech.monitoring {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.net.http;
    requires org.json;
    requires ZKFingerReader;
    requires java.sql;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    opens org.nestech.monitoring to javafx.fxml;
    opens org.nestech.monitoring.model to com.fasterxml.jackson.databind;
    exports org.nestech.monitoring;
    exports org.nestech.monitoring.model;
}