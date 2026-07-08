package com.healthsys.ui.medical;

import com.healthsys.common.entity.Appointment;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.dao.CheckItemGroupDAO;
import com.healthsys.dao.ExamRecordDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查结果录入对话框 — 医生对预约的每个检查项录入结果
 */
public class ExamResultDialog extends JDialog {
    private final Appointment appointment;
    private final Long doctorId;
    private final List<CheckItem> checkItems;
    private final List<ResultRow> resultRows = new ArrayList<>();
    private boolean saved = false;

    private final CheckItemGroupDAO checkItemGroupDAO = new CheckItemGroupDAO();
    private final ExamRecordDAO examRecordDAO = new ExamRecordDAO();

    private static final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 13);
    private static final Font FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 13);
    private static final Color MAIN_COLOR = new Color(70, 104, 197);

    public ExamResultDialog(JFrame parent, Appointment appointment, Long doctorId) {
        super(parent, "检查结果录入", true);
        this.appointment = appointment;
        this.doctorId = doctorId;

        // 加载该检查组的所有检查项
        Long groupId = appointment.getGroupId();
        this.checkItems = groupId != null ? checkItemGroupDAO.getCheckItemsByGroup(groupId) : new ArrayList<>();

        initUI();
        setSize(800, 550);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部信息栏
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        infoPanel.setBackground(new Color(245, 245, 245));
        infoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        infoPanel.add(createInfoLabel("患者: " + appointment.getUserName()));
        infoPanel.add(createInfoLabel("检查组: " + appointment.getGroupName()));
        infoPanel.add(createInfoLabel("检查日期: " + appointment.getExamDate()));
        add(infoPanel, BorderLayout.NORTH);

        // 中部：检查项录入表格（滚动）
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        // 表头
        JPanel header = createResultRowHeader();
        tablePanel.add(header);
        tablePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // 每项检查
        if (checkItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("该检查组暂无检查项", JLabel.CENTER);
            emptyLabel.setFont(LABEL_FONT);
            emptyLabel.setForeground(Color.GRAY);
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.add(emptyLabel);
            tablePanel.add(emptyPanel);
        } else {
            for (CheckItem item : checkItems) {
                ResultRow row = new ResultRow(item);
                resultRows.add(row);
                tablePanel.add(row.panel);
                tablePanel.add(Box.createRigidArea(new Dimension(0, 3)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton saveBtn = CrudPanel.createStyledButton("保存结果", new Color(102, 204, 153));
        saveBtn.addActionListener(e -> saveResults());

        JButton cancelBtn = CrudPanel.createStyledButton("取消", new Color(204, 153, 153));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        label.setForeground(MAIN_COLOR);
        return label;
    }

    private JPanel createResultRowHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JPanel leftPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        leftPanel.setOpaque(false);
        JLabel nameLabel = new JLabel("检查项");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        JLabel refLabel = new JLabel("参考范围");
        refLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        JLabel resultLabel = new JLabel("检查结果");
        resultLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        leftPanel.add(nameLabel);
        leftPanel.add(refLabel);
        leftPanel.add(resultLabel);

        JPanel rightPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        rightPanel.setOpaque(false);
        JLabel abnormalLabel = new JLabel("异常");
        abnormalLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        JLabel noteLabel = new JLabel("医生备注");
        noteLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        rightPanel.add(abnormalLabel);
        rightPanel.add(noteLabel);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private void saveResults() {
        int savedCount = 0;
        for (ResultRow row : resultRows) {
            String value = row.resultField.getText().trim();
            if (value.isEmpty()) continue; // 跳过未填写的

            ExamRecord record = new ExamRecord();
            record.setAppointmentId(appointment.getId());
            record.setItemId(row.item.getItemId());
            record.setDoctorId(doctorId);
            record.setResultValue(value);
            record.setIsAbnormal(row.abnormalCheck.isSelected());
            String note = row.noteField.getText().trim();
            record.setDoctorNote(note.isEmpty() ? null : note);
            record.setExamDate(LocalDateTime.now());

            if (examRecordDAO.addExamRecord(record)) {
                savedCount++;
            }
        }

        if (savedCount > 0) {
            // 更新预约状态为已完成
            com.healthsys.dao.AppointmentDAO appointmentDAO = new com.healthsys.dao.AppointmentDAO();
            appointmentDAO.completeAppointment(appointment.getId());

            JOptionPane.showMessageDialog(this,
                    "成功保存 " + savedCount + " 条检查结果，预约已标记为已完成。",
                    "保存成功", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "请至少填写一项检查结果。",
                    "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    /**
     * 单个检查项录入行
     */
    private class ResultRow {
        final CheckItem item;
        final JPanel panel;
        final JTextField resultField;
        final JCheckBox abnormalCheck;
        final JTextField noteField;

        ResultRow(CheckItem item) {
            this.item = item;
            panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

            // 左：检查项名称 + 参考范围 + 结果输入
            JPanel leftPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            leftPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(item.getItemName());
            nameLabel.setFont(LABEL_FONT);
            leftPanel.add(nameLabel);

            JLabel refLabel = new JLabel(item.getReferenceRange() != null ? item.getReferenceRange() : "-");
            refLabel.setFont(LABEL_FONT);
            refLabel.setForeground(Color.GRAY);
            leftPanel.add(refLabel);

            resultField = new JTextField();
            resultField.setFont(FIELD_FONT);
            resultField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            leftPanel.add(resultField);

            // 右：异常标记 + 备注
            JPanel rightPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            rightPanel.setOpaque(false);

            JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            checkPanel.setOpaque(false);
            abnormalCheck = new JCheckBox();
            abnormalCheck.setOpaque(false);
            checkPanel.add(abnormalCheck);

            noteField = new JTextField();
            noteField.setFont(FIELD_FONT);
            noteField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));

            rightPanel.add(checkPanel);
            rightPanel.add(noteField);

            panel.add(leftPanel, BorderLayout.CENTER);
            panel.add(rightPanel, BorderLayout.EAST);
        }
    }
}
