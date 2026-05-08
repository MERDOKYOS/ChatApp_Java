package client;

import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private static Socket socket;
    private static DataInputStream dis;
    private static DataOutputStream dos;

    private static boolean connected = false;

    // ================= CONNECT =================
    public static void connect(String username) {

        if (connected) return; // 🔥 PREVENT DOUBLE CONNECT
        connected = true;

        try {

            socket = new Socket("localhost", 5000);

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // send username ONCE
            dos.writeUTF(username);
            dos.flush();

            receiveMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEND MESSAGE =================
    public static void sendMessage(String message) {

        try {
            dos.writeUTF("MESSAGE");
            dos.writeUTF(message);
            dos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEND FILE =================
    public static void sendFile(File file) {

        try {

            FileInputStream fis = new FileInputStream(file);

            dos.writeUTF("FILE");
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int read;

            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }

            dos.flush();
            fis.close();

            ChatUI.displayMessage("📁 File sent: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= RECEIVE =================
    private static void receiveMessages() {

        new Thread(() -> {

            try {

                while (socket.isConnected()) {

                    String type = dis.readUTF();

                    // MESSAGE
                    if (type.equals("MESSAGE")) {

                        String msg = dis.readUTF();
                        ChatUI.displayMessage(msg);
                    }

                    // USERS
                    else if (type.equals("USERS")) {

                        String users = dis.readUTF();
                        ChatUI.updateUsers(users.split(","));
                    }

                    // FILE
                    else if (type.equals("FILE")) {

                        String fileName = dis.readUTF();
                        long fileSize = dis.readLong();

                        String savePath = "client_received_" + fileName;

                        receiveFile(savePath, fileSize);

                        ChatUI.showFileMessage(fileName, savePath);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    // ================= RECEIVE FILE =================
    private static void receiveFile(String savePath, long fileSize) {

        try {

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

            ChatUI.displayMessage("📁 File saved: " + savePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}