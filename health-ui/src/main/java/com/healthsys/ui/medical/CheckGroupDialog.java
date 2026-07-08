package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.dao.CheckItemGroupDAO;
import com.healthsys.ui.medical.CrudPanel;
import javax.swing.*;
import java.awt.*;

public class CheckGroupDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private CheckItemGroup checkItemGroup;
    private int option = CANCEL_OPTION;

    // 主色调
    private final Color MAIN_COLOR = new Color(70, 104, 197);
    private final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private final Font FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    // 对话框组件
    private JTextField idField;
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField priceField;
    private JTextField dailyLimitField;
    private JComboBox<String> statusCombo;
    private JList<CheckItem> itemList;

    public CheckGroupDialog(CheckItemGroup checkItemGroup) {
        this.checkItemGroup = checkItemGroup != null ? checkItemGroup : new CheckItemGroup();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(600, 450);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(checkItemGroup.getId() == null ? "新增检查组" : "编辑检查组");

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(checkItemGroup.getId() == null ? "新增检查组" : "编辑检查组");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID字段（仅显示，不可编辑）
        idField = createStyledTextField(checkItemGroup.getId() != null ? checkItemGroup.getId().toString() : "自动生成");
        idField.setEditable(false);
        addFormField(formPanel, gbc, 0, "检查组ID:", idField);

        // 名称字段
        nameField = createStyledTextField(checkItemGroup.getName());
        addFormField(formPanel, gbc, 1, "检查组名称:", nameField);

        // 描述字段
        descriptionField = new JTextField(checkItemGroup.getDescription());
        descriptionField.setFont(FIELD_FONT);
        descriptionField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        addFormField(formPanel, gbc, 2, "检查组描述:", descriptionField);

        // 价格字段
        priceField = createStyledTextField(checkItemGroup.getPrice() != null ? checkItemGroup.getPrice().toString() : "");
        addFormField(formPanel, gbc, 3, "检查组价格:", priceField);

        // 每日限额
        dailyLimitField = createStyledTextField(checkItemGroup.getDailyLimit() != null ? checkItemGroup.getDailyLimit().toString() : "50");
        addFormField(formPanel, gbc, 4, "每日限额:", dailyLimitField);

        // 状态字段
        statusCombo = new JComboBox<>(new String[]{"上架", "下架"});
        statusCombo.setFont(FIELD_FONT);
        statusCombo.setSelectedIndex(checkItemGroup.getStatus() != null && checkItemGroup.getStatus() == 0 ? 1 : 0);
        addFormField(formPanel, gbc, 5, "状态:", statusCombo);

        // 检查项选择（多选）
        DefaultListModel<CheckItem> listModel = new DefaultListModel<>();
        java.util.List<CheckItem> allItems = new CheckItemDAO().getAll();
        for (CheckItem it : allItems) listModel.addElement(it);
        itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        itemList.setVisibleRowCount(6);
        itemList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getItemName() + " (" + value.getCode() + ")");
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            lbl.setBackground(isSelected ? new Color(70, 104, 197) : Color.WHITE);
            lbl.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            return lbl;
        });
        itemList.setFixedCellHeight(28);
        itemList.setBackground(new Color(250, 250, 250));

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)), "检查项列表"));
        itemPanel.setBackground(Color.WHITE);
        JScrollPane itemsScroll = new JScrollPane(itemList);
        itemsScroll.setPreferredSize(new Dimension(0, 180));
        itemPanel.add(itemsScroll, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(itemPanel, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        // 如果是编辑模式，预选已有关联的检查项
        if (this.checkItemGroup.getId() != null) {
            try {
                java.util.List<CheckItem> existing = new CheckItemGroupDAO().getCheckItemsByGroup(this.checkItemGroup.getId());
                java.util.List<Integer> sel = new java.util.ArrayList<>();
                for (int i = 0; i < listModel.size(); i++) {
                    CheckItem it = listModel.get(i);
                    for (CheckItem ex : existing) {
                        if (it.getItemId().equals(ex.getItemId())) { sel.add(i); break; }
                    }
                }
                if (!sel.isEmpty()) {
                    int[] idx = sel.stream().mapToInt(Integer::intValue).toArray();
                    itemList.setSelectedIndices(idx);
                }
            } catch (Exception e) {
                // ignore preselect errors
            }
        }

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = CrudPanel.createStyledButton("确定", MAIN_COLOR);
        okButton.addActionListener(e -> {
            if (validateInput()) {
                option = OK_OPTION;
                dispose();
            }
        });

        JButton cancelButton = CrudPanel.createStyledButton("取消", new Color(204, 153, 153));
        cancelButton.addActionListener(e -> {
            option = CANCEL_OPTION;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private boolean validateInput() {
        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(dailyLimitField.getText());
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的价格和每日限额", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public int showDialog() {
        setVisible(true);
        return option;
    }

    public CheckItemGroup getCheckItemGroup() {
        if (checkItemGroup.getId() != null) {
            checkItemGroup.setId(Long.parseLong(idField.getText()));
        }
        checkItemGroup.setName(nameField.getText());
        checkItemGroup.setDescription(descriptionField.getText());
        checkItemGroup.setPrice(Double.parseDouble(priceField.getText()));
        checkItemGroup.setDailyLimit(Integer.parseInt(dailyLimitField.getText()));
        checkItemGroup.setStatus(statusCombo.getSelectedIndex() == 0 ? 1 : 0);
        return checkItemGroup;
    }

    public java.util.List<Long> getSelectedItemIds() {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (CheckItem it : itemList.getSelectedValuesList()) {
            ids.add(it.getItemId());
        }
        return ids;
    }
}

