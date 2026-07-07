package com.healthsys.common.view.appointment;

import com.healthsys.common.controller.PackageTestController;
import com.ncu.Common.CheckItemGroup;
import com.ncu.Common.CheckItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PackageDetailView {
    private JPanel detailPanel;
    private CheckItemGroup checkItemGroup;
    private PackageTestController packageTestController = new PackageTestController();

    public PackageDetailView(CheckItemGroup checkItemGroup) {
        this.checkItemGroup = checkItemGroup;
        initializeUI();
    }

    private void initializeUI() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 套餐基本信息面板
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("套餐信息"));

        infoPanel.add(new JLabel("套餐名称:"));
        infoPanel.add(new JLabel(checkItemGroup.getName()));

        infoPanel.add(new JLabel("描述:"));
        infoPanel.add(new JLabel(checkItemGroup.getDescription()));

        infoPanel.add(new JLabel("价格:"));
        infoPanel.add(new JLabel(String.valueOf(checkItemGroup.getPrice())));

        detailPanel.add(infoPanel, BorderLayout.NORTH);

        // 检查项目列表
        String[] columnNames = { "ID", "检查项目名称", "描述", "价格" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable testTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(testTable);

        loadTestsData(model);

        detailPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTestsData(DefaultTableModel model) {
        model.setRowCount(0);
        List<CheckItem> tests = packageTestController.getCheckItemsByPackage(checkItemGroup.getId());

        for (CheckItem test : tests) {
            Object[] rowData = {
                    test.getId(),
                    test.getName(),
                    test.getDescription(),
                    test.getPrice()
            };
            model.addRow(rowData);
        }
    }

    public JPanel getDetailPanel() {
        return detailPanel;
    }
}