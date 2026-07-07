package com.healthsys.common.view;

import com.healthsys.common.controller.AppointmentController;
import com.healthsys.common.controller.ExamRecordCellRenderer;
import com.healthsys.common.controller.ExamRecordController;
import com.ncu.Common.Appointment;
import com.ncu.Common.ExamRecord;
import com.ncu.Common.CheckItem;
import com.ncu.Common.CheckItemGroup;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExamRecordView {
    private JPanel healthPanel;
    private ExamRecordController examRecordController;
    private AppointmentController appointmentController;

    public ExamRecordView(Appointment dummyAppointment) {
        this.examRecordController = new ExamRecordController();
        this.appointmentController = new AppointmentController();
        initializeUI(dummyAppointment);
    }

    private void initializeUI(Appointment dummyAppointment) {
        healthPanel = new JPanel(new BorderLayout());
        healthPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // 表格模型定义
        String[] columnNames = {"预约ID", "套餐名称", "项目名称", "结果", "单位", "正常范围", "体检时间"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        // 设置自动调整模式
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // 设置自定义渲染器（仅对“结果”列）
        table.getColumnModel().getColumn(3).setCellRenderer(new ExamRecordCellRenderer());

        // 手动调整列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(10);  // 预约ID
        table.getColumnModel().getColumn(1).setPreferredWidth(60); // 套餐名称
        table.getColumnModel().getColumn(2).setPreferredWidth(60); // 项目名称
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // 结果
        table.getColumnModel().getColumn(4).setPreferredWidth(50);  // 单位
        table.getColumnModel().getColumn(5).setPreferredWidth(180); // 正常范围
        table.getColumnModel().getColumn(6).setPreferredWidth(50); // 体检时间


        JScrollPane scrollPane = new JScrollPane(table);

        loadExamRecords(model, dummyAppointment.getUserId());

        healthPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadExamRecords(DefaultTableModel model, long userId) {
        model.setRowCount(0); // 清空旧数据

        List<Appointment> completedAppointments = appointmentController.getUserAppointmentsByStatus(userId, "COMPLETED");

        for (Appointment appointment : completedAppointments) {
            Long packageId = appointment.getPackageId();

            CheckItemGroup checkItemGroup = null;
            if (packageId != null) {
                checkItemGroup = appointmentController.getTestPackageById(packageId);
            }

            String packageName = checkItemGroup != null ? checkItemGroup.getName() : "通用项目";

            List<ExamRecord> records = examRecordController.getExamRecordsByAppointment(appointment.getId());

            for (ExamRecord record : records) {
                CheckItem checkItem = appointmentController.getMedicalTestById(record.getTestId());
                String testName = checkItem != null ? checkItem.getName() : "未知项目";
                String unit = checkItem != null ? checkItem.getNormalRange().split(":")[1].replaceAll("[\\d.-]+", "").trim() : "";
                String normalRange = checkItem != null ? checkItem.getNormalRange() : "";

                Object[] row = {
                        appointment.getId(),
                        packageName,
                        testName,
                        record.getResultValue(),
                        unit,
                        normalRange,
                        record.getExamDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                };
                model.addRow(row);
            }
        }
    }

    public JPanel getHealthPanel() {
        return healthPanel;
    }
}
