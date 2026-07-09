package com.healthsys.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Health 1.0 统一主题常量 —— 医疗行业标准配色 + 字体排版
 * 所有 UI 组件应引用此类的常量，保持全局视觉一致。
 */
public final class HealthTheme {

    private HealthTheme() {} // 工具类，禁止实例化

    // ==================== 医疗行业配色 ====================
    /** 主色 - 关于页同款蓝 #2B6CB0 */
    public static final Color PRIMARY        = new Color(43, 108, 176);
    /** 主色浅底 #EBF4FF */
    public static final Color PRIMARY_LIGHT  = new Color(235, 244, 255);
    /** 主色深 #1A4971 */
    public static final Color PRIMARY_DARK   = new Color(26, 73, 113);
    /** 辅助色 - 健康绿 #38A169 */
    public static final Color SUCCESS        = new Color(56, 161, 105);
    /** 警示色 - 柔和橙 #DD6B20 */
    public static final Color WARNING        = new Color(221, 107, 32);
    /** 危险色 - 沉稳红 #E53E3E */
    public static final Color DANGER         = new Color(229, 62, 62);

    // ==================== 按钮语义色 ====================
    /** 主要操作按钮（添加/确定/保存） */
    public static final Color BTN_PRIMARY    = PRIMARY;
    /** 次要操作按钮（编辑/查看/导出） */
    public static final Color BTN_SECONDARY  = new Color(66, 153, 225); // #4299E1
    /** 成功操作按钮（开始检查/保存结果） */
    public static final Color BTN_SUCCESS    = SUCCESS;
    /** 警告操作按钮（查看结果） */
    public static final Color BTN_WARNING    = WARNING;
    /** 危险操作按钮（删除） */
    public static final Color BTN_DANGER     = DANGER;
    /** 取消/关闭按钮 */
    public static final Color BTN_CANCEL     = new Color(160, 174, 192); // #A0AEC0

    // ==================== 灰阶 ====================
    /** 页面背景 #F8FAFC */
    public static final Color BG_PAGE        = new Color(248, 250, 252);
    /** 次级背景 #F1F5F9 */
    public static final Color BG_SECONDARY   = new Color(241, 245, 249);
    /** 边框/分割线 #E2E8F0 */
    public static final Color BORDER         = new Color(226, 232, 240);
    /** 卡片白 */
    public static final Color CARD_BG        = Color.WHITE;

    // ==================== 文字色 ====================
    /** 主文字 #0F172A */
    public static final Color TEXT_PRIMARY   = new Color(15, 23, 42);
    /** 辅助文字 #475569 */
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    /** 提示文字 #94A3B8 */
    public static final Color TEXT_HINT      = new Color(148, 163, 184);

    // ==================== 表格专用 ====================
    /** 表头背景 */
    public static final Color TABLE_HEADER   = PRIMARY;
    /** 交替行底色 */
    public static final Color TABLE_ALT_ROW  = PRIMARY_LIGHT;
    /** 选中行底色 */
    public static final Color TABLE_SELECTED = new Color(219, 234, 254);

    // ==================== 导航栏（深色侧边栏） ====================
    /** 侧边栏背景 - 深蓝灰 #1E293B */
    public static final Color NAV_BG         = new Color(30, 41, 59);
    /** 侧边栏顶部品牌区 #162032 */
    public static final Color NAV_BRAND      = new Color(22, 32, 50);
    /** 导航按钮默认（透明融入底色） */
    public static final Color NAV_BTN        = new Color(30, 41, 59);
    /** 导航按钮悬停 #334155 */
    public static final Color NAV_BTN_HOVER  = new Color(51, 65, 85);
    /** 导航按钮选中/激活 #2563EB */
    public static final Color NAV_BTN_ACTIVE = new Color(37, 99, 235);
    /** 导航文字 - 柔白 #CBD5E1 */
    public static final Color NAV_TEXT       = new Color(203, 213, 225);
    /** 导航文字激活态 - 纯白 */
    public static final Color NAV_TEXT_ACTIVE = Color.WHITE;

    // ==================== 字体 ====================
    public static final String FONT_FAMILY   = "Microsoft YaHei";

    /** 页面标题 32px */
    public static final Font FONT_PAGE_TITLE  = new Font(FONT_FAMILY, Font.BOLD, 32);
    /** 大标题 24px */
    public static final Font FONT_TITLE       = new Font(FONT_FAMILY, Font.BOLD, 24);
    /** 小标题 20px */
    public static final Font FONT_SUBTITLE    = new Font(FONT_FAMILY, Font.BOLD, 20);
    /** 正文 16px */
    public static final Font FONT_BODY        = new Font(FONT_FAMILY, Font.PLAIN, 16);
    /** 正文小字 14px */
    public static final Font FONT_BODY_SM     = new Font(FONT_FAMILY, Font.PLAIN, 14);
    /** 辅助信息 12px */
    public static final Font FONT_CAPTION     = new Font(FONT_FAMILY, Font.PLAIN, 12);
    /** 按钮字体 14px Bold */
    public static final Font FONT_BUTTON      = new Font(FONT_FAMILY, Font.BOLD, 14);
    /** 导航字体 14px Bold */
    public static final Font FONT_NAV         = new Font(FONT_FAMILY, Font.BOLD, 14);

    // ==================== 间距 ====================
    /** 卡片内边距 */
    public static final int PADDING_CARD     = 20;
    /** 卡片间距 */
    public static final int GAP_CARD         = 20;
    /** 表单元素间距 */
    public static final int GAP_FORM         = 16;
    /** 内容区外边距 */
    public static final int PADDING_CONTENT  = 24;

    // ==================== FlatLaf 全局属性注入 ====================
    /**
     * 在 FlatLaf.setup() 之后调用，设置全局 UIManager 属性。
     */
    public static void applyGlobalDefaults() {
        // 按钮
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Button.focusWidth", 0);
        UIManager.put("Button.innerFocusWidth", 0);
        UIManager.put("Button.arc", 8);

        // 输入框
        UIManager.put("TextField.font", FONT_BODY_SM);
        UIManager.put("TextField.arc", 6);
        UIManager.put("PasswordField.font", FONT_BODY_SM);
        UIManager.put("PasswordField.arc", 6);

        // 标签
        UIManager.put("Label.font", FONT_BODY_SM);
        UIManager.put("Label.foreground", TEXT_PRIMARY);

        // 复选框
        UIManager.put("CheckBox.font", FONT_BODY_SM);
        UIManager.put("CheckBox.icon.focusWidth", 0);

        // 表格
        UIManager.put("Table.font", FONT_BODY_SM);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 0));
        UIManager.put("Table.selectionBackground", TABLE_SELECTED);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("TableHeader.font", FONT_BUTTON);
        UIManager.put("TableHeader.background", TABLE_HEADER);
        UIManager.put("TableHeader.foreground", Color.WHITE);

        // 面板 / 窗口
        UIManager.put("Panel.background", BG_PAGE);
        UIManager.put("OptionPane.messageFont", FONT_BODY_SM);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);

        // 滚动条
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 10);

        // 选项卡 / 弹窗
        UIManager.put("Dialog.background", BG_PAGE);
    }
}
