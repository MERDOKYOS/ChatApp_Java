package server;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;

public class ServerMain {

    // userId -> ClientHandler
    public static Map<Integer, ClientHandler> activeClients =
            new HashMap<>();

    // username -> userId
    public static Map<String, Integer> userMap =
            new HashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket =
                     new ServerSocket(5000)) {

            System.out.println("🚀 Server Started...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println("✅ Client Connected");

                ClientHandler handler =
                        new ClientHandler(socket);

                handler.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= PUBLIC MESSAGE =================
    public static synchronized void broadcast(
            String message,
            ClientHandler sender
    ) {

        for (ClientHandler client :
                activeClients.values()) {

            if (client != sender) {

                client.sendMessage(message);
            }
        }
    }

    // ================= PRIVATE MESSAGE =================
    public static synchronized void sendPrivateMessage(
            int receiverId,
            String message
    ) {

        ClientHandler client =
                activeClients.get(receiverId);

        if (client != null) {

            client.sendMessage(message);
        }
    }

    // ================= USERS =================
    public static synchronized void broadcastUsers() {

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> entry :
                userMap.entrySet()) {

            sb.append(entry.getValue())
                    .append(":")
                    .append(entry.getKey())
                    .append(",");
        }

        String users = sb.toString();

        for (ClientHandler client :
                activeClients.values()) {

            client.sendUsers(users);
        }
    }

    // ================= REMOVE =================
    public static synchronized void removeClient(
            ClientHandler client,
            int userId,
            String username
    ) {

        activeClients.remove(userId);
        userMap.remove(username);

        broadcast(
                "🔴 " + username + " left chat",
                client
        );

        broadcastUsers();
    }

    // ================= PUBLIC FILE =================
    public static synchronized void broadcastFile(
            String filePath,
            String fileName,
            long fileSize,
            ClientHandler sender
    ) {

        for (ClientHandler client :
                activeClients.values()) {

            if (client != sender) {

                client.sendFile(
                        filePath,
                        fileName,
                        fileSize
                );
            }
        }
    }

    // ================= PRIVATE FILE =================
    public static synchronized void sendPrivateFile(
            int receiverId,
            String filePath,
            String fileName,
            long fileSize
    ) {

        ClientHandler client =
                activeClients.get(receiverId);

        if (client != null) {

            client.sendFile(
                    filePath,
                    fileName,
                    fileSize
            );
        }
    }
}