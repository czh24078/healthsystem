package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItem;
import com.healthsys.ui.HealthTheme;
import com.healthsys.ui.medical.CheckItemDialog;
import com.healthsys.dao.CheckItemDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class CheckItemPanel extends CrudPanel<CheckItem> {
    private CheckItemDAO checkItemDAO;
    private JTable table;
    private CheckItemTableModel tableModel;
    private JTextField nameSearchField;
    private JTextField codeSearchField;
    public CheckItemPanel() {
        checkItemDAO = new CheckItemDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        // 添加查询字段
        getSearchPanel().add(new JLabel("名称:"));
        nameSearchField = new JTextField(15);
        nameSearchField.setFont(HealthTheme.FONT_BODY_SM);
        getSearchPanel().add(nameSearchField);

        getSearchPanel().add(new JLabel("代码:"));
        codeSearchField = new JTextField(15);
        codeSearchField.setFont(HealthTheme.FONT_BODY_SM);
        getSearchPanel().add(codeSearchField);

        // 设置查询按钮事件
        getSearchButton().addActionListener(e -> searchCheckItems());
    }

    private void searchCheckItems() {
        String name = nameSearchField.getText().trim();
        String code = codeSearchField.getText().trim();

        List<CheckItem> result = checkItemDAO.search(name, code);
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private void setupButtonListeners() {
        // 添加按钮事件
        getAddButton().addActionListener(e -> {
            CheckItemDialog dialog = new CheckItemDialog(null);
            if (dialog.showDialog() == CheckItemDialog.OK_OPTION) {
                CheckItem newItem = dialog.getCheckItem();
                if (checkItemDAO.add(newItem)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查项添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查项添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 编辑按钮事件
        getEditButton().addActionListener(e -> {
            CheckItem selected = getSelectedCheckItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的检查项", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CheckItemDialog dialog = new CheckItemDialog(selected);
            if (dialog.showDialog() == CheckItemDialog.OK_OPTION) {
                CheckItem updatedItem = dialog.getCheckItem();
                if (checkItemDAO.update(updatedItem)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查项更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查项更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 删除按钮事件
        getDeleteButton().addActionListener(e -> {
            CheckItem selected = getSelectedCheckItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的检查项", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除检查项 " + selected.getName() + " 吗?",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (checkItemDAO.delete(selected.getId())) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "检查项删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "检查项删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void initializeTable() {
        tableModel = new CheckItemTableModel();
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

        // 列宽自动调整
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setContent(scrollPane);
    }

    @Override
    public void refreshData() {
        tableModel.setData(checkItemDAO.getAll());
        tableModel.fireTableDataChanged();
    }

    public CheckItem getSelectedCheckItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getItemAt(selectedRow);
        }
        return null;
    }

    private class CheckItemTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID", "名称", "代码", "分类", "单位", "参考范围", "价格", "状态", "创建时间"};
        private List<CheckItem> data;

        public void setData(List<CheckItem> data) {
            this.data = data;
        }

        public CheckItem getItemAt(int index) {
            return data.get(index);
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            CheckItem item = data.get(row);
            switch (col) {
                case 0: return item.getId();
                case 1: return item.getName();
                case 2: return item.getCode();
                case 3: return item.getCategory();
                case 4: return item.getUnit();
                case 5: return item.getReferenceRange();
                case 6: return item.getPrice();
                case 7: return item.getStatus() != null && item.getStatus() == 1 ? "启用" : "停用";
                case 8: return item.getCreatedAt();
                default: return null;
            }
        }
    }

}

