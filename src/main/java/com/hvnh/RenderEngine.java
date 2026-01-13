//package com.hvnh;
//
//import com.spire.doc.CssStyleSheetType;
//import com.spire.doc.Document;
//import com.spire.doc.FileFormat;
//import org.apache.poi.openxml4j.opc.*;
//import org.apache.poi.xwpf.usermodel.*;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//
//import static com.hvnh.Hehe.removeSprdiv;
//
//public class RenderEngine {
//    static boolean pictureFlags = false;
//
//    public static void main(String[] args) throws Exception {
//        String inputPath = "file-sample_1MB.docx";
//        String outputFolder = "output_pages/";
//        String outputFile = "outputittrang.html";
//        String exportTYpe = "html";
//        new File(outputFolder).mkdirs();
//        List<String> htmlContents = new ArrayList<>();
//        try (XWPFDocument srcDoc = new XWPFDocument(OPCPackage.open(inputPath))) {
//
//            // --- Split document by page break ---
//            List<List<IBodyElement>> pages = splitByPageBreak(srcDoc);
//            if (pages.size() <= 10) {
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                srcDoc.write(baos);
//                htmlContents.add(convertDocToHtml(baos.toByteArray(),exportTYpe));
//                srcDoc.close();
//                mergeHtmlPages(htmlContents, outputFile);
//                return;
//            }
//            // --- Group every 8 pages ---
//            List<List<IBodyElement>> grouped = new ArrayList<>();
//            List<IBodyElement> buffer = new ArrayList<>();
//            int cnt = 0;
//            for (List<IBodyElement> p : pages) {
//                buffer.addAll(p);
//                for (IBodyElement e : p) {
//                    if (containsImg(e)&&pages.size()>cnt) {
//                        cnt = 9;
//                    }
//                }
//                if (++cnt >= 10) {
//                    removeLastPageBreak(buffer);
//                    grouped.add(new ArrayList<>(buffer));
//                    buffer.clear();
//                    cnt = 0;
//                }
//            }
//            if (!buffer.isEmpty()) grouped.add(buffer);
//            int part = 1;
//            for (int i = 0; i < grouped.size(); i++) {
//                List<IBodyElement> g = grouped.get(i);
//                XWPFDocument newDoc = new XWPFDocument();
//                copyNumbering(srcDoc, newDoc);
//                copyDocumentSettings(srcDoc, newDoc);
//                copyBodyElements(srcDoc, newDoc, g);
//                String docxPath = outputFolder + "part_" + part + ".docx";
//                try (FileOutputStream fos = new FileOutputStream(docxPath)) {
//                    newDoc.write(fos);
//                }
//                System.out.println("Created: " + docxPath + " and " + docxPath);
//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                newDoc.write(baos);
//                htmlContents.add(convertDocToHtml(baos.toByteArray(),exportTYpe));
//                newDoc.close();
//                part++;
//            }
//            mergeHtmlPages(htmlContents, outputFile);
////            System.out.println("Split completed. Parts: " + grouped.size());
//        }
//    }
//
//    public static String mergeHtmlPages(List<String> htmlContents, String outputFile) throws IOException {
//        StringBuilder merged = new StringBuilder();
//
//        merged.append("""
//            <!DOCTYPE html>
//            <html>
//            <head>
//              <meta charset="UTF-8">
//              <title>Merged Pages</title>
//              <style>
//                html, body {
//                  margin: 0;
//                  padding: 0;
//                  overflow-x: hidden;
//                }
//                iframe {
//                  width: 100%;
//                  border: none;
//                  display: block;
//                  overflow: hidden;
//                }
//              </style>
//            </head>
//            <body>
//            """);
//
//        // Add each HTML page as an iframe with unique ID
//        for (int i = 0; i < htmlContents.size(); i++) {
//            String html = htmlContents.get(i);
//
//            // Escape for srcdoc attribute
//            html = html.replace("&", "&amp;")
//                    .replace("\"", "&quot;")
//                    .replace("<", "&lt;")
//                    .replace(">", "&gt;")
//                    .replace("\n", " ");
//
//            merged.append("<!-- Page ").append(i + 1).append(" -->\n")
//                    .append("<iframe id='page").append(i).append("' srcdoc=\"")
//                    .append(html)
//                    .append("\"></iframe>\n\n");
//        }
//
//        // Add improved resizing script
//        merged.append("""
//            <script>
//              window.addEventListener('load', () => {
//                const resizeFrame = (frame) => {
//                  try {
//                    const doc = frame.contentDocument || frame.contentWindow.document;
//                    if (!doc) return;
//                    const html = doc.documentElement;
//                    const body = doc.body;
//                    const height = Math.max(
//                      body.scrollHeight, body.offsetHeight,
//                      html.clientHeight, html.scrollHeight, html.offsetHeight
//                    );
//                    frame.style.height = height + 'px';
//                  } catch (e) {
//                    console.warn('Resize failed:', e);
//                  }
//                };
//
//                document.querySelectorAll('iframe').forEach(frame => {
//                  frame.addEventListener('load', () => resizeFrame(frame));
//                  // Resize again after short delay to handle images/styles
//                  setTimeout(() => resizeFrame(frame), 500);
//                });
//              });
//            </script>
//            </body>
//            </html>
//            """);
//
//        // Optionally write to file
//        if (outputFile != null && !outputFile.isBlank()) {
//            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
//                    new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
//                writer.write(merged.toString());
//            }
//        }
//
//        return merged.toString();
//    }
//
//
//    public static String convertDocToHtml(byte[] inputBytes,String type) throws IOException {
//        File tempOutput = File.createTempFile("html_output_", ".html");
//        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes)) {
//            com.spire.doc.Document document = new Document();
//            document.loadFromStream(inputStream, FileFormat.Auto);
//            document.getHtmlExportOptions().setImageEmbedded(true);
//            document.getHtmlExportOptions().setCssStyleSheetType(CssStyleSheetType.Internal);
//            document.getHtmlExportOptions().setAllowEmbeddingPostScriptFonts(true);
//            document.getHtmlExportOptions().setScaleImageToShapeSize(true);
//            document.getHtmlExportOptions().setFontEmbedded(true);
//            document.getHtmlExportOptions().setUseHighQualityRendering(true);
//            if (type.equalsIgnoreCase("html")) {
//                document.saveToFile(tempOutput.getAbsolutePath(), FileFormat.Html);
//            }
//            if (type.equalsIgnoreCase("htmlFixed")) {
//                document.saveToFile(tempOutput.getAbsolutePath(), FileFormat.HtmlFixed);
//            }
//            if (type.equalsIgnoreCase("pdf")) {
//                document.saveToFile(tempOutput.getAbsolutePath(), FileFormat.PDF);
//            }
//            document.close();
//
//            String htmlContent = Files.readString(tempOutput.toPath());
//            Files.delete(Path.of(tempOutput.getAbsolutePath()));
//            htmlContent = htmlContent.replaceAll("(?i)Evaluation Warning: The document was created with Spire\\.Doc for JAVA\\.", "");
//            htmlContent = removeSprdiv(htmlContent);
//            return htmlContent;
//        }
//    }
//
//    // ------------------------------------------------------------
//    private static void removeLastPageBreak(List<IBodyElement> buffer) {
//        if (buffer.isEmpty()) return;
//        IBodyElement last = buffer.get(buffer.size() - 1);
//        if (last instanceof XWPFParagraph para) {
//            for (XWPFRun r : para.getRuns()) {
//                r.getCTR().getBrList().removeIf(br -> br.getType() == STBrType.PAGE);
//            }
//            CTPPr pPr = para.getCTP().getPPr();
//            if (pPr != null && pPr.getSectPr() != null) pPr.unsetSectPr();
//            if (para.getRuns().isEmpty() || para.getText().isBlank()) buffer.remove(buffer.size() - 1);
//        }
//    }
//
//    // ------------------------------------------------------------
//    private static List<List<IBodyElement>> splitByPageBreak(XWPFDocument doc) {
//        List<List<IBodyElement>> res = new ArrayList<>();
//        List<IBodyElement> cur = new ArrayList<>();
//        for (IBodyElement e : doc.getBodyElements()) {
//            cur.add(e);
//            if (containsPageOrSectionBreak(e)) {
//                res.add(new ArrayList<>(cur));
//                cur.clear();
//            }
//        }
//        if (!cur.isEmpty()) res.add(cur);
//        return res;
//    }
//
//    private static boolean containsPageOrSectionBreak(IBodyElement e) {
//        if (e instanceof XWPFParagraph p) {
//            for (XWPFRun r : p.getRuns()) {
//                for (CTBr br : r.getCTR().getBrList()) {
//                    if (br.getType() == STBrType.PAGE) return true;
//                }
//            }
//            CTPPr pp = p.getCTP().getPPr();
//            if (pp != null && pp.getSectPr() != null) return true;
//        }
//        return false;
//    }
//
//    private static boolean containsImg(IBodyElement e) {
//        if (e instanceof XWPFParagraph p) {
//            for (XWPFRun r : p.getRuns()) {
//                if (r.getEmbeddedPictures().size() > 0) return true;
//            }
//        }
//        return false;
//    }
//
//    // ------------------------------------------------------------
//    private static void copyNumbering(XWPFDocument src, XWPFDocument dst) throws Exception {
//        if (src.getNumbering() != null) {
//            XWPFNumbering srcNumbering = src.getNumbering();
//            XWPFNumbering dstNumbering = dst.createNumbering();
//            for (XWPFNum num : srcNumbering.getNums()) {
//                dstNumbering.addNum(num);
//            }
//        }
//    }
//
//    private static void copyDocumentSettings(XWPFDocument src, XWPFDocument dst) {
//        if (src.getStyles() != null) {
//            CTStyles ctStyles = src.getStyles().getCtStyles();
//            if (ctStyles != null) dst.createStyles().setStyles((CTStyles) ctStyles.copy());
//        }
//        if (src.getDocument().getBody().getSectPr() != null) {
//            CTSectPr sectPr = (CTSectPr) src.getDocument().getBody().getSectPr().copy();
//            dst.getDocument().getBody().setSectPr(sectPr);
//        }
//    }
//
//    // ------------------------------------------------------------
//    private static void copyBodyElements(XWPFDocument src, XWPFDocument dst,
//                                         List<IBodyElement> elems) throws Exception {
//        for (IBodyElement e : elems) {
//            if (e instanceof XWPFParagraph p) {
//                dst.getDocument().getBody().addNewP().set((CTP) p.getCTP().copy());
//            } else if (e instanceof XWPFTable t) {
//                dst.getDocument().getBody().addNewTbl().set((CTTbl) t.getCTTbl().copy());
//            } else if (e instanceof XWPFSDT sdt) {
//                dst.getDocument().getBody().addNewSdt().set(sdt.getDocument().getDocument().getBody().getSdtList().get(0));
//            }
//        }
//        cloneAllImageParts(src, dst);
//    }
//
//    private static void cloneAllImageParts(XWPFDocument src, XWPFDocument dst) throws Exception {
//        PackagePart srcDocPart = src.getPackagePart();
//        for (PackageRelationship rel : srcDocPart.getRelationshipsByType(
//                XWPFRelation.IMAGES.getRelation())) {
//            String rId = rel.getId();
//            PackagePart srcImg = srcDocPart.getRelatedPart(rel);
//            byte[] data = srcImg.getInputStream().readAllBytes();
//
//            PackagePart dstDocPart = dst.getPackagePart();
//            PackagePart dstImg = dstDocPart.getPackage().createPart(
//                    srcImg.getPartName(), srcImg.getContentType());
//
//            try (OutputStream out = dstImg.getOutputStream()) {
//                out.write(data);
//            }
//
//            dstDocPart.addRelationship(dstImg.getPartName(),
//                    TargetMode.INTERNAL,
//                    XWPFRelation.IMAGES.getRelation(),
//                    rId);
//        }
//    }
//}
