package com.files.server;

import java.io.File;

public class Launcher {
    public static final void main(String[] args) throws Exception {
        FilesStore store = new FilesStore(new File(args[1]));
        Server server = new Server(Integer.parseInt(args[0]), store);
        server.start();
    }
}