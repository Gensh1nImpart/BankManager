package com.bankmanager.ui.dialog;

import com.bankmanager.domain.Employee;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

/** 职员新增/编辑对话框：只收集输入，校验与落库交给 {@code EmployeeService}。 */
public final class EmployeeDialog extends JDialog {

    private final ServiceBundle services;
    private final Employee existing;
    private final Runnable onSaved;

    private final JTextField idField = UiUtils.field(12);
    private final JTextField nameField = UiUtils.field(12);
    private final JComboBox<String> sexBox = new JComboBox<>(new String[] {"男", "女"});
    private final JTextField ageField = UiUtils.field(6);
    private final JTextField teleField = UiUtils.field(12);
    private final JTextField addressField = UiUtils.field(18);
    private final JTextField positionField = UiUtils.field(12);
    private final JLabel errorLabel = UiUtils.hint(" ");
    private final JButton saveButton = UiUtils.primaryButton("保存");

    private EmployeeDialog(Window owner, ServiceBundle services, Employee existing, Runnable onSaved) {
        super(owner, existing == null ? "新增职员" : "编辑职员", ModalityType.APPLICATION_MODAL);
        this.services = services;
        this.existing = existing;
        this.onSaved = onSaved;
        buildUi();
        fill();
    }

    /** 打开对话框；关闭后由调用方决定是否刷新表格。 */
    public static void open(Window owner, ServiceBundle services, Employee existing, Runnable onSaved) {
        UiUtils.checkEdt();
        new EmployeeDialog(owner, services, existing, onSaved).setVisible(true);
    }

    private void buildUi() {
        JPanel form = UiUtils.form();
        int row = 0;
        UiUtils.addRow(form, row++, "职员编号", idField);
        UiUtils.addRow(form, row++, "姓名", nameField);
        UiUtils.addRow(form, row++, "性别", sexBox);
        UiUtils.addRow(form, row++, "年龄", ageField);
        UiUtils.addRow(form, row++, "联系电话", teleField);
        UiUtils.addRow(form, row++, "地址", addressField);
        UiUtils.addRow(form, row++, "职位", positionField);

        JPanel content = new JPanel(new BorderLayout(0, UiUtils.GAP));
        content.setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        content.add(form, BorderLayout.CENTER);
        content.add(errorLabel, BorderLayout.SOUTH);

        JButton cancelButton = UiUtils.button("取消");
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> submit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        buttons.add(cancelButton);
        buttons.add(saveButton);
        content.add(buttons, BorderLayout.PAGE_END);

        setContentPane(content);
        getRootPane().setDefaultButton(saveButton);
        getRootPane().registerKeyboardAction(e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        setMinimumSize(new Dimension(420, 380));
        pack();
        UiUtils.center(this);
    }

    private void fill() {
        if (existing == null) {
            idField.setText("（留空自动生成）");
            idField.setEnabled(false);
            sexBox.setSelectedItem("男");
            return;
        }
        idField.setText(existing.id());
        idField.setEnabled(false);
        nameField.setText(existing.name());
        if (existing.sex() != null && !existing.sex().isBlank()) {
            sexBox.setSelectedItem(existing.sex());
        }
        ageField.setText(existing.age() == null ? "" : String.valueOf(existing.age()));
        teleField.setText(existing.tele());
        addressField.setText(existing.address());
        positionField.setText(existing.position());
    }

    private void submit() {
        errorLabel.setText(" ");
        Integer age = null;
        String ageText = ageField.getText().trim();
        if (!ageText.isEmpty()) {
            try {
                age = Integer.valueOf(ageText);
            } catch (NumberFormatException e) {
                errorLabel.setText("年龄必须是数字");
                return;
            }
        }
        String id = existing == null ? "" : existing.id();
        Employee employee = new Employee(id, nameField.getText(), (String) sexBox.getSelectedItem(),
                age, teleField.getText(), addressField.getText(), positionField.getText());
        Async.run(this, saveButton, "保存中…", () -> {
            if (existing == null) {
                services.employee().create(employee);
            } else {
                services.employee().update(employee);
            }
            return null;
        }, ignored -> {
            dispose();
            onSaved.run();
        });
    }
}
