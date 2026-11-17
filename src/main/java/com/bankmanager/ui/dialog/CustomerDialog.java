package com.bankmanager.ui.dialog;

import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;
import com.bankmanager.service.ServiceException;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * 客户开户/编辑对话框。
 *
 * <p>开户时卡号可留空，由 {@code CustomerDao.nextCardNumber()} 在后台线程生成；
 * 编辑时余额与贷款只读，资金只能通过「业务办理」页签变动。
 */
public final class CustomerDialog extends JDialog {

    private final ServiceBundle services;
    private final Customer existing;
    private final Consumer<String> onSaved;

    private final JTextField cardField = UiUtils.field(12);
    private final JTextField nameField = UiUtils.field(12);
    private final JTextField teleField = UiUtils.field(12);
    private final JComboBox<AccountType> typeBox = new JComboBox<>(AccountType.values());
    private final JTextField balanceField = UiUtils.field(10);
    private final JPasswordField passwordField = new JPasswordField(12);
    private final JLabel errorLabel = UiUtils.hint(" ");
    private final JButton saveButton = UiUtils.primaryButton("保存");

    private CustomerDialog(Window owner, ServiceBundle services, Customer existing, Consumer<String> onSaved) {
        super(owner, existing == null ? "开户" : "编辑客户", ModalityType.APPLICATION_MODAL);
        this.services = services;
        this.existing = existing;
        this.onSaved = onSaved;
        buildUi();
        fill();
    }

    public static void open(Window owner, ServiceBundle services, Customer existing, Consumer<String> onSaved) {
        UiUtils.checkEdt();
        new CustomerDialog(owner, services, existing, onSaved).setVisible(true);
    }

    private void buildUi() {
        typeBox.setRenderer((list, value, index, selected, focus) -> {
            JLabel label = new JLabel(value == null ? "" : value.label());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
            return label;
        });

        JPanel form = UiUtils.form();
        int row = 0;
        UiUtils.addRow(form, row++, "卡号", cardField);
        UiUtils.addRow(form, row++, "姓名", nameField);
        UiUtils.addRow(form, row++, "联系电话", teleField);
        UiUtils.addRow(form, row++, "客户等级", typeBox);
        UiUtils.addRow(form, row++, existing == null ? "初始余额" : "当前余额", balanceField);
        UiUtils.addRow(form, row++, existing == null ? "交易密码" : "重置密码", passwordField);

        JPanel content = new JPanel(new BorderLayout(0, UiUtils.GAP));
        content.setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        content.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, UiUtils.GAP));
        footer.add(errorLabel, BorderLayout.NORTH);
        JButton cancelButton = UiUtils.button("取消");
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> submit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        buttons.add(cancelButton);
        buttons.add(saveButton);
        footer.add(buttons, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.PAGE_END);

        setContentPane(content);
        getRootPane().setDefaultButton(saveButton);
        getRootPane().registerKeyboardAction(e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        setMinimumSize(new Dimension(440, existing == null ? 360 : 340));
        pack();
        UiUtils.center(this);
    }

    private void fill() {
        if (existing == null) {
            cardField.setToolTipText("留空则自动生成 16 位卡号");
            balanceField.setText("0.00");
            return;
        }
        cardField.setText(existing.card());
        cardField.setEnabled(false);
        balanceField.setText(existing.balance().toPlainString());
        balanceField.setEnabled(false);
        balanceField.setToolTipText("余额只能通过业务办理页签变动");
        nameField.setText(existing.name());
        teleField.setText(existing.tele());
        typeBox.setSelectedItem(existing.type());
        passwordField.setToolTipText("留空表示不修改密码");
        setTitle("编辑客户 - " + existing.card());
    }

    private void submit() {
        errorLabel.setText(" ");
        BigDecimal balance;
        try {
            balance = UiUtils.parseAmount(balanceField.getText());
        } catch (ServiceException e) {
            errorLabel.setText(e.getMessage());
            return;
        }
        String pwd = new String(passwordField.getPassword());
        AccountType type = (AccountType) typeBox.getSelectedItem();
        String name = nameField.getText();
        String tele = teleField.getText();
        String card = cardField.getText().trim();

        Async.run(this, saveButton, "保存中…", () -> {
            if (existing == null) {
                String finalCard = card.isEmpty() ? services.customerDao().nextCardNumber() : card;
                Customer customer = new Customer(finalCard, name, "", tele, type, balance, BigDecimal.ZERO);
                services.account().createCustomer(customer, pwd);
                return "开户成功，卡号：" + finalCard;
            }
            Customer customer = new Customer(existing.card(), name, pwd, tele, type, balance, existing.loan());
            services.account().updateCustomer(customer);
            return "客户信息已更新";
        }, message -> {
            dispose();
            onSaved.accept(message);
        });
    }
}
