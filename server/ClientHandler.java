package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private String username;

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

        try {

            // 1. FIRST MESSAGE = username
            username = reader.readLine();

            ServerMain.activeUsers.add(username);

            ServerMain.broadcast("🟢 " + username + " joined chat");
            sendActiveUsers();

            String message;

            while (socket.isConnected()) {

                message = reader.readLine();

                if (message == null) break;

                ServerMain.broadcast(username + ": " + message);
            }

        } catch (Exception e) {
            closeEverything();
        }
    }

    public void send(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    private void sendActiveUsers() {

        try {

            String users = String.join(",", ServerMain.activeUsers);

            for (ClientHandler c : ServerMain.clients) {
                c.writer.write("USERS:" + users);
                c.writer.newLine();
                c.writer.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeEverything() {

        try {

            ServerMain.clients.remove(this);
            ServerMain.activeUsers.remove(username);

            ServerMain.broadcast("🔴 " + username + " left chat");
            sendActiveUsers();

            socket.close();
            reader.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}