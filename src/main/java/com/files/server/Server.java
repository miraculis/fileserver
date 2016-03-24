package com.files.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by azyubenko on 23.03.16.
 */
public class Server {
    private final int port;
    private FilesStore filesStore;

    public Server(int port, FilesStore filesStore) {
        this.port = port;
        this.filesStore = filesStore;
    }

    public void start() {
        ServerSocket ss;
        try {
            ss = new ServerSocket(port);
            System.out.println("Server started on " + port);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return;
        }

        Socket s = null;
        while (true) {
            try {
                s = ss.accept();
                System.out.println("Client accepted on " + s.getInetAddress());
                new Thread( new SocketProcessor(s, filesStore)).start();
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
                return;
            }
        }
    }

    private static class SocketProcessor implements Runnable {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private FilesStore filesStore;

        public SocketProcessor(Socket socket, FilesStore filesStore) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.filesStore = filesStore;
        }

        @Override
        public void run() {
            String input = null;
            try {
                while ((input = in.readUTF()) != null) {
                    if ("exit".equalsIgnoreCase(input)) {
                        close();
                        return;
                    } else if ("list".equalsIgnoreCase(input)) {
                        File[] files = filesStore.listFiles();
                        out.writeUTF("list");
                        out.writeInt(files.length);
                        for (File file : files) {
                            out.writeUTF(file.getName());
                        }
                    } else if (input.startsWith("get")) {
                        out.writeUTF(input);
                        String fileName = input.split("\\s+")[1];
                        File file = filesStore.getFile(fileName);
                        out.writeUTF(fileName);
                        out.writeLong(file.length());
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        long size = file.length();
                        int bytesRead = 0;
                        while (size > 0 && (bytesRead = fis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                            out.write(buffer, 0, bytesRead);
                            size -= bytesRead;
                        }
                    } else {
                        String message = "Unknown command " + input;
                        System.out.println(message);
                        out.writeUTF(message);
                    }
                }
            } catch (IOException e1) {
                System.out.println(e1);
                e1.printStackTrace();
                close();
                return;
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                try {
                    out.writeUTF("error");
                    out.writeUTF(e.getMessage());
                } catch (IOException e2) {
                    System.out.println(e2);
                    e2.printStackTrace();
                    close();
                    return;
                }
            }

        }

        private void close() {
            try {
                out.close();
            } catch (Exception e) {}
            try {
                in.close();
            } catch (Exception e) {}
            try {
                socket.close();
            } catch (Exception e) {}
        }
    }
}
