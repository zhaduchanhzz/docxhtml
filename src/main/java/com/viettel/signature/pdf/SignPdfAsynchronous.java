
package com.viettel.signature.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.RASInputStream;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.viettel.signature.utils.CertUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Slf4j
public class SignPdfAsynchronous {
    public static final String CRYPT_ALGORITHM_RSA = "RSA";
    public static final String CRYPT_ALGORITHM_DSA = "DSA";
    public static final String HASH_ALGORITHM_SHA1 = "SHA1";
    public static final String HASH_ALGORITHM_SHA256 = "SHA256";
    public static final String HASH_ALGORITHM_SHA384 = "SHA384";
    public static final String HASH_ALGORITHM_SHA512 = "SHA512";

    public SignPdfAsynchronous() {
    }

    private boolean emptySignature(String src, String dest, String fieldName, DisplayConfig displayConfig, Certificate cert) {
        PdfReader reader = null;
        FileOutputStream os = null;

        try {
            BouncyCastleProvider providerBC = new BouncyCastleProvider();
            Security.addProvider(providerBC);
            reader = new PdfReader(src);
            os = new FileOutputStream(dest);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\u0000', (File)null, true);
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            Calendar cal = Calendar.getInstance();
            cal.setTime(displayConfig.getSignDate());
            if ("".equals(displayConfig.getContact())) {
                String cn = CertUtils.getCN((X509Certificate)cert);
                if (cn == null) {
                    log.error("Get CN of Certificate failed");
                    boolean var41 = false;
                    return var41;
                }

                displayConfig.setContact(cn);
            }

            appearance.setContact(displayConfig.getContact());
            appearance.setSignDate(cal);
            appearance.setReason(displayConfig.getReason());
            appearance.setLocation(displayConfig.getLocation());
            SimpleDateFormat dateFormat = new SimpleDateFormat(displayConfig.getDateFormatString());
            String dateString = dateFormat.format(displayConfig.getSignDate());
            if (!displayConfig.isIsDisplaySignature()) {
                if (displayConfig.getSignType() == 1) {
                    appearance.setVisibleSignature(new Rectangle(0.0F, 0.0F, 0.0F, 0.0F), 1, fieldName);
                } else {
                    appearance.setVisibleSignature(fieldName);
                    PdfTemplate n2 = appearance.getLayer(2);
                    float x2 = n2.getBoundingBox().getLeft();
                    float y2 = n2.getBoundingBox().getBottom();
                    float w2 = n2.getBoundingBox().getWidth();
                    float h2 = n2.getBoundingBox().getHeight();
                    ColumnText ct = new ColumnText(n2);
                    ct.setSimpleColumn(x2, y2, w2, h2);
                    BaseFont bf = BaseFont.createFont(displayConfig.getFontPath(), "Identity-H", true);
                    String displayText = null;
                    Paragraph paragraph = new Paragraph(displayText, new Font(bf, (float)displayConfig.getSizeFont()));
                    paragraph.setAlignment(0);
                    ct.addElement(paragraph);
                    ct.go();
                }
            } else {
                if (displayConfig.getSignType() == 1) {
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
                            case 4:
                            case 5:
                            default:
                                coorX = widthPage - marginRight - widthRectangle;
                                coorY = marginBottom;
                                break;
                            case 6:
                                coorX = (widthPage - widthRectangle) / 2.0F;
                                coorY = heightPage - marginTop - heightRectangle;
                        }
                    }

                    Rectangle rectangle = new Rectangle(coorX, coorY, coorX + widthRectangle, coorY + heightRectangle);
                    appearance.setVisibleSignature(rectangle, signPage, fieldName);
                } else {
                    appearance.setVisibleSignature(fieldName);
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
                    paragraph.setAlignment(0);
                    ct.addElement(paragraph);
                    ct.go();
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
            }

            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
            MakeSignature.signExternalContainer(appearance, external, 8192);
            reader.close();
            boolean var51 = true;
            return var51;
        } catch (DocumentException | GeneralSecurityException | IOException ex) {
            log.error(((Exception)ex).getMessage(), ex);
            boolean stamper = false;
            return stamper;
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
    }

