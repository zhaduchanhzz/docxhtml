package com.hvnh;

import com.spire.doc.*;
import com.spire.doc.documents.*;
import com.spire.doc.pages.FixedLayoutDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SplitDocByPageBreakToHtml {

    public static void main(String[] args) throws Exception {
        String inputPath = "input.docx";
        String outputHtmlPath = "merged_output.html";

        // 1️⃣ Split Word document by manual page breaks
//        List<File> parts = splitByPageBreak(inputPath);
        Document original = new Document();
        original.loadFromFile(inputPath);
        FixedLayoutDocument layoutDoc = new FixedLayoutDocument(original);
        System.out.println("layoutDoc: " + layoutDoc.getPages().getCount());
        // 2️⃣ Convert each split file to HTML and merge
//        String mergedHtml = mergePartsToHtml(parts);

        // 3️⃣ Save merged HTML file
//        Files.write(Paths.get(outputHtmlPath), mergedHtml.getBytes("UTF-8"));
        System.out.println(" ✅ Merged HTML written to " + outputHtmlPath);
    }

    public void countPageBreak(String inputFilePath) throws Exception {
        InputStream inputStream = new FileInputStream(inputFilePath);
        XWPFDocument document = new XWPFDocument(inputStream);
        document.getParagraphs().remove(0);


    }

    /**
     * Split the document by Page_Break markers.
     */
    public static List<File> splitByPageBreak(String inputFilePath) throws Exception {
        List<File> splitFiles = new ArrayList<>();
        Document original = new Document();
        original.loadFromFile(inputFilePath);

        Document newDoc = new Document();
        Section section = newDoc.addSection();
        int index = 0;

        System.out.println(original.getSections().getCount());
        System.out.println(original.getPageCount());
        for (int s = 0; s < original.getSections().getCount(); s++) {
            Section sec = original.getSections().get(s);

            for (int c = 0; c < sec.getBody().getChildObjects().getCount(); c++) {
                DocumentObject obj = sec.getBody().getChildObjects().get(c);

                if (obj instanceof Paragraph) {
                    Paragraph para = (Paragraph) obj;
                    sec.cloneSectionPropertiesTo(section);
                    section.getBody().getChildObjects().add(para.deepClone());

                    // Loop through each element in paragraph
                    for (int i = 0; i < para.getChildObjects().getCount(); i++) {
                        DocumentObject parObj = para.getChildObjects().get(i);

                        if (parObj instanceof Break) {
                            Break br = (Break) parObj;
                            if (br.getBreakType() == BreakType.Page_Break) {
                                System.out.println("break");
                                // 🧹 Remove the page break
                                int idx = para.getChildObjects().indexOf(parObj);
                                Paragraph lastPara = (Paragraph) section.getBody().getLastParagraph();
                                lastPara.getChildObjects().removeAt(idx);

                                // 💾 Save the current part to temp file
                                File tempFile = File.createTempFile("split_part_" + index + "_", ".docx");
                                newDoc.saveToFile(tempFile.getAbsolutePath(), FileFormat.Docx);
                                splitFiles.add(tempFile);
                                System.out.println("✅ Saved part " + index);

                                index++;

                                // 🔄 Start a new document for next part
                                newDoc = new Document();
                                section = newDoc.addSection();

                                // Add remaining text from paragraph after page break
                                Paragraph newPara = (Paragraph) para.deepClone();
                                while (idx >= 0 && newPara.getChildObjects().getCount() > idx) {
                                    newPara.getChildObjects().removeAt(idx);
                                    idx--;
                                }
                                if (newPara.getChildObjects().getCount() > 0) {
                                    section.getBody().getChildObjects().add(newPara);
                                }
                            }
                        }
                    }
                } else if (obj instanceof Table) {
                    section.getBody().getChildObjects().add(obj.deepClone());
                }
            }
        }

        // 💾 Save the final section
        File lastFile = File.createTempFile("split_part_" + index + "_", ".docx");
        newDoc.saveToFile(lastFile.getAbsolutePath(), FileFormat.Docx);
        splitFiles.add(lastFile);

        System.out.println("✅ Split complete. Total parts: " + splitFiles.size());
        return splitFiles;
    }

    /**
     * Convert DOCX parts to HTML and merge them into one HTML document.
     */
    public static String mergePartsToHtml(List<File> docxParts) throws IOException {
        StringBuilder mergedHtml = new StringBuilder();
        mergedHtml.append("<html><head><meta charset='UTF-8'></head><body>\n");

        int count = 1;
        for (File part : docxParts) {
            System.out.println("Converting part " + count + " to HTML...");
            byte[] bytes = Files.readAllBytes(part.toPath());
            byte[] htmlBytes = convertDocToHtml(bytes);
            String html = new String(htmlBytes, "UTF-8");

            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            Element body = doc.body();

            mergedHtml.append("<div class='doc-part' id='part").append(count).append("'>\n");
            mergedHtml.append(body.html());
            mergedHtml.append("</div>\n");

            count++;
            part.delete(); // optional cleanup
        }

        mergedHtml.append("</body></html>");
        return mergedHtml.toString();
    }

    /**
     * Convert DOCX to HTML with embedded images and fonts.
     */
    public static byte[] convertDocToHtml(byte[] inputBytes) throws IOException {
        File tempOutput = File.createTempFile("html_output_", ".html");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes)) {
            Document document = new Document();
            document.loadFromStream(inputStream, FileFormat.Auto);
            FixedLayoutDocument layoutDoc = new FixedLayoutDocument(document);
            System.out.println("layoutDoc: " + layoutDoc.getPages().getCount());
            document.getHtmlExportOptions().setImageEmbedded(true);
            document.getHtmlExportOptions().setCssStyleSheetType(CssStyleSheetType.Internal);
            document.getHtmlExportOptions().setFontEmbedded(true);

            document.saveToFile(tempOutput.getAbsolutePath(), FileFormat.HtmlFixed);
            document.close();

            String htmlContent = Files.readString(tempOutput.toPath());
            htmlContent = htmlContent.replaceAll("(?i)Evaluation Warning: The document was created with Spire\\.Doc for JAVA\\.", "");
            htmlContent = removeSprdiv(htmlContent);

            return htmlContent.getBytes("UTF-8");
        }
    }

    /**
     * Clean Spire internal elements.
     */
    public static String removeSprdiv(String html) {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        Elements divs = doc.select("div.sprdiv");
        for (Element div : divs) {
            Elements spans = div.select("> span.sprspan.sprtext1");
            if (!spans.isEmpty()) {
                div.remove();
                break;
            }
        }
        return doc.html();
    }
}
