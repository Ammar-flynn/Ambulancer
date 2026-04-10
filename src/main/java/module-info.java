module com.example.Ambulancer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;
    requires org.controlsfx.controls;
    requires org.json;
    requires java.prefs;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires io.github.cdimascio.dotenv.java;


    exports com.example.Ambulancer.services;
    opens com.example.Ambulancer.services to javafx.fxml;
    exports com.example.Ambulancer.controllers;
    opens com.example.Ambulancer.controllers to javafx.fxml;
    exports com.example.Ambulancer.main;
    opens com.example.Ambulancer.main to javafx.fxml;
    exports com.example.Ambulancer.model;
    opens com.example.Ambulancer.model to javafx.fxml;
}