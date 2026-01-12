//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.viettel.signature.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.viettel.signature.utils.CertUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.log;

@Slf4j
public class SignPdfSynchronous {
    public static final String CRYPT_ALGORITHM_RSA = "RSA";
    public static final String CRYPT_ALGORITHM_DSA = "DSA";
    public static final String HASH_ALGORITHM_SHA1 = "SHA1";
    public static final String HASH_ALGORITHM_SHA256 = "SHA256";
    public static final String HASH_ALGORITHM_SHA384 = "SHA384";
    public static final String HASH_ALGORITHM_SHA512 = "SHA512";
    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA384 = "SHA-384";
    public static final String SHA512 = "SHA-512";

    public SignPdfSynchronous() {
    }

    public boolean sign(KeyStore.PrivateKeyEntry key, String src, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm, String cryptAlgorithm, String provider, MakeSignature.CryptoStandard subfilter, DisplayConfig displayConfig, TimestampConfig timestampConfig) {
        if (digestAlgorithm != null && !digestAlgorithm.trim().isEmpty() && ("SHA-1".equals(digestAlgorithm) || "SHA-256".equals(digestAlgorithm) || "SHA-384".equals(digestAlgorithm) || "SHA-512".equals(digestAlgorithm))) {
            if (cryptAlgorithm != null && !cryptAlgorithm.trim().isEmpty() && ("RSA".equals(cryptAlgorithm) || "DSA".equals(cryptAlgorithm))) {
                PdfReader reader = null;
                FileOutputStream os = null;

                try {
                    reader = new PdfReader(src);
                    os = new FileOutputStream(dest);
                    PdfStamper stamper = PdfStamper.createSignature(reader, os, '\u0000', (File)null, true);
                    PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(displayConfig.getSignDate());
                    if ("".equals(displayConfig.getContact())) {
                        String cn = CertUtils.getCN((X509Certificate)chain[0]);
                        if (cn == null) {
                            log.error("Get CN of Certificate failed");
                            boolean var49 = false;
                            return var49;
                        }

                        displayConfig.setContact(cn);
                    }

                    appearance.setContact(displayConfig.getContact());
                    appearance.setSignDate(cal);
                    appearance.setReason(displayConfig.getReason());
                    appearance.setLocation(displayConfig.getLocation());
                    SimpleDateFormat dateFormat = new SimpleDateFormat(displayConfig.getDateFormatString());
                    String dateString = dateFormat.format(displayConfig.getSignDate());
                    String fieldName = displayConfig.getFieldName();
                    if (fieldName == null || fieldName.trim().isEmpty()) {
                        fieldName = displayConfig.getContact().replaceAll("\\.", " ") + "_" + dateString + "_" + RandomStringUtils.random(10, true, true);
                    }

                    if (displayConfig.isIsDisplaySignature()) {
                        if (displayConfig.getSignType() != 1) {
                            appearance.setVisibleSignature(fieldName);
                        } else {
                            int numberOfPages = reader.getNumberOfPages();
                            int signPage = displayConfig.getNumberPageSign();
                            if (signPage < 1 || signPage > numberOfPages) {
                                signPage = 1;
                            }

                            Rectangle psize = reader.getPageSize(signPage);
                            int locateSign = displayConfig.getLocateSign();
                            float widthRectangle = displayConfig.getWidthRectangle();
                            float heightRectangle = displayConfig.getHeightRectangle();
                            float coorX = displayConfig.getMarginLeftOfRectangle();
                            float coorY = displayConfig.getMarginBottomOfRectangle();
                            if (locateSign != 5) {
                                float heightPage = psize.getHeight();
                                float widthPage = psize.getWidth();
                                float marginLeft = displayConfig.getMarginLeftOfRectangle();
                                float marginBottom = displayConfig.getMarginBottomOfRectangle();
                                float marginRight = displayConfig.getMarginRightOfRectangle();
                                float marginTop = displayConfig.getMarginTopOfRectangle();
                                switch (locateSign) {
                                    case 1:
                                        coorX = marginLeft;
                                        coorY = heightPage - marginTop - heightRectangle;
                                        break;
                                    case 2:
                                        coorX = widthPage - marginRight - widthRectangle;
                                        coorY = heightPage - marginTop - heightRectangle;
                                        break;
                                    case 3:
                                        coorX = marginLeft;
                                        coorY = marginBottom;
                                        break;
                                    default:
                                        coorX = widthPage - marginRight - widthRectangle;
                                        coorY = marginBottom;
                                }
                            }

                            Rectangle rectangle = new Rectangle(coorX, coorY, coorX + widthRectangle, coorY + heightRectangle);
                            appearance.setVisibleSignature(rectangle, signPage, fieldName);
                        }

                        if (displayConfig.getTypeDisplay() == 2) {
                            Image image = Image.getInstance(displayConfig.getPathImage());
                            float heightImage = image.getHeight();
                            float widthImage = image.getWidth();
                            Rectangle rec = appearance.getRect();
                            float w2 = rec.getWidth();
                            float h2 = rec.getHeight();
                            float scaleWidth = w2 / widthImage;
                            float scaleHeight = h2 / heightImage;
                            float scale = scaleWidth;
                            if (scaleHeight < scaleWidth) {
                                scale = scaleHeight;
                            }

                            if (scale > 1.0F) {
                                scale = 1.0F;
                            }

                            appearance.setLayer2Text("");
                            appearance.setImage(image);
                            appearance.setImageScale(scale);
                        } else if (displayConfig.getTypeDisplay() == 1) {
                            PdfTemplate n2 = appearance.getLayer(2);
                            float x2 = n2.getBoundingBox().getLeft();
                            float y2 = n2.getBoundingBox().getBottom();
                            float w2 = n2.getBoundingBox().getWidth();
                            float h2 = n2.getBoundingBox().getHeight();
                            ColumnText ct = new ColumnText(n2);
                            ct.setSimpleColumn(x2, y2, w2, h2);
                            BaseFont bf = BaseFont.createFont(displayConfig.getFontPath(), "Identity-H", true);
                            String displayText = "";
                            if (displayConfig.getDisplayText() != null && !displayConfig.getDisplayText().isEmpty()) {
                                displayText = displayConfig.getDisplayText();
                            } else if ("Người Ký: %s\r\n%sNgày ký: %s".equals(displayConfig.getFormatRectangleText())) {
                                if (displayConfig.getOrganizationUnit() != null && !displayConfig.getOrganizationUnit().trim().isEmpty()) {
                                    displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganizationUnit().trim(), dateString);
                                } else {
                                    displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganization().trim(), dateString);
                                }
                            } else if ("Người Ký: %s\r\n%s\r\n%s\r\nNgày ký: %s".equals(displayConfig.getFormatRectangleText())) {
                                displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganizationUnit().trim(), displayConfig.getOrganization().trim(), dateString);
                            } else {
                                displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), dateString, displayConfig.getReason(), displayConfig.getLocation());
                            }

                            Paragraph paragraph = new Paragraph(displayText, new Font(bf, (float)displayConfig.getSizeFont()));
                            paragraph.setAlignment(3);
                            ct.addElement(paragraph);
                            ct.go();
                        } else if (displayConfig.getTypeDisplay() == 5) {
                            PdfTemplate n2 = appearance.getLayer(2);
                            float x2 = n2.getBoundingBox().getLeft();
                            float y2 = n2.getBoundingBox().getBottom();
                            float w2 = n2.getBoundingBox().getWidth();
                            float h2 = n2.getBoundingBox().getHeight();
                            ColumnText ct = new ColumnText(n2);
                            ct.setSimpleColumn(x2, y2, w2, h2);
                            Font font = FontFactory.getFont("C:/windows/fonts/tahoma.ttf", "Identity-H", true, 0.8F, 0, BaseColor.BLACK);
                            BaseFont bf2 = font.getBaseFont();
                            Paragraph paragraph = new Paragraph("Signature valid", new Font(bf2, (float)(displayConfig.getSizeFont() + 2)));
                            paragraph.setAlignment(0);
                            paragraph.setSpacingAfter(5.0F);
                            ct.addElement(paragraph);
                            BaseFont bf = BaseFont.createFont(displayConfig.getFontPath(), "Identity-H", true);
                            String displayText = "";
                            if (displayConfig.getDisplayText() != null && !displayConfig.getDisplayText().isEmpty()) {
                                displayText = displayConfig.getDisplayText();
                            } else if ("Người Ký: %s\r\n%sNgày ký: %s".equals(displayConfig.getFormatRectangleText())) {
                                if (displayConfig.getOrganizationUnit() != null && !displayConfig.getOrganizationUnit().trim().isEmpty()) {
                                    displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganizationUnit().trim(), dateString);
                                } else {
                                    displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganization().trim(), dateString);
                                }
                            } else if ("Người Ký: %s\r\n%s\r\n%s\r\nNgày ký: %s".equals(displayConfig.getFormatRectangleText())) {
                                displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganizationUnit().trim(), displayConfig.getOrganization().trim(), dateString);
                            } else {
                                displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), dateString, displayConfig.getReason(), displayConfig.getLocation());
                            }

                            paragraph = new Paragraph(displayText, new Font(bf, (float)displayConfig.getSizeFont()));
                            paragraph.setAlignment(3);
                            ct.addElement(paragraph);
                            ct.go();
                            ColumnText ct2 = new ColumnText(n2);
                            ct2.setSimpleColumn(x2, y2, w2, h2);
                            Image image = Image.getInstance(displayConfig.getPathImage());
                            ct2.addElement(image);
                            ct2.go();
                        } else if (displayConfig.getTypeDisplay() == 4) {
                            Image image = Image.getInstance(displayConfig.getPathImage());
                            appearance.setSignatureGraphic(image);
                            appearance.setImageScale(-1.0F);
                            appearance.setRenderingMode(RenderingMode.GRAPHIC_AND_DESCRIPTION);
                            BaseFont bf = BaseFont.createFont(displayConfig.getFontPath(), "Identity-H", true);
                            String displayText = "";
                            if (displayConfig.getDisplayText() != null && !displayConfig.getDisplayText().isEmpty()) {
                                displayText = displayConfig.getDisplayText();
                            } else if ("Người Ký: %s\r\n%sNgày ký: %s".equals(displayConfig.getFormatRectangleText())) {
                                if (displayConfig.getOrganizationUnit() != null && !displayConfig.getOrganizationUnit().trim().isEmpty()) {
                                    displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganizationUnit().trim(), dateString);
                                } else {
                                    displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganization().trim(), dateString);
                                }
                            } else if ("Người Ký: %s\r\n%s\r\n%s\r\nNgày ký: %s".equals(displayConfig.getFormatRectangleText())) {
                                displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), displayConfig.getOrganizationUnit().trim(), displayConfig.getOrganization().trim(), dateString);
                            } else {
                                displayText = String.format(displayConfig.getFormatRectangleText(), displayConfig.getContact(), dateString, displayConfig.getReason(), displayConfig.getLocation());
                            }

                            appearance.setLayer2Text(displayText);
                            Font font = new Font(bf, 6.0F);
                            appearance.setLayer2Font(font);
                        }
                    } else if (displayConfig.getSignType() == 1) {
                        appearance.setVisibleSignature(new Rectangle(0.0F, 0.0F, 0.0F, 0.0F), 1, fieldName);
                    } else {
                        appearance.setVisibleSignature(fieldName);
                    }

                    ExternalDigest digest = new BouncyCastleDigest();
                    ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
                    TSAClient tsc = null;
                    if (timestampConfig != null && timestampConfig.isUseTimestamp()) {
                        tsc = new TSAClientBouncyCastle(timestampConfig.getTsa_url(), timestampConfig.getTsa_acc(), timestampConfig.getTsa_pass());
                    }

                    this.signDetached(key, appearance, digest, signature, chain, (Collection)null, (OcspClient)null, tsc, 0, subfilter, cryptAlgorithm);
                    boolean var70 = true;
                    return var70;
                } catch (DocumentException | GeneralSecurityException | IOException ex) {
                    log.error(((Exception)ex).getMessage(), ex);
                    return false;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    return false;
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }

                    if (reader != null) {
                        reader.close();
                    }

                }
            } else {
                log.error("Crypt Algorithm is invalid: " + cryptAlgorithm);
                return false;
            }
        } else {
            log.error("Digest Algorithm is invalid: " + digestAlgorithm);
            return false;
        }
    }

    private void signDetached(KeyStore.PrivateKeyEntry key, PdfSignatureAppearance sap, ExternalDigest externalDigest, ExternalSignature externalSignature, Certificate[] chain, Collection<CrlClient> crlList, OcspClient ocspClient, TSAClient tsaClient, int estimatedSize, MakeSignature.CryptoStandard sigtype, String cryptAlgorithm) throws IOException, DocumentException, GeneralSecurityException, Exception {
        Collection<byte[]> crlBytes = null;

        for(int i = 0; crlBytes == null && i < chain.length; crlBytes = processCrl(chain[i++], crlList)) {
        }

        if (estimatedSize == 0) {
            estimatedSize = 8192;
            if (crlBytes != null) {
                for(byte[] element : crlBytes) {
                    estimatedSize += element.length + 10;
                }
            }

            if (ocspClient != null) {
                estimatedSize += 4192;
            }

            if (tsaClient != null) {
                estimatedSize += 4192;
            }
        }

        sap.setCertificate(chain[0]);
        PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, sigtype == CryptoStandard.CADES ? PdfName.ETSI_CADES_DETACHED : PdfName.ADBE_PKCS7_DETACHED);
        dic.setReason(sap.getReason());
        dic.setLocation(sap.getLocation());
        dic.setContact(sap.getContact());
        dic.setDate(new PdfDate(sap.getSignDate()));
        sap.setCryptoDictionary(dic);
        HashMap<PdfName, Integer> exc = new HashMap();
        exc.put(PdfName.CONTENTS, new Integer(estimatedSize * 2 + 2));
        sap.preClose(exc);
        String hashAlgorithm = externalSignature.getHashAlgorithm();
        PdfPKCS7 sgn = new PdfPKCS7((PrivateKey)null, chain, hashAlgorithm, (String)null, externalDigest, false);
        InputStream data = sap.getRangeStream();
        byte[] hash = DigestAlgorithms.digest(data, externalDigest.getMessageDigest(hashAlgorithm));
        Calendar cal = Calendar.getInstance();
        byte[] ocsp = null;
        if (chain.length >= 2 && ocspClient != null) {
            ocsp = ocspClient.getEncoded((X509Certificate)chain[0], (X509Certificate)chain[1], (String)null);
        }

        byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal, ocsp, crlBytes, sigtype);
        byte[] extSignature = signDigest(sh, key.getPrivateKey(), externalSignature.getEncryptionAlgorithm(), cryptAlgorithm);
        sgn.setExternalDigest(extSignature, (byte[])null, externalSignature.getEncryptionAlgorithm());
        byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal, tsaClient, ocsp, crlBytes, sigtype);
        if (estimatedSize < encodedSig.length) {
            throw new IOException("Not enough space");
        } else {
            byte[] paddedSig = new byte[estimatedSize];
            System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
            PdfDictionary dic2 = new PdfDictionary();
            dic2.put(PdfName.CONTENTS, (new PdfString(paddedSig)).setHexWriting(true));
            sap.close(dic2);
            data.close();
        }
    }

    public static Collection<byte[]> processCrl(Certificate cert, Collection<CrlClient> crlList) {
        if (crlList == null) {
            return null;
        } else {
            ArrayList<byte[]> crlBytes = new ArrayList();

            for(CrlClient cc : crlList) {
                if (cc != null) {
                    Collection<byte[]> b = cc.getEncoded((X509Certificate)cert, (String)null);
                    if (b != null) {
                        crlBytes.addAll(b);
                    }
                }
            }

            if (crlBytes.isEmpty()) {
                return null;
            } else {
                return crlBytes;
            }
        }
    }

    public static byte[] signDigest(byte[] hash, PrivateKey privateKey, String digestAlgorithm, String cryptAlgorithm) throws Exception {
        if (hash == null) {
            throw new NullPointerException("hash digest is not null");
        } else {
            String algorithm;
            if ("SHA-1".equals(digestAlgorithm)) {
                algorithm = "SHA1with" + cryptAlgorithm;
            } else if ("SHA-256".equals(digestAlgorithm)) {
                algorithm = "SHA256with" + cryptAlgorithm;
            } else if ("SHA-384".equals(digestAlgorithm)) {
                algorithm = "SHA384with" + cryptAlgorithm;
            } else if ("SHA-512".equals(digestAlgorithm)) {
                algorithm = "SHA512with" + cryptAlgorithm;
            } else {
                algorithm = "SHA1withRSA";
            }

            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(privateKey);
            sig.update(hash);
            return sig.sign();
        }
    }
}
