package com.hvnh;

import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.*;

public class SplitDoc {

    public static void main(String[] args) throws Exception {
        String inputPath = "input.docx";
        String outputFolder = "output_pages/";
        new File(outputFolder).mkdirs();

        try (XWPFDocument srcDoc = new XWPFDocument(OPCPackage.open(inputPath))) {

            // --- Split document by page break ---
            List<List<IBodyElement>> pages = splitByPageBreak(srcDoc);

            // --- Group every 8 pages ---
            List<List<IBodyElement>> grouped = new ArrayList<>();
            List<IBodyElement> buffer = new ArrayList<>();
            int cnt = 0;
            for (List<IBodyElement> p : pages) {
                buffer.addAll(p);
                if (++cnt == 8) {
                    removeLastPageBreak(buffer);
                    grouped.add(new ArrayList<>(buffer));
                    buffer.clear();
                    cnt = 0;
                }
            }
            if (!buffer.isEmpty()) grouped.add(buffer);

            // --- Detect TOC block(s) ---
            List<CTSdtBlock> tocBlocks = extractTOCBlocks(srcDoc);
            int tocGroupIndex = findTocGroupIndex(pages, tocBlocks);

            System.out.println("Detected TOC in group index: " + tocGroupIndex);

            int part = 1;
            for (int i = 0; i < grouped.size(); i++) {
                List<IBodyElement> g = grouped.get(i);

                XWPFDocument newDoc = new XWPFDocument();
                copyNumbering(srcDoc, newDoc);
                copyDocumentSettings(srcDoc, newDoc);

                // ✅ Only copy TOC to the part that originally contained it
                if (i == tocGroupIndex && !tocBlocks.isEmpty()) {
                    for (CTSdtBlock sdt : tocBlocks) {
                        newDoc.getDocument().getBody().addNewSdt().set(sdt.copy());
                    }
                    System.out.println("→ TOC copied to part_" + part + ".docx");
                }

                // Copy the main content
                copyBodyElements(srcDoc, newDoc, g);

                // Save DOCX
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

    // ------------------------------------------------------------
    private static void removeLastPageBreak(List<IBodyElement> buffer) {
        if (buffer.isEmpty()) return;
        IBodyElement last = buffer.get(buffer.size() - 1);
        if (last instanceof XWPFParagraph para) {
            for (XWPFRun r : para.getRuns()) {
                r.getCTR().getBrList().removeIf(br -> br.getType() == STBrType.PAGE);
            }
            CTPPr pPr = para.getCTP().getPPr();
            if (pPr != null && pPr.getSectPr() != null) pPr.unsetSectPr();
            if (para.getRuns().isEmpty() || para.getText().isBlank()) buffer.remove(buffer.size() - 1);
        }
    }

    // ------------------------------------------------------------
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
            for (XWPFRun r : p.getRuns()) {
                for (CTBr br : r.getCTR().getBrList()) {
                    if (br.getType() == STBrType.PAGE) return true;
                }
            }
            CTPPr pp = p.getCTP().getPPr();
            if (pp != null && pp.getSectPr() != null) return true;
        }
        return false;
    }

    // ------------------------------------------------------------
    private static void copyNumbering(XWPFDocument src, XWPFDocument dst) throws Exception {
        if (src.getNumbering() != null) {
            XWPFNumbering srcNumbering = src.getNumbering();
            XWPFNumbering dstNumbering = dst.createNumbering();
            for (XWPFNum num : srcNumbering.getNums()) {
                dstNumbering.addNum(num);
            }
        }
    }

    private static void copyDocumentSettings(XWPFDocument src, XWPFDocument dst) {
        if (src.getStyles() != null) {
            CTStyles ctStyles = src.getStyles().getCtStyles();
            if (ctStyles != null) dst.createStyles().setStyles((CTStyles) ctStyles.copy());
        }
        if (src.getDocument().getBody().getSectPr() != null) {
            CTSectPr sectPr = (CTSectPr) src.getDocument().getBody().getSectPr().copy();
            dst.getDocument().getBody().setSectPr(sectPr);
        }
    }

    // ------------------------------------------------------------
    private static void copyBodyElements(XWPFDocument src, XWPFDocument dst,
                                         List<IBodyElement> elems) throws Exception {
        for (IBodyElement e : elems) {
            if (e instanceof XWPFParagraph p) {
                dst.getDocument().getBody().addNewP().set((CTP) p.getCTP().copy());
            } else if (e instanceof XWPFTable t) {
                dst.getDocument().getBody().addNewTbl().set((CTTbl) t.getCTTbl().copy());
            } else if (e instanceof CTSdtBlock sdt) {
                dst.getDocument().getBody().addNewSdt().set(sdt.getSdtContent().copy());
            }
        }
        cloneAllImageParts(src, dst);
    }

    private static void cloneAllImageParts(XWPFDocument src, XWPFDocument dst) throws Exception {
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
                    rId);
        }
    }

    // ------------------------------------------------------------
    private static List<CTSdtBlock> extractTOCBlocks(XWPFDocument doc) {
        List<CTSdtBlock> tocBlocks = new ArrayList<>();

        CTBody body = doc.getDocument().getBody();
        if (body == null) return tocBlocks;
        System.out.println(doc.getDocument().getBody().getSdtList().size());
        for (CTSdtBlock sdt : body.getSdtList()) {
            if (sdt.getSdtPr() != null && sdt.getSdtPr().getDocPartObj() != null) {
                CTString gallery = sdt.getSdtPr().getDocPartObj().getDocPartGallery();
                if (gallery != null ) {
                    tocBlocks.add(sdt);
                }
            }
        }

        return tocBlocks;
    }

    private static int findTocGroupIndex(List<List<IBodyElement>> groups, List<CTSdtBlock> tocBlocks) {
        if (tocBlocks.isEmpty()) return -1;

        Set<CTSdtBlock> tocSet = new HashSet<>(tocBlocks);
        for (int i = 0; i < groups.size(); i++) {
            for (IBodyElement e : groups.get(i)) {
                if (e instanceof CTSdtBlock sdt && tocSet.contains(sdt)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
