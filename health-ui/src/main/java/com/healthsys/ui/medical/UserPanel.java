package com.healthsys.ui.medical;

import com.healthsys.common.entity.Users;
import com.healthsys.ui.HealthTheme;
import com.healthsys.ui.medical.UserDialog;
import com.healthsys.dao.UserDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class UserPanel extends CrudPanel<Users> {
    private UserDAO userDAO;
    private JTable table;
    private UserTableModel tableModel;
    private JTextField idSearchField;
    private JTextField nameSearchField;

    public UserPanel() {
        userDAO = new UserDAO();
        initializeTable();
        refreshData();
        setupButtonListeners();
        setupSearchPanel();
    }

    private void setupSearchPanel() {
        // 添加查询字段
        getSearchPanel().add(new JLabel("ID:"));
        idSearchField = new JTextField(8);
        idSearchField.setFont(HealthTheme.FONT_BODY_SM);
        getSearchPanel().add(idSearchField);

        getSearchPanel().add(new JLabel("姓名:"));
        nameSearchField = new JTextField(15);
        nameSearchField.setFont(HealthTheme.FONT_BODY_SM);
        getSearchPanel().add(nameSearchField);

        // 设置查询按钮事件
        getSearchButton().addActionListener(e -> searchUsers());
    }

    private void searchUsers() {
        String idStr = idSearchField.getText().trim();
        String name = nameSearchField.getText().trim();
        Long id = null;

        try {
            if (!idStr.isEmpty()) {
                id = Long.parseLong(idStr);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的ID", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Users> result = userDAO.search(id, name);
        tableModel.setData(result);
        tableModel.fireTableDataChanged();
    }

    private void setupButtonListeners() {
        // 添加按钮事件
        getAddButton().addActionListener(e -> {
            UserDialog dialog = new UserDialog(null);
            if (dialog.showDialog() == UserDialog.OK_OPTION) {
                Users newUser = dialog.getUser();
                if (userDAO.add(newUser)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "用户添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "用户添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 编辑按钮事件
        getEditButton().addActionListener(e -> {
            Users selected = getSelectedUser();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的用户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            UserDialog dialog = new UserDialog(selected);
            if (dialog.showDialog() == UserDialog.OK_OPTION) {
                Users updatedUser = dialog.getUser();
                if (userDAO.update(updatedUser)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "用户更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "用户更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 删除按钮事件
        getDeleteButton().addActionListener(e -> {
            Users selected = getSelectedUser();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的用户", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除用户 " + selected.getName() + " 吗?",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (userDAO.delete(selected.getId())) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "用户删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "用户删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    private void initializeTable() {
        tableModel = new UserTableModel();
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
        tableModel.setData(userDAO.getAll());
        tableModel.fireTableDataChanged();
    }

    public Users getSelectedUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getItemAt(selectedRow);
        }
        return null;
    }

    private class UserTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID", "电话", "姓名", "性别", "出生日期", "密码", "身份证号", "创建时间"};
        private List<Users> data;

        public void setData(List<Users> data) {
            this.data = data;
        }

        public Users getItemAt(int index) {
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
            Users user = data.get(row);
            switch (col) {
                case 0: return user.getId();
                case 1: return user.getPhone();
                case 2: return user.getName();
                case 3: return user.getGenderDisplay();
                case 4: return user.getBirthDate();
                case 5: return user.getPassword();
                case 6: return user.getIdNumber();
                case 7: return user.getCreatedAt();
                default: return null;
            }
        }
    }
}

