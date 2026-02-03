package com.esignature.pdf;

import com.esignature.signer.SignatureParameter;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import com.itextpdf.io.util.StreamUtil;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.*;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

public class IText7ExternalSigner {

    private byte[] secondHash;     // hash gửi server
    private byte[] signedHash;     // chữ ký server trả về

    private static final String HASH_ALG = "SHA-256";
    private static final int ESTIMATED_SIZE = 8192;

    private SignatureParameter param;               // giả sử bạn vẫn giữ class này
    private X509Certificate signerCert;
    private Certificate[] certChain;

    private String reason = "Signing document";
    private String location = "Signer's office";
    private String signerName;
    private String issuerName;
    private String contact;
    private String signatureText;
    private Rectangle signatureRect;
    private Calendar signingTime = Calendar.getInstance();

    /**
     * Phase 1: tạo placeholder + lấy second hash
     */


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

    public void prepareSignature(
            String srcPdf,
            String tmpPdf,
            String fieldName
    ) throws Exception {

        PdfSigner signer = new PdfSigner(
                new PdfReader(srcPdf),
                new FileOutputStream(tmpPdf),
                new StampingProperties().useAppendMode()
        );

        signer.setFieldName(fieldName);

        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance.setReason("ha duc hanh")
                .setLocation("VN")
                .setContact("123")
                .setCertificate(signerCert)
                .setPageRect(signatureRect != null ? signatureRect : new Rectangle(36, 36, 200, 100))
                .setPageNumber(1)
                .setLayer2Text(signatureText);
        IExternalSignatureContainer external =
                new IExternalSignatureContainer() {
//                byte[] secondHash = new byte[ESTIMATED_SIZE];
                    @Override
                    public byte[] sign(InputStream data) {
                        try {
                            MessageDigest md =
                                    MessageDigest.getInstance(HASH_ALG);

                            byte[] authenticatedAttributes =
                                    StreamUtil.inputStreamToArray(data);

                            // 🔥 SECOND HASH (thứ bạn cần)
                            byte[] hasOnly = md.digest(authenticatedAttributes);
                            PdfPKCS7 sgn = new PdfPKCS7(null, certChain, "SHA256", "BC",  new BouncyCastleDigest(), false);
                            sgn.setSignDate(signingTime);
                            secondHash = sgn.getAuthenticatedAttributeBytes(
                                    hasOnly,
                                    PdfSigner.CryptoStandard.CMS,
                                    null,
                                    null
                            );

                            // ❗ chưa ký, chỉ giữ chỗ
                            return new byte[0];
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void modifySigningDictionary(PdfDictionary dic) {
                        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
                        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
                    }
                };

        // 👉 tạo placeholder + ByteRange
        signer.signExternalContainer(external, ESTIMATED_SIZE);
    }

    /**
     * Getter để gửi hash lên server
     */
    public byte[] getSecondHash() {
        return secondHash;
    }

    /**
     * Nhận chữ ký từ server
     */
    public void setSignedHash(byte[] signedHash) {
        this.signedHash = signedHash;
    }

    /**
     * Phase 2: embed chữ ký đã ký vào PDF
     */
    public void completeSignature(
            String tmpPdf,
            String finalPdf,
            String fieldName
    ) throws Exception {

        if (signedHash == null) {
            throw new IllegalStateException("Signed hash is null");
        }

        IExternalSignatureContainer realSigner =
                new IExternalSignatureContainer() {

                    @Override
                    public byte[] sign(InputStream data) {
                        // 👉 trả về chữ ký server đã ký
                        return signedHash;
                    }

                    @Override
                    public void modifySigningDictionary(PdfDictionary dic) {
                        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
                        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
                    }
                };

        PdfReader reader = new PdfReader(tmpPdf);
        PdfSigner.signDeferred(
                new PdfDocument(reader),
                fieldName,
                new FileOutputStream(finalPdf),
                realSigner
        );
    }


    public void calculateSignatureIText7(
            String srcPdf,
            String tmpPdf,
            String fieldName
    ) throws Exception {

        PdfReader reader = new PdfReader(srcPdf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfSigner signer = new PdfSigner(
                reader,
                baos,
                new StampingProperties().useAppendMode()
        );

        signer.setFieldName(fieldName);

        PdfSignatureAppearance appearance =
                signer.getSignatureAppearance();

        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setContact(contact);
        appearance.setSignatureCreator("iText 7")
                .setPageRect(new Rectangle(36, 36, 200, 100))
                .setPageNumber(1);

        // =============================
        // PRE-SIGN CONTAINER
        // =============================
        IExternalSignatureContainer preSignContainer =
                new IExternalSignatureContainer() {

                    @Override
                    public byte[] sign(InputStream data) {
                        try {
                            MessageDigest md =
                                    MessageDigest.getInstance("SHA-256");

                            byte[] authenticatedAttributes =
                                    com.itextpdf.io.util.StreamUtil
                                            .inputStreamToArray(data);

                            // 🔥 SECOND HASH (chuẩn, tương đương iText 5)
                            secondHash = md.digest(authenticatedAttributes);

                            // ❗ CHƯA ký – chỉ tạo placeholder
                            return new byte[0];

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void modifySigningDictionary(PdfDictionary dic) {
                        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
                        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
                    }
                };

        // =============================
        // CREATE PLACEHOLDER
        // =============================
        int estimatedSize = 32000; // TSA + cert chain → 32–64k an toàn

        signer.signExternalContainer(preSignContainer, estimatedSize);

        // ghi PDF tạm ra file (giống outStream của iText 5)
        try (FileOutputStream fos = new FileOutputStream(tmpPdf)) {
            fos.write(baos.toByteArray());
        }

        reader.close();
    }

}
