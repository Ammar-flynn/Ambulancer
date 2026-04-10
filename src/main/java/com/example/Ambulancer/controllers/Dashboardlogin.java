package com.example.Ambulancer.controllers;

import com.example.Ambulancer.services.SessionManager;
import com.example.Ambulancer.services.SupabaseService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class Dashboardlogin {

    private SupabaseService authService = new SupabaseService();


    @FXML
    private TextField IDLogin;

    @FXML
    private TextField IDField;

    @FXML
    private TextField branchField;

    @FXML
    private StackPane overlayPane;

    @FXML
    private Pane darkOverlay;

    @FXML
    private VBox popupCard;

    @FXML
    private TextField Signupname;

    @FXML
    private PasswordField SignupPassword;

    @FXML
    private PasswordField passwordField;

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane formPane;

    // ---------------- Hover animations ----------------
    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();  // Works for Button, StackPane
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
    }

    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    //Field validation
    private boolean validateFields() {
        if (IDLogin.getText().isEmpty() || passwordField.getText().isEmpty() ) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Enter both ID and Password!");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    private void LoginClicked(ActionEvent event) {
        if (!validateFields()) return;

        String IDlogin = IDLogin.getText();
        String password = passwordField.getText();

        boolean success = authService.login(IDlogin, password);

        if (success) {
            String Branch = authService.getBranch(IDlogin);
            SessionManager.setHospital(IDlogin, Branch);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Successful");
            alert.setHeaderText(null);
            alert.setContentText("Welcome " + IDlogin);
            alert.showAndWait();

            // Go to Main Dashboard
            try {
                // Load the new FXML
                Parent newRoot = FXMLLoader.load(
                        getClass().getResource("/com/example/Ambulancer/MainDashboard.fxml")
                );

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

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid ID or password");
            alert.showAndWait();
        }
    }


    @FXML
    private void SignupClicked(ActionEvent event) {
        // Reset popup scale to normal
        popupCard.setScaleX(1.0);
        popupCard.setScaleY(1.0);
        popupCard.setOpacity(1.0);

        // Show overlay
        overlayPane.setVisible(true);
        overlayPane.setMouseTransparent(false);

        // Fade in only for dark overlay
        darkOverlay.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), darkOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    //Pop up CLOSE
    @FXML
    private void closeForm() {
        // Fade out only for dark overlay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), darkOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            Signupname.clear();
            SignupPassword.clear();
            IDField.clear();
            branchField.clear();
        });
        fadeOut.play();
    }


    @FXML
    private void closeFormFromOverlay(MouseEvent event) {
        if (event.getTarget() == darkOverlay) {
            closeForm();
        }
    }

    @FXML
    public void Signupclick() {
        if (Signupname.getText().isEmpty()  || branchField.getText().isEmpty() || IDField.getText().isEmpty() || SignupPassword.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Info Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all the Fields");
            alert.showAndWait();
            return;
        }

        String Name = Signupname.getText();
        String Branch = branchField.getText();
        String ID = IDField.getText();
        String password = IDField.getText();

        boolean success = authService.signup(Name, Branch, ID, password);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Signup Successful");
            alert.setHeaderText(null);
            alert.setContentText("Account created successfully!");
            alert.showAndWait();
            closeForm();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Signup Failed");
            alert.setHeaderText(null);
            alert.setContentText("Username already exists or error occurred");
            alert.showAndWait();
        }
    }


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

}