package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.service.MockPaymentService;
import com.healthsys.service.PaymentService;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.ui.HealthTheme;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.ZoneId;
import java.util.Date;

public class MessagesView {
    private JPanel messagesPanel;
    private Users currentUser;
    private AppointmentService controller;
    private JTable groupTable;
    private DefaultTableModel groupModel;

    public MessagesView(Users currentUser) {
        this.currentUser = currentUser;
        this.controller = new AppointmentService();
        initializeUI();
    }

    private void initializeUI() {
        messagesPanel = new JPanel(new BorderLayout(0, 0));
        messagesPanel.setBackground(Color.WHITE);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== 标题栏 =====
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("体检套餐", JLabel.CENTER);
        titleLabel.setFont(HealthTheme.FONT_TITLE);
        titleLabel.setForeground(HealthTheme.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("浏览并预约适合您的体检套餐", JLabel.CENTER);
        subtitleLabel.setFont(HealthTheme.FONT_BODY_SM);
        subtitleLabel.setForeground(HealthTheme.TEXT_HINT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // 右侧刷新按钮
        JButton refreshBtn = createStyledButton("刷新列表", HealthTheme.BTN_SECONDARY);
        refreshBtn.setPreferredSize(new Dimension(100, 38));
        refreshBtn.addActionListener(e -> refreshGroupData());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(refreshBtn);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        // ===== 检查组表格 =====
        String[] groupColumns = { "序号", "检查组名称", "描述", "价格", "预约", "查看详情" };
        groupModel = new DefaultTableModel(groupColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };

        groupTable = new JTable(groupModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5;
            }
        };
        
        groupTable.setRowHeight(36);
        groupTable.setFont(HealthTheme.FONT_BODY_SM);
        groupTable.getTableHeader().setFont(HealthTheme.FONT_BUTTON);
        groupTable.getTableHeader().setBackground(HealthTheme.TABLE_HEADER);
        groupTable.getTableHeader().setForeground(Color.WHITE);
        groupTable.getTableHeader().setReorderingAllowed(false);
        groupTable.getTableHeader().setResizingAllowed(false);
        groupTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        groupTable.setSelectionBackground(HealthTheme.TABLE_SELECTED);
        groupTable.setSelectionForeground(HealthTheme.TEXT_PRIMARY);
        groupTable.setGridColor(HealthTheme.BORDER);
        groupTable.setShowHorizontalLines(true);
        groupTable.setShowVerticalLines(false);
        groupTable.setIntercellSpacing(new Dimension(0, 0));
        groupTable.setBackground(Color.WHITE);
        
        // 自定义表头渲染器
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(HealthTheme.TABLE_HEADER);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(HealthTheme.FONT_BUTTON);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        groupTable.getTableHeader().setDefaultRenderer(headerRenderer);
        
        // 设置列宽
        groupTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        groupTable.getColumnModel().getColumn(0).setMaxWidth(70);
        groupTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        groupTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        groupTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        groupTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        groupTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        
        // 为数据列设置居中对齐渲染器
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < groupTable.getColumnCount(); i++) {
            groupTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 加载检查组数据
        loadGroupData();

        // 自定义单元格渲染器和编辑器
        groupTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        groupTable.getColumnModel().getColumn(4)
                .setCellEditor(new GroupButtonEditor(new JCheckBox(), messagesPanel));
        groupTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        groupTable.getColumnModel().getColumn(5)
                .setCellEditor(new MessagesDetailButtonEditor(new JCheckBox(), messagesPanel, controller));

        JScrollPane scrollPane = new JScrollPane(groupTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HealthTheme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        messagesPanel.add(headerPanel, BorderLayout.NORTH);
        messagesPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadGroupData() {
        groupModel.setRowCount(0);
        java.util.List<CheckItemGroup> groups = controller.getAllGroups();
        int index = 1;
        for (CheckItemGroup pkg : groups) {
            Object[] rowData = {
                    index++,
                    pkg.getName(),
                    pkg.getDescription(),
                    pkg.getPrice(),
                    "预约",
                    "查看详情"
            };
            groupModel.addRow(rowData);
        }
    }

    private void refreshGroupData() {
        loadGroupData();
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

    // 检查组表格按钮编辑器
    class GroupButtonEditor extends DefaultCellEditor {
        private String label;
        private JPanel parentPanel;

        public GroupButtonEditor(JCheckBox checkBox, JPanel parentPanel) {
            super(checkBox);
            this.parentPanel = parentPanel;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                // 直接从数据源获取groupId
                java.util.List<CheckItemGroup> groups = controller.getAllGroups();
                if (row >= 0 && row < groups.size()) {
                    Long groupId = groups.get(row).getId();
                    showTimeSelectionDialog(groupId);
                }
                fireEditingStopped();
            });
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    private void showTimeSelectionDialog(Long groupId) {
        JDialog timeDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(messagesPanel), "选择预约时间", true);
        timeDialog.setSize(420, 330);
        timeDialog.setLocationRelativeTo(messagesPanel);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("预约日期:"), gbc);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        gbc.gridx = 1;
        panel.add(dateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("预约时间:"), gbc);

        String[] timeSlots = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"};
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        gbc.gridx = 1;
        panel.add(timeCombo, gbc);

        // 医生选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("选择医生:"), gbc);

        java.util.List<com.healthsys.common.entity.Doctor> doctors = new com.healthsys.dao.DoctorDAO().getAll();
        JComboBox<String> doctorCombo = new JComboBox<>();
        doctorCombo.addItem("不指定");
        for (com.healthsys.common.entity.Doctor d : doctors) {
            doctorCombo.addItem(d.getName() + " - " + d.getDepartment() + " - " + d.getTitle());
        }
        gbc.gridx = 1;
        panel.add(doctorCombo, gbc);

        JButton submitBtn = new JButton("确认预约");
        submitBtn.setBackground(new Color(41, 75, 166));
        submitBtn.setForeground(Color.BLACK);
        submitBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        submitBtn.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.ipady = 8;
        panel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(timeDialog, "请选择预约日期", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String timeStr = (String) timeCombo.getSelectedItem();
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            java.time.LocalDate localDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDateTime ldt = localDate.atTime(hour, minute);

            if (ldt.isBefore(java.time.LocalDateTime.now())) {
                JOptionPane.showMessageDialog(timeDialog, "不能预约过去的时间", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date appointmentTime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

            int doctorIndex = doctorCombo.getSelectedIndex();
            Long doctorId = doctorIndex > 0 ? doctors.get(doctorIndex - 1).getDoctorId() : null;

            Appointment newAppointment = controller.createAppointment(currentUser, groupId, appointmentTime, doctorId);
            if (newAppointment != null) {
                JOptionPane.showMessageDialog(timeDialog, "预约成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                timeDialog.dispose();
                int payOption = JOptionPane.showConfirmDialog(
                        messagesPanel,
                        "是否立即支付？",
                        "支付确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (payOption == JOptionPane.YES_OPTION) {
                    // 立即支付
                    Double price = controller.getAppointmentPrice(newAppointment.getId());
                    if (price != null) {
                        int confirmPay = JOptionPane.showConfirmDialog(
                                messagesPanel,
                                "您需要支付: ¥" + price + "，是否继续？",
                                "支付确认",
                                JOptionPane.YES_NO_OPTION);
                        if (confirmPay == JOptionPane.YES_OPTION) {
                            PaymentService paymentService = new MockPaymentService();
                            boolean paid = paymentService.pay(newAppointment.getId(), price);
                            if (paid && controller.updatePaymentStatus(newAppointment.getId(), true)) {
                                JOptionPane.showMessageDialog(messagesPanel,
                                        "支付成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(messagesPanel,
                                        "支付失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(messagesPanel,
                                "无法获取检查组价格", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(messagesPanel, "您可稍后支付，请注意支付截止时间", "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(timeDialog, "预约失败!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        timeDialog.add(panel);
        timeDialog.setVisible(true);
    }

    /**
     * 从表格行获取检查组ID
     */
    Long getGroupIdFromRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= groupModel.getRowCount()) {
            return null;
        }
        java.util.List<CheckItemGroup> groups = controller.getAllGroups();
        if (rowIndex < groups.size()) {
            return groups.get(rowIndex).getId();
        }
        return null;
    }

    /**
     * 创建美化的按钮
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(HealthTheme.FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
}

class MessagesDetailButtonEditor extends DefaultCellEditor {
    private String label;
    private JPanel parentPanel;
    private AppointmentService controller;

    public MessagesDetailButtonEditor(JCheckBox checkBox, JPanel parentPanel, AppointmentService controller) {
        super(checkBox);
        this.parentPanel = parentPanel;
        this.controller = controller;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        label = (value == null) ? "" : value.toString();
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            // 直接从数据源获取groupId
            java.util.List<CheckItemGroup> groups = controller.getAllGroups();
            if (row >= 0 && row < groups.size()) {
                Long groupId = groups.get(row).getId();
                CheckItemGroup selectedGroup = controller.getCheckItemGroupById(groupId);
                if (selectedGroup != null) {
                    showGroupDetail(selectedGroup);
                }
            }
            fireEditingStopped();
        });
        return button;
    }

    private void showGroupDetail(CheckItemGroup checkItemGroup) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentPanel),
                "检查组详情 - " + checkItemGroup.getName(), true);
        detailDialog.setSize(800, 600);
        detailDialog.setLocationRelativeTo(parentPanel);

        PackageDetailView detailView = new PackageDetailView(checkItemGroup);
        detailDialog.add(detailView.getDetailPanel());
        detailDialog.setVisible(true);
    }

    public Object getCellEditorValue() {
        return label;
    }
}
