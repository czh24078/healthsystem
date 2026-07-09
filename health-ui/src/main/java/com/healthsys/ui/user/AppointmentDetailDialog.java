package com.healthsys.ui.user;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.Report;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预约详情弹窗 — 支持未处理/已处理两种状态
 * 未处理：显示预约信息 + 套餐项目明细
 * 已处理：额外显示检查结果 + 医生报告
 */
public class AppointmentDetailDialog extends JDialog {

    private static final Color MAIN_COLOR = new Color(70, 104, 197);
    private static final Color ABNORMAL_COLOR = new Color(198, 40, 40);
    private static final Color NORMAL_COLOR = new Color(46, 125, 50);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AppointmentDetailDialog(Appointment appointment, CheckItemGroup group,
                                   List<CheckItem> items, List<ExamRecord> examRecords, Report report) {
        setTitle("预约详情 — " + (group != null ? group.getGroupName() : "未知套餐"));
        setSize(850, 700);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // ===== 顶部：报告摘要（仅已处理且有报告时显示）=====
        if (report != null && report.getSummary() != null && !report.getSummary().isEmpty()) {
            JPanel reportPanel = new JPanel(new BorderLayout());
            reportPanel.setBorder(BorderFactory.createTitledBorder("医生综合报告"));
            reportPanel.setBackground(Color.WHITE);

            JTextArea reportArea = new JTextArea(report.getSummary());
            reportArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            reportArea.setEditable(false);
            reportArea.setLineWrap(true);
            reportArea.setWrapStyleWord(true);
            reportArea.setBackground(new Color(252, 252, 252));
            JScrollPane reportScroll = new JScrollPane(reportArea);
            reportScroll.setPreferredSize(new Dimension(800, 120));
            reportPanel.add(reportScroll, BorderLayout.CENTER);
            mainPanel.add(reportPanel, BorderLayout.NORTH);
        }

        // ===== 中部：JTabbedPane（套餐项目 + 检查结果）=====
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 标签1：预约基本信息 + 套餐项目
        JPanel packageTab = createPackageTab(appointment, group, items);
        tabbedPane.addTab("套餐项目", packageTab);

        // 标签2：检查结果（仅已处理且有结果时）
        boolean hasResults = examRecords != null && !examRecords.isEmpty();
        if (hasResults) {
            JPanel resultsTab = createResultsTab(examRecords, items);
            tabbedPane.addTab("检查结果", resultsTab);
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        // ===== 底部：费用汇总 + 关闭按钮 =====
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(Color.WHITE);

        double totalPrice = items.stream().mapToDouble(i -> i.getPrice() != null ? i.getPrice() : 0.0).sum();
        JLabel totalLabel = new JLabel(String.format("费用合计：%d 项  |  套餐总价：¥%.2f",
                items.size(), group != null ? group.getPrice() : totalPrice));
        totalLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        totalLabel.setForeground(new Color(180, 40, 40));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        southPanel.add(totalLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        closeButton.setBackground(MAIN_COLOR);
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(closeButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);

        // 组装
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    /**
     * 构建"套餐项目"标签页：预约基本信息和检查项目明细
     */
    private JPanel createPackageTab(Appointment appointment, CheckItemGroup group, List<CheckItem> items) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 预约基本信息
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        infoPanel.setBorder(BorderFactory.createTitledBorder("预约信息"));
        infoPanel.setBackground(Color.WHITE);

        addInfoRow(infoPanel, "套餐名称：", group != null ? group.getGroupName() : "未知");
        addInfoRow(infoPanel, "套餐描述：", group != null && group.getDescription() != null
                ? group.getDescription() : "无");
        addInfoRow(infoPanel, "预约日期：",
                appointment.getExamDate() != null
                        ? appointment.getExamDate().format(DATE_FMT) : "未指定");
        addInfoRow(infoPanel, "时段：",
                appointment.getExamTimeSlot() != null ? appointment.getExamTimeSlot() : "未指定");
        addInfoRow(infoPanel, "预约时间：",
                appointment.getAppointmentTime() != null
                        ? appointment.getAppointmentTime().format(DATETIME_FMT) : "未指定");
        addInfoRow(infoPanel, "支付状态：", appointment.getPaymentStatusDisplay());

        panel.add(infoPanel, BorderLayout.NORTH);

        // 检查项目明细表
        JTable itemsTable = new JTable(new CheckItemTableModel(items));
        itemsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        itemsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        itemsTable.setRowHeight(28);
        itemsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        itemsTable.getColumnModel().getColumn(4).setMaxWidth(80);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "检查项目明细（共 " + items.size() + " 项）"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 构建"检查结果"标签页：实际体检结果
     */
    private JPanel createResultsTab(List<ExamRecord> examRecords, List<CheckItem> items) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 构建 itemId → CheckItem 映射表
        Map<Long, CheckItem> itemMap = new HashMap<>();
        for (CheckItem item : items) {
            itemMap.put(item.getId(), item);
        }

        // 结果表格
        JTable resultsTable = new JTable(new ExamResultTableModel(examRecords, itemMap));
        resultsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        resultsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        resultsTable.setRowHeight(28);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(50);   // 序号
        resultsTable.getColumnModel().getColumn(4).setMaxWidth(160);  // 参考范围
        resultsTable.getColumnModel().getColumn(5).setMaxWidth(60);   // 异常

        // 异常列颜色渲染
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(new AbnormalCellRenderer());

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "检查结果（共 " + examRecords.size() + " 项）"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lb = new JLabel(label);
        lb.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(lb);
        JLabel vl = new JLabel(value);
        vl.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(vl);
    }

    /**
     * 套餐项目表格模型
     */
    private static class CheckItemTableModel extends AbstractTableModel {
        private final String[] columnNames = {"序号", "项目名称", "科室/分类", "参考值范围", "单价"};
        private final List<CheckItem> data;

        CheckItemTableModel(List<CheckItem> data) {
            this.data = data;
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CheckItem item = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return rowIndex + 1;
                case 1: return item.getItemName();
                case 2: return item.getCategory();
                case 3: return item.getReferenceRange();
                case 4: return String.format("¥%.2f", item.getPrice() != null ? item.getPrice() : 0.0);
                default: return "";
            }
        }
    }

    /**
     * 检查结果表格模型
     */
    private static class ExamResultTableModel extends AbstractTableModel {
        private final String[] columnNames = {"序号", "项目名称", "结果值", "单位", "参考范围", "异常", "医生备注"};
        private final List<ExamRecord> data;
        private final Map<Long, CheckItem> itemMap;

        ExamResultTableModel(List<ExamRecord> data, Map<Long, CheckItem> itemMap) {
            this.data = data;
            this.itemMap = itemMap;
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ExamRecord record = data.get(rowIndex);
            CheckItem item = itemMap.get(record.getItemId());
            switch (columnIndex) {
                case 0: return rowIndex + 1;
                case 1: return record.getItemName() != null ? record.getItemName()
                        : (item != null ? item.getItemName() : "未知项目");
                case 2: return record.getResultValue();
                case 3: return item != null && item.getUnit() != null ? item.getUnit() : "";
                case 4: return item != null && item.getReferenceRange() != null ? item.getReferenceRange() : "";
                case 5: return Boolean.TRUE.equals(record.getIsAbnormal()) ? "异常" : "正常";
                case 6: return record.getDoctorNote() != null ? record.getDoctorNote() : "";
                default: return "";
            }
        }
    }

    /**
     * 异常列渲染器 — 异常红色，正常绿色
     */
    private static class AbnormalCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("微软雅黑", Font.BOLD, 12));
            if (!isSelected) {
                if ("异常".equals(value)) {
                    lbl.setForeground(ABNORMAL_COLOR);
                } else {
                    lbl.setForeground(NORMAL_COLOR);
                }
            }
            return lbl;
        }
    }
}
