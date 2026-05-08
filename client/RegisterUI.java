package client;

import db.DBConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterUI extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Button registerBtn = new Button("Register");
        Button loginBtn = new Button("Go to Login");

        VBox box = new VBox(10, nameField, emailField, passField, registerBtn, loginBtn);

        registerBtn.setOnAction(e -> {

            String name = nameField.getText();
            String email = emailField.getText();
            String pass = passField.getText();

            // ✅ VALIDATION
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("Validation Error");
              alert.setHeaderText(null);
              alert.setContentText("All fields are required!");
              alert.showAndWait();
                return;
            }

            try {

                Connection con = DBConnection.getConnection();

                String sql = "INSERT INTO users(full_name, email, password, status, created_at) VALUES (?,?,?,?,NOW())";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, pass);
                ps.setString(4, "online");

                int rows = ps.executeUpdate();

                if (rows > 0) {

                    ChatUI.setUsername(name);

                    stage.close();

                    ChatUI chat = new ChatUI();
                    chat.start(new Stage());

                    new Thread(() -> ClientConnection.connect(name)).start();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        loginBtn.setOnAction(e -> {
            stage.close();
            new LoginUI().start(new Stage());
        });

        Scene scene = new Scene(box, 300, 250);
        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();
    }
}