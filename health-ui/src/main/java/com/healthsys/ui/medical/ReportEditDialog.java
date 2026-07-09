package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.Report;
import com.healthsys.dao.AppointmentDAO;
import com.healthsys.dao.ReportDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReportEditDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private int result = CANCEL_OPTION;
    private final Long doctorId;
    private final Long fixedAppointmentId;
    private Report existingReport; // 选中预约已有的报告（可为 null）
    private final ReportDAO reportDAO = new ReportDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    private JComboBox<String> appointmentCombo;
    private JTextArea summaryArea;
    private List<Appointment> availableAppointments;

    public ReportEditDialog(Long doctorId, Report existingReport) {
        this(doctorId, null, existingReport);
    }

    public ReportEditDialog(Long doctorId, Long appointmentId, Report existingReport) {
        this.doctorId = doctorId;
        this.fixedAppointmentId = appointmentId;
        this.existingReport = existingReport;
        setTitle(existingReport == null ? "撰写报告" : "编辑报告");
        setModal(true);
        setSize(650, 450);
        setLocationRelativeTo(null);
        initUI();
        if (existingReport != null || fixedAppointmentId != null) {
            appointmentCombo.setEnabled(false);
        }
        if (existingReport != null) {
            selectAppointmentInCombo(existingReport.getAppointmentId());
            loadSummaryFromReport(existingReport);
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        Font labelFont = new Font("微软雅黑", Font.BOLD, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        JPanel apptPanel = new JPanel(new BorderLayout(10, 0));
        apptPanel.setBackground(Color.WHITE);
        apptPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel apptLabel = new JLabel("关联预约：");
        apptLabel.setFont(labelFont);
        appointmentCombo = new JComboBox<>();
        appointmentCombo.setFont(fieldFont);
        loadAppointments();
        // 切换预约时自动加载已有报告
        appointmentCombo.addActionListener(e -> onAppointmentSelected());
        apptPanel.add(apptLabel, BorderLayout.WEST);
        apptPanel.add(appointmentCombo, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new BorderLayout(10, 5));
        summaryPanel.setBackground(Color.WHITE);
        JLabel summaryLabel = new JLabel("诊断报告：");
        summaryLabel.setFont(labelFont);
        summaryArea = new JTextArea(8, 40);
        summaryArea.setFont(fieldFont);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setPreferredSize(new Dimension(500, 200));
        summaryPanel.add(summaryLabel, BorderLayout.NORTH);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);

        panel.add(apptPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(summaryPanel);

        return panel;
    }

    private void loadAppointments() {
        if (fixedAppointmentId != null) {
            Appointment a = appointmentDAO.getById(fixedAppointmentId);
            availableAppointments = a != null ? List.of(a) : List.of();
        } else {
            availableAppointments = appointmentDAO.searchByFilters(doctorId, null, null, "COMPLETED");
        }
        appointmentCombo.removeAllItems();
        for (Appointment a : availableAppointments) {
            boolean hasReport = reportDAO.getByAppointmentId(a.getId()) != null;
            String label = a.getUserName() + " — " + a.getExamDate() + " — " + a.getGroupName()
                    + (hasReport ? "  [已报告]" : "");
            appointmentCombo.addItem(label);
        }
    }

    /** 用户切换下拉框选中项时，自动加载该预约的已有报告 */
    private void onAppointmentSelected() {
        if (fixedAppointmentId != null || existingReport != null) return; // 固定预约模式不切换
        int idx = appointmentCombo.getSelectedIndex();
        if (idx < 0 || idx >= availableAppointments.size()) return;
        Appointment selected = availableAppointments.get(idx);
        Report r = reportDAO.getByAppointmentId(selected.getId());
        if (r != null) {
            loadSummaryFromReport(r);
        } else {
            summaryArea.setText("");
        }
    }

    private void selectAppointmentInCombo(Long appointmentId) {
        for (int i = 0; i < availableAppointments.size(); i++) {
            if (availableAppointments.get(i).getId().equals(appointmentId)) {
                appointmentCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadSummaryFromReport(Report r) {
        if (r.getSummary() != null) {
            summaryArea.setText(r.getSummary());
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(245, 245, 245));

        JButton saveBtn = CrudPanel.createStyledButton("保存报告", new Color(102, 204, 153));
        saveBtn.addActionListener(e -> {
            if (saveReport()) {
                result = OK_OPTION;
                dispose();
            }
        });

        JButton cancelBtn = CrudPanel.createStyledButton("取消", new Color(200, 200, 200));
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(cancelBtn);
        return panel;
    }

    private boolean saveReport() {
        Long appointmentId;
        if (fixedAppointmentId != null) {
            appointmentId = fixedAppointmentId;
        } else {
            int idx = appointmentCombo.getSelectedIndex();
            if (idx < 0) {
                JOptionPane.showMessageDialog(this, "请选择关联预约", "提示", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            appointmentId = availableAppointments.get(idx).getId();
        }
        String summary = summaryArea.getText().trim();
        if (summary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写诊断报告", "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 查找该预约是否已有报告
        Report existing = reportDAO.getByAppointmentId(appointmentId);

        if (existingReport != null) {
            // 编辑模式：更新已有报告
            existingReport.setSummary(summary);
            if (reportDAO.update(existingReport)) {
                JOptionPane.showMessageDialog(this, "报告更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else if (existing != null) {
            // 撰写模式但该预约已有报告 → 自动转为更新
            existing.setSummary(summary);
            if (reportDAO.update(existing)) {
                JOptionPane.showMessageDialog(this, "报告更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            // 新建报告
            Report report = new Report();
            report.setAppointmentId(appointmentId);
            report.setDoctorId(doctorId);
            report.setSummary(summary);
            report.setUploadTime(LocalDateTime.now());
            if (reportDAO.create(report)) {
                JOptionPane.showMessageDialog(this, "报告保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "报告保存失败", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    public int showDialog() {
        if (fixedAppointmentId == null && availableAppointments.isEmpty()) {
            JOptionPane.showMessageDialog(getOwner(),
                "当前没有已完成的预约。\n请先在「预约管理」中完成体检并录入检查结果。",
                "无可选预约", JOptionPane.INFORMATION_MESSAGE);
            return CANCEL_OPTION;
        }
        setVisible(true);
        return result;
    }
}
