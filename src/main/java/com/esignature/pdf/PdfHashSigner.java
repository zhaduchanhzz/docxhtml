//package com.esignature.pdf;
//
//import com.esignature.signer.SignatureParameter;
//import com.itextpdf.io.font.PdfEncodings;
//import com.itextpdf.io.image.ImageData;
//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.font.PdfFont;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.geom.Rectangle;
//import com.itextpdf.kernel.pdf.*;
//import com.itextpdf.signatures.*;
//
//import java.io.*;
//import java.security.MessageDigest;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.text.SimpleDateFormat;
//import java.util.Base64;
//import java.util.Calendar;
//
//
//public class PdfHashSigner {
//
//    private SignatureParameter param;
//    private Certificate[] certChain;
//    private byte[] documentHash;
//
//    public PdfHashSigner() {
//    }
//
//    // =====================================================
//    // INIT (giữ logic cũ)
//    // =====================================================
//    public void init(SignatureParameter param) throws Exception {
//        this.param = param;
//
//        CertificateFactory factory =
//                CertificateFactory.getInstance("X.509");
//
//        X509Certificate cert =
//                (X509Certificate) factory.generateCertificate(
//                        new ByteArrayInputStream(
//                                Base64.getDecoder().decode(param.getCertBase64())
//                        )
//                );
//
//        this.certChain = new Certificate[]{cert};
//    }
//
//    // =====================================================
//    // PRE-SIGN (TẠO HASH)
//    // =====================================================
//    public byte[] preSign(byte[] pdfInput) throws Exception {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfInput));
//        PdfSigner signer = new PdfSigner(
//                reader,
//                out,
//                new StampingProperties().useAppendMode()
//        );
//
//        // ===== Appearance =====
//        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
//
//        PdfFont font = PdfFontFactory.createFont(
//                param.getFontPath(),
//                PdfEncodings.IDENTITY_H,
//                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
//        );
//
//        ImageData image = ImageDataFactory.create(
//                Base64.getDecoder().decode(param.getImageBase64())
//        );
//
//        Calendar now = Calendar.getInstance();
//        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
//
//        String text =
//                "Ký bởi: " + param.getSignerName() + "\n" +
//                        "Ngày ký: " + df.format(now.getTime()) + "\n" +
//                        param.getReason();
//
//        appearance
//                .setLayer2Font(font)
//                .setLayer2Text(text)
//                .setSignatureGraphic(image)
//                .setRenderingMode(
//                        PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION
//                )
//                .setLocation(param.getLocation())
//                .setReason(param.getReason())
//                .setReuseAppearance(false);
//
//        Rectangle rect = new Rectangle(
//                param.getX(),
//                param.getY(),
//                param.getWidth(),
//                param.getHeight()
//        );
//
//        appearance.setPageRect(rect);
//        appearance.setPageNumber(param.getPage());
//
//        signer.setFieldName(param.getFieldName());
//
//        // ===== PRE-SIGN container =====
//        ExternalBlankSignatureContainer blank =
//                new ExternalBlankSignatureContainer(
//                        PdfName.Adobe_PPKLite,
//                        PdfName.Adbe_pkcs7_detached
//                );
//
//        int estimatedSize = 15000;
//        signer.signExternalContainer(blank, estimatedSize);
//
//        // ===== HASH =====
//        MessageDigest md = MessageDigest.getInstance("SHA-256");
//        this.documentHash =
//                DigestAlgorithms.digest(
//                        signer.getRangeStream(),
//                        md
//                );
//
//        return out.toByteArray();
//    }
//
//    public byte[] getDocumentHash() {
//        return documentHash;
//    }
//
//    // =====================================================
//    // POST-SIGN (GẮN CHỮ KÝ)
//    // =====================================================
//    public byte[] postSign(byte[] presignedPdf, String pkcs7Base64) throws Exception {
//
//        byte[] pkcs7 = Base64.getDecoder().decode(pkcs7Base64);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        PdfDocument pdfDoc = new PdfDocument(
//                new PdfReader(new ByteArrayInputStream(presignedPdf)),
//                new PdfWriter(out)
//        );
//
//        IExternalSignatureContainer container =
//                new PostSignContainer(pkcs7);
//
//        PdfSigner.signDeferred(
//                pdfDoc,
//                param.getFieldName(),
//                out,
//                container
//        );
//
//        return out.toByteArray();
//    }
//}
//class PostSignContainer implements IExternalSignatureContainer {
//
//    private final byte[] pkcs7;
//
//    PostSignContainer(byte[] pkcs7) {
//        this.pkcs7 = pkcs7;
//    }
//
//    @Override
//    public byte[] sign(InputStream data) {
//        return pkcs7;
//    }
//
//    @Override
//    public void modifySigningDictionary(PdfDictionary dic) {
//        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
//        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
//    }
//}
