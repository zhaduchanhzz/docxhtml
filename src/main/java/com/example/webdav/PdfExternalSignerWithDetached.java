/*
package com.example.webdav;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static com.example.webdav.PdfRemoteHashSign_7_2_5.getCertChainFromApizz;

*/
/**
 * PdfExternalSignerWithDetached - Ví dụ ký PDF external (remote/HSM) bằng signDetached()
 * Sử dụng certificate chain từ phương thức getCertChainFromApizz()
 *
 * Yêu cầu:
 * - iText 7.x (core + bouncycastle + bouncycastle-adapter)
 * - BouncyCastle provider
 *//*

public class PdfExternalSignerWithDetached {

    static {
        // Đăng ký BouncyCastle provider (cần cho digest và một số xử lý cert)
        if (java.security.Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }
    }
    */
/**
     * Ký PDF bằng signDetached() với external signature (gọi server/HSM)
     *//*

    public void signPdf(
            String inputPath,
            String outputPath,
            String signatureFieldName,          // null nếu để iText tạo field mới
            Rectangle visibleRect,              // null nếu invisible signature
            int pageNumber,                     // 1-based
            String reason,
            String location,
            byte[] signatureImageBytes,         // null nếu không dùng graphic
            int estimatedSignatureSize
    ) throws IOException, GeneralSecurityException {

        if (estimatedSignatureSize < 4000) {
            estimatedSignatureSize = 8192; // giá trị an toàn cho hầu hết trường hợp
        }

        Certificate[] chain;
        try {
            chain = getCertChainFromApizz();
        } catch (Exception e) {
            throw new GeneralSecurityException("Không lấy được certificate chain từ Apizz", e);
        }

        try (PdfReader reader = new PdfReader(inputPath);
             OutputStream os = new FileOutputStream(outputPath)) {

            StampingProperties props = new StampingProperties().useAppendMode();

            PdfSigner signer = new PdfSigner(reader, os, props);

            if (signatureFieldName != null && !signatureFieldName.trim().isEmpty()) {
                signer.setFieldName(signatureFieldName);
            }

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();

            // Cài đặt thông tin
            appearance.setReason(reason != null ? reason : "Ký số tài liệu");
            appearance.setLocation(location != null ? location : "Hà Nội, Việt Nam");

            // Visible signature với hình ảnh (nếu có)
            if (signatureImageBytes != null && signatureImageBytes.length > 0) {
                ImageData img = ImageDataFactory.create(signatureImageBytes);
                appearance.setSignatureGraphic(img);
                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);

                if (visibleRect != null) {
                    appearance.setPageRect(visibleRect);
                }
                if (pageNumber >= 1) {
                    appearance.setPageNumber(pageNumber);
                }
            } else {
                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
            }

            // Fix một số bug liên quan SigFlags / catalog modified
            signer.getDocument().getCatalog().setModified();

            // Digest (SHA-256 mặc định)
            IExternalDigest digest = new BouncyCastleDigest();

            // External signature - gọi server / HSM
            IExternalSignature externalSignature = new RemoteExternalSignature();

            // Thực hiện ký detached
            signer.signDetached(
                    digest,
                    externalSignature,
                    chain,                      // chain lấy từ Apizz
                    null,                       // crlList (có thể thêm sau)
                    null,                       // ocspClient
                    null,                       // tsaClient
                    estimatedSignatureSize,
                    PdfSigner.CryptoStandard.CMS
            );
        }
    }

    */
/**
     * Implementation gọi server ký hash (thay bằng code thật của bạn)
     *//*

    private static class RemoteExternalSignature implements IExternalSignature {

        @Override
        public String getHashAlgorithm() {
            return DigestAlgorithms.SHA256;  // → "SHA256" chuẩn (dùng constant của iText để tránh sai)
            // Nếu server dùng SHA512: return DigestAlgorithms.SHA512;
        }

        @Override
        public String getEncryptionAlgorithm() {
            // Kiểm tra cert chain để quyết định động (tốt nhất)
            try {
                Certificate[] chain = getCertChainFromApizz();
                if (chain != null && chain.length > 0) {
                    X509Certificate cert = (X509Certificate) chain[0];
                    String algo = cert.getPublicKey().getAlgorithm();
                    if ("EC".equals(algo) || "EllipticCurve".equals(algo)) {
                        return "ECDSA";
                    }
                }
            } catch (Exception ignored) {}

            return "RSA";  // fallback
        }

        @Override
        public byte[] sign(byte[] hash) throws GeneralSecurityException {
            System.out.println("Hash algorithm requested: " + getHashAlgorithm());
            System.out.println("Encryption algorithm: " + getEncryptionAlgorithm());
            System.out.println("Hash length received: " + hash.length + " bytes");  // SHA256 → 32 bytes

            // Gọi server thật ở đây
            // byte[] rawSig = ApizzService.sign(hash, getHashAlgorithm() + "with" + getEncryptionAlgorithm());
            // return rawSig;

            // Dummy cho test (signature invalid nhưng algorithm hiển thị đúng)
            byte[] dummy = new byte[256];  // cho RSA-2048
            if ("ECDSA".equals(getEncryptionAlgorithm())) {
                dummy = new byte[140];  // ECDSA thường ngắn hơn
            }
            Arrays.fill(dummy, (byte) 0xDD);
            return dummy;
        }
    }

    // ────────────────────────────────────────────────
    //                  MAIN - TEST
    // ────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            PdfExternalSignerWithDetached signer = new PdfExternalSignerWithDetached();

            // Ví dụ tham số
            signer.signPdf(
                    "input.pdf",
                    "signed_output_hehe.pdf",
                    "SigField001",                      // tên field (tùy chọn)
                    new Rectangle(36, 720 - 120, 180, 80),  // vị trí chữ ký visible
                    1,                                  // trang 1
                    "Ký xác nhận hợp đồng",
                    "Hà Nội",
                    null,                               // byte[] ảnh chữ ký (nếu có)
                    12000
            );

            System.out.println("Ký xong → file: signed_output.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}*/
