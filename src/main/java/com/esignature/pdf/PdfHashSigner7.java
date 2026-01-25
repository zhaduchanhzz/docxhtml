//package com.esignature.pdf;
//
//import com.itextpdf.io.font.PdfEncodings;
//import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.font.PdfFont;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.geom.Rectangle;
//import com.itextpdf.kernel.pdf.*;
//import com.itextpdf.signatures.*;
//import org.apache.commons.codec.binary.Base64;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateFactory;
//
//public class PdfHashSigner7 {
//
//    private final byte[] pdfInput;
//    private final Certificate[] certChain;
//
//    private final int page;
//    private final Rectangle rect;
//    private final String fieldName = "Signature1";
//
//    private final String signatureText;
//    private final PdfSignatureAppearance.RenderingMode renderMode;
//
//    private final byte[] image;
//    private final String fontPath = "font/Times_New_Roman.ttf";
//
//    private byte[] documentHash;
//    private byte[] presignedPdf;
//
//    public PdfHashSigner7(
//            byte[] pdfInput,
//            String certBase64,
//            int page,
//            String rectangle,
//            String signatureText,
//            PdfSignatureAppearance.RenderingMode renderMode,
//            byte[] image
//    ) throws Exception {
//
//        this.pdfInput = pdfInput;
//        this.page = page;
//        this.signatureText = signatureText;
//        this.renderMode = renderMode;
//        this.image = image;
//
//        // ===== Rectangle =====
//        String[] r = rectangle.split(",");
//        this.rect = new Rectangle(
//                Float.parseFloat(r[0]),
//                Float.parseFloat(r[1]),
//                Float.parseFloat(r[2]),
//                Float.parseFloat(r[3])
//        );
//
//        // ===== Cert =====
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        this.certChain = new Certificate[]{
//                cf.generateCertificate(
//                        new ByteArrayInputStream(Base64.decodeBase64(certBase64))
//                )
//        };
//    }
//
//    // =====================================================
//    // PRE-SIGN (KHÔNG getRangeStream)
//    // =====================================================
//    public void preSign() throws Exception {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        PdfSigner signer = new PdfSigner(
//                new PdfReader(new ByteArrayInputStream(pdfInput)),
//                out,
//                new StampingProperties().useAppendMode()
//        );
//
//        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
//
//        PdfFont font = PdfFontFactory.createFont(
//                fontPath,
//                PdfEncodings.IDENTITY_H,
//                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
//        );
//
//        appearance
//                .setLayer2Font(font)
//                .setLayer2Text(signatureText)
//                .setRenderingMode(renderMode)
//                .setSignatureGraphic(
//                        image != null ? ImageDataFactory.create(image) : null
//                )
//                .setPageRect(rect)
//                .setPageNumber(page)
//                .setReason("Ký số văn bản")
//                .setLocation("Việt Nam")
//                .setReuseAppearance(false);
//
//        signer.setFieldName(fieldName);
//
//        // 🔑 PRE-SIGN CONTAINER
//        PreSignContainer preSignContainer =
//                new PreSignContainer(DigestAlgorithms.SHA256);
//
//        int estimatedSize = 15000;
//        signer.signExternalContainer(preSignContainer, estimatedSize);
//
//        // ✅ HASH ĐÚNG CHUẨN
//        this.documentHash = preSignContainer.getDocumentHash();
//        this.presignedPdf = out.toByteArray();
//    }
//
//    public byte[] getDocumentHash() {
//        return documentHash;
//    }
//
//    public byte[] getPresignedPdf() {
//        return presignedPdf;
//    }
//
//    // =====================================================
//    // POST-SIGN (Deferred signing)
//    // =====================================================
//    public byte[] postSign(byte[] pkcs7) throws Exception {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        PdfDocument pdfDoc = new PdfDocument(
//                new PdfReader(new ByteArrayInputStream(presignedPdf)),
//                new PdfWriter(out)
//        );
//
//        IExternalSignatureContainer post =
//                new IExternalSignatureContainer() {
//                    @Override
//                    public byte[] sign(InputStream data) {
//                        return pkcs7;
//                    }
//
//                    @Override
//                    public void modifySigningDictionary(PdfDictionary dic) {
//                        dic.put(PdfName.Filter, PdfName.Adobe_PPKLite);
//                        dic.put(PdfName.SubFilter, PdfName.Adbe_pkcs7_detached);
//                    }
//                };
//
//        PdfSigner.signDeferred(
//                pdfDoc,
//                fieldName,
//                out,
//                post
//        );
//
//        return out.toByteArray();
//    }
//}
//
//
