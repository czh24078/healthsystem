package com.ncu.Healthcare;

import com.ncu.Healthcare.Views.MedicalStaffPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("医疗健康管理系统");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 800);

            // 确保使用BorderLayout
            frame.setLayout(new BorderLayout());
            MedicalStaffPanel medicalStaffPanel = new MedicalStaffPanel();
            frame.add(medicalStaffPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}


