package com.bankmanager.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** FlatLaf 主题：浅色/深色可切换，并统一中文字体与控件圆角。 */
public final class Theme {

    private static final String[] FONT_CANDIDATES = {
            "PingFang SC", "Microsoft YaHei", "Noto Sans CJK SC", "Source Han Sans SC", "SimHei"
    };

    private static boolean dark;

    private Theme() {
    }

    /** 安装主题；必须在创建任何窗口之前调用。 */
    public static void install(boolean darkTheme) {
        dark = darkTheme;
        UIManager.put("defaultFont", new Font(uiFontFamily(), Font.PLAIN, 13));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("TabbedPane.tabHeight", 38);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("ScrollBar.width", 12);
        FlatLaf.setup(darkTheme ? new FlatDarkLaf() : new FlatLightLaf());
    }

    public static boolean isDark() {
        return dark;
    }

    /** 切换主题并刷新所有已打开的窗口。 */
    public static void toggle() {
        install(!dark);
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
        FlatLaf.updateUI();
    }

    /** 优先使用系统中可用的中文字体，避免界面出现方框。 */
    private static String uiFontFamily() {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String candidate : FONT_CANDIDATES) {
            if (available.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
    }
}
