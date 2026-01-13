package com.example.webdav;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;

public class PdfRemoteHashSign_7_2_5 {

    private static final String BASE_URL = "http://104.156.255.132:8666/";
    private static final String WORKER_NAME = "minhlq.pa"; // thay bằng worker thật

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        try {
            String inputPdf = "input.pdf";
            String outputPdf = "outputzzz.pdf";

            signPdfWithRemoteHash(inputPdf, outputPdf);
            System.out.println("Ký xong: " + outputPdf);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void signPdfWithRemoteHash(String inputPath, String outputPath) throws Exception {
        // 1. Lấy certificate từ API (chỉ cần cert gốc để nhúng chain và kiểm tra)
        X509Certificate signingCert = getCertificateFromApi();

        // 2. Chuẩn bị PdfSigner (cách của 7.2.x)
        PdfReader reader = new PdfReader(inputPath);
        FileOutputStream fos = new FileOutputStream(outputPath);
        StampingProperties sp = new StampingProperties(); // hoặc .useAppendMode() nếu append signature

        PdfSigner signer = new PdfSigner(reader, fos, sp);

        // Cấu hình appearance (chữ ký hiển thị - tùy chọn)
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance
                .setReason("Ký điện tử hợp đồng")
                .setLocation("Hà Nội")
                .setPageRect(new Rectangle(36, 36, 200, 100))
                .setPageNumber(1)
                .setReuseAppearance(false);

        signer.setFieldName("Signature_" + Calendar.getInstance().getTimeInMillis());

        // 3. External Digest & Signature
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature externalSignature = new RemoteHashExternalSignature();

        // 4. Ký detached (dùng overload phổ biến nhất ở 7.2.5)
        // Nếu không cần CRL/OCSP/TSA thì truyền null
        signer.signDetached(
                digest,                     // external digest
                externalSignature,          // remote signing
                new Certificate[]{signingCert}, // chain (ít nhất cert gốc)
                null,                       // crlList
                null,                       // ocspClient
                null,                       // tsaClient
                0,                          // estimatedSize = 0 → iText tự tính
                PdfSigner.CryptoStandard.CMS  // hoặc CADES nếu server trả CADES
        );

        // Đóng document (signDetached sẽ tự close)
        reader.close();
    }

    // Lấy certificate từ API /api/esignature/certificate
    private static X509Certificate getCertificateFromApi() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Chuẩn bị body JSON
        String jsonBody = String.format("""
            {
                "workerName": "%s",
                "workerId": ""
            }
            """, WORKER_NAME);  // WORKER_NAME là hằng số bạn đã định nghĩa

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/esignature/certificate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Lấy certificate thất bại (HTTP "
                    + response.statusCode() + "): " + response.body());
        }

        String json = response.body();

        // Trích xuất trường "cert" (base64)
        String certBase64 = extractJsonValue(json, "\"cert\":\"", "\"");

        if (certBase64 == null || certBase64.isEmpty()) {
            throw new RuntimeException("Không tìm thấy trường 'cert' trong response");
        }

        byte[] certBytes = Base64.getDecoder().decode(certBase64);

        // Parse thành X509Certificate
        return (X509Certificate) java.security.cert.CertificateFactory
                .getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(certBytes));
    }
    // Class xử lý ký hash từ xa
    static class RemoteHashExternalSignature implements IExternalSignature {

        @Override
        public String getEncryptionAlgorithm() {
            return "RSA"; // thay bằng "ECDSA" nếu token dùng EC
        }

        @Override
        public String getHashAlgorithm() {
            return DigestAlgorithms.SHA256; // hoặc SHA1, SHA512 tùy server
        }

        @Override
        public byte[] sign(byte[] digest)  {
            // digest là hash SHA256 (đã padding đúng chuẩn) của PDF ranges
            String hashBase64 = Base64.getEncoder().encodeToString(digest);

            String jsonBody = String.format("""
                    {
                        "type": "hash",
                        "data": "%s",
                        "workerName": "%s",
                        "workerId": ""
                    }
                    """, hashBase64, WORKER_NAME);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/esignature/sign"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("Ký hash thất bại: " + response.body());
            }

            String signedBase64 = extractJsonValue(response.body(), "\"signedData\":\"", "\"");
            return Base64.getDecoder().decode(signedBase64);
        }
    }

    // Helper parse json thủ công (an toàn cho trường hợp này)
    private static String extractJsonValue(String json, String prefix, String suffix) {
        int start = json.indexOf(prefix) + prefix.length();
        int end = json.indexOf(suffix, start);
        if (start < prefix.length() || end == -1) {
            throw new RuntimeException("Không tìm thấy giá trị trong JSON");
        }
        return json.substring(start, end);
    }
}