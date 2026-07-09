package com.healthsys.ui.user;

import com.healthsys.service.AppointmentService;
import com.healthsys.service.ExamRecordCellRenderer;
import com.healthsys.service.ExamService;
import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Doctor;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.Report;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.Users;
import com.healthsys.dao.DoctorDAO;
import com.healthsys.dao.ReportDAO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体检信息面板 — 展示用户最近一次体检结果
 */
public class ExamRecordView {
    private JPanel healthPanel;
    private final ExamService examService;
    private final AppointmentService appointmentService;
    private final ReportDAO reportDAO;
    private final DoctorDAO doctorDAO;
    private final Users currentUser;
    private JTable resultsTable;
    private ExamResultTableModel resultsModel;
    private JLabel infoLabel;
    private JTextArea reportArea;
    private JPanel reportPanel;
    private JButton printButton;
    private Appointment latestAppointment;
    private CheckItemGroup latestGroup;
    private List<ExamRecord> latestRecords;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ExamRecordView(Users currentUser) {
        this.currentUser = currentUser;
        this.examService = new ExamService();
        this.appointmentService = new AppointmentService();
        this.reportDAO = new ReportDAO();
        this.doctorDAO = new DoctorDAO();
        initializeUI();
    }

    private void initializeUI() {
        healthPanel = new JPanel(new BorderLayout(0, 15));
        healthPanel.setBackground(new Color(245, 245, 245));
        healthPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== 标题栏 =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("体检信息", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 104, 197));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("查看您最近一次的体检结果", JLabel.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // 打印按钮
        printButton = new JButton("打印报告");
        printButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        printButton.setBackground(new Color(70, 104, 197));
        printButton.setForeground(Color.BLACK);
        printButton.setFocusPainted(false);
        printButton.setPreferredSize(new Dimension(120, 35));
        printButton.addActionListener(e -> handlePrint());
        printButton.setEnabled(false);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setBackground(new Color(245, 245, 245));
        rightHeader.add(printButton);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // ===== 概要信息 =====
        infoLabel = new JLabel(" ", JLabel.CENTER);
        infoLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        infoLabel.setForeground(new Color(70, 104, 197));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== 报告摘要 =====
        reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder("医生综合报告"));
        reportPanel.setBackground(new Color(245, 245, 245));

        reportArea = new JTextArea();
        reportArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setBackground(new Color(252, 252, 252));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setPreferredSize(new Dimension(750, 100));
        reportPanel.add(reportScroll, BorderLayout.CENTER);
        reportPanel.setVisible(false);

        // ===== 结果表格 =====
        resultsModel = new ExamResultTableModel();
        resultsTable = new JTable(resultsModel);
        resultsTable.setRowHeight(30);
        resultsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        resultsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        resultsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        resultsTable.setSelectionBackground(new Color(220, 230, 250));
        resultsTable.setGridColor(new Color(220, 220, 220));

        resultsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        resultsTable.getColumnModel().getColumn(3).setMaxWidth(80);
        resultsTable.getColumnModel().getColumn(5).setMaxWidth(60);

        // 异常列颜色渲染
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(new AbnormalRenderer());

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // 组装
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(infoLabel, BorderLayout.NORTH);
        centerPanel.add(reportPanel, BorderLayout.CENTER);
        centerPanel.add(tableScroll, BorderLayout.SOUTH);

        healthPanel.add(headerPanel, BorderLayout.NORTH);
        healthPanel.add(centerPanel, BorderLayout.CENTER);

