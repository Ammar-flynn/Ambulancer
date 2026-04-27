package com.example.Ambulancer.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.example.Ambulancer.model.Emergency;
import com.example.Ambulancer.services.SupabaseService;
import com.example.Ambulancer.services.TrackManager;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class EmergencyC {

    private final SupabaseService supaBase = new SupabaseService();

    private final Set<String> notifiedTracks = new HashSet<>();

    @FXML
    private VBox placeholderBox;

    @FXML private StackPane overlayPane;

    @FXML private Pane darkOverlay;

    @FXML
    private HBox contentContainer;

    @FXML
    private VBox cardsContainer;

    @FXML
    private VBox mainContent;

    @FXML
    private ScrollPane cardsScrollPane;


    @FXML
    private VBox cardsVBox;
    @FXML
    private StackPane accidentButton, symptomsButton, fahadButton;
    @FXML
    private TextField nameField;
    @FXML
    private TextField symptomsField;
    @FXML
    private TextArea addressField;
    @FXML
    private StackPane rootPane;
    @FXML
    private StackPane formPane;


    // ---------------- Hover animations ----------------
    @FXML
    private void onHoverEnter(MouseEvent event) {
        StackPane pane = (StackPane) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), pane);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
    }


    @FXML
    private void onHoverExit(MouseEvent event) {
        StackPane pane = (StackPane) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), pane);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    // ---------------- Validation ----------------
    private boolean validateFields() {
        if (nameField.getText().isEmpty() || addressField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in both Name and Address!");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // ---------------- Symptoms Priority (ONLY FOR DB VALUE) ----------------
    private int calculateSymptomsPriority(String symptoms) {
        symptoms = symptoms.toLowerCase();
        if (symptoms.contains("heart") || symptoms.contains("brain")) return 3;
        if (symptoms.contains("chest") || symptoms.contains("stroke")) return 2;
        if (symptoms.contains("stomach") || symptoms.contains("headache")) return 1;
        return 0;
    }

    // ---------------- Accident ----------------
    @FXML
    private void onAccidentClicked(MouseEvent event) {
        if (!validateFields()) return;

        String track = TrackManager.generateTrack();
        TrackManager.saveTrackLocally(track);

        Emergency emergency = new Emergency(
                null,
                nameField.getText(),
                addressField.getText(),
                null,
                "ACCIDENT",
                0,
                "pending",
                track,
                "none"
        );

        supaBase.saveEmergency(emergency);
        showSuccess("Accident reported successfully!");
        clearFields();
    }

    // ---------------- Symptoms ----------------
    @FXML
    private void onSymptomsClicked(MouseEvent event) {
        if (!validateFields()) return;

        overlayPane.setVisible(true);
        overlayPane.setMouseTransparent(false);

        // Fade in dark overlay
        darkOverlay.setOpacity(0);
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), darkOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);

        // Prepare formPane for fade-in
        formPane.setOpacity(0);
        formPane.setVisible(true);

        FadeTransition formFade = new FadeTransition(Duration.millis(300), formPane);
        formFade.setFromValue(0);
        formFade.setToValue(1);

        // Play overlay fade first, then form fade
        overlayFade.setOnFinished(e -> formFade.play());
        overlayFade.play();
    }


    @FXML
    public void sendSymptoms() {
        if (symptomsField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Symptoms");
            alert.setHeaderText(null);
            alert.setContentText("Please enter symptoms.");
            alert.showAndWait();
            return;
        }

        int priority = calculateSymptomsPriority(symptomsField.getText());

        String track = TrackManager.generateTrack();
        TrackManager.saveTrackLocally(track);

        Emergency emergency = new Emergency(
                null,
                nameField.getText(),
                addressField.getText(),
                symptomsField.getText(),
                "SYMPTOMS",
                priority,
                "pending",
                track,
                "none"
        );

        supaBase.saveEmergency(emergency);
        closeForm();
        showSuccess("Symptoms submitted successfully!");
        clearFields();
    }

    // ---------------- Fahad ----------------
    @FXML
    private void onFahadClicked(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(null);
        alert.setContentText("This feature is under development.");
        alert.showAndWait();
    }

    // ---------------- Close Form ----------------
    @FXML
    private void closeForm() {
        // Fade out formPane first
        FadeTransition formFadeOut = new FadeTransition(Duration.millis(200), formPane);
        formFadeOut.setFromValue(1);
        formFadeOut.setToValue(0);

        // Fade out overlay
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(200), darkOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);

        // Play form fade, then overlay fade, then hide everything
        formFadeOut.setOnFinished(e -> overlayFadeOut.play());
        overlayFadeOut.setOnFinished(e -> {
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            formPane.setVisible(false);
            symptomsField.clear();
        });

        formFadeOut.play();
    }


    // ---------------- Utils ----------------
    private void clearFields() {
        nameField.clear();
        addressField.clear();
        symptomsField.clear();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ---------------- Back ----------------
    @FXML
    public void Backclicked(MouseEvent event) {
        try {
            // Load the new FXML
            Parent newRoot = FXMLLoader.load(getClass().getResource("/com/example/Ambulancer/Home.fxml"));

            Scene scene = ((Node) event.getSource()).getScene();

            newRoot.setOpacity(0);

            scene.setRoot(newRoot);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), newRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Ambulance Coming notification
    @FXML
    public void Notification() {

        Task<List<Emergency>> task = new Task<>() {
            @Override
            protected List<Emergency> call() {
                List<String> tracks = TrackManager.loadActiveTracks();
                List<Emergency> result = new ArrayList<>();

                for (String track : tracks) {
                    if (notifiedTracks.contains(track)) continue;

                    Emergency e = supaBase.fetchEmergencyByTrack(track); // BACKGROUND THREAD
                    if (e != null && "assigned".equalsIgnoreCase(e.status)) {
                        notifiedTracks.add(track);
                        result.add(e);
                    }
                }
                return result;
            }
        };

        task.setOnSucceeded(e -> {
            for (Emergency em : task.getValue()) {

                if (cardsVBox.getChildren().isEmpty()) {
                    placeholderBox.setVisible(false);
                    placeholderBox.setManaged(false);
                    cardsScrollPane.setVisible(true);
                    cardsScrollPane.setManaged(true);
                }

                cardsVBox.getChildren().add(createAssignedCard(em));
                showNotification(em); // UI SAFE
            }
        });

        new Thread(task).start();
    }





    //show assigned notification
    private void showNotification(Emergency e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ambulance Assigned!");
        alert.setHeaderText(null);
        alert.setContentText("An ambulance has been assigned for " + e.name + " (" + e.type + ").");
        alert.showAndWait();
    }

    private VBox createAssignedCard(Emergency e) {

        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);

        card.setStyle("""
        -fx-background-color: linear-gradient(to bottom right, #f0f8ff, #e6f2ff);
        -fx-padding: 18;
        -fx-border-color: #1e90ff;
        -fx-border-width: 2;
        -fx-border-radius: 14;
        -fx-background-radius: 14;
        -fx-effect: dropshadow(gaussian, rgba(30, 144, 255, 0.15), 12, 0, 0, 4);
""");



        // RESPONSIVE WIDTH
        card.prefWidthProperty().bind(cardsVBox.widthProperty().subtract(20)); // subtract padding
        card.setMaxWidth(Double.MAX_VALUE);

        /* ================= HEADER ================= */

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setStyle("""
        -fx-background-color: #1e90ff;
        -fx-background-radius: 20;
        -fx-padding: 8;
        -fx-min-width: 40;
        -fx-min-height: 40;
    """);

        Label icon = new Label("🚑");
        icon.setStyle("-fx-font-size: 18px;");
        iconContainer.getChildren().add(icon);

        VBox headerText = new VBox(3);

        Label title = new Label("AMBULANCE DISPATCHED");
        title.setStyle("""
        -fx-font-weight: 800;
        -fx-font-size: 17px;
        -fx-text-fill: #1a3c6e;
    """);

        Label subtitle = new Label("Emergency Medical Services");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        headerText.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(iconContainer, headerText);

        /* ================= DIVIDER ================= */

        Separator divider = new Separator();
        divider.setPadding(new Insets(8, 0, 8, 0));

        /* ================= DETAILS ================= */

        GridPane details = new GridPane();
        details.setHgap(15);
        details.setVgap(10);
        details.setPadding(new Insets(5, 0, 0, 0));

        // FLEXIBLE GRID
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        details.getColumnConstraints().addAll(col1, col2);

        addDetailCell(details, 0, "👤 PATIENT", e.name);
        addDetailCell(details, 1, "⚠ EMERGENCY", e.type);
        addDetailCell(details, 2, "📋 TRACK ID", e.track);
        addDetailCell(details, 3, "🏥 Hospital", e.HospitalAssigned);

        /* ================= STATUS ================= */

        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(15, 0, 0, 0));

        Circle statusDot = new Circle(6, Color.LIMEGREEN);

        Label statusText = new Label("Ambulance is en route to your location");
        statusText.setStyle("""
        -fx-font-size: 13px;
        -fx-text-fill: #28a745;
        -fx-font-weight: 600;
    """);

        Button bt = new Button("Mark as Arrived");
        bt.setStyle("-fx-background-color: #ffffff;" +
                " -fx-text-fill: #006BBDFF;" +
                " -fx-font-weight: bold;" +
                " -fx-background-radius: 4;" +
                " -fx-border-color: #006BBDFF;" +
                " -fx-border-width: 1.5;" +
                " -fx-border-radius: 4;" +
                " -fx-padding: 8 16;" +
                " -fx-cursor: hand;" +
                " -fx-effect: dropshadow(gaussian, rgba(0,107,189,0.2), 4, 0, 0, 1);");

        bt.setOnMouseEntered(b -> {
            // Parallel transition for multiple effects
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), bt);
            fadeIn.setToValue(1.0);

            TranslateTransition liftUp = new TranslateTransition(Duration.millis(200), bt);
            liftUp.setToY(-2);

            // Color change
            bt.setStyle(bt.getStyle() +
                    " -fx-background-color: #006BBDFF;" +
                    " -fx-text-fill: #ffffff;" +
                    " -fx-effect: dropshadow(gaussian, rgba(0,107,189,0.4), 8, 0, 0, 2);");

            ParallelTransition parallel = new ParallelTransition(fadeIn, liftUp);
            parallel.play();
        });


        bt.setOnMouseExited(b -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), bt);
            fadeOut.setToValue(0.9);

            TranslateTransition liftDown = new TranslateTransition(Duration.millis(200), bt);
            liftDown.setToY(0);

            // Revert color
            bt.setStyle("-fx-background-color: #ffffff;" +
                    " -fx-text-fill: #006BBDFF;" +
                    " -fx-font-weight: bold;" +
                    " -fx-background-radius: 4;" +
                    " -fx-border-color: #006BBDFF;" +
                    " -fx-border-width: 1.5;" +
                    " -fx-border-radius: 4;" +
                    " -fx-padding: 8 16;" +
                    " -fx-cursor: hand;" +
                    " -fx-effect: dropshadow(gaussian, rgba(0,107,189,0.2), 4, 0, 0, 1);");

            ParallelTransition parallel = new ParallelTransition(fadeOut, liftDown);
            parallel.play();
        });

        bt.setOnMousePressed(b -> {
            ScaleTransition pressDown = new ScaleTransition(Duration.millis(100), bt);
            pressDown.setToX(0.95);
            pressDown.setToY(0.95);
            pressDown.play();

            bt.setStyle(bt.getStyle() +
                    " -fx-background-color: #005aa3;" +
                    " -fx-effect: dropshadow(gaussian, rgba(0,107,189,0.3), 4, 0, 0, 1);");
        });

        bt.setOnMouseReleased(b -> {
            ScaleTransition releaseUp = new ScaleTransition(Duration.millis(100), bt);
            releaseUp.setToX(1.0);
            releaseUp.setToY(1.0);
            releaseUp.play();

            bt.setStyle(bt.getStyle() +
                    " -fx-background-color: #006BBDFF;" +
                    " -fx-effect: dropshadow(gaussian, rgba(0,107,189,0.4), 8, 0, 0, 2);");

        });

        bt.setOnAction(c ->{
            supaBase.MarkArrived(e.track);

            FadeTransition fade = new FadeTransition(Duration.millis(300), card);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> cardsVBox.getChildren().remove(card));
            fade.play();
        });

        statusBar.getChildren().addAll(statusDot, statusText,bt);

        card.getChildren().addAll(header, divider, details, statusBar);
        return card;
    }


    private void addDetailCell(GridPane grid, int row, String label, String value) {

        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-weight: bold;");

        Label valueLbl = new Label(value);
        valueLbl.setWrapText(true);
        valueLbl.setMaxWidth(Double.MAX_VALUE);
        valueLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: 500;");

        grid.add(labelLbl, 0, row);
        grid.add(valueLbl, 1, row);
    }

    @FXML
    public void initialize() {
        cardsVBox.setSpacing(12);

        mainContent.prefWidthProperty().bind(contentContainer.widthProperty().multiply(0.5));
        cardsContainer.prefWidthProperty().bind(contentContainer.widthProperty().multiply(0.5));
        // Make ScrollPane fill the StackPane
        cardsScrollPane.prefHeightProperty().bind(cardsContainer.heightProperty());
        cardsScrollPane.prefWidthProperty().bind(cardsContainer.widthProperty());



        cardsScrollPane.setVisible(false);
        cardsScrollPane.setManaged(false);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> Notification())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

}
