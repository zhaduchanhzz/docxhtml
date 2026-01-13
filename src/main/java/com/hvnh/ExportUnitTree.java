//package com.hvnh;
//
//import org.apache.poi.xwpf.usermodel.*;
//import org.apache.xmlbeans.XmlToken;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class ExportUnitTree {
//
//    // ================================
//    //           DATA CLASSES
//    // ================================
//    static class OrgUnit {
//        String unitId;
//        String unitName;
//        String unitAddress;
//
//        public OrgUnit(String unitId, String unitName, String unitAddress) {
//            this.unitId = unitId;
//            this.unitName = unitName;
//            this.unitAddress = unitAddress;
//        }
//    }
//
//    static class ScheduleUnitDetail {
//        String unitId;
//        String unitContent;
//        String unitFire;
//
//        public ScheduleUnitDetail(String unitId, String unitContent, String unitFire) {
//            this.unitId = unitId;
//            this.unitContent = unitContent;
//            this.unitFire = unitFire;
//        }
//    }
//
//    static class UnitTree {
//        OrgUnit current;
//        List<UnitTree> childUnit = new ArrayList<>();
//
//        public UnitTree(OrgUnit current) {
//            this.current = current;
//        }
//
//        public void addChild(UnitTree child) {
//            childUnit.add(child);
//        }
//    }
//
//    // ================================
//    //      CHUYỂN SỐ → SỐ LA MÃ
//    // ================================
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
//    // ================================
//    //     CLONE ROW + GIỮ 100% STYLE
//    // ================================
//    private static XWPFTableRow cloneRow(XWPFTable table, XWPFTableRow sampleRow) {
//
//        XWPFTableRow newRow = table.createRow();
//
//        // Clone row properties
//        newRow.getCtRow().setTrPr(sampleRow.getCtRow().getTrPr());
//
//        // Clone toàn bộ cell XML
//        for (int i = 0; i < sampleRow.getTableCells().size(); i++) {
//
//            XWPFTableCell srcCell = sampleRow.getCell(i);
//            XWPFTableCell destCell = newRow.getCell(i);
//
//            try {
//                String xml = srcCell.getCTTc().toString();
//                destCell.getCTTc().set(XmlToken.Factory.parse(xml));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // Xóa text mẫu để ghi text mới
//            destCell.removeParagraph(0);
//            destCell.addParagraph();
//        }
//
//        return newRow;
//    }
//
//    // ================================
//    //         TRAVERSE CÂY
//    // ================================
//
//    private static XWPFTableRow cloneFullStyledRow(XWPFTable table, XWPFTableRow sampleRow) {
//        try {
//            CTRow ctRow = CTRow.Factory.parse(sampleRow.getCtRow().toString());
//            XWPFTableRow newRow = new XWPFTableRow(ctRow, table);
//            table.addRow(newRow);
//            return newRow;
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi clone row", e);
//        }
//    }
//    private static void clearCellKeepStyle(XWPFTableCell cell, XWPFTableCell sampleCell) {
//
//        // Xóa toàn bộ paragraph + run hiện có
//        List<XWPFParagraph> paragraphs = new ArrayList<>(cell.getParagraphs());
//        for (int i = 0; i < paragraphs.size(); i++) {
//            cell.removeParagraph(i);
//        }
//
//        // Tạo paragraph mới
//        XWPFParagraph newP = cell.addParagraph();
//
//        // Copy paragraph style từ sample cell
//        XWPFParagraph sampleP = sampleCell.getParagraphs().get(0);
//        newP.getCTP().setPPr(sampleP.getCTP().getPPr());
//    }
//
//    private static void traverse(UnitTree node,
//                                 int level,
//                                 List<Integer> indexTrack,
//                                 XWPFTable table,
//                                 XWPFTableRow sampleRow,
//                                 Map<String, ScheduleUnitDetail> detailMap) {
//
//        if (level > 1) {
//            OrgUnit org = node.current;
//
//            if (detailMap.containsKey(org.unitId)) {
//                ScheduleUnitDetail detail = detailMap.get(org.unitId);
//
//                String stt;
//                if (level == 2) stt = toRoman(indexTrack.get(0));
//                else stt = String.join(".", indexTrack.stream().map(String::valueOf).toList());
//
//                // Clone row
//                XWPFTableRow newRow = cloneFullStyledRow(table, sampleRow);
//
//                // Clear cell text but KEEP STYLE
//                for (int i = 0; i < sampleRow.getTableCells().size(); i++) {
//                    clearCellKeepStyle(newRow.getCell(i), sampleRow.getCell(i));
//                }
//
//                // Set new text
//                newRow.getCell(0).setText(stt);
//                newRow.getCell(1).setText(org.unitName);
//                newRow.getCell(2).setText(org.unitAddress);
//                newRow.getCell(3).setText(detail.unitFire);
//                newRow.getCell(4).setText(detail.unitContent);
//            }
//        }
//
//        int childIndex = 1;
//        for (UnitTree child : node.childUnit) {
//            List<Integer> next = new ArrayList<>(indexTrack);
//
//            if (level >= 2) next.add(childIndex);
//            else next = new ArrayList<>(List.of(childIndex));
//
//            traverse(child, level + 1, next, table, sampleRow, detailMap);
//            childIndex++;
//        }
//    }
//
//
//
//    // ================================
//    //       EXPORT USING TEMPLATE
//    // ================================
//    public static void exportUsingTemplate(UnitTree root,
//                                           List<ScheduleUnitDetail> details,
//                                           String templatePath,
//                                           String outputPath) throws Exception {
//
//        Map<String, ScheduleUnitDetail> detailMap = new HashMap<>();
//        for (ScheduleUnitDetail d : details) {
//            detailMap.put(d.unitId, d);
//        }
//
//        XWPFDocument doc = new XWPFDocument(new FileInputStream(templatePath));
//
//        // Tìm bảng có 5 cột
//        XWPFTable table = null;
//        for (XWPFTable t : doc.getTables()) {
//            if (t.getRow(0).getTableCells().size() == 5) {
//                table = t;
//                break;
//            }
//        }
//        if (table == null) throw new RuntimeException("Không tìm thấy bảng có 5 cột trong template!");
//
//        // Row mẫu = row thứ 2 (index 1)
//        XWPFTableRow sampleRow = table.getRow(1);
//
//        // Xóa các row cũ (nếu template có sẵn dữ liệu)
//        while (table.getNumberOfRows() > 2) {
//            table.removeRow(2);
//        }
//
//        traverse(root, 1, new ArrayList<>(), table, sampleRow, detailMap);
//
//        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
//            doc.write(fos);
//        }
//    }
//
//    // ================================
//    //             MAIN
//    // ================================
//    public static void main(String[] args) throws Exception {
//
//        // ===== TẠO CÂY =====
//        UnitTree root = new UnitTree(new OrgUnit("0", "ROOT", "ROOT"));
//
//        UnitTree a = new UnitTree(new OrgUnit("A", "Phòng A", "Hà Nội"));
//        UnitTree b = new UnitTree(new OrgUnit("B", "Phòng B", "HCM"));
//        root.addChild(a);
//        root.addChild(b);
//
//        UnitTree a1 = new UnitTree(new OrgUnit("A1", "Đội A1", "Hà Nội 1"));
//        UnitTree a2 = new UnitTree(new OrgUnit("A2", "Đội A2", "Hà Nội 2"));
//        a.addChild(a1);
//        a.addChild(a2);
//
//        UnitTree b1 = new UnitTree(new OrgUnit("B1", "Đội B1", "HCM 1"));
//        b.addChild(b1);
//
//        UnitTree a11 = new UnitTree(new OrgUnit("A11", "Tổ A1-1", "HN 1-1"));
//        a1.addChild(a11);
//
//        // ===== LIST DETAIL =====
//        List<ScheduleUnitDetail> details = Arrays.asList(
//                new ScheduleUnitDetail("A", "Nội dung A", "PCCC A"),
//                new ScheduleUnitDetail("A1", "Nội dung A1", "PCCC A1"),
//                new ScheduleUnitDetail("A2", "Nội dung A2", "PCCC A2"),
//                new ScheduleUnitDetail("B1", "Nội dung B1", "PCCC B1")
//        );
//
//        // ===== EXPORT =====
//        exportUsingTemplate(
//                root,
//                details,
//                "input.docx",    // file template
//                "hehe.docx"   // file kết quả
//        );
//
//        System.out.println("✔ Xuất file Word thành công: UnitTreeOutput.docx");
//    }
//}
//
