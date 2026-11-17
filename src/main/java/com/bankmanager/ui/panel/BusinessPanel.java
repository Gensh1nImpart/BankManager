package com.bankmanager.ui.panel;

import com.bankmanager.domain.Customer;
import com.bankmanager.service.ServiceException;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.Refreshable;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * 业务办理页签：存款、取款、贷款、还款、转账与利息试算。
 *
 * <p>界面只做格式解析与按钮禁用，金额上限、余额校验、卡号存在性等规则
 * 全部由 {@code AccountService} 负责，错误通过 {@link Async} 统一提示。
 */
public final class BusinessPanel extends JPanel implements Refreshable {

    private static final Pattern CARD_PATTERN = Pattern.compile("\\d{16}");
    private static final int RATE_SCALE = 6;

    private final ServiceBundle services;

    private final JTextField cardField = UiUtils.field(16);
    private final JButton queryButton = UiUtils.button("查询账户");
    private final JLabel nameValue = new JLabel("-");
    private final JLabel typeValue = new JLabel("-");
    private final JLabel balanceValue = new JLabel("-");
    private final JLabel loanValue = new JLabel("-");

    private final JTextField amountField = UiUtils.field(14);
    private final JTextField toCardField = UiUtils.field(16);
    private final JButton depositButton = UiUtils.primaryButton("存款");
    private final JButton withdrawButton = UiUtils.button("取款");
    private final JButton loanButton = UiUtils.button("贷款");
    private final JButton repayButton = UiUtils.button("还款");
    private final JButton transferButton = UiUtils.button("转账");

    private final JTextField rateField = UiUtils.field(8);
    private final JTextField monthsField = UiUtils.field(6);
    private final JButton interestButton = UiUtils.button("试算利息");
    private final JLabel interestValue = UiUtils.hint("按「本金 × 月利率 × 月数」计算，四舍五入两位小数");

    private final JLabel statusLabel = UiUtils.hint(" ");

    public BusinessPanel(ServiceBundle services) {
        super(new BorderLayout(0, UiUtils.GAP));
        this.services = services;
        setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        buildUi();
    }

    private void buildUi() {
        queryButton.addActionListener(e -> refresh());
        cardField.addActionListener(e -> refresh());

        depositButton.addActionListener(e -> operate("存款", depositButton, amount -> {
            services.account().deposit(card(), amount);
        }));
        withdrawButton.addActionListener(e -> operate("取款", withdrawButton, amount -> {
            services.account().withdraw(card(), amount);
        }));
        loanButton.addActionListener(e -> operate("贷款", loanButton, amount -> {
            services.account().takeLoan(card(), amount);
        }));
        repayButton.addActionListener(e -> operate("还款", repayButton, amount -> {
            services.account().repayLoan(card(), amount);
        }));
        transferButton.addActionListener(e -> operate("转账", transferButton, amount -> {
            services.account().transfer(card(), toCard(), amount);
        }));

        interestButton.addActionListener(e -> calculateInterest());

        add(buildAccountSection(), BorderLayout.PAGE_START);
        add(buildTradeSection(), BorderLayout.CENTER);
        add(buildInterestSection(), BorderLayout.PAGE_END);
    }

    private JPanel buildAccountSection() {
        JPanel body = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 6, 8);
        c.anchor = GridBagConstraints.LINE_START;

        c.gridy = 0;
        c.gridx = 0;
        body.add(new JLabel("卡号"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        body.add(cardField, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        body.add(queryButton, c);

        nameValue.setFont(nameValue.getFont().deriveFont(Font.BOLD, 16f));
        balanceValue.setFont(balanceValue.getFont().deriveFont(Font.BOLD, 18f));

        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP * 2, 4));
        info.add(label("姓名", nameValue));
        info.add(label("等级", typeValue));
        info.add(label("余额", balanceValue));
        info.add(label("贷款", loanValue));
        body.add(info, c);
        return UiUtils.section("账户信息", body);
    }

