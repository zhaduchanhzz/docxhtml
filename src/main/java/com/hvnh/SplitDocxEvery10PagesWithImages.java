package com.hvnh;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.*;

public class SplitDocxEvery10PagesWithImages {

    public static void main(String[] args) throws Exception {
        String inputPath = "input.docx";
        String outputFolder = "output_pages/";

        new File(outputFolder).mkdirs();

        try (FileInputStream fis = new FileInputStream(inputPath);
             XWPFDocument document = new XWPFDocument(OPCPackage.open(fis))) {

            // Step 1: Split by manual or section page breaks
            List<List<IBodyElement>> pages = splitByPageBreak(document);

            // Step 2: Group every 10 "pages" together
            List<List<IBodyElement>> grouped = new ArrayList<>();
            List<IBodyElement> currentGroup = new ArrayList<>();
            int pageCount = 0;

            for (List<IBodyElement> page : pages) {
                currentGroup.addAll(page);
                pageCount++;
                if (pageCount == 10) {
                    grouped.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                    pageCount = 0;
                }
            }
            if (!currentGroup.isEmpty()) grouped.add(new ArrayList<>(currentGroup));

            // Step 3: Write each group as a new .docx file
            int index = 1;
            for (List<IBodyElement> group : grouped) {
                XWPFDocument newDoc = new XWPFDocument();

                for (IBodyElement elem : group) {
                    if (elem instanceof XWPFParagraph p) {
                        copyParagraphWithImages(p, document, newDoc);
                    } else if (elem instanceof XWPFTable t) {
                        copyTableWithImages(t, document, newDoc);
                    }
                }

                String outPath = outputFolder + "part_" + index + ".docx";
                try (FileOutputStream out = new FileOutputStream(outPath)) {
                    newDoc.write(out);
                }
                newDoc.close();
                System.out.println("✅ Created: " + outPath);
                index++;
            }

            System.out.println("✅ Split completed. Total parts: " + grouped.size());
        }
    }

    /**
     * Split the document into pages based on manual or section breaks.
     */
    private static List<List<IBodyElement>> splitByPageBreak(XWPFDocument doc) {
        List<List<IBodyElement>> pages = new ArrayList<>();
        List<IBodyElement> current = new ArrayList<>();

        for (IBodyElement element : doc.getBodyElements()) {
            current.add(element);
            if (containsPageOrSectionBreak(element)) {
                pages.add(new ArrayList<>(current));
                current.clear();
            }
        }
        if (!current.isEmpty()) pages.add(new ArrayList<>(current));
        return pages;
    }

    /**
     * Detects manual or section breaks.
     */
    private static boolean containsPageOrSectionBreak(IBodyElement element) {
        if (element instanceof XWPFParagraph para) {
            for (XWPFRun run : para.getRuns()) {
                for (CTBr br : run.getCTR().getBrList()) {
                    if (br.getType() != null && br.getType() == STBrType.PAGE) {
                        return true;
                    }
                }
            }
            if (para.getCTP().getPPr() != null && para.getCTP().getPPr().getSectPr() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copy a paragraph with inline images and text styles.
     */
    private static void copyParagraphWithImages(XWPFParagraph srcPara, XWPFDocument srcDoc, XWPFDocument newDoc)
            throws Exception {

        XWPFParagraph destPara = newDoc.createParagraph();
        destPara.getCTP().setPPr(srcPara.getCTP().getPPr()); // copy paragraph properties

        for (XWPFRun srcRun : srcPara.getRuns()) {
            XWPFRun newRun = destPara.createRun();
            // Copy text style
            newRun.getCTR().setRPr(srcRun.getCTR().getRPr());
            // Copy text content
            if (srcRun.text() != null && !srcRun.text().isEmpty()) {
                newRun.setText(srcRun.text());
            }

            // Copy inline pictures
            for (XWPFPicture pic : srcRun.getEmbeddedPictures()) {
                XWPFPictureData picData = pic.getPictureData();
                if (picData != null) {
                    byte[] data = picData.getData();
                    int picType = picData.getPictureType();

                    // Add the picture to the new document and create the new drawing
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
                        newRun.addPicture(
                                (InputStream) bis,
                                picType,
                                picData.getFileName(),
                                (int) pic.getCTPicture().getSpPr().getXfrm().getExt().getCx(),
                                (int) pic.getCTPicture().getSpPr().getXfrm().getExt().getCy()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Copy tables, including images within table paragraphs.
     */
    private static void copyTableWithImages(XWPFTable srcTable, XWPFDocument srcDoc, XWPFDocument newDoc)
            throws Exception {

        XWPFTable newTable = newDoc.createTable();
        newTable.getCTTbl().setTblPr(srcTable.getCTTbl().getTblPr());
        newTable.removeRow(0); // remove default empty row

        for (XWPFTableRow srcRow : srcTable.getRows()) {
            XWPFTableRow newRow = newTable.createRow();
            for (int i = 0; i < srcRow.getTableCells().size(); i++) {
                XWPFTableCell srcCell = srcRow.getCell(i);
                XWPFTableCell newCell = newRow.getCell(i);
                if (newCell == null) newCell = newRow.addNewTableCell();

                newCell.getCTTc().setTcPr(srcCell.getCTTc().getTcPr());
                for (IBodyElement cellElem : srcCell.getBodyElements()) {
                    if (cellElem instanceof XWPFParagraph p) {
                        copyParagraphWithImages(p, srcDoc, newCell.getXWPFDocument());
                    } else if (cellElem instanceof XWPFTable t) {
                        copyTableWithImages(t, srcDoc, newCell.getXWPFDocument());
                    }
                }
            }
        }
    }
}
