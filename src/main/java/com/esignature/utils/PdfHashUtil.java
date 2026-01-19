package com.esignature.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class PdfHashUtil {

    public static String getPdfHashBase64(File pdfFile) throws Exception {

        if (pdfFile == null || !pdfFile.exists()) {
            throw new IllegalArgumentException("PDF file is null or not found.");
        }

        try (InputStream inputStream = new FileInputStream(pdfFile)) {

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
