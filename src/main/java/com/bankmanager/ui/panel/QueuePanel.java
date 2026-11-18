package com.bankmanager.ui.panel;

import com.bankmanager.domain.QueueTicket;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.Refreshable;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 排队管理页签：取号、VIP/普通两列等待队列、叫号与完成，并显示当前叫号窗口。 */
public final class QueuePanel extends JPanel implements Refreshable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] COLUMNS = {"号码", "姓名", "取号时间"};

    private final ServiceBundle services;

    private final JTextField nameField = UiUtils.field(12);
    private final JCheckBox vipBox = new JCheckBox("VIP 客户");
    private final JSpinner counterSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JButton takeButton = UiUtils.primaryButton("取号");
    private final JButton callVipButton = UiUtils.button("叫 VIP 号");
    private final JButton callNormalButton = UiUtils.button("叫普通号");
    private final JButton finishButton = UiUtils.button("完成业务");
    private final JButton refreshButton = UiUtils.button("刷新");

    private final WaitTable vipTable = new WaitTable("VIP 等待队列");
    private final WaitTable normalTable = new WaitTable("普通等待队列");
    private final JLabel callLabel = new JLabel("暂无叫号");
    private final JLabel callHint = UiUtils.hint("取号后会按先来后到顺序排队");

    public QueuePanel(ServiceBundle services) {
        super(new BorderLayout(0, UiUtils.GAP));
        this.services = services;
        setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        buildUi();
        refresh();
    }

    private void buildUi() {
        takeButton.addActionListener(e -> take());
        refreshButton.addActionListener(e -> refresh());
        callVipButton.addActionListener(e -> call(true));
        callNormalButton.addActionListener(e -> call(false));
        finishButton.addActionListener(e -> finishSelected());
        callLabel.setFont(callLabel.getFont().deriveFont(Font.BOLD, 24f));

        add(buildTop(), BorderLayout.PAGE_START);
        JPanel queues = new JPanel(new GridLayout(1, 2, UiUtils.GAP, 0));
        queues.add(vipTable);
        queues.add(normalTable);
        add(queues, BorderLayout.CENTER);
        add(buildCallArea(), BorderLayout.PAGE_END);
    }

    private JPanel buildTop() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 2));
        form.add(new JLabel("客户姓名"));
        form.add(nameField);
        form.add(vipBox);
        form.add(new JLabel("窗口号"));
        form.add(counterSpinner);
        form.add(takeButton);
        form.add(refreshButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(UiUtils.title("排队管理"), BorderLayout.WEST);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCallArea() {
        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.add(callLabel, BorderLayout.NORTH);
        info.add(callHint, BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        buttons.add(callVipButton);
        buttons.add(callNormalButton);
        buttons.add(finishButton);

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("叫号窗口"),
                BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        panel.add(info, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    @Override
    public void refresh() {
        UiUtils.checkEdt();
        loadQueues(refreshButton);
    }

    private void loadQueues(JButton trigger) {
        Async.load(this, trigger, () -> new Snapshot(
                services.queue().waiting(true),
                services.queue().waiting(false)), snapshot -> {
            vipTable.render(snapshot.vip());
            normalTable.render(snapshot.normal());
        });
    }

    private void take() {
        String name = nameField.getText();
        boolean vip = vipBox.isSelected();
        Async.run(this, takeButton, "取号中…",
                () -> services.queue().take(name, vip),
                ticket -> {
                    nameField.setText("");
                    callHint.setText("取号成功：" + ticket.vipLabel() + " " + ticket.number() + " 号，请留意叫号");
                    loadQueues(null);
                });
    }

    private void call(boolean vip) {
        int counter = (Integer) counterSpinner.getValue();
        Async.run(this, vip ? callVipButton : callNormalButton, "叫号中…",
                () -> services.queue().callNext(vip, counter),
                optional -> {
                    if (optional.isEmpty()) {
                        callLabel.setText("暂无等待客户");
                        callHint.setText(vip ? "VIP 队列为空" : "普通队列为空");
                        return;
                    }
                    QueueTicket ticket = optional.get();
                    callLabel.setText(ticket.vipLabel() + " " + String.format("%03d", ticket.number())
                            + " 号 → " + counter + " 号窗口");
                    callHint.setText("客户 " + ticket.customerName() + "，请到 "
                            + counter + " 号窗口办理业务");
                    loadQueues(null);
                });
    }

    private void finishSelected() {
        WaitTable.Selected selected = vipTable.selected();
        if (selected == null) {
            selected = normalTable.selected();
        }
        if (selected == null) {
            UiUtils.showInfo(this, "请先在等待队列中选中一条票据");
            return;
        }
        QueueTicket ticket = selected.ticket();
        if (!UiUtils.confirm(this, "确认完成 " + ticket.vipLabel() + " " + ticket.number()
                + " 号（" + ticket.customerName() + "）的业务吗？")) {
            return;
        }
        Async.runVoid(this, finishButton, "处理中…",
                () -> services.queue().finish(ticket.id()),
                () -> {
                    callLabel.setText("业务已完成");
                    callHint.setText(ticket.customerName() + " 的业务办理完成");
                    loadQueues(null);
                });
    }

    private record Snapshot(List<QueueTicket> vip, List<QueueTicket> normal) {
    }

    /** 单个等待队列：标题 + 只读表格 + 票据列表（行号与列表索引一一对应）。 */
    private static final class WaitTable extends JPanel {

        private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);
        private List<QueueTicket> tickets = new ArrayList<>();

        WaitTable(String title) {
            super(new BorderLayout(0, 4));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(title),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)));
            UiUtils.styleTable(table, 70, 130, 100);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        void render(List<QueueTicket> data) {
            tickets = data == null ? new ArrayList<>() : new ArrayList<>(data);
            model.setRowCount(0);
            for (QueueTicket ticket : tickets) {
                String time = ticket.createdAt() == null ? "" : TIME_FORMAT.format(ticket.createdAt());
                model.addRow(new Object[] {String.format("%03d", ticket.number()),
                        ticket.customerName(), time});
            }
        }

        Selected selected() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                return null;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= tickets.size()) {
                return null;
            }
            QueueTicket ticket = tickets.get(modelRow);
            // 行内容必须与数据一致，避免刷新后错位
            if (!String.format("%03d", ticket.number()).equals(model.getValueAt(modelRow, 0))) {
                return null;
            }
            return new Selected(ticket);
        }

        private record Selected(QueueTicket ticket) {
        }
    }

    /** 便于测试与调试：当前两个队列的等待数量。 */
    public int waitingCount(boolean vip) {
        return vip ? vipTable.tickets.size() : normalTable.tickets.size();
    }
}
