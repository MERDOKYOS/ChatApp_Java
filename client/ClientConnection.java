package client;

import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private static Socket socket;
    private static BufferedReader reader;
    private static BufferedWriter writer;

    public static void connect(String username) {

        try {

            socket = new Socket("localhost", 5000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // send username first
            writer.write(username);
            writer.newLine();
            writer.flush();

            System.out.println("Connected as " + username);

            receiveMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message) {

        try {
            writer.write(message);
            writer.newLine();
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessages() {

        new Thread(() -> {

            String msg;

            while (socket.isConnected()) {

                try {

                    msg = reader.readLine();

                    if (msg != null) {

                        if (msg.startsWith("USERS:")) {

                            String users = msg.substring(6);
                            String[] list = users.split(",");

                            javafx.application.Platform.runLater(() -> {
                                ChatUI.usersList.getItems().clear();
                                ChatUI.usersList.getItems().addAll(list);
                            });

                        } else {
                            ChatUI.displayMessage(msg);
                        }
                    }

                } catch (Exception e) {
                    break;
                }
            }

        }).start();
    }
}