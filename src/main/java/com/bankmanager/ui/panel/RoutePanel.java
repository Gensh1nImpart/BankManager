package com.bankmanager.ui.panel;

import com.bankmanager.service.RouteService;
import com.bankmanager.ui.Async;
import com.bankmanager.ui.Refreshable;
import com.bankmanager.ui.ServiceBundle;
import com.bankmanager.ui.UiUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.List;

/** 网点导航页签：选择起点/终点，展示 Dijkstra 最短路径与总距离。 */
public final class RoutePanel extends JPanel implements Refreshable {

    private final ServiceBundle services;

    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();
    private final JButton swapButton = UiUtils.button("交换");
    private final JButton queryButton = UiUtils.primaryButton("查询最短路径");
    private final JLabel nodesHint = UiUtils.hint("正在加载网点…");
    private final JLabel pathLabel = new JLabel("请选择起点与终点");
    private final JLabel distanceLabel = UiUtils.hint(" ");

    private boolean loaded;

    public RoutePanel(ServiceBundle services) {
        super(new BorderLayout(0, UiUtils.GAP));
        this.services = services;
        setBorder(BorderFactory.createEmptyBorder(UiUtils.PADDING, UiUtils.PADDING,
                UiUtils.PADDING, UiUtils.PADDING));
        buildUi();
        refresh();
    }

    private void buildUi() {
        queryButton.addActionListener(e -> query());
        swapButton.addActionListener(e -> {
            Object from = fromBox.getSelectedItem();
            fromBox.setSelectedItem(toBox.getSelectedItem());
            toBox.setSelectedItem(from);
        });

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, UiUtils.GAP, 2));
        form.add(new JLabel("起点"));
        fromBox.setPreferredSize(new java.awt.Dimension(140, 30));
        form.add(fromBox);
        form.add(swapButton);
        form.add(new JLabel("终点"));
        toBox.setPreferredSize(new java.awt.Dimension(140, 30));
        form.add(toBox);
        form.add(queryButton);

        JPanel header = new JPanel(new BorderLayout());
        header.add(UiUtils.title("网点导航"), BorderLayout.NORTH);
        header.add(form, BorderLayout.CENTER);
        header.add(nodesHint, BorderLayout.SOUTH);

        pathLabel.setFont(pathLabel.getFont().deriveFont(Font.BOLD, 18f));
        JPanel result = new JPanel(new BorderLayout(0, UiUtils.GAP));
        result.add(pathLabel, BorderLayout.NORTH);
        result.add(distanceLabel, BorderLayout.CENTER);
        result.add(UiUtils.hint("路径由无向带权图上的 Dijkstra 算法计算，距离单位为公里"), BorderLayout.SOUTH);

        add(header, BorderLayout.PAGE_START);
        add(UiUtils.section("最短路径", result), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        UiUtils.checkEdt();
        Async.load(this, queryButton, services.route()::nodeNames, this::fillNodes);
    }

    private void fillNodes(List<String> names) {
        List<String> previousFrom = selected(fromBox);
        List<String> previousTo = selected(toBox);
        fromBox.removeAllItems();
        toBox.removeAllItems();
        for (String name : names) {
            fromBox.addItem(name);
            toBox.addItem(name);
        }
        restore(fromBox, previousFrom);
        restore(toBox, previousTo);
        if (toBox.getItemCount() > 1 && toBox.getSelectedIndex() == 0) {
            toBox.setSelectedIndex(1);
        }
        nodesHint.setText("共 " + names.size() + " 个网点，选择起终点后查询");
        loaded = true;
    }

    private static List<String> selected(JComboBox<String> box) {
        Object value = box.getSelectedItem();
        return value == null ? List.of() : List.of(value.toString());
    }

    private static void restore(JComboBox<String> box, List<String> previous) {
        if (previous.isEmpty()) {
            return;
        }
        String value = previous.get(0);
        for (int i = 0; i < box.getItemCount(); i++) {
            if (value.equals(box.getItemAt(i))) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void query() {
        String from = (String) fromBox.getSelectedItem();
        String to = (String) toBox.getSelectedItem();
        if (!loaded || from == null || to == null) {
            UiUtils.showInfo(this, "网点数据尚未加载完成");
            return;
        }
        if (from.equals(to)) {
            UiUtils.showInfo(this, "起点与终点不能相同");
            return;
        }
        Async.load(this, queryButton, () -> services.route().shortestPath(from, to), this::showRoute);
    }

    private void showRoute(RouteService.Route route) {
        pathLabel.setText("<html><body style='width:720px'>" + route.pathText() + "</body></html>");
        BigDecimal distance = route.distance();
        distanceLabel.setText("总距离：" + distance.stripTrailingZeros().toPlainString() + " 公里，共 "
                + route.path().size() + " 个站点");
    }
}
