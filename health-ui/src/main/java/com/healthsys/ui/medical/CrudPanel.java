package com.healthsys.ui.medical;

import com.healthsys.ui.HealthTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class CrudPanel<T> extends JPanel {
    private JPanel contentPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton searchButton;
    private JPanel searchPanel;

    public CrudPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        createToolbar();
        createContent();
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        // 创建按钮 - 使用与用户端预约页面统一的颜色
        addButton = createStyledButton("添加", BTN_WARNING_YELLOW);
        editButton = createStyledButton("编辑", BTN_SUCCESS_GREEN);
        deleteButton = createStyledButton("删除", BTN_NEUTRAL_GRAY);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // 搜索面板
        searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setBackground(Color.WHITE);

        searchButton = createStyledButton("查询", BTN_INFO_BLUE);
        searchPanel.add(searchButton);

        toolbar.add(buttonPanel, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.CENTER);

        add(toolbar, BorderLayout.NORTH);
    }

    // 统一的按钮创建方法 - 使用HealthTheme
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(HealthTheme.FONT_BUTTON);
        button.setForeground(Color.WHITE); // 白色文字
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(90, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker()); // 悬停变深
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor); // 恢复原色
            }
        });

        return button;
    }
    
    // 系统适配的按钮颜色常量
    public static final Color BTN_WARNING_YELLOW = new Color(255, 193, 7);   // 黄色 - 待检查
    public static final Color BTN_SUCCESS_GREEN = new Color(76, 175, 80);    // 绿色 - 已完成
    public static final Color BTN_NEUTRAL_GRAY = new Color(158, 158, 158);   // 灰色 - 已取消
    public static final Color BTN_INFO_BLUE = new Color(33, 150, 243);       // 蓝色 - 信息

    private void createContent() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setContent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Getter方法
    public JButton getAddButton() { return addButton; }
    public JButton getEditButton() { return editButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getSearchButton() { return searchButton; }
    public JPanel getSearchPanel() { return searchPanel; }

    public abstract void refreshData();
}

