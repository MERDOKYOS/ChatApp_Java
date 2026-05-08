package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {

    public static List<ClientHandler> clients = new ArrayList<>();

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
}