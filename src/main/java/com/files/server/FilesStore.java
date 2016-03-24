package com.files.server;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by azyubenko on 22.03.16.
 */
public class FilesStore {

    private final File root;

    public FilesStore(File file) {
        if (!file.isDirectory())
            throw new IllegalArgumentException("file must be a directory");
        this.root = file;
    }

    public File[] listFiles() {
        return root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        });
    }

    public File getFile(String fileName) {
        File file = new File(root, fileName);
        if (!file.exists())
            throw new IllegalArgumentException("File " + fileName + " does not exist.");
        return file;
    }
}
