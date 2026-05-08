package client;

import java.io.File;
import java.awt.Desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ChatUI extends Application {

    public static TextArea chatArea = new TextArea();
    public static TextField inputField = new TextField();
    public static ListView<String> usersList = new ListView<>();

    // ================= USERNAME =================
    public static String username;

    public static void setUsername(String name) {
        username = name;
    }

    // store last received file path
    private static String lastFilePath = null;

    @Override
    public void start(Stage stage) {

        // ================= CHAT AREA =================
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        // ================= INPUT =================
        inputField.setPromptText("Type a message...");

        // ================= BUTTONS =================
        Button sendButton = new Button("Send");
        Button fileButton = new Button("Send File");

        // ================= CHAT BOX =================
        VBox chatBox = new VBox(10,
                chatArea,
                inputField,
                sendButton,
                fileButton
        );

        chatBox.setPrefWidth(450);

        // ================= USERS BOX =================
        VBox usersBox = new VBox(10,
                new Label("Active Users"),
                usersList
        );

        usersBox.setPrefWidth(150);

        // ================= MAIN LAYOUT =================
        HBox layout = new HBox(10, usersBox, chatBox);

        // ================= ACTIONS =================
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());
        fileButton.setOnAction(e -> sendFile());

        // ================= SCENE =================
        Scene scene = new Scene(layout, 650, 450);

        stage.setTitle("Multi User Chat App");
        stage.setScene(scene);
        stage.show();

        // ================= CONNECT ONLY ONCE =================
        if (username == null) {
            username = "User" + (int)(Math.random() * 1000);
        }

        new Thread(() -> ClientConnection.connect(username)).start();

        // ================= DOUBLE CLICK FILE OPEN =================
        chatArea.setOnMouseClicked(e -> {

            if (e.getClickCount() == 2 && lastFilePath != null) {

                try {
                    File file = new File(lastFilePath);

                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        displayMessage("❌ File not found");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // ================= SEND MESSAGE =================
    private void sendMessage() {

        String msg = inputField.getText();

        if (msg != null && !msg.trim().isEmpty()) {

            ClientConnection.sendMessage(msg);
            inputField.clear();
        }
    }

    // ================= SEND FILE =================
    private void sendFile() {

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {

            new Thread(() -> ClientConnection.sendFile(file)).start();
        }
    }

    // ================= MESSAGE =================
    public static void displayMessage(String message) {

        Platform.runLater(() ->
                chatArea.appendText(message + "\n")
        );
    }

    // ================= USERS =================
    public static void updateUsers(String[] users) {

        Platform.runLater(() -> {
            usersList.getItems().setAll(users);
        });
    }

    // ================= FILE MESSAGE =================
    public static void showFileMessage(String fileName, String path) {

        Platform.runLater(() -> {

            lastFilePath = path;
            chatArea.appendText("📁 " + fileName + " (double click to open)\n");
        });
    }

    public static void main(String[] args) {
        launch();
    }
}