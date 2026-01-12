package com.ca;

import com.viettel.cloud.ca.CertBO;
import com.viettel.signature.pdf.DisplayConfig;
import com.viettel.signature.pdf.TimestampConfig;
import com.viettel.signature.plugin.SignPdfFile;
import com.viettel.signature.utils.CertUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
public class SignatureFileTest {

    private static Map<String, CertBO> certMap = new HashMap<String, CertBO>();
    private static List<String> credentialIDList = new ArrayList<String>();

    private static Boolean insertSignaturePdfFile(SignPdfFile pdfSig, String signature, String destPath) {
        TimestampConfig timestampConfig = new TimestampConfig();
        timestampConfig.setUseTimestamp(false);
        if (!pdfSig.insertSignature(signature, destPath, timestampConfig)) {
            return false;
        } else {
            return true;
        }
    }

    public static Optional<String> signFile(String inputBase64, X509Certificate[] certChain, String signature, DisplayConfig displayConfig) {
        String info = "Ký file PDF: ";
        log.info("{}Bắt đầu ký file Base64" + info);

        Path tempInputFile = null;
        Path tempOutputFile = null;
        try {
            // Kiểm tra tham số đầu vào
            if (inputBase64 == null || inputBase64.trim().isEmpty()) {
                log.info("{}Chuỗi Base64 đầu vào rỗng" + info);
                return Optional.empty();
            }
            // if (certChain == null || certChain.length == 0) {
            //     log.info("{}Chuỗi chứng thư số không hợp lệ"+ info);
            //     return Optional.empty();
            // }
            if (signature == null || signature.trim().isEmpty()) {
                log.info("{}Chữ ký số không hợp lệ" + info);
                return Optional.empty();
            }

            // Kiểm tra định dạng chữ ký
            try {
                Base64.getDecoder().decode(signature);
            } catch (IllegalArgumentException e) {
                log.error("Định dạng chữ ký không hợp lệ: {}" + e.getMessage());
                log.info("{}Định dạng chữ ký không hợp lệ" + info);
                return Optional.empty();
            }

            // Giải mã Base64 thành file PDF tạm thời
            byte[] pdfBytes;
            try {
                pdfBytes = Base64.getDecoder().decode(inputBase64);
            } catch (IllegalArgumentException e) {
                log.error("Chuỗi Base64 đầu vào không hợp lệ: {}" + e.getMessage());
                log.info("{}Chuỗi Base64 đầu vào không hợp lệ" + info);
                return Optional.empty();
            }
            tempInputFile = Files.createTempFile("input_", ".pdf");
            Files.write(tempInputFile, pdfBytes);
            log.debug("{}Tạo file PDF tạm thời: {}" + info + tempInputFile);

            // Kiểm tra file PDF hợp lệ
            if (!Files.exists(tempInputFile) || Files.size(tempInputFile) == 0) {
                log.info("{}File PDF tạm thời không hợp lệ" + info);
                return Optional.empty();
            }

            // Tạo file đích tạm thời
            tempOutputFile = Files.createTempFile("output_", ".pdf");

            // Tạo hash cho PDF                         
            SignPdfFile pdfSig = new SignPdfFile();

            String base64Hash = new String();
            String imageFile = "images/logo.jpg";
            String fontPath = "font/times.ttf";
            // String base64Hash = HashFilePDF.getHashTypeImage(pdfSig, tempInputFile.toString(), certChain, imageFile);
            log.info("   - ImagePath: " + displayConfig.getPathImage());
            // String base64Hash = pdfSig.createHash(tempInputFile.toString(), certChain, "SHA256", "RSA", displayConfig);
            // base64Hash = HashFilePDF.getHashTypeRectangleText(pdfSig, tempInputFile.toString(), certChain, fontPath);
            base64Hash = HashFilePDF.getHashTypeImage(pdfSig, tempInputFile.toString(), certChain, imageFile);

            if (base64Hash == null || base64Hash.trim().isEmpty()) {
                log.info("{}Tạo Hash không thành công" + info);

                return Optional.empty();
            }
            log.info("{}Tạo Hash thành công" + info);
            log.info("base64Hash: " + base64Hash);
            // Chèn chữ ký vào file PDF
            boolean result = insertSignaturePdfFile(pdfSig, signature, tempOutputFile.toString());
            if (!result) {
                log.info("{}Ký không thành công" + info);
                return Optional.empty();
            }

            // Đọc file đã ký và mã hóa thành Base64
            byte[] signedPdfBytes = Files.readAllBytes(tempOutputFile);
            String signedBase64 = Base64.getEncoder().encodeToString(signedPdfBytes);
            log.info("{}Ký thành công, trả về chuỗi Base64" + info);
            return Optional.of(signedBase64);

        } catch (IOException ex) {
            log.error("Lỗi khi xử lý file tạm thời: {}" + ex.getMessage() + ex);
            log.info("{}Lỗi: {}" + info + ex.toString());
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Lỗi khi ký file: {}" + ex.getMessage() + ex);
            log.info("{}Lỗi: {}" + info + ex.toString());
            return Optional.empty();
        } finally {
            // Xóa file tạm thời
            try {
                if (tempInputFile != null && Files.exists(tempInputFile)) {
                    Files.delete(tempInputFile);
                    log.debug("{}Xóa file tạm thời: {}" + info + tempInputFile);
                }
                if (tempOutputFile != null && Files.exists(tempOutputFile)) {
                    Files.delete(tempOutputFile);
                    log.debug("{}Xóa file tạm thời: {}" + info + tempOutputFile);
                }
            } catch (IOException e) {
                log.error("Lỗi khi xóa file tạm thời: {}" + e.getMessage() + e);
            }
        }
    }

