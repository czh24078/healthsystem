package com.healthsys.ui.medical;

import com.healthsys.common.entity.Report;
import com.healthsys.dao.ReportDAO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;

/**
 * 报告上传对话框 — 医生上传PDF报告和总结
 */
public class ReportDialog extends JDialog {
    private final Long appointmentId;
    private final Long doctorId;
    private boolean saved = false;

    private JTextField filePathField;
    private JTextArea summaryArea;
    private JButton browseBtn;

    private final ReportDAO reportDAO = new ReportDAO();

    private static final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Font FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Color MAIN_COLOR = new Color(70, 104, 197);

    public ReportDialog(JFrame parent, Long appointmentId, Long doctorId) {
        super(parent, "上传体检报告", true);
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;

        // 检查是否已有报告
        Report existing = reportDAO.getByAppointmentId(appointmentId);
        initUI(existing);
        setSize(600, 450);
        setLocationRelativeTo(parent);
    }

    private void initUI(Report existing) {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // 标题
        JLabel titleLabel = new JLabel(existing != null ? "编辑体检报告" : "上传体检报告");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(MAIN_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // PDF文件路径
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel fileLabel = new JLabel("PDF报告文件:");
        fileLabel.setFont(LABEL_FONT);
        formPanel.add(fileLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setBackground(Color.WHITE);
        filePathField = new JTextField(existing != null && existing.getPdfFilePath() != null ? existing.getPdfFilePath() : "");
        filePathField.setFont(FIELD_FONT);
        filePathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        filePanel.add(filePathField, BorderLayout.CENTER);

        browseBtn = CrudPanel.createStyledButton("浏览...", new Color(153, 204, 255));
        browseBtn.setPreferredSize(new Dimension(90, 30));
        browseBtn.addActionListener(e -> chooseFile());
        filePanel.add(browseBtn, BorderLayout.EAST);
        formPanel.add(filePanel, gbc);

        // 报告总结
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        JLabel summaryLabel = new JLabel("报告总结:");
        summaryLabel.setFont(LABEL_FONT);
        formPanel.add(summaryLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        summaryArea = new JTextArea(existing != null && existing.getSummary() != null ? existing.getSummary() : "", 8, 40);
        summaryArea.setFont(FIELD_FONT);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        formPanel.add(summaryScroll, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = CrudPanel.createStyledButton("保存报告", new Color(102, 204, 153));
        saveBtn.addActionListener(e -> saveReport(existing));

        JButton cancelBtn = CrudPanel.createStyledButton("取消", new Color(204, 153, 153));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF文件 (*.pdf)", "pdf"));
        fileChooser.setDialogTitle("选择PDF报告文件");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void saveReport(Report existing) {
        String filePath = filePathField.getText().trim();
        String summary = summaryArea.getText().trim();

        if (filePath.isEmpty() && summary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写PDF路径或报告总结。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existing != null) {
            // 更新已有报告
            existing.setPdfFilePath(filePath.isEmpty() ? null : filePath);
            existing.setSummary(summary.isEmpty() ? null : summary);
            if (reportDAO.update(existing)) {
                JOptionPane.showMessageDialog(this, "报告已更新。", "保存成功", JOptionPane.INFORMATION_MESSAGE);
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "报告更新失败。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // 新建报告
            Report report = new Report();
            report.setAppointmentId(appointmentId);
            report.setDoctorId(doctorId);
            report.setPdfFilePath(filePath.isEmpty() ? null : filePath);
            report.setSummary(summary.isEmpty() ? null : summary);
            report.setUploadTime(LocalDateTime.now());

            if (reportDAO.create(report)) {
                JOptionPane.showMessageDialog(this, "报告已保存。", "保存成功", JOptionPane.INFORMATION_MESSAGE);
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "报告保存失败。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
