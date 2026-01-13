package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class FileHashUtil {

    public static String hashFileSha256Base64(String pathFile) throws Exception {
        if (pathFile == null || pathFile.isEmpty()) {
            throw new IllegalArgumentException("Path file is null or empty.");
        }

        File file = new File(pathFile);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist: " + pathFile);
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            return Base64.getEncoder().encodeToString(hashBytes);
        }
    }
}