    // Phương thức kiểm tra chuỗi Base64 hợp lệ
    private static boolean isValidBase64(String data) {
        try {
            Base64.getDecoder().decode(data);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String convertSignFile(String inputBase64, String[] certificateStrings, String signatureBase64, float x, float y, float width, float height, String imageBase64, int pageNumber) {
        // Chuyển đổi chuỗi Base64 thành X509Certificate[]
        List<X509Certificate> certList = new ArrayList<>();
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            for (int i = 0; i < certificateStrings.length; i++) {
                String certStr = certificateStrings[i];
                try {
                    String base64Data = certStr
                            .replaceAll("-----BEGIN CERTIFICATE-----", "")
                            .replaceAll("-----END CERTIFICATE-----", "")
                            .replaceAll("[\\r\\n]+", "")
                            .replaceAll("\\s+", "")
                            .replaceAll("[^A-Za-z0-9+/=]", "");
                    log.debug("Chứng thư số thứ {} sau khi làm sạch ({} ký tự): [{}]");

                    // Thêm padding nếu cần
                    if (base64Data.length() % 4 != 0) {
                        StringBuilder padded = new StringBuilder(base64Data);
                        while (padded.length() % 4 != 0) {
                            padded.append('=');
                        }
                        base64Data = padded.toString();
                        log.debug("Chứng thư số thứ {} sau khi thêm padding ({} ký tự): [{}]");
                    }

                    // Giải mã và tạo chứng thư
                    byte[] certBytes = Base64.getDecoder().decode(base64Data);
                    log.debug("Chứng thư số thứ {}: Giải mã thành công, {} byte " + certBytes.length);
                    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
                    certList.add(cert);
                    log.info("Chuyển đổi chứng thư số thứ {} thành công");

                } catch (Exception e) {
                    log.error("Lỗi khi chuyển đổi chứng thư số thứ {}: {}" + e.getMessage() + e);
                    return "Lỗi khi chuyển đổi chứng thư số thứ {}: {}" + e.getMessage() + e;
                }
            }
        } catch (CertificateException e) {
            log.error("Lỗi khởi tạo CertificateFactory: {}" + e.getMessage() + e);
            System.out.println("Không thể khởi tạo CertificateFactory. Vui lòng kiểm tra log để biết chi tiết.");
            return "Không thể khởi tạo CertificateFactory. Vui lòng kiểm tra log để biết chi tiết.";
        }
        X509Certificate[] certChain = certList.toArray(new X509Certificate[0]);

        String cleanedSignature = signatureBase64
                .replaceAll("[\\r\\n]+", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^A-Za-z0-9+/=]", "");
        log.debug("Chữ ký Base64 sau khi làm sạch ({} ký tự): [{}]" + cleanedSignature);

        // Kiểm tra tính hợp lệ của chữ ký Base64
        if (!isValidBase64(cleanedSignature)) {
            log.error("Chữ ký Base64 không hợp lệ: [{}]");
            return "Chữ ký Base64 không hợp lệ: [{}]";
        }

        // Thêm padding nếu cần
        if (cleanedSignature.length() % 4 != 0) {
            StringBuilder padded = new StringBuilder(cleanedSignature);
            while (padded.length() % 4 != 0) {
                padded.append('=');
            }
            cleanedSignature = padded.toString();
            log.debug("Chữ ký Base64 sau khi thêm padding ({} ký tự): [{}]");
        }

        // Tạo DisplayConfig
        DisplayConfig displayConfig = null;
        displayConfig = new DisplayConfig();
        displayConfig.setSignType(1); // Ký trên trang mới với tọa độ tùy chỉnh
        displayConfig.setIsDisplaySignature(true); // Hiển thị chữ ký
        displayConfig.setTypeDisplay(2); // Hiển thị hình ảnh
        displayConfig.setMarginLeftOfRectangle(x);
        displayConfig.setMarginBottomOfRectangle(y);
        displayConfig.setWidthRectangle(width);
        displayConfig.setHeightRectangle(height);
        displayConfig.setNumberPageSign(pageNumber);
        displayConfig.setSignDate(new Date()); // Ngày ký hiện tại
        displayConfig.setDateFormatString("dd/MM/yyyy HH:mm:ss"); // Định dạng ngày
        displayConfig.setContact(CertUtils.getCN(certChain[0])); // Thêm contact
        displayConfig.setReason("Ký số"); // Thêm reason
        displayConfig.setLocation("Hà Nội"); // Thêm location

        String fontPath = "font/times.ttf";
        displayConfig.setFontPath(fontPath);
        Path tempImageFile = null;
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                tempImageFile = Files.createTempFile("signature_image_", ".jpg");
                Files.write(tempImageFile, imageBytes);
                displayConfig.setPathImage(tempImageFile.toString());
                log.info("Temp img " + tempImageFile.toAbsolutePath());

            } catch (IOException | IllegalArgumentException e) {
                log.error("Lỗi khi xử lý imageBase64: {}" + e.getMessage() + e);
                return "Lỗi khi xử lý imageBase64: {}" + e.getMessage() + e;
            }
        } else {
            // Nếu không có ảnh từ client, dùng ảnh mặc định
            // displayConfig.setPathImage("images/logo.jpg");
        }
        Optional<String> result = signFile(inputBase64, certChain, cleanedSignature, displayConfig);
        if (result.isPresent()) {
            log.info("Ký file PDF thành công!");
            // Xóa tệp hình ảnh tạm
            if (tempImageFile != null && Files.exists(tempImageFile)) {
                try {
                    Files.delete(tempImageFile);
                } catch (IOException e) {
                    log.error("Lỗi khi xóa tệp hình ảnh tạm: {}" + e.getMessage() + e);
                }
            }
            return result.get();
        } else {
            System.out.println("Ký file PDF thất bại. Vui lòng kiểm tra log để biết chi tiết.");
            log.info("Ký file PDF thất bại.");
            return "Ký file PDF thất bại.";
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Khởi tạo Log4j
        BasicConfigurator.configure();
        log.info("Chương trình bắt đầu");
        String inputPath = "C:\\Users\\User\\Downloads\\document.pdf";

        // 2️⃣ Đọc toàn bộ bytes của file PDF
        byte[] pdfBytes = Files.readAllBytes(Paths.get(inputPath));

        // 3️⃣ Encode sang Base64
        String inputBase64 = Base64.getEncoder().encodeToString(pdfBytes);
        String[] certificateStrings1 = {
            "MIIEczCCA1ugAwIBAgIQVAT//rcDP7MW1nIgG9E2rTANBgkqhkiG9w0BAQsFADA9\r\nMRYwFAYDVQQDDA1WaWV0dGVsLUNBIFJTMRYwFAYDVQQKDA1WaWV0dGVsIEdyb3Vw\r\nMQswCQYDVQQGEwJWTjAeFw0yNDA1MTcwNzI5MDBaFw0yNzA1MTcwNzI5MDBaMIGQ\r\nMQswCQYDVQQGEwJWTjESMBAGA1UEBwwJSMOAIE7hu5hJMRwwGgYDVQQLDBNU4buU\r\nTkcgR0nDgU0gxJDhu5BDMS8wLQYDVQQDDCZDw5RORyBUWSBD4buUIFBI4bqmTiDE\r\nkOG6plUgVMavIE1PQklOTzEeMBwGCgmSJomT8ixkAQEMDk1TVDowMTA3MDc0Nzgw\r\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA43ChE/BPkLmwbc+g5e8P\r\nR9Eyp1oYhChz5J6cKx2pqWPoPzI25/eVA+JiE9nLuaPqswJCF/g8hSxwsNTHTRKm\r\n3AvO2jAAA7FPlL74OQPbv9+S5Run3JHPNVH7McADBB5wHV0H1Wd7NqBLPymDXo1I\r\njlc70yYe2hs3nF3DL7QO9jsB5xIDR+YOVrlY13LfSOicjZn2vQtUlIxegL79O3Gh\r\nx4agWcf0za/1rzgu4HRDnsB6ox5R6g5GbwrYDzi3YrNrye0C+KJvMVhkJSwdnPb3\r\nFos9ArKYu/JBt16Q27kY0DEKyi1UH5DAYdXJeFttrXD1cQ/RtSzx09jU5DyaT4vg\r\nAwIDAQABo4IBGTCCARUwNQYIKwYBBQUHAQEEKTAnMCUGCCsGAQUFBzABhhlodHRw\r\nOi8vb2NzcC52aWV0dGVsLWNhLnZuMB0GA1UdDgQWBBTmveutQ1CDnp2/SkpyJR7h\r\n7nk+1DAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFNP0lQs0nhvfirbYq6JyNTD8\r\n1mEjMH4GA1UdHwR3MHUwc6AuoCyGKmh0dHA6Ly9jcmwudmlldHRlbC1jYS52bi9W\r\naWV0dGVsLUNBLVJTLmNybKJBpD8wPTEWMBQGA1UEAwwNVmlldHRlbC1DQSBSUzEW\r\nMBQGA1UECgwNVmlldHRlbCBHcm91cDELMAkGA1UEBhMCVk4wDgYDVR0PAQH/BAQD\r\nAgXgMA0GCSqGSIb3DQEBCwUAA4IBAQA9XdFmk6EPh/IqAudeL/e1iu5wZZ/AhXvj\r\nXnAoqPEgoPC7TdLboozmKY3gFeSKf9RvcejFpv0xOCNl8yf8h1q5E+PLw+iv7tep\r\nvNe5hukE3HMdCfoJKoVgD1gyU1xIa8G9lKFD0bBYcuDNHvntDoQp+Lr+OTun0aa+\r\nsffoufePf9Pv2J1o1/yhGKrmIRbCcNv2hVe0fjwxsGdlWNHo7xiPZ8UR26nCcI44\r\nZRof0k0X7yEFZDSDOi2IXa/N8AhKMxLYvAh5tA3FDlXSQEsX0xYU8lpzkaHQ4j5r\r\nJUcQfiNV5xS82he0NXLacWjEyNRY223QSI5BGXZBbrJC0Tz59yoZ",
            "MIIGFDCCA/ygAwIBAgIQIHCI0OZfLP6ac+Fpx2ZSDzANBgkqhkiG9w0BAQsFADCB\r\nozELMAkGA1UEBhMCVk4xMzAxBgNVBAoMKk1pbmlzdHJ5IG9mIEluZm9ybWF0aW9u\r\nIGFuZCBDb21tdW5pY2F0aW9uczE8MDoGA1UECwwzTmF0aW9uYWwgQ2VudHJlIG9m\r\nIERpZ2l0YWwgU2lnbmF0dXJlIEF1dGhlbnRpY2F0aW9uMSEwHwYDVQQDDBhWaWV0\r\nbmFtIE5hdGlvbmFsIFJvb3QgQ0EwHhcNMjIwOTI2MDIwOTMzWhcNMjcwOTI2MDIw\r\nOTMzWjA9MRYwFAYDVQQDDA1WaWV0dGVsLUNBIFJTMRYwFAYDVQQKDA1WaWV0dGVs\r\nIEdyb3VwMQswCQYDVQQGEwJWTjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\r\nggEBANAl6bxp+CgOCAI95ConnUkLZycqYa2+qd4FbVxF6Ir5P0GpgY3kiLfwKsY5\r\n8FdST2QiM4s1MoYRqvaCV5lLWYBsbLkWoXVE5mGNmAVS/QfTyZe7NJ3KoUVK1vMe\r\nujX3J03Ft1M3eCSok4K2/C5Qsf193XJBc5i5VLeeAMcW7NP0CJpcbhdtWkwMJE+T\r\nhjMLQ26J0OEG2ggjM21dPNAnZCHAFYLI1VILYX+Ajcpd5C8cHBbGl3CbgR/dizUC\r\nMv2gnD7ZwFwwXAIfnn5Qxo9m1wfINx0zwWKwDyxxfs8DECQfReqM5WzsO49dihvQ\r\nCk7ZugbfnWRutnsZ8HIFP7HAuXECAwEAAaOCAacwggGjMEIGCCsGAQUFBwEBBDYw\r\nNDAyBggrBgEFBQcwAoYmaHR0cHM6Ly9yb290Y2EuZ292LnZuL2NydC92bnJjYTI1\r\nNi5wN2IwgeAGA1UdIwSB2DCB1YAUfvCH7bG4nfsIg2+kFv3xuKximwGhgamkgaYw\r\ngaMxCzAJBgNVBAYTAlZOMTMwMQYDVQQKDCpNaW5pc3RyeSBvZiBJbmZvcm1hdGlv\r\nbiBhbmQgQ29tbXVuaWNhdGlvbnMxPDA6BgNVBAsMM05hdGlvbmFsIENlbnRyZSBv\r\nZiBEaWdpdGFsIFNpZ25hdHVyZSBBdXRoZW50aWNhdGlvbjEhMB8GA1UEAwwYVmll\r\ndG5hbSBOYXRpb25hbCBSb290IENBghEAlZK7jO6tWiSmuPcdfTI7WjAOBgNVHQ8B\r\nAf8EBAMCAYYwHQYDVR0OBBYEFNP0lQs0nhvfirbYq6JyNTD81mEjMBIGA1UdEwEB\r\n/wQIMAYBAf8CAQAwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cHM6Ly9yb290Y2EuZ292\r\nLnZuL2NybC92bnJjYTI1Ni5jcmwwDQYJKoZIhvcNAQELBQADggIBAGNNpftziUlI\r\no47eSBakrSWpO8FDPKj5LkQdayJIR72wszvMdS2Srv8eo5aidE0zql4/QViC4e33\r\nfoe9C/PQQKgmr1ROpd7w1OrCXF/IZXO9IIhm0xqg1ktDG2CGb+2vz2J2XtE3gR7u\r\nwgyMGlZlSeztaKzwMzTxhwkdgYtSb6aBbJMM6f2M1Y8qpuqLNl5kFhI2E8M8PUBk\r\n3OqD3OascEhnqb7X1rp8Atx7IJf04kIvMQkB092hkKO4Z2sLx2gy3H/YjrEpcTYT\r\n7F85PJLX9pXdihYScstlwWFEZzJjSNiMhFeNb2XaEyWZbozjMFSdUPMQH7SyMz9k\r\nTCXDsU/aV0yaelFfSQLPybitu6O8UkUfawGNHLZyvlQ08OMlodwbs1VssF2ewecJ\r\nthQvNIzH0TI9jApPaR4/G73LjE+3EvpuD8qh64eX+WUXKWENlSU5BFUsW6uO7jec\r\nsBl9ESLvcXTukIe8ArpB/PR4tTUlx1mFsgF8LfbNiOumoomxtrdRYHQlUxHEiqIr\r\nzOIKHT182jm+L92mr6Cbi5k0ae3HxBGzdW0XuzUFr9BdUfU2r0jG4HFzMeBqs70r\r\nxRzUj10QAjf9/EzFEe10I5j0u5ItGa2FJvkMI0/Ac5ZHMx97o8K7AGs1Tf29T7l/\r\n7WG+2f+Ktg7nsuSUTYLki/ROQIcPWrao",
            "MIIG/DCCBOSgAwIBAgIRAJWSu4zurVokprj3HX0yO1owDQYJKoZIhvcNAQELBQAw\r\ngaMxCzAJBgNVBAYTAlZOMTMwMQYDVQQKDCpNaW5pc3RyeSBvZiBJbmZvcm1hdGlv\r\nbiBhbmQgQ29tbXVuaWNhdGlvbnMxPDA6BgNVBAsMM05hdGlvbmFsIENlbnRyZSBv\r\nZiBEaWdpdGFsIFNpZ25hdHVyZSBBdXRoZW50aWNhdGlvbjEhMB8GA1UEAwwYVmll\r\ndG5hbSBOYXRpb25hbCBSb290IENBMB4XDTE0MDQxNTE2MjkyMFoXDTM5MDQxNTE2\r\nMjkyMFowgaMxCzAJBgNVBAYTAlZOMTMwMQYDVQQKDCpNaW5pc3RyeSBvZiBJbmZv\r\ncm1hdGlvbiBhbmQgQ29tbXVuaWNhdGlvbnMxPDA6BgNVBAsMM05hdGlvbmFsIENl\r\nbnRyZSBvZiBEaWdpdGFsIFNpZ25hdHVyZSBBdXRoZW50aWNhdGlvbjEhMB8GA1UE\r\nAwwYVmlldG5hbSBOYXRpb25hbCBSb290IENBMIICIjANBgkqhkiG9w0BAQEFAAOC\r\nAg8AMIICCgKCAgEAuKxaewgw2XB6afUf4zeVThQDl/G9xj56UoT+8KbW7BeIjkUe\r\nvwlUmK5/j4HQaIuNg7g9oiQaU2Gt7WM/fTR8p/PkQT7yzuY0uLzSxUO3d8LxBnFR\r\nhz/5Vnk6cfWcsZUwCEgU/LHrnVuRjIYsffdc3YDgUJkcbnnxRq6zTF9BG2xH3f3C\r\n68C4Y3yERae5MCukpNELXh6GctRR2FkShFeITzJUZSguCEJJAj5qYW3rakJud4Xj\r\nFFVgMnl6+78PYxvlAA8oFQrUbAywWq6Lzn6zcpo+OZuWfF7NFVGEcAtDuN1oyvst\r\n+H68f6giZ4+dKI4dBcrFkYJ+ptf98+Dev/Ij6onjOLgVgE/6LwprDIVY7X0vdqGG\r\n7Nbh6gaeugCG5/mYtIVkHhwPK+KcTPETYZJDYxT3rUIahaYh1Qp+LfEDXTJI2XGK\r\ney9lBkmFgdGpZY65p3xvrYW+NHccbtPsR+swcuuGRV7UP/ndmRX08GiaMTfKrkR7\r\nV5RvferDiQ/vezfq2hDPHizFaqxtImTUu8wFvXGbo11hsrqLCaKQxZToonYp7ECV\r\nYFDueuL7E6Up4cXler1qLvp3w+QZVR4r58IKvxVrtHaRiZUsbDa335dAlWjgaJI8\r\nQWZ4HOHVZLQjrX+JkjDPJTMHNxuMEkElrCSF3rXqUKZ/JMvqKeY16jQDaH0CAwEA\r\nAaOCAScwggEjMA4GA1UdDwEB/wQEAwIBhjAPBgNVHRMBAf8EBTADAQH/MB0GA1Ud\r\nDgQWBBR+8Iftsbid+wiDb6QW/fG4rGKbATCB4AYDVR0jBIHYMIHVgBR+8Iftsbid\r\n+wiDb6QW/fG4rGKbAaGBqaSBpjCBozELMAkGA1UEBhMCVk4xMzAxBgNVBAoMKk1p\r\nbmlzdHJ5IG9mIEluZm9ybWF0aW9uIGFuZCBDb21tdW5pY2F0aW9uczE8MDoGA1UE\r\nCwwzTmF0aW9uYWwgQ2VudHJlIG9mIERpZ2l0YWwgU2lnbmF0dXJlIEF1dGhlbnRp\r\nY2F0aW9uMSEwHwYDVQQDDBhWaWV0bmFtIE5hdGlvbmFsIFJvb3QgQ0GCEQCVkruM\r\n7q1aJKa49x19MjtaMA0GCSqGSIb3DQEBCwUAA4ICAQBNNunXKvYvaxzgOPbKsmJL\r\nZ1gqHpJeHzT74IzBHDgp8bgbLDtqH+PZV+w7DwvfZD8xuFKQJz9v5TDpz/CYwrhA\r\n+BUsxyMbzS6Kv1lNa42Ja63BlEQ1AAVY+ZX3mFbVumOV43kLQgzQayYKPolq1o7Q\r\nxz3l2zgzhg4o436Vfek8Lrh/WcP5ezyC8Tt7VCaUOl/fuSaCPYvZbV7bZw/Eyj4x\r\nK1ud7Uq2Op54vSTegoh0+ZW28SQEgH49BjyjQTv56sTRolWZ4WxbHtbBJwTj7vli\r\nksebvvljoRYo9wg29AuY/Arw3NNhTyIbUFO75colaaF8i+5aAvmPQzfIk9m1bzK1\r\n5VOk8t8QnV8i4I42jDLbVzbZFQZHbLL8gj+LTHVZc9sfKmfhkH2HDsngb6UvKDuW\r\nHB5+XQ5QoSiyGVJ0MeUYohPI6cghZXbIflHGyse9hbARM7Ubrisf/P//FDLlJ3UL\r\n7+aLIk9fw6n7Wy0WcgN+QxjfdxUM9VSCx705+uX/aN4y0g5LMNChDOzpBYUg6smm\r\n8A0W2LIAMw0Q9U9TLnHO8Ovw3ikuO5rfTSWwbYmyt15NsFp8LM/Q0Nu9QqaMNNy2\r\n3YbQZZlfFormI9ioWEpjDbWqU9YyH6oHpGjsBbSoR4G0IUsfxaDdE3CXIx48pRol\r\nSddeayvR5sdOsNrhJOAFwg=="
        };
        String[] certificateStrings4 = {
            "MIIFpDCCBIygAwIBAgIQVAEBAWxYeD23NZpMbzEP7jANBgkqhkiG9w0BAQsFADBcMQswCQYDVQQGEwJWTjEzMDEGA1UECgwqVklFVE5BTSBQT1NUUyBBTkQgVEVMRUNPTU1VTklDQVRJT05TIEdST1VQMRgwFgYDVQQDDA9WTlBUIFNtYXJ0Q0EgUlMwHhcNMjUwOTAzMDMxMDQwWhcNMjYwOTAzMDMxMDQwWjCB6zELMAkGA1UEBhMCVk4xEjAQBgNVBAgMCUjDoCBO4buZaTEdMBsGA1UEBwwUUGjGsOG7nW5nIEjDoCDEkMO0bmcxLzAtBgNVBAoMJkPDtG5nIFR5IEPhu5UgUGjhuqduIMSQ4bqndSBUxrAgTW9iaW5vMTUwMwYDVQQMDCxQaOG7pSBUcsOhY2ggQuG7mSBQaOG6rW4gR2nhuqNpIFBow6FwIE1lbmRpeDEeMBwGA1UEAwwVTmd1eeG7hW4gVGnhur9uIETFqW5nMSEwHwYKCZImiZPyLGQBAQwRQ0NDRDowMDEwODkwMzE3MzMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDQgcvZX4aEEwLAL0TdW6r2LxwdHTKhW/Ll2ZS+X246wBJAY4NVTyW9zPc0aBBHvGhtiMwbyhJsjTa5dCkn54v7K91o6rEKP25eN6LWCc2ojcbdoOBNW4JRsSEcSvkG5J8BVigBfJHVFyW3eVwdCVab040fmXhGgKj+IRchR5FYWDkBAkhyn+9kwry02SMuTKoYD8AyYsL+66UbJsqnFm0o5xQWKzHrrWWFsM8j33aW9IIas6QKJNmRSi80dEGrsmcu6gFdrxXRK5jeggrRzUKcps/uTR/oD0munVCwPgzd58Gpyw3/46NZcYSBAuO1WTUi25EWHqmhHa/TaQmqdl3ZAgMBAAGjggHQMIIBzDAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFF/vxOs69iL3ctLaGTwpJVm7oP0vMIGABggrBgEFBQcBAQR0MHIwOgYIKwYBBQUHMAKGLmh0dHA6Ly9wdWIudm5wdC1jYS52bi9jZXJ0cy92bnB0Y2Etc21hcnRjYS5jZXIwNAYIKwYBBQUHMAGGKGh0dHA6Ly9vY3NwLXNtYXJ0Y2Eudm5wdC1jYS52bi9yZXNwb25kZXIwHwYDVR0RBBgwFoEUZHVuZ25ndXllbkBtb2Jpbm8udm4wHwYDVR0lBBgwFgYIKwYBBQUHAwQGCisGAQQBgjcKAwwwgaYGA1UdHwSBnjCBmzCBmKA0oDKGMGh0dHA6Ly9jcmwtc21hcnRjYS52bnB0LWNhLnZuL3ZucHRjYS1zbWFydGNhLmNybKJgpF4wXDEYMBYGA1UEAwwPVk5QVCBTbWFydENBIFJTMTMwMQYDVQQKDCpWSUVUTkFNIFBPU1RTIEFORCBURUxFQ09NTVVOSUNBVElPTlMgR1JPVVAxCzAJBgNVBAYTAlZOMB0GA1UdDgQWBBRxz1iGP4FHB2iEQ01A/5wEiv2yrDAOBgNVHQ8BAf8EBAMCBPAwDQYJKoZIhvcNAQELBQADggEBAGsdAKWINdWu9tlsg54R7w5VsaBNibPXZg28TzyMlr7IbCzkFJEi5Bbrxzrw9p0JjwgnXducj7HiWeR+cVcYzhdOSuhoPP6e/tz0vXSgyVFsIlys2XoFhX0jfV7fh9+/2bA0qOmqUjiY8U495zgROJTOoIkDd0N8M+zmKbvhHq3+pBu2XgM5egLlSXzMWMJhbSXoInC4vaRX/DYK46MBYhWPPRos3ZxPKzuhs8VjuDQ/34Ev5hwX7goRzVriDsxmf+MR28zjKKIsO/agCBL5kHJXgb17BaakmT+ddIFoJP/phW1E+UjlZ9X+A3eBllJn/PYHNfRHbZgIvte/aa8Xrlg="
        // "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",
        // "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"
        };
        String[] certificateStrings = {
            "MIIDbDCCAlSgAwIBAgIEaUI+1zANBgkqhkiG9w0BAQsFADB9MQswCQYDVQQGEwJWTjESMBAGA1UECAwJSMOgIE7hu5lpMRMwEQYDVQQKDApWTlBUIEdyb3VwMR4wHAYDVQQLDBVWTlBULUNBIFRydXN0IE5ldHdvcmsxJTAjBgNVBAMMHFZOUFQgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkwHhcNMjUxMjE3MDUyNTQzWhcNMjYxMjE3MDUyNTQzWjBzMQswCQYDVQQGEwJWTjEPMA0GA1UECBMGSGEgTm9pMTowOAYDVQQKDDFDw5RORyBUWSBD4buUIFBI4bqmTiBOSEnDik4gTEnhu4ZVIEJBWSBQRVRST0xJTUVYMRcwFQYDVQQDDA5Mw6ogUXVhbmcgTWluaDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKrotplsJU0INOZeDBRwgfPCN4vLOf9nE/t8cvewk6L3NeuBjNGD9BSLMUsM7d77CuDUYndGrWqgrvf9rg0VWzS5EIqkj7j8vMFfjcA3zcdfvYf0EMk1kk51/ppOHXkwyEFe4YslXHLnPTeyDFz20dF4EO8vrej+BaaiNqox8yIYh2MB+5IWu6lJ9xAydAT7pRm+7wz5M/Vl+byhTJiMdhAV/6MD/ziL7YgyCg+hfiIDxlNXPp7x3dsUHEh7Yg9Ni/bcah3alhJoyhYLf00NYkG1yprdI3QmrFWx0wiDUqQOH2NvRCJ09zX5XlRyYu3IVr04q0tlNRPpV3hKhL+zjFsCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAsUEBV779BNn2unaBf3FmniIJ2gxmXDMHTWrr6ihkkEdvg2l3o7HJQgc7363TI3Qh06ikWx1p41KqKoE05YJOV4jZHL9efIxme5+SqeWgyt0KhjBJza6/DnD21RbtK1mYPIlZ1E1Z65bEBdtedYMMKxmX1GSHXszf3v7iKVh4zEBCd+IEvSCe4yZTuduYnkT/cJsREJy6bQKK2WzjdwxFZeTN9aB1iQXYBxYdhBQ4wXZjynCigkVhLtk2+pFZke410k9eeXbkfnyu1Y13pVurjUMyPT70bVJMwKC10qO+2fPIdNwp5Tt5y89bXJkJJlrQeTImXH/A73FDgcG12XYR2Q=="
        };
        String signatureBase641 = "kQFPdgutwzQ8pAxzGI47GxwT8TPOPGPBi+wdC3LgeBD2tVK2qYqzx/+BfW+TWoySl4/2DPCvoFDE0Lc/C0/rf015JHGGVOA6Yd+0jG/ItwepVPL8ryaI/cpolSESASaka3UBkXOkrf8efOtSNEOiBeW/yY03Fwze+McP483r3X9Dfim3WB1QXGS8AFwnHK2nI7Y6Of5XTROEOIFnYMgM7zjLtl96qMjT15XGELX6PW2Hv/NwmRyJqBl4gzMSQ2/s0QhdTU8xmLiY4Vk/+VwjoWkTuDrfXR4mp5eSDwpgpvR9eIGH4QDEjuNaByaAVQHYw7E5mmDUB5RiP72ZhSYX9Q=="; // Thay bằng chữ ký thực tế
        String signatureBase64 = "mc12qb/KVixTn/0uwjY9oGp2vErXfrRO4kPA7jpzFQri8rVyhKyd8dqk+DPyAR3DPQxgkkvkQUoAAlhpj6T+Gi833mGQsJvaDPNBzJynZ7WtDAOnzm3n87ZRfiPhiIahIBCynCzxdR5LVSq4hZRXKhJH5SOumZ1bXPG985eUrDjEQ2ooG5vqO3gCtVI1GOAA6CNO9x1O2OIl+EXx234zQOoCSFodR/9V5isS0qHD8/Qm0d+xT8mgzlSK9SCWXNAvCFCUV53PiIW6O7qqo1ptcIcnmOS1E7mX2eTXRBldMG4JFOP4W64mYhXTBjJwxl2ZWHc+3EqEy5jA4uD49RlAQg==";

        float x = 300.0f; // Tọa độ x
        float y = 300.0f; // Tọa độ y
        float width = 200.0f; // Chiều rộng
        float height = 100.0f; // Chiều cao
        String imageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAGQAAAAyCAYAAACqNX6+AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAEvWlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSfvu78nIGlkPSdXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQnPz4KPHg6eG1wbWV0YSB4bWxuczp4PSdhZG9iZTpuczptZXRhLyc+CjxyZGY6UkRGIHhtbG5zOnJkZj0naHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyc+CgogPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9JycKICB4bWxuczpBdHRyaWI9J2h0dHA6Ly9ucy5hdHRyaWJ1dGlvbi5jb20vYWRzLzEuMC8nPgogIDxBdHRyaWI6QWRzPgogICA8cmRmOlNlcT4KICAgIDxyZGY6bGkgcmRmOnBhcnNlVHlwZT0nUmVzb3VyY2UnPgogICAgIDxBdHRyaWI6Q3JlYXRlZD4yMDI1LTEyLTA0PC9BdHRyaWI6Q3JlYXRlZD4KICAgICA8QXR0cmliOkV4dElkPjMyODVkN2I3LTNhODQtNGVhMi1hODAzLTYyM2QyYzViZGM5YTwvQXR0cmliOkV4dElkPgogICAgIDxBdHRyaWI6RmJJZD41MjUyNjU5MTQxNzk1ODA8L0F0dHJpYjpGYklkPgogICAgIDxBdHRyaWI6VG91Y2hUeXBlPjI8L0F0dHJpYjpUb3VjaFR5cGU+CiAgICA8L3JkZjpsaT4KICAgPC9yZGY6U2VxPgogIDwvQXR0cmliOkFkcz4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PScnCiAgeG1sbnM6ZGM9J2h0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvJz4KICA8ZGM6dGl0bGU+CiAgIDxyZGY6QWx0PgogICAgPHJkZjpsaSB4bWw6bGFuZz0neC1kZWZhdWx0Jz5UcuG6p24gTWluaCBUaOG6r25nIC0gMTwvcmRmOmxpPgogICA8L3JkZjpBbHQ+CiAgPC9kYzp0aXRsZT4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKIDxyZGY6RGVzY3Jp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";
        int pageNumber = 1; // Số trang cần ký
        String sign = convertSignFile(inputBase64, certificateStrings, signatureBase64, x, y, width, height, imageBase64, pageNumber);
        // System.out.println("Ký: " + sign);
        // Đường dẫn file (bạn thay đổi cho phù hợp)
        String filePath = "C:\\Users\\output1.pdf";

        // Ghi file
        try {
            File file = new File(filePath);
            byte[] outByte = Base64.getDecoder().decode(sign);

            // Tạo thư mục cha nếu chưa tồn tại
            file.getParentFile().mkdirs();
//
//            // Ghi nội dung vào file
//            FileWriter fw = new FileWriter(file);
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write(sign);
//
//            // Đóng stream
//            bw.close();
            Files.write(Path.of(file.getAbsolutePath()),outByte);
            System.out.println("Đã ghi thành công vào file: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
