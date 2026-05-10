package server;

import java.io.*;
import java.net.Socket;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ClientHandler extends Thread {

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private String username;
    private int userId;

    public ClientHandler(Socket socket) {

        this.socket = socket;

        try {

            dis = new DataInputStream(
                    socket.getInputStream()
            );

            dos = new DataOutputStream(
                    socket.getOutputStream()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {

            while (socket.isConnected()) {

                String type = dis.readUTF();

                // ================= USER =================
                if (type.equals("USERNAME")) {

                    userId = dis.readInt();
                    username = dis.readUTF();

                    ServerMain.activeClients.put(
                            userId,
                            this
                    );

                    ServerMain.userMap.put(
                            username,
                            userId
                    );

                    ServerMain.broadcast(
                            "🟢 " + username + " joined chat",
                            this
                    );

                    ServerMain.broadcastUsers();
                }

                // ================= PUBLIC MESSAGE =================
                else if (type.equals("MESSAGE")) {

                    String message = dis.readUTF();

                    saveMessage(
                            userId,
                            null,
                            message
                    );

                    ServerMain.broadcast(
                            username + ": " + message,
                            this
                    );
                }

                // ================= PRIVATE MESSAGE =================
                else if (type.equals("PRIVATE")) {

                    int receiverId = dis.readInt();

                    String message = dis.readUTF();

                    saveMessage(
                            userId,
                            receiverId,
                            message
                    );

                    ServerMain.sendPrivateMessage(
                            receiverId,
                            "(Private) " +
                                    username +
                                    ": " +
                                    message
                    );
                }

                // ================= PUBLIC FILE =================
                else if (type.equals("FILE")) {

                    String fileName = dis.readUTF();

                    long fileSize = dis.readLong();

                    receiveFile(
                            fileName,
                            fileSize,
                            null
                    );
                }

                // ================= PRIVATE FILE =================
                else if (type.equals("PRIVATE_FILE")) {

                    int receiverId = dis.readInt();

                    String fileName = dis.readUTF();

                    long fileSize = dis.readLong();

                    receiveFile(
                            fileName,
                            fileSize,
                            receiverId
                    );
                }
            }

        } catch (Exception e) {

            closeEverything();
        }
    }

    // ================= SAVE MESSAGE =================
    private void saveMessage(
            int senderId,
            Integer receiverId,
            String message
    ) {

        try {

            Connection con =
                    Database.getConnection();

            String sql =
                    "INSERT INTO messages " +
                    "(sender_id, receiver_id, message) " +
                    "VALUES (?,?,?)";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, senderId);

            if (receiverId == null) {

                ps.setNull(
                        2,
                        java.sql.Types.INTEGER
                );

            } else {

                ps.setInt(2, receiverId);
            }

            ps.setString(3, message);

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= SAVE FILE =================
    private void saveFile(
            int senderId,
            Integer receiverId,
            String fileName,
            long fileSize
    ) {

        try {

            Connection con =
                    Database.getConnection();

            String sql =
                    "INSERT INTO files " +
                    "(sender_id, receiver_id, file_name, file_size) " +
                    "VALUES (?,?,?,?)";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, senderId);

            if (receiverId == null) {

                ps.setNull(
                        2,
                        java.sql.Types.INTEGER
                );

            } else {

                ps.setInt(2, receiverId);
            }

            ps.setString(3, fileName);

            ps.setLong(4, fileSize);

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ================= RECEIVE FILE =================
    private void receiveFile(
            String fileName,
            long fileSize,
            Integer receiverId
    ) {

        try {

            String savePath =
                    "received_" +
                            System.currentTimeMillis() +
                            "_" +
                            fileName;

            FileOutputStream fos =
                    new FileOutputStream(savePath);

            byte[] buffer = new byte[4096];

            int read;

            long remaining = fileSize;

            while (remaining > 0 &&
                    (read = dis.read(
                            buffer,
                            0,
                            (int)Math.min(
                                    buffer.length,
                                    remaining
                            )
                    )) > 0) {

                fos.write(buffer, 0, read);

                remaining -= read;
            }

            fos.close();

            saveFile(
                    userId,
                    receiverId,
                    fileName,
                    fileSize
            );

            // PUBLIC FILE
            if (receiverId == null) {

                ServerMain.broadcast(
                        "📁 " + username +
                                " sent file: " +
                                fileName,
                        this
                );

                ServerMain.broadcastFile(
                        savePath,
                        fileName,
                        fileSize,
                        this
                );
            }

            // PRIVATE FILE
            else {

                ServerMain.sendPrivateMessage(
                        receiverId,
                        "📁 Private file from " +
                                username +
                                ": " +
                                fileName
                );

                ServerMain.sendPrivateFile(
                        receiverId,
                        savePath,
                        fileName,
                        fileSize
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
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
    public void sendFile(
            String filePath,
            String fileName,
            long fileSize
    ) {

        try {

            dos.writeUTF("FILE");

            dos.writeUTF(fileName);

            dos.writeLong(fileSize);

            FileInputStream fis =
                    new FileInputStream(filePath);

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

    // ================= CLEANUP =================
    private void closeEverything() {

        try {

            ServerMain.removeClient(
                    this,
                    userId,
                    username
            );

            if (socket != null) socket.close();

            if (dis != null) dis.close();

            if (dos != null) dos.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}