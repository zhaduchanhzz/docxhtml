//package com.hvnh;
//
//import org.apache.poi.xwpf.usermodel.XWPFTable;
//import org.apache.poi.xwpf.usermodel.XWPFTableRow;
//import org.apache.poi.xwpf.usermodel.*;
//import org.apache.xmlbeans.XmlToken;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import java.util.stream.Collectors;
//
//public class VTWORD {
//
//
//    public static void main(String[] args) throws Exception {
//
//        // ===== TẠO CÂY ĐƠN VỊ =====
//
//        // Root (không in)
//        UnitTree root = new UnitTree(new OrgUnit("0", "ROOT", "ROOT"));
//
//        // Level 2
//        UnitTree a = new UnitTree(new OrgUnit("A", "Phòng A", "Hà Nội"));
//        UnitTree b = new UnitTree(new OrgUnit("B", "Phòng B", "HCM"));
//        root.addChild(a);
//        root.addChild(b);
//
//        // Level 3 (thuộc A)
//        UnitTree a1 = new UnitTree(new OrgUnit("A1", "Đội A1", "Hà Nội 1"));
//        UnitTree a2 = new UnitTree(new OrgUnit("A2", "Đội A2", "Hà Nội 2"));
//        a.addChild(a1);
//        a.addChild(a2);
//
//        // Level 3 (thuộc B)
//        UnitTree b1 = new UnitTree(new OrgUnit("B1", "Đội B1", "HCM 1"));
//        b.addChild(b1);
//
//        // Level 4 (thuộc A1)
//        UnitTree a11 = new UnitTree(new OrgUnit("A11", "Tổ A1-1", "Hà Nội 1-1"));
//        a1.addChild(a11);
//
//        // ===== TẠO LIST ScheduleUnitDetail (chỉ in đơn vị có trong list) =====
//        List<ScheduleUnitDetail> details = Arrays.asList(
//                new ScheduleUnitDetail("A", "Nội dung A", "PCCC A"),
//                new ScheduleUnitDetail("A1", "Nội dung A1", "PCCC A1"),
//                new ScheduleUnitDetail("A2", "Nội dung A2", "PCCC A2"),
//                new ScheduleUnitDetail("B", "Nội dung A2", "PCCC A2"),
//                new ScheduleUnitDetail("B1", "Nội dung B1", "PCCC B1")
//                // A11 KHÔNG CÓ → không in
//        );
//
//        // ===== GỌI EXPORT =====
//        exportUsingTemplate(
//                root,
//                details,
//                "input.docx",   // template có header, style
//                "outtttttt.docx"  // file xuất
//        );
//
//        System.out.println("✔ Xuất file Word thành công: outtttttt.docx");
//    }
//
//    private static final String[] roman = {
//            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
//            "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
//    };
//
//    private static String toRoman(int n) {
//        if (n <= 0 || n >= roman.length) return String.valueOf(n);
//        return roman[n];
//    }
//
//
//    public static void exportUsingTemplate(UnitTree root,
//                                           List<ScheduleUnitDetail> details,
//                                           String templatePath,
//                                           String outputPath) throws Exception {
//
//        // Map unitId → ScheduleUnitDetail
//        Map<String, ScheduleUnitDetail> detailMap = new HashMap<>();
//        for (ScheduleUnitDetail d : details) {
//            detailMap.put(d.unitId, d);
//        }
//
//        // Load template Word
//        XWPFDocument doc = new XWPFDocument(new FileInputStream(templatePath));
//
//        // Tìm bảng có 5 cột
//        XWPFTable table = doc.getTables().get(0);
//        // Row mẫu = row số 1 (index 1)
//        XWPFTableRow sampleRow = table.getRow(1);
//
//        while (table.getNumberOfRows() > 2) {
//            table.removeRow(2);
//        }
//
//        // Duyệt cây
//        traverseWithTemplate(root, 1, new ArrayList<>(), table, sampleRow, detailMap);
//
//        // Ghi file
//        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
//            doc.write(fos);
//        }
//    }
//
//    private static void traverseWithTemplate(UnitTree node,
//                                             int level,
//                                             List<Integer> indexTrack,
//                                             XWPFTable table,
//                                             XWPFTableRow sampleRow,
//                                             Map<String, ScheduleUnitDetail> detailMap) {
//
//        // Bỏ qua root
//        if (level > 1) {
//            OrgUnit org = node.current;
//
//            if (detailMap.containsKey(org.unitId)) {
//                ScheduleUnitDetail detail = detailMap.get(org.unitId);
//
//                // Tạo STT
//                String stt;
//                if (level == 2) {
//                    stt = toRoman(indexTrack.get(0));
//                } else {
//                    stt = indexTrack.stream()
//                            .map(Object::toString)
//                            .collect(Collectors.joining("."));
//                }
//
//                // Clone row mẫu
//                XWPFTableRow newRow = table.createRow();
//
//                // Copy style row mẫu
//                newRow.getCtRow().setTrPr(sampleRow.getCtRow().getTrPr());
//
//                // Copy cell style + nội dung
//                for (int i = 0; i < sampleRow.getTableCells().size(); i++) {
//                    XWPFTableCell newCell = newRow.getCell(i);
//                    XWPFTableCell sampleCell = sampleRow.getCell(i);
//
//                    copyCellStyleAndSetText(sampleCell, newCell, stt);
//                }
//            }
//        }
//        // Duyệt con
//        int childIndex = 1;
//        for (UnitTree child : node.childUnit) {
//            List<Integer> newIndex = new ArrayList<>(indexTrack);
//
//            if (level >= 2)
//                newIndex.add(childIndex);
//            else {
//                newIndex = new ArrayList<>();
//                newIndex.add(childIndex);
//            }
//            traverseWithTemplate(child, level + 1, newIndex, table, sampleRow, detailMap);
//            childIndex++;
//        }
//    }
//    public static void copyCellStyleAndSetText(XWPFTableCell sourceCell, XWPFTableCell targetCell, String newText) {
//        // 1. Xóa tất cả paragraph cũ trong cell đích
//        while (targetCell.getParagraphs().size() > 0) {
//            targetCell.removeParagraph(0);
//        }
//
//        // 2. Tách text mới theo dòng (\n)
//        String[] lines = newText.split("\n");
//
//        // 3. Lấy paragraph đầu tiên của sourceCell làm mẫu
//        XWPFParagraph sourcePara = sourceCell.getParagraphs().get(0);
//
//        for (XWPFParagraph paragraph : sourceCell.getParagraphs()) {
//            if (paragraph.getText().trim().isBlank()) continue;
//            else {
//                sourcePara = paragraph;
//            }
//        }
//
//        for (String line : lines) {
//            // Tạo paragraph mới trong target cell
//            XWPFParagraph targetPara = targetCell.addParagraph();
//
//            // Copy paragraph-level style
//            if (sourcePara.getCTP().getPPr() != null) {
//                targetPara.getCTP().setPPr(sourcePara.getCTP().getPPr());
//            }
//
//            // Copy run style từ run đầu tiên
//            XWPFRun newRun = targetPara.createRun();
//            if (!sourcePara.getRuns().isEmpty()) {
//                XWPFRun sourceRun = sourcePara.getRuns().get(0);
//                newRun.setBold(sourceRun.isBold());
//                newRun.setItalic(sourceRun.isItalic());
//                newRun.setUnderline(sourceRun.getUnderline());
//                newRun.setFontFamily(sourceRun.getFontFamily());
////                newRun.setFontSize(sourceRun.getFontSize());
//                newRun.setColor(sourceRun.getColor());
//            }
//
//            // Set text
//            newRun.setText(line);
//        }
//    }
//    private static void traverse(UnitTree node,
//                                 int level,
//                                 List<Integer> indexTrack,
//                                 XWPFTable table,
//                                 Map<String, ScheduleUnitDetail> detailMap) {
//        // Bỏ qua root
//        if (level > 1) {
//            OrgUnit org = node.current;
//
//            if (detailMap.containsKey(org.unitId)) {
//                ScheduleUnitDetail detail = detailMap.get(org.unitId);
//
//                // Tạo STT
//                String stt;
//                if (level == 2) {
//                    stt = toRoman(indexTrack.get(0));
//                } else {
//                    stt = indexTrack.stream()
//                            .map(Object::toString)
//                            .collect(Collectors.joining("."));
//                }
//
//                XWPFTableRow row = table.createRow();
//                row.getCell(0).setText(stt);
//                row.getCell(1).setText(org.unitName);
//                row.getCell(2).setText(org.unitAddress);
//                row.getCell(3).setText(detail.unitFire);
//                row.getCell(4).setText(detail.unitContent);
//            }
//        }
//
//        // DFS duyệt con
//        int childIndex = 1;
//        for (UnitTree child : node.childUnit) {
//            List<Integer> newIndex = new ArrayList<>(indexTrack);
//
//            if (level >= 2) {
//                newIndex.add(childIndex);
//            } else {
//                newIndex = new ArrayList<>();
//                newIndex.add(childIndex);
//            }
//
//            traverse(child, level + 1, newIndex, table, detailMap);
//            childIndex++;
//        }
//    }
//
//
//}
