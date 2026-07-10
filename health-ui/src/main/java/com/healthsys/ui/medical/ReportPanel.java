package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Report;
import com.healthsys.dao.AppointmentDAO;
import com.healthsys.dao.ReportDAO;
import com.healthsys.ui.HealthTheme;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReportPanel extends JPanel {
    private final Long doctorId;
    private final ReportDAO reportDAO = new ReportDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private JTable table;
    private ReportTableModel tableModel;
    private String quickFilter = null; // null=全部, "REPORTED", "UNREPORTED"

    private static final Color REPORTED_BG = new Color(232, 245, 233);
    private static final Color REPORTED_FG = new Color(46, 125, 50);
    private static final Color UNREPORTED_BG = new Color(255, 243, 224);
    private static final Color UNREPORTED_FG = new Color(230, 81, 0);

    public ReportPanel(Long doctorId) {
        this.doctorId = doctorId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        createToolbar();
        initializeTable();
        refreshData();
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton writeBtn = CrudPanel.createStyledButton("撰写报告", CrudPanel.BTN_WARNING_YELLOW);
        writeBtn.addActionListener(e -> {
            ReportEditDialog dialog = new ReportEditDialog(doctorId, null);
            if (dialog.showDialog() == ReportEditDialog.OK_OPTION) {
                refreshData();
            }
        });

        JButton editBtn = CrudPanel.createStyledButton("查看/编辑", CrudPanel.BTN_SUCCESS_GREEN);
        editBtn.addActionListener(e -> {
            Appointment selected = getSelectedAppointment();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择一条记录", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Report report = reportDAO.getByAppointmentId(selected.getId());
            if (report == null) {
                JOptionPane.showMessageDialog(this, "该预约尚未撰写报告", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ReportEditDialog dialog = new ReportEditDialog(doctorId, report);
            if (dialog.showDialog() == ReportEditDialog.OK_OPTION) {
                refreshData();
            }
        });

        buttonPanel.add(writeBtn);
        buttonPanel.add(editBtn);
        toolbar.add(buttonPanel, BorderLayout.WEST);

        // 快速分类按钮放在右侧
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        filterPanel.setBackground(Color.WHITE);

        JButton allBtn = new JButton("全部");
        JButton reportedBtn = new JButton("已撰写");
        JButton unreportedBtn = new JButton("未撰写");
        Font ff = new Font("微软雅黑", Font.BOLD, 12);
        for (JButton btn : new JButton[]{allBtn, reportedBtn, unreportedBtn}) {
            btn.setFont(ff); btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(75, 28));
        }
        allBtn.setBackground(new Color(70, 104, 197)); allBtn.setForeground(Color.BLACK);
        reportedBtn.setBackground(new Color(76, 175, 80)); reportedBtn.setForeground(Color.BLACK);
        unreportedBtn.setBackground(new Color(255, 152, 0)); unreportedBtn.setForeground(Color.BLACK);

        allBtn.addActionListener(e -> { quickFilter = null; refreshData(); });
        reportedBtn.addActionListener(e -> { quickFilter = "REPORTED"; refreshData(); });
        unreportedBtn.addActionListener(e -> { quickFilter = "UNREPORTED"; refreshData(); });

        filterPanel.add(allBtn);
        filterPanel.add(reportedBtn);
        filterPanel.add(unreportedBtn);
        toolbar.add(filterPanel, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initializeTable() {
        tableModel = new ReportTableModel();
        table = new JTable(tableModel);

        // 使用HealthTheme统一样式
        table.setFont(HealthTheme.FONT_BODY_SM);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        // 自定义表头渲染器
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(HealthTheme.TABLE_HEADER);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(HealthTheme.FONT_BUTTON);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        // 数据列居中对齐
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, cellRenderer);

        // 列: 患者, 检查组, 检查日期, 报告状态
        int[] widths = {80, 180, 110, 80};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // 报告状态列 - 居中显示,无彩色背景
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    lbl.setForeground(HealthTheme.TEXT_PRIMARY);
                }
                lbl.setFont(HealthTheme.FONT_BODY_SM);
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        // 获取该医生所有已完成的预约
        List<Appointment> allCompleted = appointmentDAO.searchByFilters(
                doctorId, null, null, "COMPLETED");

        // 按报告状态过滤
        if ("REPORTED".equals(quickFilter)) {
            allCompleted = allCompleted.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getHasReport()))
                    .collect(Collectors.toList());
        } else if ("UNREPORTED".equals(quickFilter)) {
            allCompleted = allCompleted.stream()
                    .filter(a -> !Boolean.TRUE.equals(a.getHasReport()))
                    .collect(Collectors.toList());
        }

        tableModel.setData(allCompleted);
        tableModel.fireTableDataChanged();
    }

    private Appointment getSelectedAppointment() {
        int row = table.getSelectedRow();
        if (row >= 0) return tableModel.getItemAt(row);
        return null;
    }

    private class ReportTableModel extends AbstractTableModel {
        private final String[] columnNames = {"患者", "检查组", "检查日期", "报告"};
        private List<Appointment> data;

        public void setData(List<Appointment> data) { this.data = data; }
        public Appointment getItemAt(int index) { return data.get(index); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Appointment a = data.get(row);
            return switch (col) {
                case 0 -> a.getUserName() != null ? a.getUserName() : "";
                case 1 -> a.getGroupName() != null ? a.getGroupName() : "";
                case 2 -> a.getExamDate() != null ? a.getExamDate() : "";
                case 3 -> a.getHasReportDisplay();
                default -> null;
            };
        }
    }
}
