package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientMain() {

        try {
            socket = new Socket("localhost", 5000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            System.out.println("✅ Connected to server!");

            // thread for receiving messages
            new Thread(this::receiveMessages).start();

            sendMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send messages from keyboard
    private void sendMessages() {

        Scanner scanner = new Scanner(System.in);

        while (socket.isConnected()) {

            try {
                String message = scanner.nextLine();

                writer.write(message);
                writer.newLine();
                writer.flush();

            } catch (Exception e) {
                closeEverything();
                break;
            }
        }
    }

    // receive messages from server
    private void receiveMessages() {

        String message;

        while (socket.isConnected()) {

            try {
                message = reader.readLine();

                if (message == null) break;

                System.out.println("📩 " + message);

            } catch (Exception e) {
                closeEverything();
                break;
            }
        }
    }

    private void closeEverything() {
        try {
            socket.close();
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClientMain();
    }
}