package com.example.Ambulancer.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import java.io.IOException;


public class Home {

    @FXML
    private StackPane callButton, viewButton;

    // Generic hover enter method
    @FXML
    private void onHoverEnter(MouseEvent event) {
        StackPane pane = (StackPane) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), pane);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
    }

    // Generic hover exit method
    @FXML
    private void onHoverExit(MouseEvent event) {
        StackPane pane = (StackPane) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(200), pane);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    @FXML
    private void onCallClicked(MouseEvent event) {
        try {
            // Load the new FXML
            Parent newRoot = FXMLLoader.load(
                    getClass().getResource("/com/example/Ambulancer/Emergency.fxml")
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
    }

    @FXML
    private void onViewClicked(MouseEvent event) {
        try {
            // Load the new FXML
            Parent newRoot = FXMLLoader.load(getClass().getResource("/com/example/Ambulancer/HospitalList.fxml"));

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

    public void gotoDashboard(ActionEvent event) {
        try {
            // Load the new FXML
            Parent newRoot = FXMLLoader.load(getClass().getResource("/com/example/Ambulancer/Dashboardlogin.fxml"));

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