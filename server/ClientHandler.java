package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String username;

    public ClientHandler(Socket socket) {

        this.socket = socket;

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {

            while (socket.isConnected()) {

                String type = dis.readUTF();

                // ================= USERNAME =================
                if (type.equals("USERNAME")) {

                    username = dis.readUTF();

                    // prevent duplicates
                    if (!ServerMain.activeUsers.contains(username)) {
                        ServerMain.activeUsers.add(username);
                    }

                    ServerMain.broadcast("🟢 " + username + " joined chat");

                    ServerMain.broadcastUsers();
                }

                // ================= MESSAGE =================
                else if (type.equals("MESSAGE")) {

                    String message = dis.readUTF();

                    ServerMain.broadcast(username + ": " + message);
                }

                // ================= FILE =================
                else if (type.equals("FILE")) {

                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();

                    receiveFile(fileName, fileSize);
                }
            }

        } catch (Exception e) {
            closeEverything();
        }
    }

    // ================= SEND MESSAGE =================
    public void sendMessage(String message) {

        try {
            dos.writeUTF("MESSAGE");
            dos.writeUTF(message);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEND USERS =================
    public void sendUsers(String users) {

        try {
            dos.writeUTF("USERS");
            dos.writeUTF(users);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEND FILE =================
    public void sendFile(String filePath, String fileName, long fileSize) {

        try {

            dos.writeUTF("FILE");
            dos.writeUTF(fileName);
            dos.writeLong(fileSize);

            FileInputStream fis = new FileInputStream(filePath);

            byte[] buffer = new byte[4096];
            int read;

            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }

            dos.flush();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= RECEIVE FILE =================
    private void receiveFile(String fileName, long fileSize) {

        try {

            String savePath = "received_" + fileName;

            FileOutputStream fos = new FileOutputStream(savePath);

            byte[] buffer = new byte[4096];
            int read;
            long remaining = fileSize;

            while (remaining > 0 &&
                    (read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {

                fos.write(buffer, 0, read);
                remaining -= read;
            }

            fos.close();

            ServerMain.broadcast("📁 File received: " + fileName);

            ServerMain.broadcastFile(savePath, fileName, fileSize, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CLEANUP =================
    private void closeEverything() {

        try {

            ServerMain.removeClient(this, username);

            socket.close();
            dis.close();
            dos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}