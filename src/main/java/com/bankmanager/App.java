package com.bankmanager;

import com.bankmanager.config.Config;
import com.bankmanager.dao.LogDao;
import com.bankmanager.dao.impl.JdbcCustomerDao;
import com.bankmanager.dao.impl.JdbcEmployeeDao;
import com.bankmanager.dao.impl.JdbcGraphDao;
import com.bankmanager.dao.impl.JdbcLogDao;
import com.bankmanager.dao.impl.JdbcQueueDao;
import com.bankmanager.dao.impl.JdbcUserDao;
import com.bankmanager.db.Database;
import com.bankmanager.service.AccountService;
import com.bankmanager.service.AuthService;
import com.bankmanager.service.EmployeeService;
import com.bankmanager.service.QueueService;
import com.bankmanager.service.RouteService;
import com.bankmanager.service.impl.DefaultAccountService;
import com.bankmanager.service.impl.DefaultAuthService;
import com.bankmanager.service.impl.DefaultEmployeeService;
import com.bankmanager.service.impl.DefaultQueueService;
import com.bankmanager.service.impl.DefaultRouteService;
import com.bankmanager.service.impl.TransactionAwareDataSource;
import com.bankmanager.ui.LoginFrame;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.SessionContext;
import com.bankmanager.ui.Theme;
import com.bankmanager.ui.UiUtils;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用入口：安装 FlatLaf、读取配置、装配数据源/DAO/服务，最后在 EDT 上打开登录窗口。
 *
 * <p>关闭窗口时统一调用 {@link #shutdown(Database)} 释放 Hikari 连接池。
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        boolean dark = args != null && java.util.Arrays.asList(args).contains("--dark");
        Theme.install(dark);

        Config config;
        Database database;
        try {
            config = Config.load();
            // 事务感知数据源：让 Database.inTransaction 内的多个 DAO 共用同一连接
            database = new Database(new TransactionAwareDataSource(config));
            // 触发一次连接，尽早暴露配置错误（连接池按需初始化）
            database.getConnection().close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "无法连接数据库，请检查 config.properties 或环境变量：\n" + UiUtils.message(e),
                    "启动失败", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        ServiceBundle services = assemble(database);
        AtomicBoolean closed = new AtomicBoolean(false);
        Runnable exit = () -> {
            if (closed.compareAndSet(false, true)) {
                database.close();
            }
            System.exit(0);
        };

        SwingUtilities.invokeLater(() -> {
            SessionContext.clear();
            new LoginFrame(services, exit).setVisible(true);
        });
    }

    /** 装配 DAO 与业务服务；UI 只拿到接口。 */
    private static ServiceBundle assemble(Database database) {
        JdbcUserDao userDao = new JdbcUserDao(database);
        JdbcCustomerDao customerDao = new JdbcCustomerDao(database);
        JdbcEmployeeDao employeeDao = new JdbcEmployeeDao(database);
        JdbcQueueDao queueDao = new JdbcQueueDao(database);
        JdbcGraphDao graphDao = new JdbcGraphDao(database);
        LogDao logDao = new JdbcLogDao(database);

        AuthService auth = new DefaultAuthService(userDao);
        AccountService account = new DefaultAccountService(customerDao, logDao, database,
                SessionContext::username);
        EmployeeService employee = new DefaultEmployeeService(employeeDao);
        QueueService queue = new DefaultQueueService(queueDao);
        RouteService route = new DefaultRouteService(graphDao);

        return new ServiceBundle(auth, account, employee, queue, route, customerDao, logDao);
    }
}
