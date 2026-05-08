package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {

    public static List<ClientHandler> clients =
            new ArrayList<>();

    public static List<String> activeUsers =
            new ArrayList<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(5000)) {

            System.out.println("🚀 Server started on port 5000...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println("✅ New client connected");

                ClientHandler handler = new ClientHandler(socket);

                clients.add(handler);

                handler.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BROADCAST MESSAGE =================
    public static void broadcast(String message) {

        for (ClientHandler client : clients) {
            try {
                client.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= BROADCAST USERS =================
    public static synchronized void broadcastUsers() {

        String users = String.join(",", activeUsers);

        for (ClientHandler client : clients) {
            try {
                client.sendUsers(users);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================= REMOVE CLIENT =================
    public static synchronized void removeClient(ClientHandler client, String username) {

        clients.remove(client);
        activeUsers.remove(username);

        broadcast("🔴 " + username + " left chat");

        broadcastUsers();
    }

    // ================= FILE BROADCAST =================
    public static void broadcastFile(
            String filePath,
            String fileName,
            long fileSize,
            ClientHandler sender
    ) {

        for (ClientHandler client : clients) {

            if (client != sender) {
                try {
                    client.sendFile(filePath, fileName, fileSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}