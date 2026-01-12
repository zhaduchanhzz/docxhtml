package com.example.webdav;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Base64;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;


public class PdfExternalHashSignFixed {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
//        if (args.length < 5) {
//            System.out.println("Usage: java PdfExternalHashSignFixed <inputPdf> <outputPdf> <apiBaseUrl> <workerName> <digestMethod>");
//            return;
//        }

        String inputPdf = "input.pdf";
        String outputPdf = "output.pdf";
        String apiBaseUrl = "http://104.156.255.132:8666";
        String workerName = "minhlq.pa";
        String digestMethod = "SHA256WITHRSA";

        String tempPrepared = "prepared_temp.pdf";

        // Bước 1 & 2: Prepare + lấy hash ranges đúng cách
        byte[] hashToSign = prepareAndGetHash(inputPdf, tempPrepared, digestMethod);
        String base64Hash = Base64.getEncoder().encodeToString(hashToSign);

        // Bước 3: Gọi API ký hash (type "hash")
        String base64Cms = callSignApi(apiBaseUrl, workerName, base64Hash, digestMethod);

        // Bước 4: Nhúng CMS detached vào prepared PDF
        embedCmsIntoPdf(tempPrepared, outputPdf, Base64.getDecoder().decode(base64Cms));

        new File(tempPrepared).delete();
        System.out.println("Ký thành công! File: " + outputPdf);
    }

    /**
     * Custom container để lấy hash ranges trong lúc prepare
     */
    static class HashCapturingContainer implements IExternalSignatureContainer {
        private byte[] capturedHash;
        private final PdfName filter;
        private final PdfName subFilter;

        public HashCapturingContainer(PdfName filter, PdfName subFilter) {
            this.filter = filter;
            this.subFilter = subFilter;
        }

        @Override
        public byte[] sign(InputStream data) throws GeneralSecurityException {
            // Ở đây iText gọi sign() với InputStream là ranges đã loại placeholder
            // Chúng ta tính hash ngay tại đây
            try {
                BouncyCastleDigest bcDigest = new BouncyCastleDigest();
                capturedHash = DigestAlgorithms.digest(data, bcDigest.getMessageDigest("SHA256"));
            } catch (IOException e) {
                throw new GeneralSecurityException(e);
            }
            // Trả về placeholder rỗng (dummy) để reserve space
            return new byte[0];  // hoặc một mảng dummy đủ lớn
        }

        @Override
        public void modifySigningDictionary(com.itextpdf.kernel.pdf.PdfDictionary signDic) {
            signDic.put(PdfName.Filter, filter);
            signDic.put(PdfName.SubFilter, subFilter);
        }

        public byte[] getCapturedHash() {
            return capturedHash;
        }
    }
    private static byte[] prepareAndGetHash(String inputPdfPath, String tempOutputPath, String digestAlgo) throws Exception {
        // Tạo PdfReader MỚI từ file gốc
        PdfReader reader = new PdfReader(inputPdfPath);

        // Tạo PdfSigner trực tiếp với reader và output file (không cần PdfDocument riêng)
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(tempOutputPath), new StampingProperties().useAppendMode());

        signer.setFieldName("SignatureField1");

        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance.setReason("Ký số tập trung");
        appearance.setLocation("Hà Nội");
        appearance.setPageRect(new Rectangle(50, 50, 200, 80));
        appearance.setPageNumber(1);
        appearance.setLayer2Text("Đã ký bởi hệ thống ESIGNATURE");

        // Custom container để capture hash ranges
        HashCapturingContainer container = new HashCapturingContainer(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);

        // Gọi signExternalContainer → iText tự mở/close PdfDocument bên trong, capture hash trong container.sign()
        signer.signExternalContainer(container, 16384);  // 16384 bytes reserved cho CMS

        // Không cần pdfDoc.close() nữa vì signExternalContainer đã close document

        byte[] capturedHash = container.getCapturedHash();
        if (capturedHash == null || capturedHash.length == 0) {
            throw new RuntimeException("Không capture được hash ranges từ PDF!");
        }

        return capturedHash;
    }

    private static String callSignApi(String baseUrl, String workerName, String base64Hash, String digestMethod) throws Exception {
        URL url = new URL(baseUrl + "/api/esignature/sign");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        String json = String.format(
                "{\"type\":\"hash\",\"data\":\"%s\",\"workerName\":\"%s\",\"option\":{\"digestMethod\":\"%s\",\"signatureMethod\":\"%swithRSA\"}}",
                base64Hash, workerName, digestMethod, digestMethod
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("API error: " + conn.getResponseCode());
        }

        StringBuilder resp = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                resp.append(line);
            }
        }

        // Parse đơn giản (nên dùng JSON lib như Gson/Jackson)
        String jsonResp = resp.toString();
        if (!jsonResp.contains("\"responseCode\":1")) {
            throw new RuntimeException("Ký thất bại: " + jsonResp);
        }
        // Giả sử signedData ở giữa dấu "
        return jsonResp.split("\"signedData\":\"")[1].split("\"")[0];
    }

    private static void embedCmsIntoPdf(String preparedPdfPath, String finalPdfPath, byte[] cmsBytes) throws Exception {
        // Tạo PdfReader MỚI từ file tạm
        PdfReader newReader = new PdfReader(preparedPdfPath);

        PdfSigner embedSigner = new PdfSigner(newReader, new FileOutputStream(finalPdfPath), new StampingProperties().useAppendMode());

        IExternalSignatureContainer embedContainer = new IExternalSignatureContainer() {
            @Override
            public byte[] sign(InputStream data) {
                return cmsBytes;
            }

            @Override
            public void modifySigningDictionary(com.itextpdf.kernel.pdf.PdfDictionary signDic) {
                // Không cần
            }
        };

        embedSigner.signExternalContainer(embedContainer, cmsBytes.length + 8192);
    }
}