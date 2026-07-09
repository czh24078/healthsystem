package com.healthsys.ui.user;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.ui.HealthTheme;


public class HealthAdvicePanel extends JPanel {
    public HealthAdvicePanel(List<ExamRecord> records) {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(HealthTheme.BORDER, 1, true),
                "健康建议"
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextPane adviceText = new JTextPane();
        adviceText.setContentType("text/html");
        adviceText.setEditable(false);
        adviceText.setBackground(HealthTheme.BG_SECONDARY);
        adviceText.setFont(HealthTheme.FONT_BODY_SM);
        
        // 设置HTML样式
        String htmlStyle = "<style>" +
                "body { font-family: 'Microsoft YaHei'; font-size: 14px; color: #475569; }" +
                "p { margin: 8px 0; line-height: 1.6; }" +
                ".warning { color: #E53E3E; font-weight: bold; }" +
                ".normal { color: #38A169; }" +
                ".item-name { color: #2B6CB0; font-weight: bold; }" +
                "</style>";

        StringBuilder html = new StringBuilder("<html>").append(htmlStyle).append("<body>");

        boolean hasAbnormal = false;
        for (ExamRecord record : records) {
            Long testId = record.getTestId();
            CheckItem checkItem = new CheckItemDAO().getById(testId);
            String testName = checkItem != null ? checkItem.getName() : "未知项目";
            String result = record.getResultValue();

            try {
                double value = Double.parseDouble(result);
                String normalRange = checkItem != null ? checkItem.getNormalRange() : "";
                if (normalRange == null || normalRange.isEmpty()) {
                    normalRange = "0-0";
                }

                String[] rangeParts = normalRange.split("[-~]");
                double min = Double.parseDouble(rangeParts[0]);
                double max = Double.parseDouble(rangeParts[1]);

                if (value < min || value > max) {
                    hasAbnormal = true;
                    html.append("<p><span class='item-name'>")
                        .append(testName)
                        .append("</span>: 值为 <b>")
                        .append(value)
                        .append("</b>, 超出正常范围 (")
                        .append(min).append("-").append(max)
                        .append(")，<span class='warning'>建议及时复查。</span></p>");
                }
            } catch (Exception ignored) {}
        }
        
        if (!hasAbnormal) {
            html.append("<p class='normal'>✓ 所有检查项目均在正常范围内，请继续保持良好的生活习惯！</p>");
        }

        html.append("</body></html>");
        adviceText.setText(html.toString());

        JScrollPane scrollPane = new JScrollPane(adviceText);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(HealthTheme.BG_SECONDARY);
        add(scrollPane, BorderLayout.CENTER);
    }
}
