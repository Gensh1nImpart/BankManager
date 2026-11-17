package com.bankmanager.service.impl;

import com.bankmanager.dao.CustomerDao;
import com.bankmanager.dao.LogDao;
import com.bankmanager.db.Database;
import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;
import com.bankmanager.service.AccountService;
import com.bankmanager.service.ServiceException;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 账户与资金业务实现。
 *
 * <p>金额一律使用 {@link BigDecimal}，统一保留两位小数；所有写操作都在
 * {@link Database#inTransaction} 中执行，余额变动与 {@code operation_log} 同事务提交，
 * 任一环节失败整体回滚（数据源需支持事务内连接复用，见 {@link TransactionAwareDataSource}）。
 *
 * <p>余额与贷款只能通过业务方法变动，{@link #updateCustomer(Customer)} 会忽略传入的
 * 余额/贷款，避免界面直接篡改资金数据。
 */
public final class DefaultAccountService implements AccountService {

    /** 单笔金额上限：1 亿元。 */
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("100000000");
    /** 卡号格式：16 位数字。 */
    public static final int CARD_LENGTH = 16;
    /** 客户密码长度下限。 */
    public static final int PASSWORD_MIN_LENGTH = 6;
    /** 利息试算的最大月数（50 年）。 */
    public static final int MAX_MONTHS = 600;
    /** 账户余额上限，避免 DECIMAL(16,2) 溢出。 */
    public static final BigDecimal MAX_BALANCE = new BigDecimal("99999999999999.99");

    private static final Pattern CARD_PATTERN = Pattern.compile("\\d{" + CARD_LENGTH + "}");
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    private static final int MONEY_SCALE = 2;
    private static final String DEFAULT_OPERATOR = "柜员";
    private static final int NAME_MAX_LENGTH = 50;
    private static final int TELE_MAX_LENGTH = 20;

    private final CustomerDao customerDao;
    private final LogDao logDao;
    private final Database database;
    private final Supplier<String> operator;

    public DefaultAccountService(CustomerDao customerDao, LogDao logDao, Database database) {
        this(customerDao, logDao, database, () -> DEFAULT_OPERATOR);
    }

    /**
     * @param operator 当前操作员（用于写操作日志），由界面传入登录用户名
     */
    public DefaultAccountService(CustomerDao customerDao, LogDao logDao, Database database,
                                 Supplier<String> operator) {
        this.customerDao = Objects.requireNonNull(customerDao, "customerDao");
        this.logDao = Objects.requireNonNull(logDao, "logDao");
        this.database = Objects.requireNonNull(database, "database");
        this.operator = Objects.requireNonNull(operator, "operator");
    }

    @Override
    public List<Customer> customers() {
        try {
            return customerDao.findAll();
        } catch (RuntimeException e) {
            throw new ServiceException("查询客户列表失败：" + describe(e), e);
        }
    }

    @Override
    public Customer customer(String card) {
        return requireCustomer(card);
    }

    @Override
    public void createCustomer(Customer customer, String plainPassword) {
        Objects.requireNonNull(customer, "customer");
        String name = requireName(customer.name());
        requirePassword(plainPassword);
        String card = customer.card() == null || customer.card().isBlank()
                ? nextCardNumber()
                : requireCard(customer.card(), "卡号");
        AccountType type = customer.type() == null ? AccountType.NORMAL : customer.type();
        BigDecimal balance = customer.balance() == null
                ? BigDecimal.ZERO
                : requireNonNegative("初始余额", customer.balance());
        BigDecimal loan = customer.loan() == null
                ? BigDecimal.ZERO
                : requireNonNegative("贷款余额", customer.loan());

        try {
            if (customerDao.findByCard(card).isPresent()) {
                throw new ServiceException("卡号已存在：" + card);
            }
            String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            customerDao.insert(new Customer(card, name, hash, normalizeTele(customer.tele()), type, balance, loan));
            logDao.append(operator.get(), "开户",
                    "卡号 " + card + "，姓名 " + name + "，" + type.label() + "，初始余额 " + money(balance));
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("开户失败：" + describe(e), e);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        Objects.requireNonNull(customer, "customer");
        String card = requireCard(customer.card(), "卡号");
        Customer existing = requireCustomer(card);
        String name = requireName(customer.name());
        AccountType type = customer.type() == null ? existing.type() : customer.type();
        String hash = existing.passwordHash();
        String supplied = customer.passwordHash();
        if (supplied != null && !supplied.isBlank()) {
            requirePassword(supplied);
            hash = BCRYPT_PATTERN.matcher(supplied).matches()
                    ? supplied
                    : BCrypt.hashpw(supplied, BCrypt.gensalt());
        }

        Customer updated = new Customer(card, name, hash, normalizeTele(customer.tele()), type,
                existing.balance(), existing.loan());
        try {
            customerDao.update(updated);
            logDao.append(operator.get(), "修改客户", "卡号 " + card + "，姓名 " + name + "，" + type.label());
        } catch (RuntimeException e) {
            throw new ServiceException("修改客户失败：" + describe(e), e);
        }
    }

    @Override
    public void deleteCustomer(String card) {
        String validCard = requireCard(card, "卡号");
        Customer existing = requireCustomer(validCard);
        try {
            customerDao.deleteByCard(validCard);
            logDao.append(operator.get(), "删除客户", "卡号 " + validCard + "，姓名 " + existing.name());
        } catch (RuntimeException e) {
            throw new ServiceException("删除客户失败：" + describe(e), e);
        }
    }

    @Override
    public void deposit(String card, BigDecimal amount) {
        BigDecimal money = requireAmount(amount);
        Customer customer = requireCustomer(card);
        BigDecimal balance = requireBalanceCapacity(customer.balance().add(money));
        runAtomically("存款", () -> {
            customerDao.updateBalance(customer.card(), balance);
            logDao.append(operator.get(), "存款", detail(customer, "存入", money, "余额", balance));
        });
    }

    @Override
    public void withdraw(String card, BigDecimal amount) {
        BigDecimal money = requireAmount(amount);
        Customer customer = requireCustomer(card);
        if (customer.balance().compareTo(money) < 0) {
            throw new ServiceException("余额不足，当前余额 " + money(customer.balance()));
        }
        BigDecimal balance = customer.balance().subtract(money);
        runAtomically("取款", () -> {
            customerDao.updateBalance(customer.card(), balance);
            logDao.append(operator.get(), "取款", detail(customer, "支取", money, "余额", balance));
        });
    }

    @Override
    public void takeLoan(String card, BigDecimal amount) {
        BigDecimal money = requireAmount(amount);
        Customer customer = requireCustomer(card);
        BigDecimal loan = customer.loan().add(money);
        if (loan.compareTo(MAX_AMOUNT) > 0) {
            throw new ServiceException("贷款总额不能超过 " + money(MAX_AMOUNT)
                    + "，当前贷款余额 " + money(customer.loan()));
        }
        BigDecimal balance = requireBalanceCapacity(customer.balance().add(money));
        runAtomically("贷款", () -> {
            customerDao.updateLoan(customer.card(), loan);
            customerDao.updateBalance(customer.card(), balance);
            logDao.append(operator.get(), "贷款", detail(customer, "放款", money, "余额", balance)
                    + "，贷款余额 " + money(loan));
        });
    }

    @Override
    public void repayLoan(String card, BigDecimal amount) {
        BigDecimal money = requireAmount(amount);
        Customer customer = requireCustomer(card);
        if (customer.loan().signum() == 0) {
            throw new ServiceException("当前没有未结清的贷款");
        }
        if (money.compareTo(customer.loan()) > 0) {
            throw new ServiceException("还款金额不能超过贷款余额 " + money(customer.loan()));
        }
        if (customer.balance().compareTo(money) < 0) {
            throw new ServiceException("余额不足，无法还款，当前余额 " + money(customer.balance()));
        }
        BigDecimal loan = customer.loan().subtract(money);
        BigDecimal balance = customer.balance().subtract(money);
        runAtomically("还款", () -> {
            customerDao.updateLoan(customer.card(), loan);
            customerDao.updateBalance(customer.card(), balance);
            logDao.append(operator.get(), "还款", detail(customer, "归还", money, "余额", balance)
                    + "，贷款余额 " + money(loan));
        });
    }

    @Override
    public void transfer(String fromCard, String toCard, BigDecimal amount) {
        String from = requireCard(fromCard, "转出卡号");
        String to = requireCard(toCard, "转入卡号");
        if (from.equals(to)) {
            throw new ServiceException("不能向自己转账");
        }
        BigDecimal money = requireAmount(amount);
        Customer payer = requireCustomer(from);
        Customer payee = requireCustomer(to);
        if (payer.balance().compareTo(money) < 0) {
            throw new ServiceException("余额不足，当前余额 " + money(payer.balance()));
        }
        BigDecimal payerBalance = payer.balance().subtract(money);
        BigDecimal payeeBalance = requireBalanceCapacity(payee.balance().add(money));
        runAtomically("转账", () -> {
            customerDao.updateBalance(payer.card(), payerBalance);
            customerDao.updateBalance(payee.card(), payeeBalance);
            logDao.append(operator.get(), "转账", "卡号 " + payer.card() + " → " + payee.card()
                    + "，转出 " + money(money) + "，转出方余额 " + money(payerBalance)
                    + "，转入方余额 " + money(payeeBalance));
        });
    }

    @Override
    public BigDecimal interest(String card, BigDecimal monthlyRate, int months) {
        Customer customer = requireCustomer(card);
        if (monthlyRate == null) {
            throw new ServiceException("月利率不能为空");
        }
        if (monthlyRate.signum() < 0) {
            throw new ServiceException("月利率不能为负数");
        }
        if (monthlyRate.compareTo(BigDecimal.ONE) > 0) {
            throw new ServiceException("月利率不能超过 100%");
        }
        if (months < 1 || months > MAX_MONTHS) {
            throw new ServiceException("月数必须在 1 到 " + MAX_MONTHS + " 之间");
        }
        return customer.balance()
                .multiply(monthlyRate)
                .multiply(BigDecimal.valueOf(months))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /** 事务内执行写操作，失败统一转成面向用户的中文提示。 */
    private void runAtomically(String action, Runnable body) {
        try {
            database.inTransaction(connection -> {
                body.run();
                return null;
            });
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException(action + "失败：" + describe(e), e);
        }
    }

    private String nextCardNumber() {
        try {
            return customerDao.nextCardNumber();
        } catch (RuntimeException e) {
            throw new ServiceException("生成卡号失败：" + describe(e), e);
        }
    }

    private Customer requireCustomer(String card) {
        String validCard = requireCard(card, "卡号");
        try {
            return customerDao.findByCard(validCard)
                    .orElseThrow(() -> new ServiceException("卡号不存在：" + validCard));
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("查询账户失败：" + describe(e), e);
        }
    }

    /** 金额校验：必填、大于 0、最多两位小数、不超过 1 亿元。 */
    private static BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ServiceException("金额不能为空");
        }
        if (amount.signum() <= 0) {
            throw new ServiceException("金额必须大于 0");
        }
        if (amount.stripTrailingZeros().scale() > MONEY_SCALE) {
            throw new ServiceException("金额最多保留两位小数");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ServiceException("单笔金额不能超过 " + money(MAX_AMOUNT));
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal requireNonNegative(String role, BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new ServiceException(role + "不能为负数");
        }
        if (amount.stripTrailingZeros().scale() > MONEY_SCALE) {
            throw new ServiceException(role + "最多保留两位小数");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ServiceException(role + "不能超过 " + money(MAX_AMOUNT));
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal requireBalanceCapacity(BigDecimal balance) {
        if (balance.compareTo(MAX_BALANCE) > 0) {
            throw new ServiceException("账户余额超出系统上限");
        }
        return balance.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static String requireCard(String card, String role) {
        String value = card == null ? "" : card.trim();
        if (!CARD_PATTERN.matcher(value).matches()) {
            throw new ServiceException(role + "必须是 " + CARD_LENGTH + " 位数字");
        }
        return value;
    }

    private static String requireName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) {
            throw new ServiceException("客户姓名不能为空");
        }
        if (value.length() > NAME_MAX_LENGTH) {
            throw new ServiceException("客户姓名不能超过 " + NAME_MAX_LENGTH + " 个字符");
        }
        return value;
    }

    private static void requirePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new ServiceException("密码至少 " + PASSWORD_MIN_LENGTH + " 位");
        }
    }

    private static String normalizeTele(String tele) {
        String value = tele == null ? "" : tele.trim();
        if (value.length() > TELE_MAX_LENGTH) {
            throw new ServiceException("联系电话不能超过 " + TELE_MAX_LENGTH + " 个字符");
        }
        return value;
    }

    private static String detail(Customer customer, String verb, BigDecimal amount,
                                 String balanceLabel, BigDecimal balance) {
        return "卡号 " + customer.card() + "，姓名 " + customer.name() + "，" + verb + " "
                + money(amount) + "，" + balanceLabel + " " + money(balance);
    }

    /** 面向用户的金额文本，例如 1,234.50 元。 */
    static String money(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        return format.format(amount) + " 元";
    }

    private static String describe(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
