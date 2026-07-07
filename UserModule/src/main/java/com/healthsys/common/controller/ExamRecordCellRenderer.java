package com.healthsys.common.controller;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ExamRecordCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (column == 3) { // 结果列
            try {
                String resultStr = value.toString();
                double resultValue = Double.parseDouble(resultStr.trim());

                String normalRange = (String) table.getValueAt(row, 5); // 正常范围列
                if (normalRange != null && !normalRange.isEmpty()) {
                    String[] parts = normalRange.split("[-~]"); // 支持格式如 "4.0-10.0" 或 "4.0~10.0"
                    if (parts.length >= 2) {
                        double min = Double.parseDouble(parts[0].replaceAll("[^\\d.]", ""));
                        double max = Double.parseDouble(parts[1].replaceAll("[^\\d.]", ""));

                        if (resultValue < min || resultValue > max) {
                            setForeground(Color.RED);
                        } else {
                            setForeground(Color.BLACK);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                setForeground(Color.BLACK); // 非数字不处理
            }
        }

        return this;
    }
}
