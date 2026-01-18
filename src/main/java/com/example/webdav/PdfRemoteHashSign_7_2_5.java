package com.example.webdav;

import com.itextpdf.io.font.FontNames;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

public class PdfRemoteHashSign_7_2_5 {
    private PdfReader reader = null;
    private PdfSignatureAppearance sap = null;
    private PdfSignature dic = null;
    private PdfPKCS7 sgn = null;

    private String signerName = "Me";
    private String issuerName = "CA";

    private X509Certificate signer;
    private X509Certificate[] certChain;

    private ByteArrayOutputStream outStream;

    private byte[] hash;
    private byte[] _hashOnlyBytes;
    private byte[] secondHash;
    private Calendar signingTime;
    private int r = 0, g = 0, b = 0;
    private PdfSignatureAppearance.RenderingMode renderMode;
    private FontName fontName;
    private FontStyle fontStyle;
    private int fontSize;
    private String reason;
    private String location;
    private String contact;

    private String tsaUrl;
    private String tsaUsername;
    private String tsaPassword;
    private int _estimatedSize = 0;

    private static final String X509CERT = "x509";
    //	private static final String CIPHER_CONFIG = "RSA/ECB/PKCS1Padding";
    private static final String DEFAULT_IMG = "iVBORw0KGgoAAAANSUhEUgAAAJYAAACWCAYAAAA8AXHiAAAPT0lEQVR4Xu2dy28bxxnAv32QokS9JVtynIcLG64PRuFDDr6V/BNsy6/GSZTESIPCKHrIoTeTtxxyMIocgtZpHEeJH5GUP2F59CEHo/DBDRzUSeNa0pIiJVESKXI5BSnJoinu7sxwZ2e4OzoZ5s7j+77ffDPzzUsB+Sc1wEADCoM8ZZZSAyDBkhAw0YAEi4laZaYSLMkAEw1IsJioVWYqwZIMMNGABIuJWmWmEizJABMNSLCYqFVmKsGSDDDRgASLiVplphIsyQATDUiwmKhVZirBkgww0YAEi4laZaYSLMkAEw1IsJiolW+mx/9kpp4s1q4P9SmQ//ogFxtzKZSv2oNXet+lHNooWwCA7IVTVID5A77Z27eCgmdOvhINX8miwnodJrK/4XgtWZg5lCFLRf61BKtFZ69cNVMLy9Wd/1WhBrXGv1VQAaAGUV2F0v2JFLmqO09x7JqZePI/1QC0Wz+6PE8dVZMPPz3AFK5QgnXsIzOxVgZjcVUFqHVmpIZpFQ2iOoJXhiH99O8HPIfuxDUz8fgZGHWwPfv7foKp7Zlm7pkSPMhIm8oiyyLvOqiLVnQ4fbyWfPBJh57hjIkcgdJiALWNdN2fjsT13+c2tATUKq7V7uuB9MZddp430GBFLy6jrS13JbtaocMPdF2D6nfjRLqOXMihSqWNN60PwtVoGmaHnD3jWRMBcvJwCsD37GaMRMJ2qF9fkkcv5FJb1dp1Z6X6UpU2hSgwEI9l1mYGk3Y1OPh+LrWUr17f9zvNrO7MEnKcKTLsDgMDVu/lgrFZKid4IUNabiyqQeleixc7m0WAXu6uFUUBNE/nWSanzcTCSs2wq9ubR9XkD4wG8V0PVuS8aVSqta4BqtXIsagOgGrpUqW2z0udekNNPrzR6Rht0T64pahpmPd+stGYz5C2NFG+77+0mCqWYX+XIUoFO6iHpioZa+6gbXdJlLVTd6hAGubZDOC7E6wzDq2QSOvifTw5piYXbnbopZrFOptDtnEvCda2pvTzy0a1Wunabg8XY13XofrdmDeNXoLlona3eA6u1brmO4/CAU5hhzCPsepR8ieL9jObruGEsqKjQ3p6+dYYfTTfYdjAMkjqjbulVJpbstjFJaO0hQLf9bnpIR5TYf0O5c4Ep/FoGONYsYtZVNrycQnGzbq8f1d0gHmKcZcEq8lybQKFvO0qRPmE0fext00jV3SI8YXKY7ktQwhhYZ6VIBjUOzVQWg+IKbpQYyzlrImQ48IpplSB/0wF+B5jzOXQDQ7F1czKzAFvgrBt9C0MWOo5E9VqHu43CjpcigYwb79jQp8yU1Vr/zLRrlpYrhPWyxACLOVc1kA1K/SzP9K2oGkaWLM2cDkFRusFMRxfCQHW5PRiamElmGt+pKDQfN/bo2U2747v79Icl70Ixmk0lRLCYwV43Y/SJsTJThxWk48/21tfHLySQ6vr9luu4z1KZv2uR4vcNrXl2xVKqIghsk3Q3LW56ZVxN8i1K4ycz6JKFScAqoCmKWBBLA21IgzGVFjd6gMNla5btfpWI4ezdN6ZTfycdgbzB6aXDXPFZaE+qGAd+chMPHVZ/+uJalBu3WFpZ97QLVC3V0RfbzS9sVm57tTYNC0C1uwo856KeQFtVcBgYPnGH7PGz0tyZunqWn3wVly6QqdjWIdH1cyzLzoM2rmNL1w1H/APggoW2Bjek/3du0xIuNq2jt4eHTbvUixkU7Q1f7tCu01nHreinotZo7wlu8V9PHisZyfefAPr1Mdm4uFPbTbssRJWeq0Wu7MPijYX6BtY0GalfXJITS7c8vDgQJNkY9MFlFspUzjxYCY5OByFpS9HfLO3bwW1jq36e3Uofsu4v5dea6+VsOoZuEbeWxdECTesUfsQCda26lx2QlDr1yGhPx6r1cB+tZ5zywjn5hUWihUpT12DdHWWzcFUOzmZgzX09pqxUtx4sSWmv1eD4rdkN6/QGqn/DzlU3PTg/ivaCoiSzq+G3CQvc7Cgdauxn0JOraXA2gjkMXxcZttePoKbuIPvfABr7zg8y3NsbXVwYTMFldVQg8V6Qx+XrrD/rTwqbmxxm5lA2MFifGCCX4C0qRscH4B09ra/A8jQg+XnsGN/OLaDjtQtafNskIeQU+spsIoh7Qr9jbS3osBsjDX6zmJqeW17L3v9crHSPcbB0DaQT3yQR4vLTV2xW0MI0u88GrIfs8KXjnNxEjJy3kSVagiPlCkKAOX1kl61LWYea28JB/NwpVcSNefjdgSKRZki5MmpITeLzhys8aFIJntrlNmJW0c7hnHLMoflm3Y2YA4WrzhKQ9gwrhUK4K3qqmcC1vg7i6lsY+DOd2YSNrCiEQ227tsvlx26uoKe3xxiYnN/ZoXnCghqZYhFVSjdw7i8gsG4ZPjdglFY7Z573z1RgZu3OpdDMOfP7JwNvTub+vp61fTGt2zuEXc1RMiuQxqIKZm1Oy6nm+tDAzf4XBWL9wEbsHbHNj4J0VbUsI2vXHQ99u6ykVutJCRYeA2j7Vcn/2ImHv0cngtxj02oySefu2zx3vXgPjV2hh6L38A9THdtKYoKCOdJXp97EXZgqRGAOfZHuUPfDWJ4IHUqh2rWzoZHjO876CxeJGUHFqctG5PvL6YW8uG4b+vVMS3z6802d2O1ksFhMwAzsCZHdFj4pz9T25f0GJZBO+aBlJG3TZQvNq2XBsBjpWG+gxcVaP1xWMDCBYTTQRZmHgsU3X+wQnIq58BQNGPeGnFdfx16yzRWNlrueccFkrZh76RjCFY0DfMj9G/A0AgWBm+F2QXarpVKsMjI6r+cNYqlEFwEggmG7cUomOnJtL//a0YeawmBEvHXY4XAW702rmX++w+MWaDTzo6uBqu+VgiKb2Os2AUz1e5N5U5bnVDpCcI3fZezaKPU7n5X/zZdsvFY9VV0VGP2kPU+g4dhwZnE09h4b0VVAc35s9uECVivXzVTZhGub95lL8TwleeJwrpqCOVdPK4M0fMkDi+qDg/0QOH2MBOb+7Mfq15KfU/WnA9COD1N67GBeWQ3OhiB5a/wlsbcXqP93Rtq+l83/NnGxI7e+kEGmocbCax34pqZePwswLsYSPevu01gSLpTAju0+5QdWH5sKnNTZIfK4Z6cAITIlIkqlstRN4L8OpWdGViNa7ftXqbqtNYAEHRvNRxXk4UZgms03RqZqoNf25Lr5mUG1sk/m6lHf2PYn7sp0gN4eWWhqGoGzRHcd48xKx4Z7MvkvxpwXQbySmZmYNUr2Igv3fceruPXzNSPz+wfefRKOXzyIYs1xS+ZxnrZ4d3nXSF87AaZeqx65gNv5dHaNwxu6sVooXyg8KBUUgBwPTdpvh2KwtRjjb1XRLkv+z0tQ596nqhaAY1bkRrf6THxJjD8DIzuFuup0TuEHC95QI/Nk955P/buMsqtVrB0dvKwmnzU9FAmVqIOP+oqsIIaZe+JRjLle4T3W+B2gXVASD1hh1AxH2PVC2i8+XzLo5v8Ahhlp+qmCKCKx1RYv8N+aa2Vxa7xWCevmYlHQYuyk2za27Gcci6LUA3nZdqdBBy8lS8eywOvup1FAL0VaRd1cHoxtbRCcAKJdEnIM2MxDJB6WMftrAjcv+dls8iQxpMQ6uD0UTX54FOC6L2HcnZHVxiwuNWJw2ryMeksjWY2TAOvR3B1CVh7jxB4JDe3bI5MRDJPPyebAUYuZFGlQjCu4jQTbFaq+GAFyFtpmpaxZjH3rO9Y6TcfZY3/LJIdEsG+z4Fh8+oCsILhraLRKGzdo1jeIhxXNVjh2AXusio2WIG59Zjy5h0KqIb6I7DyNd6OU4YOi922GU8qTaFYT8r1NBNaqJYQACKviQDeSug4Vt9lE22UAnD5P42hMReXW6k7MqEmn7pdwEaOKlUKcbvCIHgrGqjOmAYAxv6qFnP39qjgx6koXMqEBEufMlNVq8s38lFANT6dNbIrZDPAbUNTdre4lFB8JyRYQNkVUMjPJgkNVFfMRHad8sQRRXlsBN/LVUywurkbpDDy6b/mEw/+vUV16PbYYTX5hDSKz5oqlocpaOs+8k4e5de68ym4079Vkw8+oVibo2xI44NaJvsVWcCV1i6k6cTzWF0aace6EruddSihGhmIQP42/3iVHXACgtV9kfZXx9Tkrzf981S9PTps3uVwvyuB2xIKrJ6LOVTe2rk2mkAInp9OjqnJBR+hisd0WL8jNlTiBUgpuwVeYJEegHhRT1o51UgG5sh2RvDSjVAeq6s281HM/hpGpoRqpF/L5L8Wc6DeDl5hwLK/hY5Xm3MolwKq8WkzkV2hi1OJtFSDaw1hwKJtybiCevYdBVSH3jON5wXyZZpGnSnK80zWDjKSYOEqj+JEzXbXt2QAoARuMbvfibBZj7TOzd9LsDC0F4noULlPMROjXJoajteShZlDGYyqCfuJEGAd+dBMPTXFXHQ+NBrJPP+CYiZGEejVNBWsWf8Pl7KgUwiwDr2fRc/zhIcFWGijJU+aQfObH5uJH34iHKQTXLXtg9ieFCEEWI2LcGtlTwTyJhOyO6p2y+y9nDc2S1v44ylFhROvAPlRMG+EZJqLGGAJtLe9J6JDmWo8Vb/bHn/V4NRRNfmQ02FSpkTtZC4IWFkEiH9XeGRMTT4lXJ55/epi6pcc5rH3AHZ5dpBKsOqaoTU4zqxP0WG8v5rO3vboxh0/3I0HZYgBFsd3BofjaqYwQ3CRLAA4z2IViOoAB4eU9K83vb9/1QOb+5KFEGA1ru62fO4Kab3UizCCAqBooCk1sJRYGmYH/H2b0Rc86AsRAqzXPzRTv/gYx+qP6+niDIdnhent1HUphQBre+mD/QY/TdPBmqWIoHedWflXWCCwKE/+4uqwSxdzccUT7TtxwGLktY4fVtM/fhbeQTQv4IQCS5vKIcvCDzLaK02B4ThKF2bCNcXnBVG7coUCq1FBnNiQnQbVCJx8zUo+ukFxsEEkqwSgLuKBVX+D59KyUSpX8NbcFA3GBnsyuVv+PUAUALszF0FIsHalnnhv2ciuWQnL2rvOR1EA4r1RKH7jw+utzNUf3AKEBiu4ag++ZBKs4NuYi4QSLC5qD36hEqzg25iLhBIsLmoPfqESrODbmIuEEiwuag9+oRKs4NuYi4QSLC5qD36hEqzg25iLhBIsLmoPfqESrODbmIuEEiwuag9+oRKs4NuYi4QSLC5qD36hEqzg25iLhBIsLmoPfqESrODbmIuE/weUDk7iXWYMAAAAAABJRU5ErkJggg==";

