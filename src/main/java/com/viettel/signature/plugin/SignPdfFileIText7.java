package com.viettel.signature.plugin;


import com.itextpdf.signatures.*;
import com.viettel.signature.pdf.DisplayConfig;
import com.viettel.signature.pdf.SignPdfAsynchronous7;
import com.viettel.signature.pdf.TimestampConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.security.*;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class SignPdfFileIText7 {

    private String tmpFile;
    private Date signDate;
    private byte[] hash;
    private Certificate[] chain;
    private String fieldName;
    private String digestAlgorithm = "SHA1";
    private String cryptAlgorithm = "RSA";

    public SignPdfFileIText7() {}

    /**
     * Tạo hash PDF để ký sau
     */
    public String createHash(String filePath,
                             Certificate[] chain,
                             String digestAlg,
                             String cryptAlg,
                             DisplayConfig displayConfig) {
        try {
            // Validate thuật toán
            if (!List.of("SHA1", "SHA256", "SHA384", "SHA512").contains(digestAlg)) {
                log.error("Digest Algorithm is invalid: {}", digestAlg);
                return null;
            }
            if (!List.of("RSA", "DSA").contains(cryptAlg)) {
                log.error("Crypt Algorithm is invalid: {}", cryptAlg);
                return null;
            }

            this.digestAlgorithm = digestAlg;
            this.cryptAlgorithm = cryptAlg;
            this.chain = chain;

            // Thiết lập tên field
            String dateString = new SimpleDateFormat(displayConfig.getDateFormatString())
                    .format(Optional.ofNullable(displayConfig.getSignDate()).orElse(new Date()));
            this.fieldName = Optional.ofNullable(displayConfig.getFieldName())
                    .filter(f -> !f.isEmpty())
                    .orElse(displayConfig.getContact().replaceAll("\\.", " ") + "_" + dateString);

            this.signDate = Optional.ofNullable(displayConfig.getSignDate()).orElse(new Date());

            // File tạm để chuẩn bị ký
            File tempFile = File.createTempFile("temp_pdf_", ".pdf");
            this.tmpFile = tempFile.getAbsolutePath();

            // Tạo hash và chữ ký offline
            SignPdfAsynchronous7 pdfSig = new SignPdfAsynchronous7();
            List<byte[]> lstHash = pdfSig.createHash(filePath, this.tmpFile, this.fieldName, digestAlg, chain, displayConfig);
            if (lstHash == null) return null;

            this.hash = lstHash.get(1);

            // Trả về Base64 hash để ký ngoài
            return Base64.getEncoder().encodeToString(lstHash.get(0));

        } catch (Exception ex) {
            log.error("createHash failed: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Ký PDF bằng hash đã tạo hoặc chữ ký ngoài
     */
    public boolean insertSignature(String extSigBase64, String destFile, TimestampConfig timestampConfig) {
        try {
            byte[] extSig = Base64.getDecoder().decode(extSigBase64);
            SignPdfAsynchronous7 pdfSig = new SignPdfAsynchronous7();

            File fileTemp = new File(this.tmpFile);
            boolean signed = pdfSig.insertSignatureItext7(
                    this.tmpFile,
                    destFile,
                    this.fieldName,
                    this.digestAlgorithm,
                    this.cryptAlgorithm,
                    this.hash,
                    extSig,
                    this.chain,
                    this.signDate,
                    timestampConfig
            );

            if (fileTemp.exists()) fileTemp.delete();
            return signed;

        } catch (Exception ex) {
            log.error("insertSignature failed: {}", ex.getMessage(), ex);
            return false;
        }
    }

    public byte[] getHash() { return hash; }
    public void setHash(byte[] hash) { this.hash = hash; }
}
