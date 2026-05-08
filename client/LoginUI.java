package client;

import db.DBConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        VBox box = new VBox(10, emailField, passField, loginBtn, registerBtn);

        loginBtn.setOnAction(e -> {

            String email = emailField.getText();
            String pass = passField.getText();

            // ✅ VALIDATION
            if (email.isEmpty() || pass.isEmpty()) {
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("Login Error");
              alert.setHeaderText(null);
              alert.setContentText("Email and Password required!");
              alert.showAndWait();
                return;
            }

            try {

                Connection con = DBConnection.getConnection();

                String sql = "SELECT full_name FROM users WHERE email=? AND password=?";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, pass);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    String name = rs.getString("full_name");

                    ChatUI.setUsername(name);

                    stage.close();

                    ChatUI chat = new ChatUI();
                    chat.start(new Stage());

                    new Thread(() -> ClientConnection.connect(name)).start();

                } else {
                    ChatUI.displayMessage("❌ Invalid login!");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        registerBtn.setOnAction(e -> {
            stage.close();
            new RegisterUI().start(new Stage());
        });

        Scene scene = new Scene(box, 300, 200);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
}