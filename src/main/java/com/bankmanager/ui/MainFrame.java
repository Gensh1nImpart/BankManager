package com.bankmanager.ui;

import com.bankmanager.service.AuthService;
import com.bankmanager.ui.panel.BusinessPanel;
import com.bankmanager.ui.panel.CustomerPanel;
import com.bankmanager.ui.panel.EmployeePanel;
import com.bankmanager.ui.panel.LogPanel;
import com.bankmanager.ui.panel.QueuePanel;
import com.bankmanager.ui.panel.RoutePanel;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** 主窗口：六个业务页签 + 状态栏 + 主题/退出菜单。 */
public final class MainFrame extends JFrame {

    private final ServiceBundle services;
    private final AuthService.Session session;
    private final Runnable onExit;
    private final JTabbedPane tabs = new JTabbedPane();
    private final BusinessPanel businessPanel;

    private MainFrame(ServiceBundle services, AuthService.Session session, Runnable onExit) {
        super("银行管理系统 — " + session.username());
        this.services = services;
        this.session = session;
        this.onExit = onExit;

        EmployeePanel employeePanel = new EmployeePanel(services);
        CustomerPanel customerPanel = new CustomerPanel(services, this::openBusiness);
        businessPanel = new BusinessPanel(services);
        QueuePanel queuePanel = new QueuePanel(services);
        RoutePanel routePanel = new RoutePanel(services);
        LogPanel logPanel = new LogPanel(services);

        tabs.addTab("职员管理", employeePanel);
        tabs.addTab("客户管理", customerPanel);
        tabs.addTab("业务办理", businessPanel);
        tabs.addTab("排队管理", queuePanel);
        tabs.addTab("网点导航", routePanel);
        tabs.addTab("操作日志", logPanel);
        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected instanceof Refreshable refreshable) {
                refreshable.refresh();
            }
        });

        setJMenuBar(buildMenuBar());
        JPanel content = new JPanel(new BorderLayout());
        content.add(tabs, BorderLayout.CENTER);
        content.add(buildStatusBar(), BorderLayout.PAGE_END);
        setContentPane(content);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (UiUtils.confirm(MainFrame.this, "确定退出银行管理系统吗？")) {
                    onExit.run();
                }
            }
        });
        setMinimumSize(new Dimension(1100, 720));
        setSize(1200, 780);
        UiUtils.center(this);
    }

    /** 在 EDT 上创建并显示主窗口。 */
    public static MainFrame open(ServiceBundle services, AuthService.Session session, Runnable onExit) {
        UiUtils.checkEdt();
        MainFrame frame = new MainFrame(services, session, onExit);
        frame.setVisible(true);
        return frame;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu system = new JMenu("系统");
        JCheckBoxMenuItem themeItem = new JCheckBoxMenuItem("深色主题", Theme.isDark());
        themeItem.addActionListener(e -> {
            Theme.toggle();
            themeItem.setSelected(Theme.isDark());
        });
        JMenuItem refreshItem = new JMenuItem("刷新当前页");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(e -> refreshCurrentTab());
        JMenuItem logoutItem = new JMenuItem("退出登录");
        logoutItem.addActionListener(e -> logout());
        JMenuItem exitItem = new JMenuItem("退出系统");
        exitItem.addActionListener(e -> {
            if (UiUtils.confirm(this, "确定退出银行管理系统吗？")) {
                onExit.run();
            }
        });
        system.add(refreshItem);
        system.addSeparator();
        system.add(themeItem);
        system.addSeparator();
        system.add(logoutItem);
        system.add(exitItem);

        JMenu help = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "银行管理系统 2.0\nSwing + FlatLaf + MySQL\n\n"
                        + "分层结构：config / db / dao / service / ui\n"
                        + "密码使用 BCrypt 存储，金额使用 BigDecimal 计算。",
                "关于", JOptionPane.INFORMATION_MESSAGE));
        help.add(aboutItem);

        bar.add(system);
        bar.add(help);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0,
                        javax.swing.UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        JLabel user = new JLabel("当前用户：" + session.username() + "（" + roleLabel() + "）");
        JLabel tech = UiUtils.hint("Swing + FlatLaf · 数据来自 MySQL 银行库");
        status.add(user, BorderLayout.WEST);
        status.add(tech, BorderLayout.EAST);
        return status;
    }

    private String roleLabel() {
        return "ADMIN".equalsIgnoreCase(session.role()) ? "管理员" : "柜员";
    }

    private void refreshCurrentTab() {
        Component selected = tabs.getSelectedComponent();
        if (selected instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
    }

    /** 从客户管理页跳转到业务办理并带入卡号。 */
    private void openBusiness(String card) {
        tabs.setSelectedComponent(businessPanel);
        businessPanel.focusCard(card);
    }

    private void logout() {
        if (!UiUtils.confirm(this, "确定退出当前账号吗？")) {
            return;
        }
        SessionContext.clear();
        dispose();
        new LoginFrame(services, onExit).setVisible(true);
    }
}
