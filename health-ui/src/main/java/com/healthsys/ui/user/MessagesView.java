package com.healthsys.common.view;

import com.healthsys.service.AppointmentController;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.CheckItemGroup;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MessagesView {
    private JPanel messagesPanel;
    private Users currentUser;
    private AppointmentController controller;

    public MessagesView(Users currentUser) {
        this.currentUser = currentUser;
        this.controller = new AppointmentController();
        initializeUI();
    }

    private void initializeUI() {
        messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("体检套餐信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titlePanel.add(titleLabel);

        // 套餐表格
        String[] packageColumns = { "ID", "套餐名称", "描述", "价格", "预约", "查看详情" };
        DefaultTableModel packageModel = new DefaultTableModel(packageColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // 只有操作列可编辑
            }
        };

        JTable packageTable = new JTable(packageModel);
        packageTable.setRowHeight(30);
        packageTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        packageTable.getColumnModel().getColumn(4)
                .setCellEditor(new PackageButtonEditor(new JCheckBox(), messagesPanel));
        packageTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        packageTable.getColumnModel().getColumn(5)
                .setCellEditor(new DetailButtonEditor(new JCheckBox(), messagesPanel, controller));

        // 加载套餐数据
        loadPackageData(packageModel);

        messagesPanel.add(titlePanel, BorderLayout.NORTH);
        messagesPanel.add(new JScrollPane(packageTable), BorderLayout.CENTER);
    }

    private void loadPackageData(DefaultTableModel model) {
        model.setRowCount(0);
        // 加载套餐数据
        java.util.List<CheckItemGroup> packages = controller.getAllPackages();
        for (CheckItemGroup pkg : packages) {
            Object[] rowData = {
                    pkg.getId(),
                    pkg.getName(),
                    pkg.getDescription(),
                    pkg.getPrice(),
                    "预约",
                    "查看详情"
            };
            model.addRow(rowData);
        }
    }

    public JPanel getMessagesPanel() {
        return messagesPanel;
    }

    // 表格按钮渲染器
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // 套餐表格按钮编辑器
    class PackageButtonEditor extends DefaultCellEditor {
        private String label;
        private JPanel parentPanel;

        public PackageButtonEditor(JCheckBox checkBox, JPanel parentPanel) {
            super(checkBox);
            this.parentPanel = parentPanel;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                Long packageId = (Long) table.getValueAt(row, 0);
                showTimeSelectionDialog(packageId);
                fireEditingStopped();
            });
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    private void showTimeSelectionDialog(Long packageId) {
        JDialog timeDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(messagesPanel), "选择预约时间", true);
        timeDialog.setSize(400, 200);
        timeDialog.setLocationRelativeTo(messagesPanel);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 预约时间选择
        panel.add(new JLabel("预约时间:"));
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "yyyy-MM-dd HH:mm");
        timeSpinner.setEditor(timeEditor);
        panel.add(timeSpinner);

        // 提交按钮
        JButton submitBtn = new JButton("确认预约");
        submitBtn.addActionListener(e -> {
            java.util.Date appointmentTime = (java.util.Date) timeSpinner.getValue();

            if (controller.createAppointment(currentUser, packageId, appointmentTime)) {
                JOptionPane.showMessageDialog(timeDialog, "预约成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                timeDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(timeDialog, "预约失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel());
        panel.add(submitBtn);

        timeDialog.add(panel);
        timeDialog.setVisible(true);
    }
}

class DetailButtonEditor extends DefaultCellEditor {
    private String label;
    private JPanel parentPanel;
    private AppointmentController controller;

    public DetailButtonEditor(JCheckBox checkBox, JPanel parentPanel, AppointmentController controller) {
        super(checkBox);
        this.parentPanel = parentPanel;
        this.controller = controller;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            Long packageId = (Long) table.getValueAt(row, 0);
            CheckItemGroup selectedPackage = controller.getAllPackages().stream()
                    .filter(p -> p.getId().equals(packageId))
                    .findFirst()
                    .orElse(null);

            if (selectedPackage != null) {
                showPackageDetail(selectedPackage);
            }
            fireEditingStopped();
        });
        return button;
    }

    private void showPackageDetail(CheckItemGroup checkItemGroup) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentPanel),
                "套餐详情 - " + checkItemGroup.getName(), true);
        detailDialog.setSize(800, 600);
        detailDialog.setLocationRelativeTo(parentPanel);

        com.healthsys.common.view.appointment.PackageDetailView detailView = new com.healthsys.common.view.appointment.PackageDetailView(
                checkItemGroup);
        detailDialog.add(detailView.getDetailPanel());
        detailDialog.setVisible(true);
    }

    public Object getCellEditorValue() {
        return label;
    }
}