        // 初次加载
        loadLatestExamRecords();
    }

    /**
     * 加载最近一次体检记录
     */
    private void loadLatestExamRecords() {
        resultsModel.clear();
        reportArea.setText("");
        reportPanel.setVisible(false);
        printButton.setEnabled(false);

        List<Appointment> completedAppointments = appointmentService.getUserAppointmentsByStatus(
                currentUser.getId(), "COMPLETED");

        if (completedAppointments.isEmpty()) {
            infoLabel.setText("暂无已完成体检记录");
            return;
        }

        // 按 examDate 降序取最近一次
        completedAppointments.sort((a, b) -> {
            if (a.getExamDate() == null) return 1;
            if (b.getExamDate() == null) return -1;
            return b.getExamDate().compareTo(a.getExamDate());
        });

        latestAppointment = completedAppointments.get(0);
        Long groupId = latestAppointment.getGroupId();
        latestGroup = groupId != null ? appointmentService.getCheckItemGroupById(groupId) : null;

        // 医生信息
        String doctorInfo = "";
        Long doctorId = latestAppointment.getDoctorId();
        if (doctorId != null && doctorId > 0) {
            Doctor doctor = doctorDAO.getById(doctorId);
            if (doctor != null) {
                doctorInfo = " | 医生：" + doctor.getName()
                        + (doctor.getTitle() != null ? " " + doctor.getTitle() : "");
            }
        }

        String examDateStr = latestAppointment.getExamDate() != null
                ? latestAppointment.getExamDate().format(DATE_FMT) : "未知";

        infoLabel.setText(String.format("检查组：%s  |  体检日期：%s%s",
                latestGroup != null ? latestGroup.getGroupName() : "未知套餐",
                examDateStr, doctorInfo));

        // 加载检查结果
        latestRecords = examService.getExamRecordsByAppointment(latestAppointment.getId());

        // 加载报告
        Report report = reportDAO.getByAppointmentId(latestAppointment.getId());
        if (report != null && report.getSummary() != null && !report.getSummary().isEmpty()) {
            reportArea.setText(report.getSummary());
            reportPanel.setVisible(true);
        }

        // 填充表格
        if (latestRecords != null) {
            // 构建 item 映射表
            Map<Long, CheckItem> itemMap = new HashMap<>();
            if (latestGroup != null) {
                List<CheckItem> items = appointmentService.getCheckItemsByGroupId(latestGroup.getId());
                for (CheckItem item : items) {
                    itemMap.put(item.getId(), item);
                }
            }
            resultsModel.setData(latestRecords, itemMap);
        }

        printButton.setEnabled(true);
    }

    public void refreshData() {
        loadLatestExamRecords();
    }

    private void handlePrint() {
        if (latestAppointment == null) {
            JOptionPane.showMessageDialog(healthPanel, "没有可打印的体检记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String defaultFileName = "体检报告_"
                + (latestGroup != null ? sanitizeFileName(latestGroup.getGroupName()) : "未知")
                + "_" + (latestAppointment.getExamDate() != null
                        ? latestAppointment.getExamDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        : "nodate")
                + ".docx";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存体检报告");
        fileChooser.setSelectedFile(new File(defaultFileName));

        if (fileChooser.showSaveDialog(healthPanel) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".docx")) {
                filePath += ".docx";
            }
            try {
                // 构建 item 映射表
                Map<Long, CheckItem> itemMap = new HashMap<>();
                if (latestGroup != null) {
                    List<CheckItem> items = appointmentService.getCheckItemsByGroupId(latestGroup.getId());
                    for (CheckItem item : items) {
                        itemMap.put(item.getId(), item);
                    }
                }

                Report report = reportDAO.getByAppointmentId(latestAppointment.getId());

                WordExportService exportService = new WordExportService();
                exportService.exportExamRecordReport(filePath, currentUser, latestAppointment,
                        latestGroup, latestRecords, itemMap, report);
                JOptionPane.showMessageDialog(healthPanel,
                        "报告已成功导出到：\n" + filePath,
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(healthPanel,
                        "导出失败：" + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public JPanel getHealthPanel() {
        return healthPanel;
    }

    /**
     * 检查结果表格模型
     */
    private static class ExamResultTableModel extends AbstractTableModel {
        private final String[] columns = {"序号", "检查项目", "结果值", "单位", "参考范围", "异常"};
        private List<ExamRecord> data = new ArrayList<>();
        private Map<Long, CheckItem> itemMap = new HashMap<>();

        void setData(List<ExamRecord> data, Map<Long, CheckItem> itemMap) {
            this.data = data != null ? data : new ArrayList<>();
            this.itemMap = itemMap != null ? itemMap : new HashMap<>();
            fireTableDataChanged();
        }

        void clear() {
            this.data = new ArrayList<>();
            this.itemMap = new HashMap<>();
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            ExamRecord record = data.get(row);
            CheckItem item = itemMap.get(record.getItemId());
            switch (col) {
                case 0: return row + 1;
                case 1: return record.getItemName() != null ? record.getItemName()
                        : (item != null ? item.getItemName() : "未知");
                case 2: return record.getResultValue();
                case 3: return item != null && item.getUnit() != null ? item.getUnit() : "";
                case 4: return item != null && item.getReferenceRange() != null ? item.getReferenceRange() : "";
                case 5: return Boolean.TRUE.equals(record.getIsAbnormal()) ? "异常" : "正常";
                default: return "";
            }
        }
    }

    /**
     * 异常列渲染器
     */
    private static class AbnormalRenderer extends DefaultTableCellRenderer {
        private static final Color ABNORMAL_COLOR = new Color(198, 40, 40);
        private static final Color NORMAL_COLOR = new Color(46, 125, 50);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("微软雅黑", Font.BOLD, 12));
            if (!isSelected) {
                if ("异常".equals(value)) {
                    lbl.setForeground(ABNORMAL_COLOR);
                } else {
                    lbl.setForeground(NORMAL_COLOR);
                }
            }
            return lbl;
        }
    }
}
