package com.hvnh;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.*;

public class ThuyHoa {

    public static void main(String[] args) throws Exception {
        String inputPath = "input.docx";
        String outputFolder = "output_pages/";
        new File(outputFolder).mkdirs();

        try (XWPFDocument srcDoc = new XWPFDocument(OPCPackage.open(inputPath))) {
            List<List<IBodyElement>> pages = splitByPageBreak(srcDoc);

            // Group every 10 pages
            List<List<IBodyElement>> grouped = new ArrayList<>();
            List<IBodyElement> buffer = new ArrayList<>();
            int count = 0;
            for (List<IBodyElement> page : pages) {
                buffer.addAll(page);
                if (++count == 10) {
                    grouped.add(new ArrayList<>(buffer));
                    buffer.clear();
                    count = 0;
                }
            }
            if (!buffer.isEmpty()) grouped.add(buffer);

            int part = 1;
            for (List<IBodyElement> group : grouped) {
                XWPFDocument newDoc = createPerfectClone(srcDoc, group);

                // Save as DOCX
                String docxPath = outputFolder + "part_" + part + ".docx";
                try (FileOutputStream fos = new FileOutputStream(docxPath)) {
                    newDoc.write(fos);
                }

                // Convert to HTML
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                newDoc.write(baos);
                byte[] htmlBytes = Hehe.convertDocToHtml(baos.toByteArray());

                // Save HTML file
                String htmlPath = outputFolder + "part_" + part + ".html";
                try (FileOutputStream fosHtml = new FileOutputStream(htmlPath)) {
                    fosHtml.write(htmlBytes);
                }

                newDoc.close();
                System.out.println("Created: " + docxPath + " and " + htmlPath);
                part++;
            }
            System.out.println("Split and conversion completed. Total parts: " + grouped.size());
        }
    }

    /**
     * Creates a 100% identical document containing only the specified body elements.
     */
    private static XWPFDocument createPerfectClone(XWPFDocument src, List<IBodyElement> keepElements) throws Exception {
        // 1. Full deep clone via serialization
        XWPFDocument clone = cloneViaSerialization(src);

        // 2. Remove ALL body elements using the correct method
        while (clone.getBodyElements().size() > 0) {
            clone.removeBodyElement(0);  // This method exists on XWPFDocument
        }

        // 3. Re-add only the desired elements
        for (IBodyElement elem : keepElements) {
            if (elem instanceof XWPFParagraph para) {
                CTP ctp = (CTP) para.getCTP().copy();
                clone.getDocument().getBody().addNewP().set(ctp);
            } else if (elem instanceof XWPFTable table) {
                CTTbl cttbl = (CTTbl) table.getCTTbl().copy();
                clone.getDocument().getBody().addNewTbl().set(cttbl);
            }
        }

        return clone;
    }

    /**
     * Clones the entire DOCX package perfectly via write → read.
     */
    private static XWPFDocument cloneViaSerialization(XWPFDocument src) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            src.write(baos);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                OPCPackage pkg = OPCPackage.open(bais);
                return new XWPFDocument(pkg);
            }
        }
    }

    // ────────────────────────────── PAGE SPLIT LOGIC ──────────────────────────────

    private static List<List<IBodyElement>> splitByPageBreak(XWPFDocument doc) {
        List<List<IBodyElement>> pages = new ArrayList<>();
        List<IBodyElement> current = new ArrayList<>();

        for (IBodyElement elem : doc.getBodyElements()) {
            current.add(elem);
            if (containsPageOrSectionBreak(elem)) {
                pages.add(new ArrayList<>(current));
                current.clear();
            }
        }
        if (!current.isEmpty()) pages.add(current);
        return pages;
    }

    private static boolean containsPageOrSectionBreak(IBodyElement elem) {
        if (elem instanceof XWPFParagraph para) {
            // Page break in run
            for (XWPFRun run : para.getRuns()) {
                for (CTBr br : run.getCTR().getBrList()) {
                    if (br.getType() == STBrType.PAGE) {
                        return true;
                    }
                }
            }
            // Section break in paragraph properties
            CTPPr pPr = para.getCTP().getPPr();
            if (pPr != null && pPr.getSectPr() != null) {
                return true;
            }
        }
        return false;
    }
}