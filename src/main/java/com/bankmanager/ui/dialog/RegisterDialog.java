package com.bankmanager.ui.dialog;

import com.bankmanager.ui.Async;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/** 注册对话框：用户名、密码、确认密码，错误直接显示在表单下方。 */
public final class RegisterDialog extends JDialog {

    private static final Color ERROR_COLOR = new Color(0xC0, 0x39, 0x2B);

    private final ServiceBundle services;
    private final Consumer<String> onRegistered;

    private final JTextField usernameField = UiUtils.field(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JPasswordField confirmField = new JPasswordField(16);
    private final JLabel errorLabel = new JLabel(" ");
    private final JButton registerButton = UiUtils.primaryButton("注册");

    private RegisterDialog(Window owner, ServiceBundle services, Consumer<String> onRegistered) {
        super(owner, "注册新账号", ModalityType.APPLICATION_MODAL);
        this.services = services;
        this.onRegistered = onRegistered;
        buildUi();
    }

    public static void open(Window owner, ServiceBundle services, Consumer<String> onRegistered) {
        UiUtils.checkEdt();
        new RegisterDialog(owner, services, onRegistered).setVisible(true);
    }

    private void buildUi() {
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, UiUtils.GAP, 0));

        JPanel form = UiUtils.form();
        int row = 0;
        UiUtils.addRow(form, row++, "用户名", usernameField);
        UiUtils.addRow(form, row++, "密码", passwordField);
        UiUtils.addRow(form, row++, "确认密码", confirmField);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(UiUtils.sectionTitle("注册柜员账号"), BorderLayout.NORTH);
        header.add(UiUtils.hint("用户名 3–32 位字母、数字或下划线，密码至少 6 位"), BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, UiUtils.GAP));
        content.setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        content.add(header, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, UiUtils.GAP));
        footer.add(errorLabel, BorderLayout.NORTH);
        JButton cancelButton = UiUtils.button("取消");
        cancelButton.addActionListener(e -> dispose());
        registerButton.addActionListener(e -> submit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        buttons.add(cancelButton);
        buttons.add(registerButton);
        footer.add(buttons, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.PAGE_END);

        setContentPane(content);
        getRootPane().setDefaultButton(registerButton);
        getRootPane().registerKeyboardAction(e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        setMinimumSize(new Dimension(400, 320));
        pack();
        UiUtils.center(this);
    }

    private void submit() {
        errorLabel.setText(" ");
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (!password.equals(confirm)) {
            errorLabel.setText("两次输入的密码不一致");
            return;
        }
        Async.run(this, registerButton, "注册中…",
                () -> {
                    services.auth().register(username, password);
                    return username == null ? "" : username.trim();
                },
                name -> {
                    dispose();
                    onRegistered.accept(name);
                },
                error -> errorLabel.setText(UiUtils.message(error)));
    }
}
