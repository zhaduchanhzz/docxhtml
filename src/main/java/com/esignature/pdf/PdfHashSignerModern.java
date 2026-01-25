package com.esignature.pdf;

import com.esignature.signer.SignatureParameter;
import com.esignature.signer.SignerProfile;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

@Getter
public class PdfHashSignerModern implements Serializable {

    private static final String HASH_ALGORITHM = "SHA256";  // có thể thay đổi
    private static final String BC_PROVIDER = "BC";

    // Các field chính
    private SignatureParameter param;               // giả sử bạn vẫn giữ class này
    private X509Certificate signerCert;
    private Certificate[] certChain;

    private byte[] documentHash;                    // hash giai đoạn 1 (document digest)
    private byte[] secondHash;                      // hash giai đoạn 2 (authenticated attributes)
    private byte[] tempPdf;                         // PDF đã thêm trường chữ ký (giai đoạn 1)

    private Calendar signingTime = Calendar.getInstance();
    private String reason = "Signing document";
    private String location = "Signer's office";
    private String signerName;
    private String issuerName;
    private String contact;

    // Appearance
    private Rectangle signatureRect;
    private int pageNumber = 1;
    private String signatureFieldName;
    private String signatureText;
    private ImageData signatureImage;
    private PdfFont layer2Font;
    private float fontSize = 10f;
    private com.itextpdf.kernel.colors.Color textColor = ColorConstants.BLACK;

    private TSAClientBouncyCastle tsaClient;
    private String tsaUrl;
    private String tsaUsername;
    private String tsaPassword;

    // estimated size cho container (thường 8-16KB là đủ)
    private int estimatedSize = 12000;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PdfHashSignerModern() {
    }

    public void init(SignatureParameter param) throws Exception {
        this.param = param;

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        byte[] certBytes = Base64.getDecoder().decode(param.getCertBase64());
        this.signerCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));

        // tạm thời chỉ dùng 1 cert, bạn có thể mở rộng thành chain
        this.certChain = new Certificate[]{signerCert};

        signerName = getCommonName(signerCert, false);
        issuerName = getCommonName(signerCert, true);
        contact = signerName;

        // Default appearance text
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String timeStr = df.format(signingTime.getTime());
        this.signatureText = String.format("Ký bởi: %s\nNgày ký: %s\nTổ chức xác thực: %s",
                signerName, timeStr, issuerName);
    }

    /**
     * Giai đoạn 1: Chuẩn bị PDF, thêm chữ ký field, tính hash cần ký
     *
     * @throws Exception
     */
    public void prepareSignature() throws Exception {
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(param.getUnsignData()));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfSigner signer = new PdfSigner(reader, baos, new StampingProperties().useAppendMode());

            // Appearance
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance
                    .setReason(reason)
                    .setLocation(location)
                    .setContact(contact)
                    .setCertificate(signerCert)
//                    .setSigningDate(signingTime)
//                    .setFieldName(generateUniqueFieldName())
                    .setPageRect(signatureRect != null ? signatureRect : new Rectangle(36, 36, 200, 100))
                    .setPageNumber(pageNumber)
                    .setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION); // hoặc GRAPHIC, DESCRIPTION...

            if (signatureImage != null) {
                appearance.setSignatureGraphic(signatureImage);
                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
            }

            if (signatureText != null && !signatureText.isBlank()) {
                appearance.setLayer2Text(signatureText);
                appearance.setLayer2Font(layer2Font != null ? layer2Font : PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN));
                appearance.setLayer2FontSize(fontSize);
                appearance.setLayer2FontColor(textColor);
            }

            // Chuẩn bị chữ ký deferred (external)
            signer.signDetached(
                    new BouncyCastleDigest(),
                    new ExternalSignatureStub(),   // stub → chỉ để tạo hash
                    certChain,
                    null,
                    null,
                    tsaClient,
                    estimatedSize,
                    PdfSigner.CryptoStandard.CMS
            );

            // Lấy document hash (đã bao gồm range digest)
