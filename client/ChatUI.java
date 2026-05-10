package client;

import java.io.File;
import java.awt.Desktop;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ChatUI extends Application {

    public static int userId;

    public static String username;

    public static TextArea chatArea =
            new TextArea();

    public static TextField inputField =
            new TextField();

    public static ListView<UserItem> usersList =
            new ListView<>();

    private static String lastFilePath = null;

    public static void setUsername(String name) {

        username = name;
    }

    @Override
    public void start(Stage stage) {

        chatArea.setEditable(false);

        chatArea.setWrapText(true);

        inputField.setPromptText(
                "Type message..."
        );

        Button sendBtn =
                new Button("Send");

        Button fileBtn =
                new Button("Send File");

        VBox chatBox = new VBox(
                10,
                chatArea,
                inputField,
                sendBtn,
                fileBtn
        );

        chatBox.setPrefWidth(450);

        VBox usersBox = new VBox(
                10,
                new Label("Active Users"),
                usersList
        );

        usersBox.setPrefWidth(180);

        HBox root = new HBox(
                10,
                usersBox,
                chatBox
        );

        sendBtn.setOnAction(e -> sendMessage());

        inputField.setOnAction(
                e -> sendMessage()
        );

        fileBtn.setOnAction(
                e -> sendFile()
        );

        Scene scene =
                new Scene(root, 700, 500);

        stage.setScene(scene);

        stage.setTitle(
                "Multi User Chat App"
        );

        stage.show();

        // FILE OPEN
        chatArea.setOnMouseClicked(e -> {

            if (e.getClickCount() == 2 &&
                    lastFilePath != null) {

                try {

                    File file =
                            new File(lastFilePath);

                    if (file.exists()) {

                        Desktop.getDesktop()
                                .open(file);
                    }

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }
        });
    }

    // ================= MESSAGE =================
    private void sendMessage() {

        String msg =
                inputField.getText();

        if (msg == null ||
                msg.trim().isEmpty()) {

            return;
        }

        UserItem selectedUser =
                usersList.getSelectionModel()
                        .getSelectedItem();

        // PRIVATE
        if (selectedUser != null) {

            ClientConnection.sendPrivateMessage(
                    selectedUser.getId(),
                    msg
            );

            displayMessage(
                    "(Private to "
                            + selectedUser.getName()
                            + "): "
                            + msg
            );
        }

        // PUBLIC
        else {

            ClientConnection.sendMessage(msg);

            displayMessage(
                    "Me: " + msg
            );
        }

        inputField.clear();
    }

    // ================= FILE =================
    private void sendFile() {

        FileChooser chooser =
                new FileChooser();

        File file =
                chooser.showOpenDialog(null);

        if (file == null) return;

        UserItem selectedUser =
                usersList.getSelectionModel()
                        .getSelectedItem();

        // PRIVATE FILE
        if (selectedUser != null) {

            new Thread(() -> {

                ClientConnection.sendPrivateFile(
                        selectedUser.getId(),
                        file
                );

            }).start();

            displayMessage(
                    "📁 Private file sent to "
                            + selectedUser.getName()
            );
        }

        // PUBLIC FILE
        else {

            new Thread(() -> {

                ClientConnection.sendFile(file);

            }).start();

            displayMessage(
                    "📁 Public file sent"
            );
        }
    }

    // ================= DISPLAY =================
    public static void displayMessage(
            String message
    ) {

        Platform.runLater(() -> {

            chatArea.appendText(
                    message + "\n"
            );
        });
    }

    // ================= USERS =================
    public static void updateUsers(
            String[] users
    ) {

        Platform.runLater(() -> {

            usersList.getItems().clear();

            for (String user : users) {

                if (user.trim().isEmpty())
                    continue;

                String[] parts =
                        user.split(":");

                int id =
                        Integer.parseInt(parts[0]);

                String name =
                        parts[1];

                if (!name.equals(username)) {

                    usersList.getItems().add(

                            new UserItem(
                                    id,
                                    name
                            )
                    );
                }
            }
        });
    }

    // ================= FILE MESSAGE =================
    public static void showFileMessage(
            String fileName,
            String path
    ) {

        Platform.runLater(() -> {

            lastFilePath = path;

            chatArea.appendText(
                    "📁 "
                            + fileName
                            + " (double click to open)\n"
            );
        });
    }

    public static void main(String[] args) {

        launch();
    }
}