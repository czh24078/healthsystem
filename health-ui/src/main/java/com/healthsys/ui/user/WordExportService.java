package com.healthsys.ui.user;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.Report;
import com.healthsys.common.entity.Users;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Word 文档导出服务 — 将预约信息及检查项目明细导出为 .docx 文件
 */
public class WordExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出预约报告到 Word 文档
     *
     * @param filePath 保存路径
     * @param appointment 预约信息
     * @param group      检查组（套餐）
     * @param items      检查项目列表
     */
    public void exportAppointmentReport(String filePath, Appointment appointment,
                                        CheckItemGroup group, List<CheckItem> items) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {

            // ===== 标题 =====
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("健康体检管理系统 — 预约报告");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setFontFamily("宋体");

            // 打印时间
            XWPFParagraph timePara = doc.createParagraph();
            timePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun timeRun = timePara.createRun();
            timeRun.setText("打印时间：" + java.time.LocalDateTime.now().format(DATETIME_FMT));
            timeRun.setFontSize(11);
            timeRun.setFontFamily("宋体");
            timeRun.setColor("808080");

            addEmptyLine(doc);

            // ===== 分隔线 =====
            addSeparator(doc);

            // ===== 预约信息 =====
            addSectionTitle(doc, "【预约信息】");

            XWPFTable infoTable = doc.createTable(5, 2);
            infoTable.setWidth("100%");
            setTableWidth(infoTable, "50%", "50%");

            fillInfoRow(infoTable, 0, "套餐名称", group != null ? group.getGroupName() : "未知");
            fillInfoRow(infoTable, 1, "套餐描述",
                    group != null && group.getDescription() != null ? group.getDescription() : "无");
            fillInfoRow(infoTable, 2, "预约日期",
                    appointment.getExamDate() != null
                            ? appointment.getExamDate().format(DATE_FMT) : "未指定");
            fillInfoRow(infoTable, 3, "时段",
                    appointment.getExamTimeSlot() != null ? appointment.getExamTimeSlot() : "未指定");
            fillInfoRow(infoTable, 4, "支付状态", appointment.getPaymentStatusDisplay());

            addEmptyLine(doc);

            // ===== 检查项目明细 =====
            addSectionTitle(doc, "【检查项目明细】");

            XWPFTable itemsTable = doc.createTable(items.size() + 1, 5);
            itemsTable.setWidth("100%");
            setTableWidth(itemsTable, "8%", "32%", "22%", "26%", "12%");

            // 表头
            String[] headers = {"序号", "项目名称", "科室/分类", "参考值范围", "单价"};
            XWPFTableRow headerRow = itemsTable.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                XWPFTableCell cell = headerRow.getCell(i);
                setCellText(cell, headers[i], true);
                setCellBackground(cell, "4D88C5");
                setCellTextColor(cell, "FFFFFF");
            }

            // 数据行
            double totalPrice = 0.0;
            for (int i = 0; i < items.size(); i++) {
                CheckItem item = items.get(i);
                XWPFTableRow row = itemsTable.getRow(i + 1);
                Double price = item.getPrice() != null ? item.getPrice() : 0.0;
                totalPrice += price;

                fillItemsRow(row, 0, String.valueOf(i + 1), false);
                fillItemsRow(row, 1, item.getItemName(), false);
                fillItemsRow(row, 2, item.getCategory() != null ? item.getCategory() : "", false);
                fillItemsRow(row, 3, item.getReferenceRange() != null ? item.getReferenceRange() : "", false);
                fillItemsRow(row, 4, String.format("¥%.2f", price), false);
            }

            addEmptyLine(doc);

            // ===== 费用汇总 =====
            addSeparator(doc);
            XWPFParagraph summaryTitle = doc.createParagraph();
            XWPFRun summaryTitleRun = summaryTitle.createRun();
            summaryTitleRun.setText("【费用汇总】");
            summaryTitleRun.setBold(true);
            summaryTitleRun.setFontSize(14);
            summaryTitleRun.setFontFamily("宋体");

            XWPFParagraph countPara = doc.createParagraph();
            XWPFRun countRun = countPara.createRun();
            countRun.setText("检查项目数量：" + items.size() + " 项");
            countRun.setFontSize(12);
            countRun.setFontFamily("宋体");

            XWPFParagraph totalPara = doc.createParagraph();
            XWPFRun totalRun = totalPara.createRun();
            Double groupPrice = group != null ? group.getPrice() : totalPrice;
            totalRun.setText("套餐总价：¥" + String.format("%.2f", groupPrice));
            totalRun.setBold(true);
            totalRun.setFontSize(14);
            totalRun.setFontFamily("宋体");
            totalRun.setColor("CC0000");

            doc.write(out);
        }
    }

    /**
     * 导出体检报告（含检查结果）到 Word 文档
     */
    public void exportExamRecordReport(String filePath, Users user, Appointment appointment,
                                        CheckItemGroup group, List<ExamRecord> records,
                                        Map<Long, CheckItem> itemMap, Report report) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {

            // ===== 标题 =====
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("健康体检管理系统 — 体检报告");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("宋体");

            XWPFParagraph timePara = doc.createParagraph();
            timePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun timeRun = timePara.createRun();
            timeRun.setText("打印时间：" + java.time.LocalDateTime.now().format(DATETIME_FMT));
            timeRun.setFontSize(11);
            timeRun.setFontFamily("宋体");
            timeRun.setColor("808080");

            addEmptyLine(doc);
            addSeparator(doc);
            addEmptyLine(doc);

            // ===== 用户信息 =====
            addSectionTitle(doc, "【用户信息】");
            XWPFTable userTable = doc.createTable(3, 2);
            fillInfoRow(userTable, 0, "姓名", user.getRealName() != null ? user.getRealName() : "");
            fillInfoRow(userTable, 1, "手机号", user.getPhone() != null ? user.getPhone() : "");
            fillInfoRow(userTable, 2, "性别",
                    user.getGender() != null ? (user.getGender() == 1 ? "男" : "女") : "");

            addEmptyLine(doc);

            // ===== 预约信息 =====
            addSectionTitle(doc, "【预约信息】");
            XWPFTable infoTable = doc.createTable(4, 2);
            fillInfoRow(infoTable, 0, "检查组", group != null ? group.getGroupName() : "未知");
            fillInfoRow(infoTable, 1, "体检日期",
                    appointment.getExamDate() != null
                            ? appointment.getExamDate().format(DATE_FMT) : "未指定");
            fillInfoRow(infoTable, 2, "时段",
                    appointment.getExamTimeSlot() != null ? appointment.getExamTimeSlot() : "未指定");
            fillInfoRow(infoTable, 3, "支付状态", appointment.getPaymentStatusDisplay());

            addEmptyLine(doc);
            addSeparator(doc);
            addEmptyLine(doc);

            // ===== 检查结果 =====
            addSectionTitle(doc, "【检查结果】");
            if (records != null && !records.isEmpty()) {
                XWPFTable resultsTable = doc.createTable(records.size() + 1, 6);
                String[] headers = {"序号", "项目名称", "结果值", "单位", "参考范围", "异常"};
                XWPFTableRow headerRow = resultsTable.getRow(0);
                for (int h = 0; h < headers.length; h++) {
                    XWPFTableCell cell = headerRow.getCell(h);
                    setCellText(cell, headers[h], true);
                    setCellBackground(cell, "4D88C5");
                    setCellTextColor(cell, "FFFFFF");
                }

                for (int i = 0; i < records.size(); i++) {
                    ExamRecord record = records.get(i);
                    CheckItem item = itemMap.get(record.getItemId());
                    XWPFTableRow row = resultsTable.getRow(i + 1);

                    fillItemsRow(row, 0, String.valueOf(i + 1), false);
                    fillItemsRow(row, 1,
                            record.getItemName() != null ? record.getItemName()
                                    : (item != null ? item.getItemName() : "未知"), false);
                    fillItemsRow(row, 2,
                            record.getResultValue() != null ? record.getResultValue() : "", false);
                    fillItemsRow(row, 3,
                            item != null && item.getUnit() != null ? item.getUnit() : "", false);
                    fillItemsRow(row, 4,
                            item != null && item.getReferenceRange() != null
                                    ? item.getReferenceRange() : "", false);
                    String abnormalText = Boolean.TRUE.equals(record.getIsAbnormal()) ? "异常" : "正常";
                    XWPFRun abnormalRun = fillItemsRow(row, 5, abnormalText, false);
                    if (Boolean.TRUE.equals(record.getIsAbnormal())) {
                        abnormalRun.setColor("CC0000");
                    }
                }

                addEmptyLine(doc);

                // 异常项统计
                long abnormalCount = records.stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsAbnormal())).count();
                XWPFParagraph statPara = doc.createParagraph();
                XWPFRun statRun = statPara.createRun();
                statRun.setText("共 " + records.size() + " 项，其中异常 " + abnormalCount + " 项");
                statRun.setFontSize(12);
                statRun.setFontFamily("宋体");
            } else {
                XWPFParagraph emptyPara = doc.createParagraph();
                XWPFRun emptyRun = emptyPara.createRun();
                emptyRun.setText("（暂无检查结果记录）");
                emptyRun.setFontSize(12);
                emptyRun.setFontFamily("宋体");
                emptyRun.setColor("999999");
            }

            addEmptyLine(doc);
            addSeparator(doc);
            addEmptyLine(doc);

            // ===== 医生报告 =====
            addSectionTitle(doc, "【医生综合报告】");
            if (report != null && report.getSummary() != null && !report.getSummary().isEmpty()) {
                XWPFParagraph reportPara = doc.createParagraph();
                XWPFRun reportRun = reportPara.createRun();
                reportRun.setText(report.getSummary());
                reportRun.setFontSize(12);
                reportRun.setFontFamily("宋体");
            } else {
                XWPFParagraph noReportPara = doc.createParagraph();
                XWPFRun noReportRun = noReportPara.createRun();
                noReportRun.setText("（暂无医生综合报告）");
                noReportRun.setFontSize(12);
                noReportRun.setFontFamily("宋体");
                noReportRun.setColor("999999");
            }

            addEmptyLine(doc);
            addEmptyLine(doc);

            // ===== 页脚 =====
            XWPFParagraph footer = doc.createParagraph();
            footer.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun footerRun = footer.createRun();
            footerRun.setText("— 本报告由健康管理系统自动生成 —");
            footerRun.setFontSize(10);
            footerRun.setFontFamily("宋体");
            footerRun.setColor("999999");

            doc.write(out);
        }
    }

    // ===== 辅助方法 =====

    /**
     * 批量导出全部预约报告到一个 Word 文档
     */
    public void exportBatchReport(String filePath, List<Appointment> appointments,
                                   List<CheckItemGroup> groups, List<List<CheckItem>> allItems) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {

            // ===== 总标题 =====
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("健康体检管理系统 — 预约报告（全部）");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("宋体");

            XWPFParagraph timePara = doc.createParagraph();
            timePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun timeRun = timePara.createRun();
            timeRun.setText("打印时间：" + java.time.LocalDateTime.now().format(DATETIME_FMT));
            timeRun.setFontSize(11);
            timeRun.setFontFamily("宋体");
            timeRun.setColor("808080");

            XWPFParagraph countPara = doc.createParagraph();
            countPara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun countRun = countPara.createRun();
            countRun.setText("共 " + appointments.size() + " 条预约记录");
            countRun.setFontSize(12);
            countRun.setFontFamily("宋体");

            addEmptyLine(doc);

            // 总费用汇总
            double grandTotal = 0.0;
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i) != null) {
                    grandTotal += groups.get(i).getPrice() != null ? groups.get(i).getPrice() : 0.0;
                }
            }
            XWPFParagraph totalSummary = doc.createParagraph();
            totalSummary.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun totalSummaryRun = totalSummary.createRun();
            totalSummaryRun.setText("全部预约总费用：¥" + String.format("%.2f", grandTotal));
            totalSummaryRun.setBold(true);
            totalSummaryRun.setFontSize(14);
            totalSummaryRun.setFontFamily("宋体");
            totalSummaryRun.setColor("CC0000");

            addEmptyLine(doc);
            addSeparator(doc);
            addEmptyLine(doc);

            // 逐条输出
            for (int idx = 0; idx < appointments.size(); idx++) {
                Appointment app = appointments.get(idx);
                CheckItemGroup group = groups.get(idx);
                List<CheckItem> items = allItems.get(idx);

                // 序号标题
                XWPFParagraph idxPara = doc.createParagraph();
                XWPFRun idxRun = idxPara.createRun();
                idxRun.setText("第 " + (idx + 1) + " 条 — " + (group != null ? group.getGroupName() : "未知套餐"));
                idxRun.setBold(true);
                idxRun.setFontSize(14);
                idxRun.setFontFamily("宋体");

                addEmptyLine(doc);

                // 预约信息
                addSectionTitle(doc, "【预约信息】");
                XWPFTable infoTable = doc.createTable(5, 2);
                fillInfoRow(infoTable, 0, "套餐名称", group != null ? group.getGroupName() : "未知");
                fillInfoRow(infoTable, 1, "套餐描述",
                        group != null && group.getDescription() != null ? group.getDescription() : "无");
                fillInfoRow(infoTable, 2, "预约日期",
                        app.getExamDate() != null ? app.getExamDate().format(DATE_FMT) : "未指定");
                fillInfoRow(infoTable, 3, "时段",
                        app.getExamTimeSlot() != null ? app.getExamTimeSlot() : "未指定");
                fillInfoRow(infoTable, 4, "支付状态", app.getPaymentStatusDisplay());

                addEmptyLine(doc);

                // 检查项目明细
                addSectionTitle(doc, "【检查项目明细】");
                if (items != null && !items.isEmpty()) {
                    XWPFTable itemsTable = doc.createTable(items.size() + 1, 5);
                    String[] headers = {"序号", "项目名称", "科室/分类", "参考值范围", "单价"};
                    XWPFTableRow headerRow = itemsTable.getRow(0);
                    for (int h = 0; h < headers.length; h++) {
                        XWPFTableCell cell = headerRow.getCell(h);
                        setCellText(cell, headers[h], true);
                        setCellBackground(cell, "4D88C5");
                        setCellTextColor(cell, "FFFFFF");
                    }
                    double totalPrice = 0.0;
                    for (int j = 0; j < items.size(); j++) {
                        CheckItem item = items.get(j);
                        XWPFTableRow row = itemsTable.getRow(j + 1);
                        Double price = item.getPrice() != null ? item.getPrice() : 0.0;
                        totalPrice += price;
                        fillItemsRow(row, 0, String.valueOf(j + 1), false);
                        fillItemsRow(row, 1, item.getItemName(), false);
                        fillItemsRow(row, 2, item.getCategory() != null ? item.getCategory() : "", false);
                        fillItemsRow(row, 3, item.getReferenceRange() != null ? item.getReferenceRange() : "", false);
                        fillItemsRow(row, 4, String.format("¥%.2f", price), false);
                    }
                    addEmptyLine(doc);
                    XWPFParagraph costPara = doc.createParagraph();
                    XWPFRun costRun = costPara.createRun();
                    Double gp = group != null ? group.getPrice() : totalPrice;
                    costRun.setText("费用合计：" + items.size() + " 项  |  套餐总价：¥" + String.format("%.2f", gp));
                    costRun.setBold(true);
                    costRun.setFontSize(12);
                    costRun.setFontFamily("宋体");
                    costRun.setColor("CC0000");
                } else {
                    XWPFParagraph emptyPara = doc.createParagraph();
                    XWPFRun emptyRun = emptyPara.createRun();
                    emptyRun.setText("（该预约暂无检查项目明细）");
                    emptyRun.setFontSize(12);
                    emptyRun.setFontFamily("宋体");
                    emptyRun.setColor("999999");
                }

                // 分页（最后一条不加）
                if (idx < appointments.size() - 1) {
                    XWPFParagraph breakPara = doc.createParagraph();
                    breakPara.setPageBreak(true);
                }
            }

            doc.write(out);
        }
    }

    private void addEmptyLine(XWPFDocument doc) {
        doc.createParagraph().createRun().setText("");
    }

    private void addSeparator(XWPFDocument doc) {
        XWPFParagraph sep = doc.createParagraph();
        XWPFRun sepRun = sep.createRun();
        sepRun.setText("——————————————————————————————————————————");
        sepRun.setColor("CCCCCC");
        sepRun.setFontSize(10);
    }

    private void addSectionTitle(XWPFDocument doc, String title) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("宋体");
    }

    private void setTableWidth(XWPFTable table, String... widths) {
        // POI 简单设置列宽
    }

    private void fillInfoRow(XWPFTable table, int rowIdx, String label, String value) {
        XWPFTableRow row = table.getRow(rowIdx);
        setCellText(row.getCell(0), label, true);
        setCellText(row.getCell(1), value, false);
    }

    private XWPFRun fillItemsRow(XWPFTableRow row, int cellIdx, String text, boolean bold) {
        return setCellText(row.getCell(cellIdx), text, bold);
    }

    private XWPFRun setCellText(XWPFTableCell cell, String text, boolean bold) {
        cell.removeParagraph(0);
        XWPFParagraph para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = para.createRun();
        run.setText(text != null ? text : "");
        run.setBold(bold);
        run.setFontSize(11);
        run.setFontFamily("宋体");
        return run;
    }

    private void setCellBackground(XWPFTableCell cell, String hexColor) {
        cell.setColor(hexColor);
    }

    private void setCellTextColor(XWPFTableCell cell, String hexColor) {
        // 通过 run 设置颜色
        if (!cell.getParagraphs().isEmpty()) {
            XWPFParagraph para = cell.getParagraphs().get(0);
            if (!para.getRuns().isEmpty()) {
                para.getRuns().get(0).setColor(hexColor);
            }
        }
    }
}
