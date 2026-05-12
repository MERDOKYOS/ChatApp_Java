package client;

import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private static Socket socket;

    private static DataInputStream dis;

    private static DataOutputStream dos;

    private static boolean connected = false;

    // CREATE CONNECTION
    public static void connect(String username) {

        if (connected)
            return;

        connected = true;

        try {

            socket = new Socket("localhost", 5000);

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF("USERNAME");
            dos.writeInt(ChatUI.userId);
            dos.writeUTF(username);
            dos.flush();

            ChatUI.displayMessage("YOU LOGGED IN AS " + username);

            receiveMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // PUBLIC MESSAGE
    public static void sendMessage(
            String message) {

        try {

            dos.writeUTF("MESSAGE");

            dos.writeUTF(message);

            dos.flush();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // PRIVATE MESSAGE
    public static void sendPrivateMessage(
            int receiverId,
            String message) {

        try {

            dos.writeUTF("PRIVATE");

            dos.writeInt(receiverId);

            dos.writeUTF(message);

            dos.flush();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // PUBLIC FILE
    public static void sendFile(
            File file) {

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

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // PRIVATE FILE
    public static void sendPrivateFile(
            int receiverId,
            File file) {

        try {

            FileInputStream fis = new FileInputStream(file);

            dos.writeUTF("PRIVATE_FILE");

            dos.writeInt(receiverId);

            dos.writeUTF(file.getName());

            dos.writeLong(file.length());

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

    // RECEIVE
    private static void receiveMessages() {

        new Thread(() -> {

            try {

                while (socket.isConnected()) {

                    String type = dis.readUTF();

                    
                    if (type.equals("MESSAGE")) {

                        String msg = dis.readUTF();

                        ChatUI.displayMessage(msg);
                    }

                    
                    else if (type.equals("USERS")) {

                        String users = dis.readUTF();

                        ChatUI.updateUsers(
                                users.split(","));
                    }

                    
                    else if (type.equals("FILE")) {

                        String fileName = dis.readUTF();

                        long fileSize = dis.readLong();

                        String savePath = "client_received_"
                                + System.currentTimeMillis()
                                + "_"
                                + fileName;

                        receiveFile(
                                savePath,
                                fileSize);

                        ChatUI.showFileMessage(
                                fileName,
                                savePath);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        }).start();
    }

    // RECEIVE FILE
    private static void receiveFile(
            String savePath,
            long fileSize) {

        try {

            FileOutputStream fos = new FileOutputStream(savePath);

            byte[] buffer = new byte[4096];

            int read;

            long remaining = fileSize;

            while (remaining > 0 &&
                    (read = dis.read(
                            buffer,
                            0,
                            (int) Math.min(
                                    buffer.length,
                                    remaining))) > 0) {

                fos.write(buffer, 0, read);

                remaining -= read;
            }

            fos.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}