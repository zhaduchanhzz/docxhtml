package com.example.webdav;

import com.esignature.utils.PdfHashUtil;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.Paths;
import com.itextpdf.signatures.IExternalSignatureContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

public class ExternalServiceSignatureContainer implements IExternalSignatureContainer {

    /**
     * Thực hiện thêm Filter/SubFilter vào dictionary của chữ ký
     * để PDF Reader hiểu đúng định dạng chữ ký PKCS7 detached.
     */
    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        signDic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
        signDic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
    }

    /**
     * Phương thức này được iText gọi để lấy chữ ký CMS (PKCS7)
     * sau khi đã chuẩn bị phần dữ liệu cần ký.
     *
     * @param data InputStream chứa "range data" PDF mà ta cần ký (đã loại bỏ nội dung chữ ký).
     * @return byte[] chứa CMS/PKCS7 signature container (đã được tạo bởi server).
     */
    @Override
    public byte[] sign(InputStream data) {
        try {


            // 1) Tính hash SHA-256 từ InputStream (range data)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = data.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            byte[] digest = md.digest();

            // 2) Gửi digest lên server để ký, nhận lại PKCS7 dạng BASE64
            //    (giả dụ server trả về Base64PKCS7)
            String base64Pkcs7 = callRemoteSignService(digest);

            // 3) decode Base64 thành binary buffer để nhúng vào PDF
            return Base64.getDecoder().decode(base64Pkcs7);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error signing data", e);
        }
    }

    /**
     * Gọi server ký ngoài. CHỈ LÀ MẪU — bạn thay bằng logic gọi API của bạn.
     */
    private static final String BASE_URL = "http://104.156.255.132:8666/";
    private static final String WORKER_NAME = "minhlq.pa"; // thay bằng worker thật

    private String callRemoteSignService(byte[] digest) throws Exception {
        String hashBase64 = Base64.getEncoder().encodeToString(digest);
        hashBase64 = PdfHashUtil.getPdfHashBase64(new File("input.pdf"));
        System.out.println(hashBase64 + "\n Hash value");
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
        return signedBase64;
    }

    private static String extractJsonValue(String json, String prefix, String suffix) {
        int start = json.indexOf(prefix) + prefix.length();
        int end = json.indexOf(suffix, start);
        if (start < prefix.length() || end == -1) {
            System.out.println(json.toString());
            throw new RuntimeException("Không tìm thấy giá trị trong JSON");
        }
        return json.substring(start, end);
    }
}
