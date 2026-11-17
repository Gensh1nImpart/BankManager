package com.bankmanager.ui.panel;

import com.bankmanager.domain.Employee;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.Refreshable;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;
import com.bankmanager.ui.dialog.EmployeeDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** 职员管理页签：表格 + 新增/编辑/删除（按钮与右键菜单）。 */
public final class EmployeePanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS = {"编号", "姓名", "性别", "年龄", "电话", "地址", "职位"};

    private final ServiceBundle services;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JButton addButton = UiUtils.primaryButton("新增职员");
    private final JButton editButton = UiUtils.button("编辑");
    private final JButton deleteButton = UiUtils.button("删除");
    private final JButton refreshButton = UiUtils.button("刷新");

    private List<Employee> employees = new ArrayList<>();

    public EmployeePanel(ServiceBundle services) {
        super(new BorderLayout(0, UiUtils.GAP));
        this.services = services;
        setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        buildUi();
        refresh();
    }

    private void buildUi() {
        addButton.addActionListener(e -> openDialog(null));
        editButton.addActionListener(e -> openDialog(selectedEmployee()));
        deleteButton.addActionListener(e -> deleteSelected());
        refreshButton.addActionListener(e -> refresh());
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        JPanel toolbar = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 0));
        left.add(UiUtils.title("职员管理"));
        left.add(UiUtils.hint("双击行或右键菜单可编辑"));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        right.add(addButton);
        right.add(editButton);
        right.add(deleteButton);
        right.add(refreshButton);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        UiUtils.styleTable(table, 90, 120, 70, 70, 130, 240, 110);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selectedEmployee() != null) {
                    openDialog(selectedEmployee());
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                javax.swing.UIManager.getColor("Component.borderColor")));

        add(toolbar, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.CENTER);
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
        JMenuItem add = new JMenuItem("新增职员");
        add.addActionListener(e -> openDialog(null));
        JMenuItem edit = new JMenuItem("编辑职员");
        edit.setEnabled(selectedEmployee() != null);
        edit.addActionListener(e -> openDialog(selectedEmployee()));
        JMenuItem delete = new JMenuItem("删除职员");
        delete.setEnabled(selectedEmployee() != null);
        delete.addActionListener(e -> deleteSelected());
        menu.add(add);
        menu.add(edit);
        menu.add(delete);
        menu.show(table, event.getX(), event.getY());
    }

    @Override
    public void refresh() {
        UiUtils.checkEdt();
        Async.load(this, refreshButton, services.employee()::employees, this::render);
    }

    private void render(List<Employee> data) {
        String selectedId = selectedEmployee() == null ? null : selectedEmployee().id();
        employees = data == null ? new ArrayList<>() : new ArrayList<>(data);
        model.setRowCount(0);
        for (Employee employee : employees) {
            model.addRow(employee.toRow());
        }
        if (selectedId != null) {
            for (int row = 0; row < employees.size(); row++) {
                if (selectedId.equals(employees.get(row).id())) {
                    table.setRowSelectionInterval(row, row);
                    break;
                }
            }
        }
    }

    private Employee selectedEmployee() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        // 视图行与模型行在未启用排序时一致，但仍显式换算，避免以后加排序时出错
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < employees.size() ? employees.get(modelRow) : null;
    }

    private void openDialog(Employee employee) {
        EmployeeDialog.open(SwingUtilities.getWindowAncestor(this), services, employee, this::refresh);
    }

    private void deleteSelected() {
        Employee employee = selectedEmployee();
        if (employee == null) {
            return;
        }
        if (!UiUtils.confirm(this, "确定删除职员 " + employee.name() + "（" + employee.id() + "）吗？")) {
            return;
        }
        Async.runVoid(this, deleteButton, "删除中…",
                () -> services.employee().delete(employee.id()),
                () -> {
                    refresh();
                    UiUtils.showInfo(this, "职员已删除");
                });
    }
}
