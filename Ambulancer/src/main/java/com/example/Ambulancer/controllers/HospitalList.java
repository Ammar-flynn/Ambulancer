package com.example.Ambulancer.controllers;

import com.example.Ambulancer.services.SupabaseService;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HospitalList implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox hospitalsContainer;
    @FXML private Label resultsCountLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private List<Hospital> hospitals = new ArrayList<>();
    private SupabaseService supabaseService;
    private Timeline refreshTimeline;
    private Timeline pulseTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supabaseService = new SupabaseService();

        // Start loading animation
        startLoadingAnimation();

        // Load hospitals
        loadHospitalsFromDatabase();

        // Setup search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchHospitals());

        // Setup live updates
        setupLiveUpdates();
    }

    private void startLoadingAnimation() {
        loadingIndicator.setVisible(true);

        // Rotate animation for loading indicator
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), loadingIndicator);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();
    }

    private void loadHospitalsFromDatabase() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> data = supabaseService.fetchAllHospitals();
                List<Hospital> list = new ArrayList<>();
                for (Map<String, Object> map : data) {
                    list.add(new Hospital(
                            map.getOrDefault("name", "Unknown Hospital").toString(),
                            map.getOrDefault("branch", "Main Branch").toString()
                    ));
                }

                javafx.application.Platform.runLater(() -> {
                    hospitals = list;
                    displayHospitals(list);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showNoResults();
                });
            }
        }).start();
    }

    private void displayHospitals(List<Hospital> list) {
        hospitalsContainer.getChildren().clear();

        if (list.isEmpty()) {
            showNoResults();
            return;
        }

        // Staggered entrance animation
        for (int i = 0; i < list.size(); i++) {
            VBox card = createHospitalCard(list.get(i));
            card.setOpacity(0);
            card.setTranslateY(20);
            hospitalsContainer.getChildren().add(card);

            // Staggered fade in
            PauseTransition delay = new PauseTransition(Duration.millis(i * 100));
            delay.setOnFinished(e -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                fadeIn.setToValue(1);

                TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), card);
                slideUp.setToY(0);

                ParallelTransition parallel = new ParallelTransition(fadeIn, slideUp);
                parallel.play();
            });
            delay.play();
        }

        resultsCountLabel.setText("📊 " + list.size() + " Hospitals Found");
        updateLastUpdatedTime();

        // Pulse animation for results count
        ScaleTransition pulse = new ScaleTransition(Duration.millis(200), resultsCountLabel);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private VBox createHospitalCard(Hospital hospital) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Hover animation
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20; -fx-background-radius: 10; " +
                    "-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);");

            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; " +
                    "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        // Header
        HBox header = new HBox(10);
        Label name = new Label(hospital.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label("● AVAILABLE");
        status.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 12px;");

        header.getChildren().addAll(name, spacer, status);

        // Branch
        Label branch = new Label("📍 " + hospital.getBranch());
        branch.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        // Info
        VBox infoBox = new VBox(8);
        infoBox.getChildren().addAll(
                createInfoRow("🏥", "Type:", "General Hospital"),
                createInfoRow("📍", "Location:", "City Center"),
                createInfoRow("📞", "Emergency:", "112"),
                createInfoRow("👨‍⚕️", "Doctors:", "24/7 Available"),
                createInfoRow("🚑", "Ambulances:", "5 Available")
        );

        // Buttons
        HBox buttonBox = new HBox(10);

        Button viewBtn = createAnimatedButton("👁️ Details", "#3498db");
        Button callBtn = createAnimatedButton("📞 Call", "#2ecc71");
        Button emergencyBtn = createAnimatedButton("🚨 Emergency", "#e74c3c");

        viewBtn.setOnAction(e -> viewHospitalDetails(hospital));
        callBtn.setOnAction(e -> callHospital(hospital));
        emergencyBtn.setOnAction(e -> reportEmergencyForHospital(hospital));

        buttonBox.getChildren().addAll(viewBtn, callBtn, emergencyBtn);

        card.getChildren().addAll(header, branch, infoBox, buttonBox);
        return card;
    }

    private Button createAnimatedButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; " +
                "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return btn;
    }

    private HBox createInfoRow(String icon, String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        labelLabel.setMinWidth(100);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: 700;");

        row.getChildren().addAll(iconLabel, labelLabel, valueLabel);
        return row;
    }

    @FXML
    private void searchHospitals() {
        String term = searchField.getText().toLowerCase().trim();
        if (term.isEmpty()) {
            displayHospitals(hospitals);
            return;
        }

        List<Hospital> filtered = new ArrayList<>();
        for (Hospital h : hospitals) {
            if (h.getName().toLowerCase().contains(term) || h.getBranch().toLowerCase().contains(term)) {
                filtered.add(h);
            }
        }
        displayHospitals(filtered);
    }

    @FXML
    private void refreshHospitals() {
        // Button rotation animation
        Node refreshBtn = searchField.getScene().lookup(".refresh-button");
        if (refreshBtn != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(500), refreshBtn);
            rotate.setByAngle(360);
            rotate.play();
        }

        loadingIndicator.setVisible(true);
        loadHospitalsFromDatabase();
    }

    private void setupLiveUpdates() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> refreshHospitalData())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void refreshHospitalData() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> data = supabaseService.fetchAllHospitals();
                List<Hospital> list = new ArrayList<>();
                for (Map<String, Object> map : data) {
                    list.add(new Hospital(
                            map.getOrDefault("name", "Unknown Hospital").toString(),
                            map.getOrDefault("branch", "Main Branch").toString()
                    ));
                }

                javafx.application.Platform.runLater(() -> {
                    hospitals = list;
                    if (searchField.getText().isEmpty()) {
                        displayHospitals(hospitals);
                    } else {
                        searchHospitals();
                    }

                    // Flash animation for update
                    FadeTransition flash = new FadeTransition(Duration.millis(300), lastUpdatedLabel);
                    flash.setFromValue(0.3);
                    flash.setToValue(1);
                    flash.setCycleCount(2);
                    flash.setAutoReverse(true);
                    flash.play();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateLastUpdatedTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lastUpdatedLabel.setText("Last updated: " + time);
    }

    @FXML
    private void reportEmergency() {
        // Scale animation for emergency button
        Node emergencyBtn = searchField.getScene().lookup(".emergency-btn");
        if (emergencyBtn != null) {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(100), emergencyBtn);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            pulse.play();
        }

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/Ambulancer/Emergency.fxml")
            );
            Stage stage = (Stage) searchField.getScene().getWindow();

            // Page transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), searchField.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(new Scene(root));

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportEmergencyForHospital(Hospital hospital) {
        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), hospitalsContainer);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🚨 Emergency Alert");
        alert.setHeaderText("Emergency Reported to " + hospital.getName());
        alert.setContentText(
                "📍 Hospital: " + hospital.getName() + "\n" +
                        "🏥 Branch: " + hospital.getBranch() + "\n\n" +
                        "🕐 Estimated Response: 5-7 minutes\n" +
                        "📞 Stay on line for instructions"
        );
        alert.showAndWait();
    }

    private void viewHospitalDetails(Hospital hospital) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hospital Details");
        alert.setHeaderText(hospital.getName());
        alert.setContentText(
                "Name: " + hospital.getName() + "\n" +
                        "Branch: " + hospital.getBranch() + "\n" +
                        "Location: City Center\n" +
                        "Emergency Contact: 112\n" +
                        "Type: General Hospital\n\n" +
                        "Services:\n" +
                        "• 24/7 Emergency\n" +
                        "• ICU & Trauma Center\n" +
                        "• Ambulance Services"
        );
        alert.showAndWait();
    }

    private void callHospital(Hospital hospital) {
        // Phone ring animation
        ScaleTransition ring = new ScaleTransition(Duration.millis(100), hospitalsContainer);
        ring.setFromX(1.0);
        ring.setFromY(1.0);
        ring.setToX(0.98);
        ring.setToY(0.98);
        ring.setCycleCount(4);
        ring.setAutoReverse(true);
        ring.play();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📞 Contact Hospital");
        alert.setHeaderText("Contacting " + hospital.getName());
        alert.setContentText(
                "Hospital: " + hospital.getName() + "\n" +
                        "Branch: " + hospital.getBranch() + "\n\n" +
                        "Emergency Contact: 112\n" +
                        "Direct Line: (123) 456-7890"
        );
        alert.showAndWait();
    }

    private void showNoResults() {
        hospitalsContainer.getChildren().clear();

        VBox noResults = new VBox(20);
        noResults.setAlignment(javafx.geometry.Pos.CENTER);
        noResults.setPadding(new javafx.geometry.Insets(60));

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");

        // Pulse animation for icon
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), icon);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        Label text = new Label("No hospitals found");
        text.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        Label subtext = new Label("Try adjusting your search or refresh");
        subtext.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");

        noResults.getChildren().addAll(icon, text, subtext);
        hospitalsContainer.getChildren().add(noResults);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), noResults);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        resultsCountLabel.setText("0 Hospitals Found");
    }

    @FXML
    public void Backclicked(MouseEvent event) {
        try {
            // Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300),
                    ((Node) event.getSource()).getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                try {
                    Parent newRoot = FXMLLoader.load(
                            getClass().getResource("/com/example/Ambulancer/Home.fxml")
                    );
                    Scene scene = ((Node) event.getSource()).getScene();
                    scene.setRoot(newRoot);

                    // Fade in
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newRoot);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Hospital {
        private final String name;
        private final String branch;
        public Hospital(String name, String branch) {
            this.name = name;
            this.branch = branch;
        }
        public String getName() { return name; }
        public String getBranch() { return branch; }
    }
}