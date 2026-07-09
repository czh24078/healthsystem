package com.healthsys.ui.user;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约详情弹窗 — 展示预约套餐包含的检查项目明细和费用汇总
 */
public class AppointmentDetailDialog extends JDialog {

    private static final Color MAIN_COLOR = new Color(70, 104, 197);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AppointmentDetailDialog(Appointment appointment, CheckItemGroup group,
                                   List<CheckItem> items) {
        setTitle("预约详情 — " + (group != null ? group.getGroupName() : "未知套餐"));
        setSize(850, 600);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // ===== 顶部：预约基本信息 =====
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
                        ? appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "未指定");
        addInfoRow(infoPanel, "支付状态：", appointment.getPaymentStatusDisplay());

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // ===== 中部：检查项目明细表格 =====
        JTable itemsTable = new JTable(new CheckItemTableModel(items));
        itemsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        itemsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        itemsTable.setRowHeight(28);
        itemsTable.getColumnModel().getColumn(0).setMaxWidth(50);  // 序号
        itemsTable.getColumnModel().getColumn(4).setMaxWidth(80);  // 单价

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "检查项目明细（共 " + items.size() + " 项）"));

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
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(southPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
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
     * 检查项目表格模型
     */
    private static class CheckItemTableModel extends AbstractTableModel {
        private final String[] columnNames = {"序号", "项目名称", "科室/分类", "参考值范围", "单价"};
        private final List<CheckItem> data;

        CheckItemTableModel(List<CheckItem> data) {
            this.data = data;
        }

        @Override
        public int getRowCount() { return data.size(); }

        @Override
        public int getColumnCount() { return columnNames.length; }

        @Override
        public String getColumnName(int column) { return columnNames[column]; }

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
}
