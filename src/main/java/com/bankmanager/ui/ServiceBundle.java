package com.bankmanager.ui;

import com.bankmanager.dao.CustomerDao;
import com.bankmanager.dao.LogDao;
import com.bankmanager.service.AccountService;
import com.bankmanager.service.AuthService;
import com.bankmanager.service.EmployeeService;
import com.bankmanager.service.QueueService;
import com.bankmanager.service.RouteService;

/**
 * 界面依赖的服务集合，由 {@code App} 在启动时装配一次。
 *
 * <p>界面只依赖服务接口与两个只读 DAO 接口（客户卡号生成、操作日志查询），
 * 不直接持有 {@code Database}，因此不会在 EDT 上误用连接。
 */
public record ServiceBundle(
        AuthService auth,
        AccountService account,
        EmployeeService employee,
        QueueService queue,
        RouteService route,
        CustomerDao customerDao,
        LogDao logDao) {
}