    private String signatureText;

    public enum FontName {
        Times_New_Roman, Roboto, Arial
    }

    public enum FontStyle {
        Normal, Bold, Italic, BoldItalic, Underline
    }

    private static final String BASE_URL = "http://104.156.255.132:8666/";
    private static final String WORKER_NAME = "minhlq.pa"; // thay bằng worker thật

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        try {
            calculateSignatureItext7();
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    public static void calculateSignatureItext7() throws Exception {

        // ===== 1. PDF input / output =====
        byte[] pdfInput = Files.readAllBytes(Paths.get("input.pdf"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfInput));
        PdfSigner signer = new PdfSigner(
                reader,
                out,
                new StampingProperties().useAppendMode()
        );
        // ===== 2. PdfSignatureAppearance =====
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();

        // ----- Font (Unicode, hỗ trợ tiếng Việt) -----
        PdfFont font = PdfFontFactory.createFont(
                "font/arial.ttf",
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );

        // ----- Image chữ ký -----
        ImageData image = ImageDataFactory.create("images/logo.jpg");

        // ----- Signature text -----
        Calendar signingTime = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

        String signatureText =
                "Ký bởi: Nguyễn Văn A\n" +
                        "Ngày ký: " + df.format(signingTime.getTime()) + "\n" +
                        "Tổ chức xác thực: Demo CA";

        // ----- Render mode -----
        PdfSignatureAppearance.RenderingMode renderMode =
                PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION;

        // ----- Signature meta -----
        String reason = "Ký duyệt văn bản";
        String location = "Hà Nội, Việt Nam";
        String contact = "nguyenvana@example.com";

        // ===== 3. Set appearance =====
        appearance
                .setLayer2Font(font)
                .setLayer2Text(signatureText)
                .setSignatureGraphic(image)
                .setRenderingMode(renderMode)
                .setReason(reason)
                .setLocation(location)
                .setContact(contact)
                .setReuseAppearance(false);

        // ===== 4. Visible signature =====
        Rectangle rect = new Rectangle(36, 650, 200, 100); // x, y, w, h
        appearance.setPageRect(rect);
        appearance.setPageNumber(1);

        signer.setFieldName("Signature1");

        // ===== 5. External container (Pre-sign) =====
        Certificate[] certChain = getCertChainFromApizz(); // demo
        PreSignContainer container = new PreSignContainer(
                DigestAlgorithms.SHA256,
                certChain,
                signingTime
        );

        ITSAClient tsaClient = new TSAClientBouncyCastle("http://tsa.ca.gov.vn/", null, null);
        int _estimatedSize = calculateEstimatedSignatureSize(certChain, tsaClient, null, null);

        System.out.println("Estimated Signature Size: " + _estimatedSize);

        signer.signExternalContainer(container, _estimatedSize);

        // ===== 6. Output =====
        byte[] tempPdf = out.toByteArray();
        Files.write(Paths.get("presigned.pdf"), tempPdf);

        System.out.println("Document hash: " +
                Base64.getEncoder().encodeToString(container.getDocumentHash()));


    }

    public static boolean checkHashSignature(byte[] signerProfile, DigestAlgorithms alg, String certBase64, String signedHashBase64) {
        Signature verify = null;
        try {
            verify = Signature.getInstance(alg + "withRSA");
            CertificateFactory fac = CertificateFactory.getInstance(X509CERT);
            Certificate cert = fac.generateCertificate(new ByteArrayInputStream(org.bouncycastle.util.encoders.Base64.decode(certBase64)));
            if (!(cert instanceof X509Certificate)) {
                return false;
            }
            X509Certificate signerFromProfile = (X509Certificate) cert;
            verify.initVerify(signerFromProfile.getPublicKey());
            verify.update(signerProfile, 0, signerProfile.length);
            Boolean r = verify.verify(org.bouncycastle.util.encoders.Base64.decode(signedHashBase64));
            return r;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int calculateEstimatedSignatureSize(
            Certificate[] certChain,
            ITSAClient tsaClient,
            byte[] ocsp,
            CRL[] crlList) {

        int estimatedSize = 0;

        // ----- Certificate chain -----
        if (certChain != null) {
            for (Certificate cert : certChain) {
                try {
                    estimatedSize += cert.getEncoded().length;
                } catch (CertificateEncodingException ignored) {
                }
            }
        }

        // ----- PKCS7 structure + hash -----
        estimatedSize += 2000;

        // ----- OCSP -----
        if (ocsp != null) {
            estimatedSize += ocsp.length;
        }

        // ----- TSA -----
        if (tsaClient != null) {
            // iText recommend ~4KB – 8KB
            estimatedSize += 4096;
        }

        // ----- CRL -----
        if (crlList != null) {
            for (CRL crl : crlList) {
                if (crl instanceof X509CRL) {
                    try {
                        int crlSize = ((X509CRL) crl).getEncoded().length;
                        // CRL được nhúng 2 lần trong PKCS7
                        estimatedSize += crlSize * 2;
                    } catch (CRLException ignored) {
                    }
                }
            }
            estimatedSize += 100;
        }

        return estimatedSize;
    }


    private Certificate[] getCertChainFromApi() throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        // ===== Request body =====
        String jsonBody = String.format("""
                {
                    "workerName": "%s",
                    "workerId": ""
                }
                """, WORKER_NAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/esignature/certificate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Lấy certificate thất bại (HTTP "
                            + response.statusCode() + "): " + response.body()
            );
        }

        // ===== Parse JSON =====
        String json = response.body();

        String certBase64 = extractJsonValue(json, "\"cert\":\"", "\"");

        if (certBase64 == null || certBase64.isEmpty()) {
            throw new RuntimeException("Không tìm thấy trường 'cert' trong response");
        }

        // ===== Decode Base64 =====
        byte[] certBytes = Base64.getDecoder().decode(certBase64);

        // ===== Parse certificate =====
        CertificateFactory fac =
                CertificateFactory.getInstance("X.509");

        Certificate cert = fac.generateCertificate(
                new ByteArrayInputStream(certBytes)
        );

        if (!(cert instanceof X509Certificate)) {
            throw new RuntimeException(
                    "cert returned from API is not X509Certificate"
            );
        }

        // ===== iText 7 cần Certificate[] =====
        return new Certificate[]{cert};
    }

    private static Certificate[] getCertChainFromApizz() throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        // ===== Request body =====
        String jsonBody = String.format("""
                {
                    "workerName": "%s",
                    "workerId": ""
                }
                """, WORKER_NAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/esignature/certificate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Lấy certificate thất bại (HTTP "
                            + response.statusCode() + "): " + response.body()
            );
        }

        // ===== Parse JSON =====
        String json = response.body();

        String certBase64 = extractJsonValue(json, "\"cert\":\"", "\"");

        if (certBase64 == null || certBase64.isEmpty()) {
            throw new RuntimeException("Không tìm thấy trường 'cert' trong response");
        }

        // ===== Decode Base64 =====
        byte[] certBytes = Base64.getDecoder().decode(certBase64);

        // ===== Parse certificate =====
        CertificateFactory fac =
                CertificateFactory.getInstance("X.509");

        Certificate cert = fac.generateCertificate(
                new ByteArrayInputStream(certBytes)
        );

        if (!(cert instanceof X509Certificate)) {
            throw new RuntimeException(
                    "cert returned from API is not X509Certificate"
            );
        }

        // ===== iText 7 cần Certificate[] =====
        return new Certificate[]{cert};
    }


    private void initSignatureFontForItext7(PdfSignatureAppearance appearance) throws Exception {
        PdfFont fontName = appearance.getLayer2Font();
        String fn = fontName.getFontProgram().getFontNames().getFontName();
        String fontPath;
        switch (fn) {
            case "Roboto":
                fontPath = "font/ROBOTOCONDENSED-REGULAR.TTF";
                break;
            case "Arial":
                fontPath = "font/ARIAL.TTF";
                break;
            default:
                fontPath = "font/TIMES.TTF"; // Times New Roman
        }

        PdfFont pdfFont;
        try {
            pdfFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception e) {
            pdfFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN); // fallback
        }

        appearance.setLayer2Font(pdfFont);
        appearance.setLayer2FontSize(13);
        // Màu chữ (nếu cần)
        // appearance.setLayer2FontColor(new DeviceRgb(this.r, this.g, this.b));
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
        public byte[] sign(byte[] digest) {
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