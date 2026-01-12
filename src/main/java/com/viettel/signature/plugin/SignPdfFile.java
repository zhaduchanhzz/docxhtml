//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.viettel.signature.plugin;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.viettel.signature.pdf.DisplayConfig;
import com.viettel.signature.pdf.SignPdfAsynchronous;
import com.viettel.signature.pdf.TimestampConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.utils.Base64;

@Slf4j
public class SignPdfFile {
    private String tmpFile;
    private Date signDate;
    private byte[] hash;
    private Certificate[] chain;
    private String fieldName;
    private String digestAlgorithm = "SHA1";
    private String cryptAlgorithm = "RSA";

    public SignPdfFile() {
    }

    public String createHash(String filePath, Certificate[] chain, String digestAlg, String cryptAlg, DisplayConfig displayConfig) {
        if (digestAlg != null && !digestAlg.trim().isEmpty() && ("SHA1".equals(digestAlg) || "SHA256".equals(digestAlg) || "SHA384".equals(digestAlg) || "SHA512".equals(digestAlg))) {
            if (cryptAlg != null && !cryptAlg.trim().isEmpty() && ("RSA".equals(cryptAlg) || "DSA".equals(cryptAlg))) {
                this.digestAlgorithm = digestAlg;
                this.cryptAlgorithm = cryptAlg;

                try {
                    SignPdfAsynchronous pdfSig = new SignPdfAsynchronous();
                    File tempFile = File.createTempFile("temp", ".pdf");
                    SimpleDateFormat dateFormat = new SimpleDateFormat(displayConfig.getDateFormatString());
                    String dateString = dateFormat.format(displayConfig.getSignDate());
                    this.fieldName = displayConfig.getFieldName();
                    if (this.fieldName == null || this.fieldName.trim().isEmpty()) {
                        this.fieldName = displayConfig.getContact().replaceAll("\\.", " ") + "_" + dateString;
                    }

                    if (displayConfig.getSignDate() == null) {
                        Date signDateNow = new Date();
                        displayConfig.setSignDate(signDateNow);
                        this.signDate = signDateNow;
                    } else {
                        this.signDate = displayConfig.getSignDate();
                    }

                    List<byte[]> lstHash = pdfSig.createHash(filePath, tempFile.getAbsolutePath(), this.fieldName, digestAlg, chain, displayConfig);
                    if (lstHash == null) {
                        return null;
                    } else {
                        this.tmpFile = tempFile.getAbsolutePath();
                        this.hash = (byte[])lstHash.get(1);
                        this.chain = chain;
                        return Base64.encode(this.encodeData((byte[])lstHash.get(0), this.digestAlgorithm));
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    return null;
                }
            } else {
                log.error("Crypt Algorithm is invalid: " + cryptAlg);
                return null;
            }
        } else {
            log.error("Digest Algorithm is invalid: " + digestAlg);
            return null;
        }
    }

    public boolean insertSignature(String extSig, String destFile, TimestampConfig timestampConfig) {
        try {
            SignPdfAsynchronous pdfSig = new SignPdfAsynchronous();
            File fileTemp = new File(this.tmpFile);
            if (pdfSig.insertSignature(this.tmpFile, destFile, this.fieldName, this.digestAlgorithm, this.cryptAlgorithm, this.hash, Base64.decode(extSig), this.chain, this.signDate, timestampConfig)) {
                if (fileTemp.exists()) {
                    fileTemp.delete();
                }

                return true;
            } else {
                if (fileTemp.exists()) {
                    fileTemp.delete();
                }

                return false;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    private byte[] encodeData(byte[] orginalData, String algorithm) throws Exception {
        return MessageDigest.getInstance(algorithm).digest(orginalData);
    }

    public void addPageEmpty(String src, String dest, DisplayConfig config) {
        PdfReader reader = null;
        FileOutputStream os = null;

        try {
            reader = new PdfReader(src);
            os = new FileOutputStream(dest);
            PdfStamper stamper = new PdfStamper(reader, os);

            for(int i = 0; i < config.getNumberPageSign(); ++i) {
                stamper.insertPage(i, config.getPageSize());
            }

            PdfContentByte content = stamper.getUnderContent(1);
            float heightPage = config.getPageSize().getHeight();
            float widthPage = config.getPageSize().getWidth();
            float x = config.getMarginRightOfTable();
            float y = heightPage - config.getMarginTopOfTable() - config.getHeightTitle();
            float h = config.getHeightTitle();
            float w = widthPage - config.getMarginRightOfTable() * 2.0F;
            BaseFont bf = BaseFont.createFont(config.getFontPath(), "Identity-H", true);
            if (config.isIsDisplayTitlePageSign()) {
                ColumnText ct = new ColumnText(content);
                ct.setSimpleColumn(x, y + config.getHeightTitle(), x + w, y + config.getHeightTitle() + config.getHeightRowTitlePageSign());
                Paragraph paragraph = new Paragraph(config.getTitlePageSign(), new Font(bf, (float)config.getFontSizeTitlePageSign(), 1));
                paragraph.setAlignment(1);
                ct.addElement(paragraph);
                ct.go();
            }

            PdfPTable tableMainHeader = new PdfPTable(config.getWidthsPercen().length);
            tableMainHeader.setWidths(config.getWidthsPercen());
            tableMainHeader.setWidthPercentage(100.0F);

            for(String title : config.getTitles()) {
                Paragraph paragraph = new Paragraph(title, new Font(bf, (float)config.getSizeFont(), 1));
                paragraph.setAlignment(1);
                PdfPCell cell = new PdfPCell();
                cell.addElement(paragraph);
                cell.setFixedHeight(config.getHeightTitle());
                cell.setBackgroundColor(config.getBackgroundColorTitle());
                tableMainHeader.addCell(cell);
            }

            ColumnText ct = new ColumnText(content);
            ct.setSimpleColumn(x, y, x + w, y + config.getHeightTitle());
            ct.addElement(tableMainHeader);
            ct.go();
            stamper.close();
            reader.close();
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage(), ex);
        } catch (DocumentException | IOException ex) {
            log.error(((Exception)ex).getMessage(), ex);
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

    public String getTmpFile() {
        return this.tmpFile;
    }

    public void setTmpFile(String tmpFile) {
        this.tmpFile = tmpFile;
    }

    public Date getSignDate() {
        return this.signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public byte[] getHash() {
        return this.hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public Certificate[] getChain() {
        return this.chain;
    }

    public void setChain(Certificate[] chain) {
        this.chain = chain;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
