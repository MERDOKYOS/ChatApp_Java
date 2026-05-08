package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String message;

        while (socket.isConnected()) {

            try {
                message = reader.readLine();

                if (message == null) {
                    break;
                }

                System.out.println("📩 Received: " + message);

                broadcast(message);

            } catch (Exception e) {
                closeEverything();
                break;
            }
        }
    }

    private void broadcast(String message) {

        for (ClientHandler client : ServerMain.clients) {
            try {
                client.writer.write(message);
                client.writer.newLine();
                client.writer.flush();

            } catch (Exception e) {
                e.printStackTrace();
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
}