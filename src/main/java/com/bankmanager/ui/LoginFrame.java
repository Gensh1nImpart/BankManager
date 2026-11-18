package com.bankmanager.ui;

import com.bankmanager.service.AuthService;
import com.bankmanager.ui.dialog.RegisterDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** 登录窗口：登录与注册两个入口，回车提交，错误内联展示。 */
public final class LoginFrame extends JFrame {

    private static final Color ERROR_COLOR = new Color(0xC0, 0x39, 0x2B);
    private static final Color SUCCESS_COLOR = new Color(0x2E, 0x7D, 0x32);
    private static final String CARD_WIDTH_HTML = "<html><body style='width:320px'>";

    private final ServiceBundle services;
    private final Runnable onExit;

    private final JTextField usernameField = UiUtils.field(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton loginButton = UiUtils.primaryButton("登 录");

    public LoginFrame(ServiceBundle services, Runnable onExit) {
        super("银行管理系统 — 登录");
        this.services = services;
        this.onExit = onExit;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit.run();
            }
        });

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel title = UiUtils.title("银行管理系统");
        title.setFont(title.getFont().deriveFont(26f));
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = UiUtils.hint("BankManager 2.0 · 柜员工作台");
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        brand.add(title);
        brand.add(Box.createVerticalStrut(4));
        brand.add(subtitle);

        passwordField.setPreferredSize(new Dimension(240, 32));
        usernameField.setPreferredSize(new Dimension(240, 32));
        messageLabel.setForeground(ERROR_COLOR);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(labelled("用户名", usernameField));
        form.add(Box.createVerticalStrut(UiUtils.GAP));
        form.add(labelled("密　码", passwordField));
        form.add(Box.createVerticalStrut(UiUtils.GAP));
        form.add(messageLabel);
        form.add(Box.createVerticalStrut(UiUtils.GAP));

        loginButton.setAlignmentX(CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginButton.addActionListener(e -> login());
        form.add(loginButton);
        form.add(Box.createVerticalStrut(UiUtils.GAP / 2));

        JButton registerButton = UiUtils.button("还没有账号？立即注册");
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setAlignmentX(CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> RegisterDialog.open(this, services, this::afterRegistered));
        form.add(registerButton);

        JPanel card = new JPanel(new BorderLayout(0, UiUtils.PADDING));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(javax.swing.UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)));
        card.add(brand, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().setDefaultButton(loginButton);
        setResizable(false);
        setSize(460, 430);
        UiUtils.center(this);
    }

    private static JPanel labelled(String text, javax.swing.JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(new JLabel(text), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return panel;
    }

    private void login() {
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setText(" ");
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty()) {
            messageLabel.setText("请输入用户名");
            usernameField.requestFocusInWindow();
            return;
        }
        if (password.isEmpty()) {
            messageLabel.setText("请输入密码");
            passwordField.requestFocusInWindow();
            return;
        }
        Async.run(this, loginButton, "登录中…",
                () -> services.auth().login(username, password),
                this::onLoggedIn,
                error -> {
                    messageLabel.setText(CARD_WIDTH_HTML + UiUtils.message(error) + "</body></html>");
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                });
    }

    private void onLoggedIn(AuthService.Session session) {
        SessionContext.set(session);
        dispose();
        MainFrame.open(services, session, onExit);
    }

    private void afterRegistered(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        passwordField.requestFocusInWindow();
        messageLabel.setForeground(SUCCESS_COLOR);
        messageLabel.setText("注册成功，请使用新账号登录");
    }
}
