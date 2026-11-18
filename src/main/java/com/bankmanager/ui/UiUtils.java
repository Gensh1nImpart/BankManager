package com.bankmanager.ui;

import com.bankmanager.service.ServiceException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 界面公共工具：EDT 断言、金额格式化/解析、表格样式、统一提示框。
 * 所有方法都只做纯界面工作，不访问数据库。
 */
public final class UiUtils {

    /** 统一外边距。 */
    public static final int PADDING = 20;
    /** 控件之间的间距。 */
    public static final int GAP = 10;
    /** 业务金额统一保留两位小数。 */
    public static final int MONEY_SCALE = 2;

    private static final DecimalFormat MONEY_FORMAT =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.CHINA));
    private static final Font TITLE_FONT = new Font("", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("", Font.BOLD, 14);
    private static final Font HINT_FONT = new Font("", Font.PLAIN, 12);

    private UiUtils() {
    }

    /** 断言当前在事件分发线程上，数据库/耗时操作绝不会误在主线程执行。 */
    public static void checkEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("界面操作必须在事件分发线程（EDT）上执行");
        }
    }

    /** 确保逻辑回到 EDT 执行。 */
    public static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    /** 金额展示：¥ 1,234.56。 */
    public static String money(BigDecimal amount) {
        return "¥ " + MONEY_FORMAT.format(amount == null ? BigDecimal.ZERO : amount);
    }

    /** 金额展示（不带货币符号），用于表格输入框回填。 */
    public static String plainMoney(BigDecimal amount) {
        return MONEY_FORMAT.format(amount == null ? BigDecimal.ZERO : amount);
    }

    /**
     * 解析用户输入的金额：允许千分位、空格、￥/¥ 与“元”后缀。
     * 只做格式解析，业务规则（大于 0、两位小数、上限）由服务层统一校验。
     */
    public static BigDecimal parseAmount(String text) {
        String value = text == null ? "" : text.trim()
                .replace(",", "")
                .replace("，", "")
                .replace("¥", "")
                .replace("￥", "")
                .replace("元", "")
                .replace(" ", "");
        if (value.isEmpty()) {
            throw new ServiceException("请输入金额");
        }
        try {
            return new BigDecimal(value).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new ServiceException("金额格式不正确，请输入数字");
        }
    }

    /** 把任意异常转成可以直接展示的一句中文。 */
    public static String message(Throwable error) {
        if (error == null) {
            return "未知错误";
        }
        if (error instanceof ServiceException) {
            return error.getMessage();
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return message;
    }

    /** 统一错误弹窗。 */
    public static void showError(Component parent, Throwable error) {
        showError(parent, message(error));
    }

    public static void showError(Component parent, String message) {
        checkEdt();
        JOptionPane.showMessageDialog(parent, message, "操作失败", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        checkEdt();
        JOptionPane.showMessageDialog(parent, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        checkEdt();
        return JOptionPane.showConfirmDialog(parent, message, "请确认",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    /** 标题标签。 */
    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        return label;
    }

    /** 分区块标题。 */
    public static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, GAP, 0));
        return label;
    }

    /** 次要说明文字。 */
    public static JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HINT_FONT);
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        return label;
    }

    /** 普通按钮。 */
    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        return button;
    }

    /** 主操作按钮（FlatLaf 会按默认按钮样式渲染）。 */
    public static JButton primaryButton(String text) {
        JButton button = button(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        return button;
    }

    /** 创建 GridBagLayout 表单面板。 */
    public static JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return panel;
    }

    /** 向表单追加一行：左侧标签，右侧输入组件。 */
    public static void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.LINE_END;
        left.insets = new Insets(0, 0, GAP, GAP);
        form.add(new JLabel(label), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.anchor = GridBagConstraints.LINE_START;
        right.insets = new Insets(0, 0, GAP, 0);
        form.add(field, right);
    }

    /** 表单单行输入框，统一首选宽度。 */
    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        field.setPreferredSize(new Dimension(Math.max(columns * 10, 120), 30));
        return field;
    }

    /** 带内边距与标题的分区容器。 */
    public static JPanel section(String title, JComponent content) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, GAP));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        panel.add(content, java.awt.BorderLayout.CENTER);
        return panel;
    }

    /** 表格统一外观：行高、字体、表头、列宽。 */
    public static void styleTable(JTable table, int... preferredWidths) {
        checkEdt();
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null); // 只读，编辑走对话框
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        int index = 0;
        for (int width : preferredWidths) {
            if (index < table.getColumnModel().getColumnCount()) {
                TableColumn column = table.getColumnModel().getColumn(index++);
                column.setPreferredWidth(width);
                if (width <= 70) {
                    column.setMaxWidth(width + 20);
                }
            }
        }
    }

    /** 金额列：右对齐并按千分位展示。 */
    public static void moneyColumn(JTable table, int... columns) {
        for (int column : columns) {
            table.getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object value) {
                    setHorizontalAlignment(SwingUtilities.RIGHT);
                    setText(value instanceof BigDecimal amount ? money(amount) : "");
                }
            });
        }
    }

    /** 居中显示窗口。 */
    public static void center(Window window) {
        window.setLocationRelativeTo(null);
    }
}
