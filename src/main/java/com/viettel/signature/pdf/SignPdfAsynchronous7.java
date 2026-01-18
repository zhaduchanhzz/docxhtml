package com.viettel.signature.pdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.IRandomAccessSource;
import com.itextpdf.io.source.RASInputStream;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.signatures.*;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.viettel.signature.utils.CertUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Slf4j
public class SignPdfAsynchronous7 {

    public static final String CRYPT_ALGORITHM_RSA = "RSA";
    public static final String HASH_ALGORITHM_SHA256 = "SHA256";

    private void prepareEmptySignature(
            PdfSigner signer,
            String fieldName,
            DisplayConfig displayConfig
    ) {

        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason(displayConfig.getReason())
                .setLocation(displayConfig.getLocation())
                .setContact(displayConfig.getContact())
                .setPageNumber(displayConfig.getNumberPageSign());

        if (displayConfig.isIsDisplaySignature()) {
            Rectangle rect = new Rectangle(
                    displayConfig.getMarginLeftOfRectangle(),
                    displayConfig.getMarginBottomOfRectangle(),
                    displayConfig.getWidthRectangle(),
                    displayConfig.getHeightRectangle()
            );
            appearance.setPageRect(rect);
        }

        signer.setFieldName(fieldName);
    }

    private boolean emptySignatureItext7(
            String src,
            String dest,
            String fieldName,
            DisplayConfig displayConfig,
            Certificate cert) {

        PdfReader reader = null;
        FileOutputStream os = null;

        try {
            Security.addProvider(new BouncyCastleProvider());

            reader = new PdfReader(src);
            os = new FileOutputStream(dest);

            PdfSigner signer = new PdfSigner(
                    reader,
                    os,
                    new StampingProperties().useAppendMode()
            );

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();

            // ===== Thông tin chữ ký =====
            Calendar cal = Calendar.getInstance();
            cal.setTime(displayConfig.getSignDate());

            if (displayConfig.getContact() == null || displayConfig.getContact().isEmpty()) {
                String cn = CertUtils.getCN((X509Certificate) cert);
                if (cn == null) {
                    log.error("Get CN of Certificate failed");
                    return false;
                }
                displayConfig.setContact(cn);
            }

            appearance
                    .setContact(displayConfig.getContact())
                    .setReason(displayConfig.getReason())
                    .setLocation(displayConfig.getLocation());
//                    .setSignDate(cal);

            SimpleDateFormat sdf =
                    new SimpleDateFormat(displayConfig.getDateFormatString());
            String dateString = sdf.format(displayConfig.getSignDate());

            // ===== Visible / Invisible =====
            if (!displayConfig.isIsDisplaySignature()) {

                // Invisible signature
                signer.setFieldName(fieldName);

            } else {

                if (displayConfig.getSignType() == 1) {
                    // Ký theo tọa độ
                    int page = displayConfig.getNumberPageSign();
                    int totalPages = signer.getDocument().getNumberOfPages();
                    if (page < 1 || page > totalPages) {
                        page = 1;
                    }

                    PdfPage pdfPage = signer.getDocument().getPage(page);
                    Rectangle pageSize = pdfPage.getPageSize();

                    float w = displayConfig.getWidthRectangle();
                    float h = displayConfig.getHeightRectangle();

                    float x = displayConfig.getMarginLeftOfRectangle();
                    float y = displayConfig.getMarginBottomOfRectangle();

                    if (displayConfig.getLocateSign() != 5) {
                        float pw = pageSize.getWidth();
                        float ph = pageSize.getHeight();

                        switch (displayConfig.getLocateSign()) {
                            case 1: // top-left
                                x = displayConfig.getMarginLeftOfRectangle();
                                y = ph - displayConfig.getMarginTopOfRectangle() - h;
                                break;
                            case 2: // top-right
                                x = pw - displayConfig.getMarginRightOfRectangle() - w;
                                y = ph - displayConfig.getMarginTopOfRectangle() - h;
                                break;
                            case 3: // bottom-left
                                x = displayConfig.getMarginLeftOfRectangle();
                                y = displayConfig.getMarginBottomOfRectangle();
                                break;
                            case 6: // top-center
                                x = (pw - w) / 2;
                                y = ph - displayConfig.getMarginTopOfRectangle() - h;
                                break;
                            default: // bottom-right
                                x = pw - displayConfig.getMarginRightOfRectangle() - w;
                                y = displayConfig.getMarginBottomOfRectangle();
                        }
                    }

                    Rectangle rect = new Rectangle(x, y, w, h);
                    appearance
                            .setPageRect(rect)
                            .setPageNumber(page);

                }

                signer.setFieldName(fieldName);

                // ===== Hiển thị =====
                if (displayConfig.getTypeDisplay() == 2) {
                    // Image only
                    ImageData image =
                            ImageDataFactory.create(displayConfig.getPathImage());
                    appearance
                            .setSignatureGraphic(image)
                            .setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

                } else {
                    // Text hoặc Graphic + Text
                    byte[] fontBytes = Files.readAllBytes(Paths.get(displayConfig.getFontPath()));
                    PdfFont font = PdfFontFactory.createFont(
                            fontBytes,
                            PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                    );
                    String displayText;
                    if (displayConfig.getDisplayText() != null &&
                            !displayConfig.getDisplayText().isEmpty()) {
                        displayText = displayConfig.getDisplayText();
                    } else {
                        displayText = String.format(
                                displayConfig.getFormatRectangleText(),
                                displayConfig.getContact(),
                                dateString,
                                displayConfig.getReason(),
                                displayConfig.getLocation()
                        );
                    }

                    appearance
                            .setLayer2Text(displayText)
                            .setLayer2Font(font)
                            .setLayer2FontSize(displayConfig.getSizeFont());

                    if (displayConfig.getTypeDisplay() == 4) {
                        ImageData image =
                                ImageDataFactory.create(displayConfig.getPathImage());
                        appearance
                                .setSignatureGraphic(image)
                                .setRenderingMode(
                                        PdfSignatureAppearance.RenderingMode
                                                .GRAPHIC_AND_DESCRIPTION
                                );
                    }
                }
            }

            // ===== Empty signature container =====
            IExternalSignatureContainer external =
                    new ExternalBlankSignatureContainer(
                            PdfName.Adobe_PPKLite,
                            PdfName.Adbe_pkcs7_detached
                    );

            signer.signExternalContainer(external, 8192);
            return true;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        } finally {
            try {
                if (os != null) os.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private boolean emptySignatureTableItext7(
            String src,
            String dest,
            String fieldName,
            DisplayConfig displayConfig,
            Certificate cert) {

        PdfReader reader = null;
        FileOutputStream os = null;

        try {
            Security.addProvider(new BouncyCastleProvider());

            reader = new PdfReader(src);
            os = new FileOutputStream(dest);

            PdfSigner signer = new PdfSigner(
                    reader,
                    os,
                    new StampingProperties().useAppendMode()
            );

            PdfDocument pdfDoc = signer.getDocument();

            // ===== AcroForm =====
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, false);
            Map<String, PdfFormField> fields =
                    acroForm != null ? acroForm.getFormFields() : Collections.emptyMap();

            int numberSignPage = 1;
            float[] totalHeight = new float[displayConfig.getMaxPageSign() + 1];

            for (PdfFormField field : fields.values()) {
                if (!field.getFormType().equals(PdfName.Sig)) {
                    continue;
                }

                List<PdfWidgetAnnotation> widgets = field.getWidgets();
                if (widgets.isEmpty()) continue;

                PdfWidgetAnnotation widget = widgets.get(0);
                int page = pdfDoc.getPageNumber(widget.getPage());
                Rectangle rect = widget.getRectangle().toRectangle();

                numberSignPage = Math.max(numberSignPage, page);
                totalHeight[page] += rect.getHeight();
            }

            PdfPage page = pdfDoc.getPage(numberSignPage);
            Rectangle pageSize = page.getPageSize();

            float heightPage = pageSize.getHeight();
            float widthPage = pageSize.getWidth();

            float x = displayConfig.getMarginRightOfTable();
            float w = widthPage - displayConfig.getMarginRightOfTable() * 2;

            // ===== Signature appearance =====
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();

            if (displayConfig.getContact() == null || displayConfig.getContact().isEmpty()) {
                displayConfig.setContact(
                        CertUtils.getCN((X509Certificate) cert)
                );
            }

            appearance
                    .setContact(displayConfig.getContact())
                    .setReason(displayConfig.getReason())
                    .setLocation(displayConfig.getLocation());

            // ===== Font =====
            byte[] fontBytes =
                    Files.readAllBytes(Paths.get(displayConfig.getFontPath()));

            PdfFont font = PdfFontFactory.createFont(
                    fontBytes,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            // ===== Build table =====
            Table table = new Table(displayConfig.getWidthsPercen().length)
                    .setWidth(UnitValue.createPointValue(w));

            table.setFont(font)
                    .setFontSize(displayConfig.getSizeFont());

            for (int i = 0; i < displayConfig.getTextArray().length; i++) {
                Cell cell = new Cell()
                        .add(new Paragraph(displayConfig.getTextArray()[i])
                                .setTextAlignment(
                                        TextAlignment.values()
                                                [displayConfig.getAlignmentArray()[i].ordinal()]
                                ));
                table.addCell(cell);
            }

            float tableHeight = table.createRendererSubTree()
                    .setParent(new Document(pdfDoc).getRenderer())
                    .getOccupiedArea()
                    .getBBox()
                    .getHeight();

            float y = heightPage
                    - totalHeight[numberSignPage]
                    - displayConfig.getMarginTopOfTable()
                    - tableHeight
                    - displayConfig.getHeightTitle();

            // ===== Nếu không đủ chỗ =====
            if (y < displayConfig.getMarginBottomOfTable()) {
                if (numberSignPage >= displayConfig.getTotalPageSign()) {
                    signer.setFieldName(fieldName);
                    signer.signExternalContainer(
                            new ExternalBlankSignatureContainer(
                                    PdfName.Adobe_PPKLite,
                                    PdfName.Adbe_pkcs7_detached
                            ),
                            8192
                    );
                    return true;
                }

                numberSignPage++;
                y = heightPage
                        - displayConfig.getMarginTopOfTable()
                        - tableHeight
                        - displayConfig.getHeightTitle();
            }

            Rectangle rect = new Rectangle(x, y, w, tableHeight);

            appearance
                    .setPageRect(rect)
                    .setPageNumber(numberSignPage);

            signer.setFieldName(fieldName);

// ===== Draw table into signature appearance =====
            PdfCanvas pdfCanvas =
                    new PdfCanvas(
                            appearance.getLayer2(),
                            pdfDoc
                    );

            Canvas canvas = new Canvas(
                    pdfCanvas,
                    rect
            );

            canvas.add(table);
            canvas.close();

            // ===== Empty signature =====
            signer.signExternalContainer(
                    new ExternalBlankSignatureContainer(
                            PdfName.Adobe_PPKLite,
                            PdfName.Adbe_pkcs7_detached
                    ),
                    8192
            );

            return true;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        } finally {
            try {
                if (reader != null) reader.close();
                if (os != null) os.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private List<byte[]> preSignItext7(
            String src,
            String fieldName,
            String digestAlgorithm,
            Certificate[] chain,
            Date signDate) {

        PdfReader reader = null;

        try {
            List<byte[]> result = new ArrayList<>();

            reader = new PdfReader(src);
            PdfDocument pdfDoc = new PdfDocument(reader);

            // ===== Signature dictionary =====
            SignatureUtil signUtil = new SignatureUtil(pdfDoc);
            log.info(fieldName);
//            PdfDictionary sigDict = signUtil.getSignatureDictionary(fieldName);
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, false);
            Map<String, PdfFormField> fields = acroForm.getFormFields();
            log.info("filedname");
            fields.keySet().forEach(System.out::println);

            PdfDictionary sigDict = signUtil.getSignatureDictionary(fieldName);
            if (sigDict == null) {
                log.error("No signature field: {}", fieldName);
                return null;
            }

            PdfArray byteRange = sigDict.getAsArray(PdfName.ByteRange);
            if (byteRange == null || byteRange.size() != 4) {
                log.error("Invalid ByteRange");
                return null;
            }

            long[] gaps = byteRange.toLongArray();
            if (gaps[0] != 0) {
                log.error("Single exclusion space supported only");
                return null;
            }

            // ===== Create ranged stream =====
            IRandomAccessSource source =
                    reader.getSafeFile().createSourceView();

            InputStream rangeStream =
                    new RASInputStream(
                            new RandomAccessSourceFactory()
                                    .createRanged(source, gaps)
                    );

            // ===== Digest =====
            BouncyCastleDigest digest = new BouncyCastleDigest();

            byte[] hash = DigestAlgorithms.digest(
                    rangeStream,
                    digest.getMessageDigest(digestAlgorithm)
            );

            // ===== PKCS7 (no private key) =====
            PdfPKCS7 pkcs7 = new PdfPKCS7(
                    null,
                    chain,
                    digestAlgorithm,
                    null,
                    digest,
                    false
            );

            Calendar cal = Calendar.getInstance();
            cal.setTime(signDate);

            byte[] authenticatedAttributes =
                    pkcs7.getAuthenticatedAttributeBytes(
                            hash,
                            PdfSigner.CryptoStandard.CMS,
                            null,
                            null
                    );


            result.add(authenticatedAttributes);
            result.add(hash);

            pdfDoc.close();
            return result;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void drawSignatureTable(
            PdfSigner signer,
            DisplayConfig displayConfig
    ) {

        float[] widths = displayConfig.getWidthsPercen();
        Table table = new Table(UnitValue.createPercentArray(widths))
                .setWidth(UnitValue.createPercentValue(100));

        for (int i = 0; i < displayConfig.getTextArray().length; i++) {
            Cell cell = new Cell()
                    .add(new Paragraph(displayConfig.getTextArray()[i]))
                    .setTextAlignment(displayConfig.getAlignmentArray()[i]);
            table.addCell(cell);
        }

        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        Canvas canvas = new Canvas(
                appearance.getLayer2(),
                signer.getDocument()
        );
        canvas.add(table);
        canvas.close();
    }

//    public List<byte[]> createHash(String src, String tempFile, String fieldName, String digestAlgorithm, Certificate[] chain, DisplayConfig displayConfig) {
//        if (digestAlgorithm != null && !digestAlgorithm.trim().isEmpty() && ("SHA1".equals(digestAlgorithm) || "SHA256".equals(digestAlgorithm) || "SHA384".equals(digestAlgorithm) || "SHA512".equals(digestAlgorithm))) {
//            if (displayConfig.getSignType() == 1) {
//                if (displayConfig.getTypeDisplay() == 3) {
//                    if (!this.emptySignatureTableItext7(src, tempFile, fieldName, displayConfig, chain[0])) {
//                        return null;
//                    }
//                } else if (!this.emptySignatureItext7(src, tempFile, fieldName, displayConfig, chain[0])) {
//                    return null;
//                }
//            } else if (!this.emptySignatureItext7(src, tempFile, fieldName, displayConfig, chain[0])) {
//                return null;
//            }
//
//            return this.preSignItext7(tempFile, fieldName, digestAlgorithm, chain, displayConfig.getSignDate());
//        } else {
//            log.error("Digest Algorithm is invalid: " + digestAlgorithm);
//            return null;
//        }
//    }

    public List<byte[]> createHash(
            String src,
            String tempFile,
            String fieldName,
            String digestAlgorithm,
            Certificate[] chain,
            DisplayConfig displayConfig
    ) {
        PdfDocument pdfDoc = null;
        try {
            // 1️⃣ Copy PDF gốc sang file tạm
            Files.copy(Paths.get(src), Paths.get(tempFile), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 2️⃣ Chuẩn hóa fieldName (loại bỏ ký tự đặc biệt /, :)
            String safeFieldName = fieldName.replaceAll("[/:]", "_").trim();
//            String safeFieldName = fieldName;

            // 3️⃣ Mở PDF tạm để chuẩn bị field
            pdfDoc = new PdfDocument(new PdfReader(tempFile), new PdfWriter(tempFile + "_tmp.pdf"));
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, true);

            // 4️⃣ Tạo field signature nếu chưa có
            if (!acroForm.getFormFields().containsKey(safeFieldName)) {
                PdfFormField sigField = PdfFormField.createSignature(pdfDoc);
                sigField.setFieldName(safeFieldName);
                acroForm.addField(sigField);
            }

            pdfDoc.close(); // Flush field vào PDF, không ký

            // 5️⃣ Tính hash với preSignItext7 trên file tạm mới
            return preSignItext7(tempFile + "_tmp.pdf", safeFieldName, digestAlgorithm, chain, displayConfig.getSignDate());

        } catch (Exception ex) {
            log.error("createHash failed: {}", ex.getMessage(), ex);
            return null;
        } finally {
            try {
                if (pdfDoc != null && !pdfDoc.isClosed()) pdfDoc.close();
            } catch (Exception e) {
                log.error("Error closing PDF: {}", e.getMessage(), e);
            }
        }
    }


    private boolean prepareEmptySignatureField(
            String src,
            String dest,
            String fieldName,
            DisplayConfig displayConfig,
            Certificate cert
    ) {
        try (PdfReader reader = new PdfReader(src);
             FileOutputStream os = new FileOutputStream(dest)) {

            PdfSigner signer = new PdfSigner(
                    reader,
                    os,
                    new StampingProperties().useAppendMode()
            );

            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(signer.getDocument(), true);
            if (!acroForm.getFormFields().containsKey(fieldName)) {
                PdfFormField sigField = PdfFormField.createSignature(signer.getDocument());
                sigField.setFieldName(fieldName);
                acroForm.addField(sigField);
            }

            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private Certificate[] loadCertChainFromBase64(String certBase64) {
        try {
            CertificateFactory certFactory =
                    CertificateFactory.getInstance("X.509");
            // Decode Base64
            byte[] certBytes = Base64.getDecoder().decode(certBase64);

            // Có thể chứa 1 hoặc nhiều cert
            Collection<? extends Certificate> certs =
                    certFactory.generateCertificates(
                            new ByteArrayInputStream(certBytes)
                    );

            if (certs == null || certs.isEmpty()) {
                throw new RuntimeException("Certificate chain is empty");
            }

            List<Certificate> chain = new ArrayList<>();

            for (Certificate cert : certs) {
                if (!(cert instanceof X509Certificate)) {
                    throw new RuntimeException(
                            "Certificate must be instance of X509Certificate"
                    );
                }
                chain.add(cert);
            }

            return chain.toArray(new Certificate[0]);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Certificate must be instance of X509Certificate"
            );
        }
    }

    public List<byte[]> createHash(String src, String fieldName, String digestAlgorithm,
                                   Certificate[] chain, DisplayConfig displayConfig) throws IOException {
        // 1️⃣ Tạo field trống (nếu chưa có)
        File tempFile = File.createTempFile("tmp_", ".pdf");
        prepareEmptySignatureField(src, tempFile.getAbsolutePath(), fieldName, displayConfig, chain[0]);

        // 2️⃣ Tính hash với preSignItext7
        return preSignItext7(tempFile.getAbsolutePath(), fieldName, digestAlgorithm, chain, displayConfig.getSignDate());
    }

    public boolean insertSignatureItext7(
            String src,
            String dest,
            String fieldName,
            String digestAlgorithm,
            String cryptAlgorithm,
            byte[] hash,
            byte[] extSignature,
            Certificate[] chain,
            Date signDate,
            TimestampConfig timestampConfig) {

        // ===== Validate =====
        if (!List.of("SHA1", "SHA256", "SHA384", "SHA512").contains(digestAlgorithm)) {
            log.error("Digest Algorithm is invalid: {}", digestAlgorithm);
            return false;
        }

        if (!List.of("RSA", "DSA").contains(cryptAlgorithm)) {
            log.error("Crypt Algorithm is invalid: {}", cryptAlgorithm);
            return false;
        }

        try {
            Security.addProvider(new BouncyCastleProvider());

            PdfReader reader = new PdfReader(src);
            FileOutputStream os = new FileOutputStream(dest);

            PdfSigner signer = new PdfSigner(
                    reader,
                    os,
                    new StampingProperties().useAppendMode()
            );

            // ===== Check last signature =====
            SignatureUtil signUtil = new SignatureUtil(signer.getDocument());
            if (!signUtil.signatureCoversWholeDocument(fieldName)) {
                log.error("Not the last signature");
                return false;
            }

            signer.setFieldName(fieldName);

            IExternalSignatureContainer container =
                    new ExternalHashContainer(
                            digestAlgorithm,      // hashAlgorithm
                            "BC",                 // provider
                            8192,                 // estimatedSize
                            extSignature,         // extSignature
                            chain,                // chain
                            cryptAlgorithm,       // cryptAlgorithm
                            signDate,             // signDate
                            timestampConfig       // timestampConfig
                    );

            signer.signExternalContainer(container, 8192);
            return true;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }


}
