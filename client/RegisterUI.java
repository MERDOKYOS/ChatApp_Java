package client;

import db.DBConnection;

import javafx.application.Application;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class RegisterUI extends Application {

        public static Stage stage;

        @Override
        public void start(Stage primaryStage) {

                stage = primaryStage;

                TextField nameField = new TextField();

                nameField.setPromptText(
                                "Full Name");

                TextField emailField = new TextField();

                emailField.setPromptText(
                                "Email");

                PasswordField passField = new PasswordField();

                passField.setPromptText(
                                "Password");

                Button registerBtn = new Button("Register");

                Button loginBtn = new Button("Go To Login");

                VBox box = new VBox(
                                10,
                                nameField,
                                emailField,
                                passField,
                                registerBtn,
                                loginBtn);

                registerBtn.setOnAction(e -> {

                        String name = nameField.getText();

                        String email = emailField.getText();

                        String pass = passField.getText();

                        if (name.isEmpty()
                                        || email.isEmpty()
                                        || pass.isEmpty()) {

                                showAlert(
                                                "All fields required!");

                                return;
                        }

                        try {

                                Connection con = DBConnection.getConnection();

                                String sql = "INSERT INTO users " +
                                                "(full_name,email,password,created_at) " +
                                                "VALUES (?,?,?,NOW())";

                                PreparedStatement ps = con.prepareStatement(
                                                sql,
                                                Statement.RETURN_GENERATED_KEYS);

                                ps.setString(1, name);

                                ps.setString(2, email);

                                ps.setString(3, pass);

                                int rows = ps.executeUpdate();

                                if (rows > 0) {

                                        ResultSet rs = ps.getGeneratedKeys();

                                        if (rs.next()) {

                                                ChatUI.userId = rs.getInt(1);
                                        }

                                        ChatUI.setUsername(name);

                                        stage.close();

                                        ChatUI chat = new ChatUI();

                                        chat.start(new Stage());

                                        new Thread(() -> {

                                                ClientConnection.connect(name);

                                        }).start();
                                }

                        } catch (Exception ex) {

                                ex.printStackTrace();
                        }
                });

                loginBtn.setOnAction(e -> {

                        stage.close();

                        new LoginUI().start(
                                        new Stage());
                });

                Scene scene = new Scene(box, 320, 260);

                stage.setScene(scene);

                stage.setTitle("Register");

                stage.show();
        }

        private void showAlert(String msg) {

                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setContentText(msg);

                alert.showAndWait();
        }
}