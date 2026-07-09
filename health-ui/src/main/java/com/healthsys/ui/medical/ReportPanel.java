package com.healthsys.ui.medical;

import com.healthsys.common.entity.Report;
import com.healthsys.dao.ReportDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class ReportPanel extends JPanel {
    private final Long doctorId;
    private final ReportDAO reportDAO = new ReportDAO();
    private JTable table;
    private ReportTableModel tableModel;

    public ReportPanel(Long doctorId) {
        this.doctorId = doctorId;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        createToolbar();
        initializeTable();
        refreshData();
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(245, 245, 245));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton writeBtn = CrudPanel.createStyledButton("撰写报告", new Color(102, 204, 153));
        writeBtn.addActionListener(e -> {
            ReportEditDialog dialog = new ReportEditDialog(doctorId, null);
            if (dialog.showDialog() == ReportEditDialog.OK_OPTION) {
                refreshData();
            }
        });

        JButton editBtn = CrudPanel.createStyledButton("查看/编辑", new Color(153, 204, 255));
        editBtn.addActionListener(e -> {
            Report selected = getSelectedReport();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择一份报告", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ReportEditDialog dialog = new ReportEditDialog(doctorId, selected);
            if (dialog.showDialog() == ReportEditDialog.OK_OPTION) {
                refreshData();
            }
        });

        buttonPanel.add(writeBtn);
        buttonPanel.add(editBtn);
        toolbar.add(buttonPanel, BorderLayout.WEST);
        add(toolbar, BorderLayout.NORTH);
    }

    private void initializeTable() {
        tableModel = new ReportTableModel();
        table = new JTable(tableModel);

        Font tableFont = new Font("微软雅黑", Font.PLAIN, 14);
        table.setFont(tableFont);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(36);
        table.setSelectionBackground(new Color(220, 230, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // 列: 患者, 检查组, 检查日期, 上传时间
        int[] widths = {80, 180, 100, 150};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        tableModel.setData(reportDAO.getByDoctorIdWithJoin(doctorId));
        tableModel.fireTableDataChanged();
    }

    private Report getSelectedReport() {
        int row = table.getSelectedRow();
        if (row >= 0) return tableModel.getItemAt(row);
        return null;
    }

    private class ReportTableModel extends AbstractTableModel {
        private final String[] columnNames = {"患者", "检查组", "检查日期", "上传时间"};
        private List<Report> data;

        public void setData(List<Report> data) { this.data = data; }
        public Report getItemAt(int index) { return data.get(index); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Report r = data.get(row);
            return switch (col) {
                case 0 -> r.getUserName() != null ? r.getUserName() : "";
                case 1 -> r.getGroupName() != null ? r.getGroupName() : "";
                case 2 -> r.getExamDate() != null ? r.getExamDate() : "";
                case 3 -> r.getUploadTime();
                default -> null;
            };
        }
    }
}
