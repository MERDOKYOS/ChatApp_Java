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

public class LoginUI extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;

        TextField emailField =
                new TextField();

        emailField.setPromptText("Email");

        PasswordField passField =
                new PasswordField();

        passField.setPromptText("Password");

        Button loginBtn =
                new Button("Login");

        Button registerBtn =
                new Button("Register");

        VBox box = new VBox(
                10,
                emailField,
                passField,
                loginBtn,
                registerBtn
        );

        loginBtn.setOnAction(e -> {

            String email =
                    emailField.getText();

            String pass =
                    passField.getText();

            if (email.isEmpty()
                    || pass.isEmpty()) {

                showAlert(
                        "All fields required!"
                );

                return;
            }

            try {

                Connection con =
                        DBConnection.getConnection();

                String sql =
                        "SELECT user_id, full_name " +
                        "FROM users " +
                        "WHERE email=? AND password=?";

                PreparedStatement ps =
                        con.prepareStatement(sql);

                ps.setString(1, email);

                ps.setString(2, pass);

                ResultSet rs =
                        ps.executeQuery();

                if (rs.next()) {

                    int id =
                            rs.getInt("user_id");

                    String name =
                            rs.getString("full_name");

                    ChatUI.userId = id;

                    ChatUI.setUsername(name);

                    stage.close();

                    ChatUI chat =
                            new ChatUI();

                    chat.start(new Stage());

                    new Thread(() -> {

                        ClientConnection.connect(name);

                    }).start();

                } else {

                    showAlert(
                            "Invalid login!"
                    );
                }

            } catch (Exception ex) {

                ex.printStackTrace();
            }
        });

        registerBtn.setOnAction(e -> {

            stage.close();

            new RegisterUI().start(
                    new Stage()
            );
        });

        Scene scene =
                new Scene(box, 300, 220);

        stage.setScene(scene);

        stage.setTitle("Login");

        stage.show();
    }

    private void showAlert(String msg) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setContentText(msg);

        alert.showAndWait();
    }
}