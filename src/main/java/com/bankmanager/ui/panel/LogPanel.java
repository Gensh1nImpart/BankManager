package com.bankmanager.ui.panel;

import com.bankmanager.domain.OperationLog;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.Refreshable;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 操作日志页签：最近 N 条（默认 200）日志，前端按 20 条一页翻页。 */
public final class LogPanel extends JPanel implements Refreshable {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] COLUMNS = {"时间", "操作员", "操作", "详情"};
    private static final int PAGE_SIZE = 20;

    private final ServiceBundle services;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<Integer> limitBox = new JComboBox<>(new Integer[] {50, 100, 200});
    private final JButton refreshButton = UiUtils.primaryButton("刷新");
    private final JButton previousButton = UiUtils.button("上一页");
    private final JButton nextButton = UiUtils.button("下一页");
    private final JLabel pageLabel = UiUtils.hint("第 1 / 1 页");

    private List<OperationLog> logs = new ArrayList<>();
    private int page;

    public LogPanel(ServiceBundle services) {
        super(new BorderLayout(0, UiUtils.GAP));
        this.services = services;
        setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        buildUi();
        refresh();
    }

    private void buildUi() {
        limitBox.setSelectedItem(200);
        refreshButton.addActionListener(e -> refresh());
        previousButton.addActionListener(e -> turnPage(-1));
        nextButton.addActionListener(e -> turnPage(1));

        JPanel toolbar = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 0));
        left.add(UiUtils.title("操作日志"));
        left.add(UiUtils.hint("按时间倒序，最多返回 200 条"));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiUtils.GAP, 0));
        right.add(new JLabel("条数"));
        right.add(limitBox);
        right.add(previousButton);
        right.add(pageLabel);
        right.add(nextButton);
        right.add(refreshButton);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        UiUtils.styleTable(table, 170, 110, 110, 620);
        add(toolbar, BorderLayout.PAGE_START);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        UiUtils.checkEdt();
        int limit = (Integer) limitBox.getSelectedItem();
        Async.load(this, refreshButton, () -> services.logDao().recent(limit), data -> {
            logs = data == null ? new ArrayList<>() : new ArrayList<>(data);
            page = 0;
            renderPage();
        });
    }

    private void turnPage(int delta) {
        int pages = pageCount();
        int target = page + delta;
        if (target < 0 || target >= pages) {
            return;
        }
        page = target;
        renderPage();
    }

    private void renderPage() {
        model.setRowCount(0);
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, logs.size());
        for (int i = from; i < to; i++) {
            OperationLog log = logs.get(i);
            model.addRow(new Object[] {
                    log.createdAt() == null ? "" : TIME_FORMAT.format(log.createdAt()),
                    log.username(),
                    log.action(),
                    log.detail()
            });
        }
        int pages = pageCount();
        pageLabel.setText("第 " + (page + 1) + " / " + pages + " 页（共 " + logs.size() + " 条）");
        previousButton.setEnabled(page > 0);
        nextButton.setEnabled(page + 1 < pages);
    }

    private int pageCount() {
        return Math.max(1, (logs.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }
}
