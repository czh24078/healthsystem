package com.healthsys.ui.user;

import com.healthsys.dao.CheckItemGroupDAO;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.ui.HealthTheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class PackageDetailView {
    private JPanel detailPanel;
    private CheckItemGroup checkItemGroup;
    private CheckItemGroupDAO checkItemGroupDAO = new CheckItemGroupDAO();

    public PackageDetailView(CheckItemGroup checkItemGroup) {
        this.checkItemGroup = checkItemGroup;
        initializeUI();
    }

    private void initializeUI() {
        detailPanel = new JPanel(new BorderLayout(0, 15));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== 标题栏 =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel(checkItemGroup.getName(), JLabel.CENTER);
        titleLabel.setFont(HealthTheme.FONT_TITLE);
        titleLabel.setForeground(HealthTheme.PRIMARY);

        JLabel subtitleLabel = new JLabel("套餐详细信息", JLabel.CENTER);
        subtitleLabel.setFont(HealthTheme.FONT_BODY_SM);
        subtitleLabel.setForeground(HealthTheme.TEXT_HINT);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setBackground(Color.WHITE);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        titleContainer.add(subtitleLabel);

        headerPanel.add(titleContainer, BorderLayout.CENTER);

        // ===== 检查组基本信息面板 =====
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 15, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(HealthTheme.BORDER, 1, true),
                "基本信息"
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        Font labelFont = HealthTheme.FONT_BODY_SM;
        Font valueFont = new Font("微软雅黑", Font.PLAIN, 14);
        Color labelColor = HealthTheme.TEXT_SECONDARY;
        Color valueColor = HealthTheme.TEXT_PRIMARY;

        addInfoRow(infoPanel, "检查组名称:", checkItemGroup.getName(), labelFont, valueFont, labelColor, valueColor);
        addInfoRow(infoPanel, "描述:", checkItemGroup.getDescription() != null ? checkItemGroup.getDescription() : "无", labelFont, valueFont, labelColor, valueColor);
        
        JLabel priceLabel = new JLabel("价格:");
        priceLabel.setFont(labelFont);
        priceLabel.setForeground(labelColor);
        infoPanel.add(priceLabel);
        
        JLabel priceValue = new JLabel(String.format("¥%.2f", checkItemGroup.getPrice()));
        priceValue.setFont(new Font("微软雅黑", Font.BOLD, 16));
        priceValue.setForeground(new Color(229, 62, 62)); // 红色突出价格
        infoPanel.add(priceValue);

        detailPanel.add(headerPanel, BorderLayout.NORTH);

        // ===== 检查项目列表 =====
        String[] columnNames = { "序号", "检查项目名称", "描述", "价格" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable testTable = new JTable(model);
        testTable.setRowHeight(36);
        testTable.setFont(HealthTheme.FONT_BODY_SM);
        testTable.getTableHeader().setFont(HealthTheme.FONT_BUTTON);
        testTable.getTableHeader().setBackground(HealthTheme.TABLE_HEADER);
        testTable.getTableHeader().setForeground(Color.WHITE);
        testTable.getTableHeader().setReorderingAllowed(false);
        testTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        testTable.setSelectionBackground(HealthTheme.TABLE_SELECTED);
        testTable.setSelectionForeground(HealthTheme.TEXT_PRIMARY);
        testTable.setGridColor(HealthTheme.BORDER);
        testTable.setShowHorizontalLines(true);
        testTable.setShowVerticalLines(false);
        testTable.setIntercellSpacing(new Dimension(0, 0));
        testTable.setBackground(Color.WHITE);
        
        // 自定义表头渲染器
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(HealthTheme.TABLE_HEADER);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(HealthTheme.FONT_BUTTON);
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        testTable.getTableHeader().setDefaultRenderer(headerRenderer);
        
        // 设置列宽
        testTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        testTable.getColumnModel().getColumn(0).setMaxWidth(70);
        testTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        testTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        testTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        // 为数据列设置左对齐渲染器
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        cellRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        testTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        testTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
        testTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
        testTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        loadTestsData(model);

        JScrollPane scrollPane = new JScrollPane(testTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HealthTheme.BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // 添加表格标题
        JLabel tableTitle = new JLabel("包含的检查项目");
        tableTitle.setFont(HealthTheme.FONT_SUBTITLE);
        tableTitle.setForeground(HealthTheme.PRIMARY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 将基本信息和检查项目列表放在中间区域，避免标题被遮挡
        JPanel centerContent = new JPanel(new BorderLayout(0, 15));
        centerContent.setBackground(Color.WHITE);
        centerContent.add(infoPanel, BorderLayout.NORTH);
        centerContent.add(tablePanel, BorderLayout.CENTER);
        
        detailPanel.add(centerContent, BorderLayout.CENTER);
    }

    private void loadTestsData(DefaultTableModel model) {
        model.setRowCount(0);
        List<CheckItem> tests = checkItemGroupDAO.getCheckItemsByGroup(checkItemGroup.getId());
        int index = 1;
        for (CheckItem test : tests) {
            Object[] rowData = {
                    index++,
                    test.getName(),
                    test.getDescription() != null ? test.getDescription() : "",
                    String.format("¥%.2f", test.getPrice())
            };
            model.addRow(rowData);
        }
    }

    /**
     * 添加信息行
     */
    private void addInfoRow(JPanel panel, String labelText, String valueText, 
                           Font labelFont, Font valueFont, Color labelColor, Color valueColor) {
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(labelColor);
        panel.add(label);

        JLabel value = new JLabel(valueText);
        value.setFont(valueFont);
        value.setForeground(valueColor);
        panel.add(value);
    }

    public JPanel getDetailPanel() {
        return detailPanel;
    }
}