    private boolean emptySignatureTable(String src, String dest, String fieldName, DisplayConfig displayConfig, Certificate cert) {
        PdfReader reader = null;
        FileOutputStream os = null;

        try {
            BouncyCastleProvider providerBC = new BouncyCastleProvider();
            Security.addProvider(providerBC);
            reader = new PdfReader(src);
            AcroFields fields = reader.getAcroFields();
            ArrayList<String> listSignature = fields.getSignatureNames();
            int numberSignPage = 1;
            float[] totalHeight = new float[displayConfig.getMaxPageSign()];

            for(String signame : listSignature) {
                List<AcroFields.FieldPosition> positions = fields.getFieldPositions(signame);
                int page = ((AcroFields.FieldPosition)positions.get(0)).page;
                if (page > numberSignPage) {
                    numberSignPage = page;
                }

                Rectangle rect = ((AcroFields.FieldPosition)positions.get(0)).position;
                float height = rect.getHeight();
                totalHeight[page] += height;
            }

            Rectangle psize = reader.getPageSize(numberSignPage);
            float heightPage = psize.getHeight();
            float widthPage = psize.getWidth();
            float x = displayConfig.getMarginRightOfTable();
            float w = widthPage - displayConfig.getMarginRightOfTable() * 2.0F;
            os = new FileOutputStream(dest);
            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\u0000', (File)null, true);
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            if ("".equals(displayConfig.getContact())) {
                displayConfig.setContact(CertUtils.getCN((X509Certificate)cert));
            }

            appearance.setContact(displayConfig.getContact());
            appearance.setReason(displayConfig.getReason());
            appearance.setLocation(displayConfig.getLocation());
            Calendar signDate = Calendar.getInstance();
            signDate.setTime(displayConfig.getSignDate());
            appearance.setSignDate(signDate);
            PdfPTable tableMainHeader = new PdfPTable(displayConfig.getWidthsPercen().length);
            tableMainHeader.setWidths(displayConfig.getWidthsPercen());
            tableMainHeader.setWidthPercentage(100.0F);
            tableMainHeader.setTotalWidth(w);
            BaseFont bf = BaseFont.createFont(displayConfig.getFontPath(), "Identity-H", true);

            for(int i = 0; i < displayConfig.getTextArray().length; ++i) {
                Paragraph paragraph = new Paragraph(displayConfig.getTextArray()[i], new Font(bf, (float)displayConfig.getSizeFont()));
                paragraph.setAlignment(displayConfig.getAlignmentArray()[i]);
                PdfPCell cell = new PdfPCell();
                cell.addElement(paragraph);
                tableMainHeader.addCell(cell);
            }

            float h = tableMainHeader.getTotalHeight();
            float y = heightPage - totalHeight[numberSignPage] - displayConfig.getMarginTopOfTable() - h - displayConfig.getHeightTitle();
            if (y < displayConfig.getMarginBottomOfTable()) {
                if (numberSignPage >= displayConfig.getTotalPageSign()) {
                    Rectangle rectangle = new Rectangle(0.0F, 0.0F, 0.0F, 0.0F);
                    appearance.setVisibleSignature(rectangle, numberSignPage, fieldName);
                    ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
                    MakeSignature.signExternalContainer(appearance, external, 8192);
                    reader.close();
                    boolean var59 = true;
                    return var59;
                }

                ++numberSignPage;
                y = heightPage - displayConfig.getMarginTopOfTable() - h - displayConfig.getHeightTitle();
            }

            Rectangle rectangle = new Rectangle(x, y, x + w, y + h);
            appearance.setVisibleSignature(rectangle, numberSignPage, fieldName);
            PdfTemplate layer0 = appearance.getLayer(0);
            float x1 = layer0.getBoundingBox().getLeft();
            float y1 = layer0.getBoundingBox().getBottom();
            float w1 = layer0.getBoundingBox().getWidth();
            float h1 = layer0.getBoundingBox().getHeight();
            PdfTemplate n2 = appearance.getLayer(2);
            ColumnText ct = new ColumnText(n2);
            ct.setSimpleColumn(x1, y1, x1 + w1, y1 + h1);
            ct.addElement(tableMainHeader);
            ct.go();
            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
            MakeSignature.signExternalContainer(appearance, external, 8192);
            boolean var34 = true;
            return var34;
        } catch (DocumentException | GeneralSecurityException | IOException ex) {
            log.error(((Exception)ex).getMessage(), ex);
            boolean fields = false;
            return fields;
        } finally {
            if (reader != null) {
                reader.close();
            }

            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }

        }
    }

    private List<byte[]> preSign(String src, String fieldName, String digestAlgorithm, Certificate[] chain, Date signDate) {
        PdfReader reader = null;

        Object b;
        try {
            List<byte[]> result = new ArrayList();
            reader = new PdfReader(src);
            AcroFields af = reader.getAcroFields();
            PdfDictionary v = af.getSignatureDictionary(fieldName);
            if (v != null) {
                PdfArray b = v.getAsArray(PdfName.BYTERANGE);
                long[] gaps = b.asLongArray();
                if (b.size() == 4 && gaps[0] == 0L) {
                    RandomAccessSource readerSource = reader.getSafeFile().createSourceView();
                    InputStream rg = new RASInputStream((new RandomAccessSourceFactory()).createRanged(readerSource, gaps));
                    BouncyCastleDigest digest = new BouncyCastleDigest();
                    PdfPKCS7 sgn = new PdfPKCS7((PrivateKey)null, chain, digestAlgorithm, (String)null, digest, false);
                    byte[] hash = DigestAlgorithms.digest(rg, digest.getMessageDigest(digestAlgorithm));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(signDate);
                    byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal, (byte[])null, (Collection)null, CryptoStandard.CMS);
                    result.add(sh);
                    result.add(hash);
                    Object var19 = result;
                    return (List<byte[]>)var19;
                }

                log.error("Single exclusion space supported");
                Object readerSource = null;
                return (List<byte[]>)readerSource;
            }

            log.error("No field");
            b = null;
        } catch (InvalidKeyException | NoSuchProviderException | NoSuchAlgorithmException | IOException ex) {
            log.error(((Exception)ex).getMessage(), ex);
            Object var27 = null;
            return (List<byte[]>)var27;
        } catch (GeneralSecurityException ex) {
            log.error(ex.getMessage(), ex);
            Object af = null;
            return (List<byte[]>)af;
        } finally {
            if (reader != null) {
                reader.close();
            }

        }

        return (List<byte[]>)b;
    }

    public List<byte[]> createHash(String src, String tempFile, String fieldName, String digestAlgorithm, Certificate[] chain, DisplayConfig displayConfig) {
        if (digestAlgorithm != null && !digestAlgorithm.trim().isEmpty() && ("SHA1".equals(digestAlgorithm) || "SHA256".equals(digestAlgorithm) || "SHA384".equals(digestAlgorithm) || "SHA512".equals(digestAlgorithm))) {
            if (displayConfig.getSignType() == 1) {
                if (displayConfig.getTypeDisplay() == 3) {
                    if (!this.emptySignatureTable(src, tempFile, fieldName, displayConfig, chain[0])) {
                        return null;
                    }
                } else if (!this.emptySignature(src, tempFile, fieldName, displayConfig, chain[0])) {
                    return null;
                }
            } else if (!this.emptySignature(src, tempFile, fieldName, displayConfig, chain[0])) {
                return null;
            }

            return this.preSign(tempFile, fieldName, digestAlgorithm, chain, displayConfig.getSignDate());
        } else {
            log.error("Digest Algorithm is invalid: " + digestAlgorithm);
            return null;
        }
    }

    public boolean insertSignature(String src, String dest, String fieldName, String digestAlgorithm, String cryptAlgorithm, byte[] hash, byte[] extSignature, Certificate[] chain, Date signDate, TimestampConfig timestampConfig) {
        if (digestAlgorithm != null && !digestAlgorithm.trim().isEmpty() && ("SHA1".equals(digestAlgorithm) || "SHA256".equals(digestAlgorithm) || "SHA384".equals(digestAlgorithm) || "SHA512".equals(digestAlgorithm))) {
            if (cryptAlgorithm != null && !cryptAlgorithm.trim().isEmpty() && ("RSA".equals(cryptAlgorithm) || "DSA".equals(cryptAlgorithm))) {
                PdfReader reader = null;
                FileOutputStream os = null;

                boolean b;
                try {
                    BouncyCastleProvider providerBC = new BouncyCastleProvider();
                    Security.addProvider(providerBC);
                    reader = new PdfReader(src);
                    os = new FileOutputStream(dest);
                    AcroFields af = reader.getAcroFields();
                    PdfDictionary v = af.getSignatureDictionary(fieldName);
                    if (v != null) {
                        if (!af.signatureCoversWholeDocument(fieldName)) {
                            log.error("Not the last signature");
                            b = false;
                            return b;
                        }

                        PdfArray b = v.getAsArray(PdfName.BYTERANGE);
                        long[] gaps = b.asLongArray();
                        if (b.size() == 4 && gaps[0] == 0L) {
                            RandomAccessSource readerSource = reader.getSafeFile().createSourceView();
                            BouncyCastleDigest digest = new BouncyCastleDigest();
                            PdfPKCS7 sgn = new PdfPKCS7((PrivateKey)null, chain, digestAlgorithm, (String)null, digest, false);
                            sgn.setExternalDigest(extSignature, (byte[])null, cryptAlgorithm);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(signDate);
                            TSAClient tsc = null;
                            if (timestampConfig.isUseTimestamp()) {
                                tsc = new TSAClientBouncyCastle(timestampConfig.getTsa_url(), timestampConfig.getTsa_acc(), timestampConfig.getTsa_pass());
                            }

                            byte[] signedContent = sgn.getEncodedPKCS7(hash, cal, tsc, (byte[])null, (Collection)null, CryptoStandard.CMS);
                            int spaceAvailable = (int)(gaps[2] - gaps[1]) - 2;
                            if ((spaceAvailable & 1) == 0) {
                                spaceAvailable /= 2;
                                if (spaceAvailable < signedContent.length) {
                                    log.error("Not enough space");
                                    boolean var51 = false;
                                    return var51;
                                }

                                StreamUtil.CopyBytes(readerSource, 0L, gaps[1] + 1L, os);
                                ByteBuffer bb = new ByteBuffer(spaceAvailable * 2);

                                for(byte bi : signedContent) {
                                    bb.appendHex(bi);
                                }

                                int remain = (spaceAvailable - signedContent.length) * 2;

                                for(int k = 0; k < remain; ++k) {
                                    bb.append((byte)48);
                                }

                                bb.writeTo(os);
                                StreamUtil.CopyBytes(readerSource, gaps[2] - 1L, gaps[3] + 1L, os);
                                bb.close();
                                boolean var54 = true;
                                return var54;
                            }

                            log.error("Gap is not a multiple of 2");
                            boolean bb = false;
                            return bb;
                        }

                        log.error("Single exclusion space supported");
                        boolean readerSource = false;
                        return readerSource;
                    }

                    log.error("No field");
                    b = false;
                } catch (InvalidKeyException | NoSuchProviderException | NoSuchAlgorithmException | IOException ex) {
                    log.error(((Exception)ex).getMessage(), ex);
                    boolean af = false;
                    return af;
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

                return b;
            } else {
                log.error("Crypt Algorithm is invalid: " + digestAlgorithm);
                return false;
            }
        } else {
            log.error("Digest Algorithm is invalid: " + digestAlgorithm);
            return false;
        }
    }
}
