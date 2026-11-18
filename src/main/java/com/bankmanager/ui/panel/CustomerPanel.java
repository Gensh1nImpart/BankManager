package com.bankmanager.ui.panel;

import com.bankmanager.domain.AccountType;
import com.bankmanager.domain.Customer;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.Refreshable;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;
import com.bankmanager.ui.dialog.CustomerDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 客户管理页签：开户、编辑、删除与 VIP 标识，余额列只读。 */
public final class CustomerPanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS = {"卡号", "姓名", "等级", "电话", "余额", "贷款"};
    private static final Color VIP_COLOR = new Color(0xD9, 0x7A, 0x0B);

    private final ServiceBundle services;
    private final Consumer<String> openBusiness;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JButton addButton = UiUtils.primaryButton("开户");
    private final JButton editButton = UiUtils.button("编辑");
    private final JButton deleteButton = UiUtils.button("删除");
    private final JButton businessButton = UiUtils.button("办理业务");
    private final JButton refreshButton = UiUtils.button("刷新");

    private List<Customer> customers = new ArrayList<>();

    public CustomerPanel(ServiceBundle services, Consumer<String> openBusiness) {
        super(new BorderLayout(0, UiUtils.GAP));
        this.services = services;
        this.openBusiness = openBusiness;
        setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        buildUi();
        refresh();
    }

    private void buildUi() {
        addButton.addActionListener(e -> openDialog(null));
        editButton.addActionListener(e -> openDialog(selectedCustomer()));
        deleteButton.addActionListener(e -> deleteSelected());
        businessButton.addActionListener(e -> openBusinessOfSelected());
        refreshButton.addActionListener(e -> refresh());
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        businessButton.setEnabled(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 0));
        left.add(UiUtils.title("客户管理"));
        left.add(UiUtils.hint("余额只读，资金变动请到「业务办理」"));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        right.add(addButton);
        right.add(editButton);
        right.add(deleteButton);
        right.add(businessButton);
        right.add(refreshButton);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        UiUtils.styleTable(table, 170, 110, 90, 130, 130, 130);
        table.getColumnModel().getColumn(2).setCellRenderer(new VipRenderer());
        UiUtils.moneyColumn(table, 4, 5);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
            businessButton.setEnabled(selected);
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selectedCustomer() != null) {
                    openDialog(selectedCustomer());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
        });

        add(toolbar, BorderLayout.PAGE_START);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void showPopup(MouseEvent event) {
        if (!event.isPopupTrigger()) {
            return;
        }
        int row = table.rowAtPoint(event.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = new JMenuItem("开户…");
        add.addActionListener(e -> openDialog(null));
        JMenuItem edit = new JMenuItem("编辑客户…");
        edit.setEnabled(selectedCustomer() != null);
        edit.addActionListener(e -> openDialog(selectedCustomer()));
        JMenuItem business = new JMenuItem("办理业务…");
        business.setEnabled(selectedCustomer() != null);
        business.addActionListener(e -> openBusinessOfSelected());
        JMenuItem delete = new JMenuItem("删除客户");
        delete.setEnabled(selectedCustomer() != null);
        delete.addActionListener(e -> deleteSelected());
        menu.add(add);
        menu.add(edit);
        menu.add(business);
        menu.addSeparator();
        menu.add(delete);
        menu.show(table, event.getX(), event.getY());
    }

    @Override
    public void refresh() {
        UiUtils.checkEdt();
        Async.load(this, refreshButton, services.account()::customers, this::render);
    }

    private void render(List<Customer> data) {
        Customer selected = selectedCustomer();
        String selectedCard = selected == null ? null : selected.card();
        customers = data == null ? new ArrayList<>() : new ArrayList<>(data);
        model.setRowCount(0);
        for (Customer customer : customers) {
            model.addRow(new Object[] {
                    customer.card(),
                    customer.name(),
                    customer.typeLabel(),
                    customer.tele(),
                    customer.balance(),
                    customer.loan()
            });
        }
        if (selectedCard != null) {
            for (int row = 0; row < customers.size(); row++) {
                if (selectedCard.equals(customers.get(row).card())) {
                    table.setRowSelectionInterval(row, row);
                    break;
                }
            }
        }
    }

    private Customer selectedCustomer() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= customers.size()) {
            return null;
        }
        Customer customer = customers.get(modelRow);
        // 防御：表格行与数据列表必须指向同一张卡
        if (!customer.card().equals(model.getValueAt(modelRow, 0))) {
            return null;
        }
        return customer;
    }

    private void openDialog(Customer customer) {
        CustomerDialog.open(SwingUtilities.getWindowAncestor(this), services, customer, message -> {
            refresh();
            UiUtils.showInfo(this, message);
        });
    }

    private void openBusinessOfSelected() {
        Customer customer = selectedCustomer();
        if (customer != null && openBusiness != null) {
            openBusiness.accept(customer.card());
        }
    }

    private void deleteSelected() {
        Customer customer = selectedCustomer();
        if (customer == null) {
            return;
        }
        String message = "确定删除客户 " + customer.name() + "（" + customer.card() + "）吗？\n"
                + "余额 " + UiUtils.money(customer.balance())
                + "，贷款 " + UiUtils.money(customer.loan());
        if (!UiUtils.confirm(this, message)) {
            return;
        }
        Async.runVoid(this, deleteButton, "删除中…",
                () -> services.account().deleteCustomer(customer.card()),
                () -> {
                    refresh();
                    UiUtils.showInfo(this, "客户已删除");
                });
    }

    /** 等级列：VIP 用醒目颜色加粗展示。 */
    private static final class VipRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean selected, boolean focus,
                                                                int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            boolean vip = value != null && AccountType.VIP.label().equals(value.toString());
            if (!selected) {
                setForeground(vip ? VIP_COLOR : table.getForeground());
            }
            setFont(table.getFont().deriveFont(vip ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
            return this;
        }
    }

}
