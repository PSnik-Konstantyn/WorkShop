package com.example.workshop;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class HelloController {
    public TextField enterId;
    public Button startButton;

    @FXML
    public void clickStart() {
        String enteredId = enterId.getText();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/updatedDb", "root", "pass0000");
            try {
                String query = "SELECT Location, AmountOfWorkers, Specialization FROM Craft WHERE idCraft = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, enteredId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String location = resultSet.getString("Location");
                    int workersCount = resultSet.getInt("AmountOfWorkers");
                    String specialization = resultSet.getString("Specialization");
                    openWorkWindow(Integer.parseInt(enteredId),location, workersCount, specialization);
                }
            } catch (SQLException e) {
                enterId.setText("Не вдалося отримати дані про цех");
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            enterId.setText("Некорректний ID цеху");
            e.printStackTrace();
        }
    }

    private void openWorkWindow(int craftID, String location, int workersCount, String specialization) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("work.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) startButton.getScene().getWindow();
            currentStage.close();
            WorkController workController = loader.getController();
            workController.initial(new Stage(), craftID, location, specialization, workersCount);

            Stage workStage = new Stage();
            workStage.setTitle("Work Window");
            workStage.setScene(new Scene(root, 1000, 700));
            workStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}