    private JPanel buildTradeSection() {
        JPanel form = UiUtils.form();
        int row = 0;
        UiUtils.addRow(form, row++, "交易金额", amountField);
        UiUtils.addRow(form, row++, "转入卡号", toCardField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 0));
        buttons.add(depositButton);
        buttons.add(withdrawButton);
        buttons.add(loanButton);
        buttons.add(repayButton);
        buttons.add(transferButton);

        JPanel body = new JPanel(new BorderLayout(0, UiUtils.GAP));
        body.add(form, BorderLayout.NORTH);
        body.add(buttons, BorderLayout.CENTER);
        body.add(UiUtils.hint("转入卡号仅在转账时填写；金额最多两位小数，单笔不超过 1 亿元"), BorderLayout.SOUTH);
        return UiUtils.section("交易办理", body);
    }

    private JPanel buildInterestSection() {
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 0));
        fields.add(new JLabel("月利率(%)"));
        fields.add(rateField);
        fields.add(new JLabel("月数"));
        fields.add(monthsField);
        fields.add(interestButton);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.add(fields, BorderLayout.NORTH);
        body.add(interestValue, BorderLayout.CENTER);
        body.add(statusLabel, BorderLayout.SOUTH);
        return UiUtils.section("利息试算", body);
    }

    private static JPanel label(String text, JLabel value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.add(UiUtils.hint(text));
        panel.add(value);
        return panel;
    }

    @Override
    public void refresh() {
        String card = card();
        if (card.isEmpty()) {
            // 页签刚打开时还没有输入卡号，保持界面安静
            return;
        }
        if (!CARD_PATTERN.matcher(card).matches()) {
            setStatus("请输入 16 位卡号后查询", true);
            return;
        }
        Async.load(this, queryButton, () -> services.account().customer(card), this::showCustomer);
    }

    /** 供客户管理页跳转过来时直接定位账户。 */
    public void focusCard(String card) {
        cardField.setText(card);
        refresh();
        cardField.requestFocusInWindow();
    }

    private void showCustomer(Customer customer) {
        nameValue.setText(customer.name());
        typeValue.setText(customer.typeLabel());
        balanceValue.setText(UiUtils.money(customer.balance()));
        loanValue.setText(UiUtils.money(customer.loan()));
        setStatus("已加载 " + customer.card() + " 的账户信息", false);
    }

    private void operate(String label, JButton trigger, java.util.function.Consumer<BigDecimal> operation) {
        String card = card();
        if (!CARD_PATTERN.matcher(card).matches()) {
            setStatus("请先输入正确的 16 位卡号", true);
            return;
        }
        BigDecimal amount;
        try {
            amount = UiUtils.parseAmount(amountField.getText());
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
            return;
        }
        Async.runVoid(this, trigger, "处理中…", () -> operation.accept(amount), () -> {
            setStatus(label + "成功，正在刷新余额…", false);
            amountField.setText("");
            Async.load(this, null, () -> services.account().customer(card), this::showCustomer);
        });
    }

    private void calculateInterest() {
        String card = card();
        if (!CARD_PATTERN.matcher(card).matches()) {
            setStatus("请先输入正确的 16 位卡号", true);
            return;
        }
        BigDecimal rate;
        int months;
        try {
            BigDecimal percent = UiUtils.parseAmount(rateField.getText());
            if (percent.signum() < 0) {
                throw new ServiceException("月利率不能为负数");
            }
            rate = percent.divide(BigDecimal.valueOf(100), RATE_SCALE, RoundingMode.HALF_UP);
            months = Integer.parseInt(monthsField.getText().trim());
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
            return;
        } catch (NumberFormatException e) {
            setStatus("月数必须是整数", true);
            return;
        }
        Async.load(this, interestButton, () -> services.account().interest(card, rate, months),
                result -> interestValue.setText("预计利息：" + UiUtils.money(result)
                        + "（本金不变，仅供参考）"));
    }

    private String card() {
        return cardField.getText() == null ? "" : cardField.getText().trim();
    }

    private String toCard() {
        return toCardField.getText() == null ? "" : toCardField.getText().trim();
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setForeground(error
                ? new java.awt.Color(0xC0, 0x39, 0x2B)
                : javax.swing.UIManager.getColor("Label.disabledForeground"));
    }
}
