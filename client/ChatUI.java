package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatUI extends Application {

    public static TextArea chatArea = new TextArea();
    public static TextField inputField = new TextField();
    public static ListView<String> usersList = new ListView<>();

    @Override
    public void start(Stage stage) {

        chatArea.setEditable(false);

        Button sendButton = new Button("Send");

        VBox chatBox = new VBox(10, chatArea, inputField, sendButton);
        VBox usersBox = new VBox(10, new Label("Active Users"), usersList);

        HBox layout = new HBox(10, usersBox, chatBox);

        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        Scene scene = new Scene(layout, 600, 400);

        stage.setTitle("Chat App");
        stage.setScene(scene);
        stage.show();

        // start connection (ASK USERNAME SIMPLE)
        String username = "User" + (int)(Math.random() * 100);

        new Thread(() -> ClientConnection.connect(username)).start();
    }

    private void sendMessage() {

        String msg = inputField.getText();

        if (!msg.isEmpty()) {

            ClientConnection.sendMessage(msg);
            inputField.clear();
        }
    }

    public static void displayMessage(String message) {

        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    public static void main(String[] args) {
        launch();
    }
}