package com.hvnh;

import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.*;

public class SplitDocxByPageBreak_FullCopy {

    public static void main(String[] args) throws Exception {
        String inputPath = "input.docx";
        String outputFolder = "output_pages/";
        new File(outputFolder).mkdirs();

        try (XWPFDocument srcDoc = new XWPFDocument(OPCPackage.open(inputPath))) {
            List<List<IBodyElement>> pages = splitByPageBreak(srcDoc);

            // ---- group every 10 pages ----
            List<List<IBodyElement>> grouped = new ArrayList<>();
            List<IBodyElement> buffer = new ArrayList<>();
            int cnt = 0;
            for (List<IBodyElement> p : pages) {
                buffer.addAll(p);
                if (++cnt == 10) {
                    grouped.add(new ArrayList<>(buffer));
                    buffer.clear();
                    cnt = 0;
                }
            }
            if (!buffer.isEmpty()) grouped.add(buffer);

            int part = 1;
            for (List<IBodyElement> g : grouped) {
                XWPFDocument newDoc = new XWPFDocument();

                copyDocumentSettings(srcDoc, newDoc);
                copyBodyElements(srcDoc, newDoc, g);   // <-- clones images

                String out = outputFolder + "part_" + part + ".docx";
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    newDoc.write(fos);
                }
                newDoc.close();
                System.out.println("Created: " + out);
                part++;
            }
            System.out.println("Split completed. Parts: " + grouped.size());
        }
    }

    /* ---- unchanged splitByPageBreak / containsPageOrSectionBreak ---- */
    private static List<List<IBodyElement>> splitByPageBreak(XWPFDocument doc) {
        List<List<IBodyElement>> res = new ArrayList<>();
        List<IBodyElement> cur = new ArrayList<>();
        for (IBodyElement e : doc.getBodyElements()) {
            cur.add(e);
            if (containsPageOrSectionBreak(e)) {
                res.add(new ArrayList<>(cur));
                cur.clear();
            }
        }
        if (!cur.isEmpty()) res.add(cur);
        return res;
    }

    private static boolean containsPageOrSectionBreak(IBodyElement e) {
        if (e instanceof XWPFParagraph p) {
            for (XWPFRun r : p.getRuns())
                for (CTBr br : r.getCTR().getBrList())
                    if (br.getType() == STBrType.PAGE) return true;
            CTPPr pp = p.getCTP().getPPr();
            if (pp != null && pp.getSectPr() != null) return true;
        }
        return false;
    }

    /* ---- copy settings ------------------------------------------------ */
    private static void copyDocumentSettings(XWPFDocument src, XWPFDocument dst) {
        // === COPY STYLES ===
        if (src.getStyles() != null) {
            CTStyles ctStyles = src.getStyles().getCtStyles(); // This is the real CTStyles
            if (ctStyles != null) {
                dst.createStyles().setStyles((CTStyles) ctStyles.copy());
            }
        }

        // === COPY SECTION PROPERTIES ===
        if (src.getDocument().getBody().getSectPr() != null) {
            CTSectPr sectPr = (CTSectPr) src.getDocument().getBody().getSectPr().copy();
            dst.getDocument().getBody().setSectPr(sectPr);
        }
    }

    /* ---- copy body + clone images ------------------------------------ */
    private static void copyBodyElements(XWPFDocument src, XWPFDocument dst,
                                         List<IBodyElement> elems) throws Exception {
        for (IBodyElement e : elems) {
            if (e instanceof XWPFParagraph p) {
                dst.getDocument().getBody().addNewP().set((CTP) p.getCTP().copy());
            } else if (e instanceof XWPFTable t) {
                dst.getDocument().getBody().addNewTbl().set((CTTbl) t.getCTTbl().copy());
            }
        }
        cloneAllImageParts(src, dst);
    }

    private static void cloneAllImageParts(XWPFDocument src, XWPFDocument dst)
            throws Exception {
        PackagePart srcDocPart = src.getPackagePart();
        for (PackageRelationship rel : srcDocPart.getRelationshipsByType(
                XWPFRelation.IMAGES.getRelation())) {

            String rId = rel.getId();
            PackagePart srcImg = srcDocPart.getRelatedPart(rel);
            byte[] data = srcImg.getInputStream().readAllBytes();

            PackagePart dstDocPart = dst.getPackagePart();
            PackagePart dstImg = dstDocPart.getPackage().createPart(
                    srcImg.getPartName(), srcImg.getContentType());

            try (OutputStream out = dstImg.getOutputStream()) {
                out.write(data);
            }

            dstDocPart.addRelationship(dstImg.getPartName(),
                    TargetMode.INTERNAL,
                    XWPFRelation.IMAGES.getRelation(),
                    rId);               // <-- keep original rId
        }
    }
}