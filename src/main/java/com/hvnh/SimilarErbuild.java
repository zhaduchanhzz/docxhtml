package com.hvnh;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.*;

public class SimilarErbuild {

    public static void main(String[] args) throws Exception {
        String inputPath = "input.docx";
        String outputFolder = "output_pages/";

        new File(outputFolder).mkdirs();

        try (FileInputStream fis = new FileInputStream(inputPath);
             XWPFDocument document = new XWPFDocument(OPCPackage.open(fis))) {

            // Step 1: Split document into "pages" (based on manual/section breaks)
            List<List<IBodyElement>> pages = splitByPageBreak(document);

            // Step 2: Group every 10 pages together
            List<List<IBodyElement>> grouped = new ArrayList<>();
            List<IBodyElement> temp = new ArrayList<>();
            int count = 0;

            for (List<IBodyElement> page : pages) {
                temp.addAll(page);
                count++;
                if (count == 10) {
                    grouped.add(new ArrayList<>(temp));
                    temp.clear();
                    count = 0;
                }
            }
            if (!temp.isEmpty()) grouped.add(temp);

            // Step 3: Write each group as a separate .docx
            int part = 1;
            for (List<IBodyElement> group : grouped) {
                XWPFDocument newDoc = new XWPFDocument();
                CTSectPr sectPr = document.getDocument().getBody().getSectPr();
                if (sectPr != null) {
                    newDoc.getDocument().getBody().setSectPr((CTSectPr) sectPr.copy());
                }

                for (IBodyElement elem : group) {
                    copyElement(elem, newDoc);
                }

                String outPath = outputFolder + "part_" + part + ".docx";
                try (FileOutputStream fos = new FileOutputStream(outPath)) {
                    newDoc.write(fos);
                }
                newDoc.close();
                System.out.println("✅ Created: " + outPath);
                part++;
            }

            System.out.println("✅ Split completed. Total parts: " + grouped.size());
        }
    }

    /**
     * Split the document by manual or section page breaks.
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

    private static boolean containsPageOrSectionBreak(IBodyElement element) {
        if (element instanceof XWPFParagraph para) {
            for (XWPFRun run : para.getRuns()) {
                for (CTBr br : run.getCTR().getBrList()) {
                    if (br.getType() == STBrType.PAGE) {
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
     * Copy a full paragraph/table as raw XML — preserves full formatting.
     */
    private static void copyElement(IBodyElement elem, XWPFDocument newDoc) {
        if (elem instanceof XWPFParagraph para) {
            // Deep copy of paragraph
            CTP newCTP = (CTP) para.getCTP().copy();
            newDoc.getDocument().getBody().addNewP().set(newCTP);
        } else if (elem instanceof XWPFTable table) {
            // Deep copy of table
            CTTbl newCTTbl = (CTTbl) table.getCTTbl().copy();
            newDoc.getDocument().getBody().addNewTbl().set(newCTTbl);
        }
    }

}
