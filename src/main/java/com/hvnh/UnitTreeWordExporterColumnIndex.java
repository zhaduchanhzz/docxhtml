//package com.hvnh;
//
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.xwpf.usermodel.*;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class UnitTreeWordExporterColumnIndex {
//
//    // ==========================================
//    // MODELS
//    // ==========================================
//    @Data
//    static class OrgUnit {
//        Long id;
//        String name;
//
//        OrgUnit(Long id, String name) {
//            this.id = id;
//            this.name = name;
//        }
//    }
//
//    @Data
//    static class UnitTree {
//        OrgUnit currentUnit;
//        List<UnitTree> listChild = new ArrayList<>();
//        int currentLevel;
//
//        UnitTree(OrgUnit unit) {
//            this.currentUnit = unit;
//        }
//
//        void addChild(UnitTree child) {
//            listChild.add(child);
//        }
//    }
//
//    @Data
//    static class ScheduleDetail {
//        Long superiorUnitId;
//        String employee;
//        String shift;
//        boolean isChinhThuc = false;
//        boolean isDubi = false;
//
//        ScheduleDetail(Long uid, String employee, String shift) {
//            this.superiorUnitId = uid;
//            this.employee = employee;
//            this.shift = shift;
//        }
//
//        Long getSuperiorUnitId() {
//            return superiorUnitId;
//        }
//    }
//
//    // ==========================================
//    // FILTER TREE
//    // ==========================================
//    public static UnitTree filterTree(UnitTree root, Set<Long> validIds, int level) {
//        if (root == null) return null;
//
//        root.currentLevel = level;
//        List<UnitTree> filteredChildren = new ArrayList<>();
//
//        for (UnitTree child : root.listChild) {
//            UnitTree filtered = filterTree(child, validIds, level + 1);
//            if (filtered != null) filteredChildren.add(filtered);
//        }
//
//        boolean isValid = validIds.contains(root.currentUnit.id);
//
//        if (isValid || !filteredChildren.isEmpty()) {
//            root.listChild = filteredChildren;
//            return root;
//        }
//        return null;
//    }
//
//    // ==========================================
//    // FLATTEN TREE (DFS)
//    // ==========================================
//
//    public static void flattenTree(UnitTree root, List<UnitTree> output) {
//        if (root == null) return;
//        output.add(root);
//        for (UnitTree child : root.listChild) {
//            flattenTree(child, output);
//        }
//    }
//
//    // ==========================================
//    // FILL WORD BY COLUMN INDEX
//    // ==========================================
//    public static void fillTreeScheduleToTemplateColumnIndex(
//            UnitTree root,
//            List<ScheduleDetail> details,
//            String templatePath,
//            String outputPath
//    ) throws Exception {
//
//        XWPFDocument doc = new XWPFDocument(new FileInputStream(templatePath));
//
//        // Flatten tree
//        List<UnitTree> nodes = new ArrayList<>();
//        flattenTree(root, nodes);
//        nodes.forEach(child -> {
//            log.info(child.currentUnit.toString());
//        });
//        // Group ScheduleDetail theo UnitID
//        Map<Long, List<ScheduleDetail>> detailMap =
//                details.stream().collect(Collectors.groupingBy(ScheduleDetail::getSuperiorUnitId));
//
//        // Lấy bảng đầu tiên
//        XWPFTable table = doc.getTables().get(0);
//
//        // Row đầu tiên trong bảng làm TEMPLATE
//        XWPFTableRow templateRow = table.getRow(0);
//        int columnCount = templateRow.getTableCells().size();
//
//        // Xóa row mẫu để bắt đầu fill
////        table.removeRow(0);
//        String parentNode = "";
//        String chinhThuc = "";
//        String duBi = "";
//        // Loop tree
//        for (UnitTree node : nodes) {
//            XWPFTableRow row = table.createRow();
//            if (node.getCurrentLevel() <= 2) {
//                parentNode = node.currentUnit.getName();
////                continue;
//            }
//            List<ScheduleDetail> list = detailMap.getOrDefault(node.currentUnit.id, List.of());
//            if (list.isEmpty()) {
//                // In 1 row trắng nếu không có Schedule
//                ensureCellCount(row, columnCount);
//                fillRowByColumnIndex(row, node, null);
//            } else {
//                for (ScheduleDetail sd : list) {
//                    ensureCellCount(row, columnCount);
//                    fillRowByColumnIndex(row, node, sd);
//                }
//            }
//        }
//
//        // Save output
//        try (FileOutputStream out = new FileOutputStream(outputPath)) {
//            doc.write(out);
//        }
//
//        System.out.println("DONE → " + outputPath);
//    }
//
//    // ==========================================
//    // FILL ROW BY COLUMN INDEX
//    // ==========================================
//    private static void fillRowByColumnIndex(XWPFTableRow row, UnitTree node, ScheduleDetail sd) {
//
//        // COLUMN 0: Level
//        row.getCell(0).setText(String.valueOf(node.currentLevel));
//
//        // COLUMN 1: Unit ID
//        row.getCell(1).setText(String.valueOf(node.currentUnit.id));
//
//        // COLUMN 2: Unit Name (indent theo level)
//        row.getCell(2).setText("  ".repeat(node.currentLevel) + node.currentUnit.name);
//
//        // COLUMN 3: Employee
//        row.getCell(3).setText(sd == null ? "" : sd.employee);
//
//        // COLUMN 4: Shift
//        row.getCell(4).setText(sd == null ? "" : sd.shift);
//    }
//
//    // Đảm bảo row đủ số cell
//    private static void ensureCellCount(XWPFTableRow row, int count) {
//        while (row.getTableCells().size() < count) {
//            row.addNewTableCell();
//        }
//    }
//
//    // ==========================================
//    // MAIN DEMO
//    // ==========================================
//    public static void main(String[] args) throws Exception {
//
//        // Build TREE
//        UnitTree root = new UnitTree(new OrgUnit(1L, "Root"));
//        UnitTree n2 = new UnitTree(new OrgUnit(2L, "Team A"));
//        UnitTree n3 = new UnitTree(new OrgUnit(3L, "Team B"));
//        UnitTree n5 = new UnitTree(new OrgUnit(5L, "Unit 5"));
//        UnitTree n12 = new UnitTree(new OrgUnit(12L, "Unit 12"));
//
//        root.addChild(n2);
//        root.addChild(n3);
//        n2.addChild(n5);
//        n3.addChild(n12);
//
//        // ScheduleDetails
//        List<ScheduleDetail> details = Arrays.asList(
//                new ScheduleDetail(5L, "John", "Morning"),
//                new ScheduleDetail(5L, "Night", "Morning"),
//                new ScheduleDetail(5L, "Nigh11t", "Morning"),
//                new ScheduleDetail(5L, "Night11", "Morn111ing"),
//                new ScheduleDetail(5L, "John", "Morning"),
//                new ScheduleDetail(5L, "John", "Morning"),
//                new ScheduleDetail(5L, "Anna", "Night"),
//                new ScheduleDetail(12L, "Mike", "Evening")
//        );
//
//        // validIds from ScheduleDetail
//        Set<Long> validIds = details.stream()
//                .map(ScheduleDetail::getSuperiorUnitId)
//                .collect(Collectors.toSet());
//
//        // FILTER TREE
//        UnitTree filtered = filterTree(root, validIds, 1);
//
//        // EXPORT
//        fillTreeScheduleToTemplateColumnIndex(
//                filtered,
//                details,
//                "input.docx",
//                "output_by_column_index.docx"
//        );
//
//        System.out.println("DONE export!");
//    }
//}