//            this.documentHash = appearance.getRangeDigest(HASH_ALGORITHM);

            // Lấy authenticated attributes bytes → tính second hash
            PdfPKCS7 pkcs7 = new PdfPKCS7(null, certChain, HASH_ALGORITHM, BC_PROVIDER, new BouncyCastleDigest(), false);
            byte[] authAttrBytes = pkcs7.getAuthenticatedAttributeBytes(
                    documentHash,
                    PdfSigner.CryptoStandard.CMS,
                    null,
                    null
            );

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            this.secondHash = md.digest(authAttrBytes);

            // Lưu temp PDF đã thêm chữ ký field (chứa placeholder)
            this.tempPdf = baos.toByteArray();
        }
    }
    public byte[] completeSigning(byte[] signedBytes) {
        // signedBytes ở đây là chữ ký RSA thô (ký trên second hash / authenticated attributes)

        try (ByteArrayInputStream tempInput = new ByteArrayInputStream(tempPdf);
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(tempInput);
            PdfSigner signer = new PdfSigner(reader, result, new StampingProperties().useAppendMode());

            // Tạo external signature container: chỉ cần trả về signedBytes đã có
            IExternalSignature externalSignature = new IExternalSignature() {
                @Override
                public byte[] sign(byte[] data) throws GeneralSecurityException {
                    return signedBytes;  // Đây chính là chữ ký đã ký từ bên ngoài
                }

                @Override
                public String getHashAlgorithm() {
                    return "SHA256";  // ví dụ "SHA256"
                }

                @Override
                public String getEncryptionAlgorithm() {
                    return "RSA";  // hoặc "ECDSA" tùy key
                }
            };

            // Thực hiện ký deferred / external
            // estimatedSize nên bằng hoặc lớn hơn kích thước thực của PKCS#7
            int estimatedSize = 16384;  // 16KB thường đủ, có thể tính chính xác hơn nếu cần

            signer.signDetached(
                    new BouncyCastleDigest(),
                    externalSignature,
                    certChain,
                    null,
                    null,        // hoặc certChain của bạn
                    tsaClient,
                    estimatedSize,
                    PdfSigner.CryptoStandard.CMS
            );

            return result.toByteArray();

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Giai đoạn 2: Nhận chữ ký từ bên ngoài (đã ký secondHash) → hoàn tất PDF
     */
//    public byte[] completeSignature(byte[] externalSignatureValue) throws Exception {
//        if (tempPdf == null) {
//            throw new IllegalStateException("Please call prepareSignature() first");
//        }
//
//        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(tempPdf));
//             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
//
//            PdfSigner signer = new PdfSigner(reader, result, new StampingProperties().useAppendMode());
//
//            // Tạo external signature container thật
//            IExternalSignature externalSig = (hashAlgorithm, dataToSign) -> externalSignatureValue;
//
//            signer.signDetached(
//                    new BouncyCastleDigest(),
//                    externalSig,
//                    certChain,
//                    null,
//                    null,
//                    tsaClient != null ? tsaClient : null,
//                    estimatedSize,
//                    PdfSigner.CryptoStandard.CMS
//            );
//
//            return result.toByteArray();
//        }
//    }

    // -------------------------------------------------------------------------
    // Các getter tiện ích
    // -------------------------------------------------------------------------
    public String getDocumentHashBase64() {
        return documentHash != null ? Base64.getEncoder().encodeToString(documentHash) : null;
    }

    public String getSecondHashBase64() {
        return secondHash != null ? Base64.getEncoder().encodeToString(secondHash) : null;
    }

    public byte[] getTempPdf() {
        return tempPdf;
    }


    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------
    private String generateUniqueFieldName() {
        return "Signature_" + UUID.randomUUID().toString().substring(0, 8);
    }

//    private static String getCommonName(X509Certificate cert, boolean issuer) {
//        try {
//            String dn = issuer ? cert.getIssuerX500Principal().getName() : cert.getSubjectX500Principal().getName();
//            for (String part : dn.split(",")) {
//                if (part.trim().startsWith("CN=")) {
//                    return part.trim().substring(3);
//                }
//            }
//        } catch (Exception ignored) {}
//        return "Unknown";
//    }

    private static String getCommonName(X509Certificate cert, boolean caName) {
        String dnName = "";
        if (!caName) {
            dnName = cert.getSubjectDN().getName();
        } else {
            dnName = cert.getIssuerDN().getName();
        }
        LdapName ldap;
        try {
            ldap = new LdapName(dnName);
            for (Rdn rdn : ldap.getRdns()) {
                if ("CN".equalsIgnoreCase(rdn.getType())) {
                    return rdn.getValue().toString();
                }
            }
        } catch (InvalidNameException e) {
        }

        return "Unknown";
    }


    public void setSignatureText(String signatureText) {
        if (signatureText == null || "".equals(signatureText)) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            this.signatureText = "Ký bởi: " + signerName + "\nNgày ký: " + df.format(signingTime.getTime())
                    + "\nTổ chức xác thực: " + issuerName;
        } else {
            this.signatureText = signatureText;
        }
    }

    // Stub để tính hash ở giai đoạn 1
    private static class ExternalSignatureStub implements IExternalSignature {

        private byte[] signedBytes;

        public ExternalSignatureStub(){
        }

        public ExternalSignatureStub(byte[] signedBytes)
        {
            this.signedBytes = signedBytes;
        }
        @Override
        public byte[] sign(byte[] data) throws GeneralSecurityException {
            return this.signedBytes;
        }

        @Override
        public String getHashAlgorithm() {
            return HASH_ALGORITHM;
        }

        @Override
        public String getEncryptionAlgorithm() {
            return "RSA";
        }
    }

    // -------------------------------------------------------------------------
    // Setter cho appearance, TSA, font, image, ...
    // -------------------------------------------------------------------------
    public void setSignatureRectangle(Rectangle rect, int page) {
        this.signatureRect = rect;
        this.pageNumber = page;
    }

    public void setSignatureImage(byte[] imageBytes) {
        if (imageBytes != null) {
            this.signatureImage = ImageDataFactory.create(imageBytes);
        }
    }

    public void setTsaClient(String url, String user, String pass) {
        this.tsaUrl = url;
        this.tsaUsername = user;
        this.tsaPassword = pass;
        if (url != null && !url.isEmpty()) {
            this.tsaClient = new TSAClientBouncyCastle(url, user, pass);
        }
    }

    public void setFont(String fontPath, float size, com.itextpdf.kernel.colors.Color color) throws IOException {
        this.layer2Font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        this.fontSize = size;
        this.textColor = color != null ? color : ColorConstants.BLACK;
    }

    // ... thêm setter khác nếu cần (reason, location, render mode, ...)
}