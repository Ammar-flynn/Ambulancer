package com.example.Ambulancer.controllers;

import com.example.Ambulancer.model.Emergency;
import com.example.Ambulancer.services.SessionManager;
import com.example.Ambulancer.services.SupabaseService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainDashboard implements Initializable {

    @FXML private FlowPane assignedContainer;
    @FXML private Button pendingTabBtn;
    @FXML private Button assignedTabBtn;
    @FXML private Label totalEmergenciesLabel;
    @FXML private FlowPane cardsContainer;
    @FXML private Button refreshBtn;
    @FXML private Button addEmergencyBtn;
    @FXML private Button addHospitalBtn;
    @FXML private Button logoutBtn;

    private PriorityQueue<Emergency> emergencyQueue;
    private SupabaseService supabaseService;

    private ScheduledExecutorService scheduler;

    private SessionManager session;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        supabaseService = new SupabaseService();

        setupHover(addEmergencyBtn);
        setupHover(addHospitalBtn);
        setupHover(refreshBtn);
        setupHover(logoutBtn);
        setupTabs();
        refreshData();

        startRealtimePolling();
    }

    private void startRealtimePolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            PriorityQueue<Emergency> latest = supabaseService.fetchEmergencies();
            Platform.runLater(() -> {
                emergencyQueue = latest;
                updateUI();
            });
        }, 0, 4, TimeUnit.SECONDS);
    }

    private void setupHover(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    @FXML
    private void refreshData() {
        emergencyQueue = supabaseService.fetchEmergencies();
        updateUI();
    }

    private void updateUI() {
        cardsContainer.getChildren().clear();
        assignedContainer.getChildren().clear();

        if (emergencyQueue == null || emergencyQueue.isEmpty()) {
            Label empty = new Label("No emergencies found");
            empty.setStyle("-fx-text-fill: #ccc; -fx-font-size: 16px;");
            cardsContainer.getChildren().add(empty);
            totalEmergenciesLabel.setText("0");
            return;
        }

        int pendingCount = 0;
        PriorityQueue<Emergency> copy = new PriorityQueue<>(emergencyQueue);
        while (!copy.isEmpty()) {
            Emergency e = copy.poll();
            if ("assigned".equalsIgnoreCase(e.status)) {
                assignedContainer.getChildren().add(createAssignedCard(e));

            } else if ("pending".equalsIgnoreCase(e.status)) {
                cardsContainer.getChildren().add(createCard(e));
                pendingCount++;
            }
        }
        totalEmergenciesLabel.setText(String.valueOf(pendingCount));
    }


    private VBox createAssignedCard(Emergency e) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-radius: 12; -fx-padding: 15; -fx-background-color: #006BBDFF; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5,0,0,2);");

        // Header
        HBox header = new HBox(10);
        Circle status = new Circle(7, Color.GREEN);
        Label title = new Label(e.type + " - " + e.name);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: 14px;");
        header.getChildren().addAll(status, title);

        // Details
        VBox details = new VBox(5);
        details.setStyle("-fx-background-color: white");
        if (!e.address.isEmpty()) {
            Label addr = new Label("📍 " + e.address);
            addr.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 12px;");
            details.getChildren().add(addr);
        }
        if (!e.symptoms.isEmpty()) {
            Label sym = new Label("✚" + e.symptoms);
            sym.setStyle("-fx-text-fill: #cf9a00; -fx-font-weight: bold; -fx-font-size: 11px;");
            details.getChildren().add(sym);
        }

        card.getChildren().addAll(header, details);
        return card;
    }



    private VBox createCard(Emergency e) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgb(0,107,189), 5,0,0,2);");

        // Color based on type
        String bgColor;
        Color statusColor;
        switch (e.type.toUpperCase()) {
            case "ACCIDENT":
                bgColor = "#006BBDFF";  // Red
                statusColor = Color.RED;
                break;
            case "SYMPTOMS":
                bgColor = "#006BBDFF";  // Orange
                statusColor = Color.ORANGE;
                break;
            default:
                bgColor = "#00bfff";  // Dodger Blue
                statusColor = Color.DEEPSKYBLUE;
                break;
        }
        card.setStyle(card.getStyle() + "-fx-background-color: " + bgColor + ";");

        // Header
        HBox header = new HBox(10);
        header.setStyle("-fx-background-radius: 6");
        Circle status = new Circle(7, statusColor);
        Label title = new Label(e.type + " - " + e.name);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 14px; -fx-background-radius: 6");
        header.getChildren().addAll(status, title);

        // Details
        VBox details = new VBox(5);
        details.setStyle("-fx-background-radius: 6; -fx-background-color: #ffffff");
        if (!e.address.isEmpty()) {
            Label addr = new Label("📍 " + e.address);
            addr.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;");
            details.getChildren().add(addr);
        }
        if (!e.symptoms.isEmpty()) {
            Label sym = new Label("✚" + e.symptoms);
            sym.setStyle("-fx-text-fill: #cf9a00; -fx-font-weight: bold; -fx-font-size: 11px;");
            details.getChildren().add(sym);
        }

        // Send Ambulance Button
        Button sendBtn = new Button("🚑 Send Ambulance");
        sendBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #006BBDFF; -fx-font-weight: bold; -fx-background-radius: 8;");
        sendBtn.setOnAction(ev -> {
            supabaseService.dispatchEmergency(e);
            sendBtn.setText("✅ Dispatched");
            sendBtn.setDisable(true);
        });

        // Hover effect
        card.setOnMouseEntered(ev -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        card.setOnMouseExited(ev -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });


        card.getChildren().addAll(header, details, sendBtn);
        return card;

    }

    @FXML
    private void addEmergency() {
        try {
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/com/example/Ambulancer/Emergency.fxml"))));
            stage.setTitle("Add Emergency");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void addHospital() {
        System.out.println(SessionManager.getHospitalBranch());
        System.out.println(SessionManager.getHospitalName());

    }

    @FXML
    public void handleLogout(ActionEvent event) {
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

    private void setupTabs() {
        pendingTabBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                showPending();
            }
        });

        assignedTabBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                showAssigned();
            }
        });
    }

    private void showPending() {
        cardsContainer.setVisible(true);
        assignedContainer.setVisible(false);

        // Active styling
        pendingTabBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 15 5 15;");
        assignedTabBtn.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 15 5 15;");
    }

    private void showAssigned() {
        cardsContainer.setVisible(false);
        assignedContainer.setVisible(true);

        // Active styling
        assignedTabBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 15 5 15;");
        pendingTabBtn.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 15 5 15;");
    }

}
