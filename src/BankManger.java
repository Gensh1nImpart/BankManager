/*
@ BankManager - BankManager.java
@ A JFrame for BankManager
@ Autor : YuRuiH
@ Data : 2022-11-11
 */
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.awt.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class BankManger extends JFrame{
    String now_user;
    static Statement stmt;
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    JPanel[] panel = new JPanel[8];

    BankManger(Statement stmt, String now_user) {
        BankManger.stmt = stmt;
        this.now_user = now_user;
        this.setTitle("银行管理-管理员" + " - " + now_user);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, 580, 500);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.init();
        this.setVisible(true);
    }
    static void Write_Log(Date date, String User, String Action){
        try{
            String sql = "insert into log values ('" + date + "', '" + User + "', '" + Action + "')";
            stmt.executeUpdate(sql);
            System.out.println("写入日志成功");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void init(){
        this.getContentPane().setLayout(new BorderLayout(0, 0));
        this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        for(int i = 0; i <= 7; i++){
            panel[i] = new JPanel();
        }
        tabbedPane.addTab("银行职员管理", null, panel[0], null);
        tabbedPane.addTab("客户账户管理", null, panel[1], null);
        tabbedPane.addTab("存取贷业务管理", null, panel[2], null);
        tabbedPane.addTab("银行排队管理", null, panel[4], null);
        tabbedPane.addTab("银行网点查询", null, panel[5], null);
        tabbedPane.addTab("关于", null, panel[7], null);
        Init_panel_0();
        tabbedPane.setSelectedIndex(0);
        //切换标签页时触发
        tabbedPane.addChangeListener(L->{
            int index = tabbedPane.getSelectedIndex();
            switch (index) {
                case 0 -> this.Init_panel_0();
                case 1 -> this.Init_panel_1();
                case 2 -> this.init_panel2();
                case 3 -> this.init_panel4();
                case 4 -> this.init_panel5();
                case 5 -> this.init_panel6();

            }
        });
    }

    // 关于
    void init_panel6(){
        // 关于界面
        panel[7].setLayout(null);
        JLabel label = new JLabel("数据结构大作业-银行管理系统");
        label.setFont(new Font("宋体", Font.PLAIN, 20));
        label.setBounds(150, 50, 300, 50);
        panel[7].add(label);
        JLabel label_1 = new JLabel("作者：YuRuiH & 游海城");
        label_1.setFont(new Font("宋体", Font.PLAIN, 20));
        label_1.setBounds(150, 100, 300, 50);
        panel[7].add(label_1);
        JLabel label_4 = new JLabel("学号：2021181405 + 34/33");
        label_4.setFont(new Font("宋体", Font.PLAIN, 20));
        label_4.setBounds(150, 150, 300, 50);
        panel[7].add(label_4);
        JLabel label_2 = new JLabel("版本：1.0");
        label_2.setFont(new Font("宋体", Font.PLAIN, 20));
        label_2.setBounds(150, 200, 300, 50);
        panel[7].add(label_2);
        JLabel label_3 = new JLabel("日期：2022-11-29");
        label_3.setFont(new Font("宋体", Font.PLAIN, 20));
        label_3.setBounds(150, 250, 300, 50);
        panel[7].add(label_3);
        JLabel label_5 = new JLabel("技术栈: JavaSwing + MySQL");
        label_5.setFont(new Font("宋体", Font.PLAIN, 20));
        label_5.setBounds(150, 300, 300, 50);
        panel[7].add(label_5);


    }
    //@ 地图
    JLabel label = new JLabel("共有");
    Edge[] edges = new Edge[100];
    JScrollPane scrollpan_map = new JScrollPane();
    JTable table_map = new JTable();
    int cnt = 0;
    void update_map(DefaultTableModel model){
        // 清除表格
        model.setRowCount(0);
        for(int i = 0; i < cnt; i += 2){
            model.addRow(new Object[]{edges[i].getSource(), edges[i].getDestination(), edges[i].getWeight()});
        }
    }
    void init_panel5() {
        String sql = "select * from grap";
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                edges[cnt++] = new Edge(rs.getString(1), rs.getString(2), rs.getDouble(3));
                edges[cnt++] = new Edge(rs.getString(2), rs.getString(1), rs.getDouble(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        panel[5].setLayout(null);
        label.setBounds(10, 10, 100, 20);
        label.setText("共有" + cnt/2 + "条边");
        panel[5].add(label);

        scrollpan_map.setBounds(10, 40, 550, 300);
        panel[5].add(scrollpan_map);

        table_map.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "起点", "终点", "距离"
                }
        ));
        table_map.getColumnModel().getColumn(0).setPreferredWidth(100);
        table_map.getColumnModel().getColumn(1).setPreferredWidth(100);
        table_map.getColumnModel().getColumn(2).setPreferredWidth(100);
        scrollpan_map.setViewportView(table_map);
        DefaultTableModel model = (DefaultTableModel) table_map.getModel();
        update_map(model);
        // 设置头部不可动
        table_map.getTableHeader().setReorderingAllowed(false);
        // 设置单元格不可编辑
        table_map.setDefaultEditor(Object.class, null);
        JButton btn_add = new JButton("添加");
        btn_add.setBounds(30, 350, 120, 30);
        panel[5].add(btn_add);
        JButton btn_delete = new JButton("查询");
        btn_delete.setBounds(400, 350, 120, 30);
        panel[5].add(btn_delete);
        btn_add.addActionListener(L->{
            String source = JOptionPane.showInputDialog("请输入起点");
            String destination = JOptionPane.showInputDialog("请输入终点");
            String weight = JOptionPane.showInputDialog("请输入距离");
            if(source == null || destination == null || weight == null){
                // 警告
                JOptionPane.showMessageDialog(null, "输入不能为空", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sql_add = "insert into grap values ('" + source + "', '" + destination + "', " + weight + ")";
            try {
                stmt.executeUpdate(sql_add);
                JOptionPane.showMessageDialog(null, "添加成功");
                edges[cnt++] = new Edge(source, destination, Double.parseDouble(weight));
                edges[cnt++] = new Edge(destination, source, Double.parseDouble(weight));
                this.init_panel5();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        btn_delete.addActionListener(L->{
            String source = JOptionPane.showInputDialog("请输入起点");
            String destination = JOptionPane.showInputDialog("请输入终点");
            // 判断是否存在
            boolean flag1 = false;
            boolean flag2 = false;
            for(int i = 0; i < cnt; i += 2){
                if(edges[i].getSource().equals(source) || edges[i].getDestination().equals(source)){
                    flag1 = true;
                }
                if(edges[i].getSource().equals(destination) || edges[i].getDestination().equals(destination)){
                    flag2 = true;
                }
            }
            if(!flag1 || !flag2){
                JOptionPane.showMessageDialog(null, "不存在该路径");
                return;
            }
            // 求最短路径
            JOptionPane.showMessageDialog(null, "最短路径为：" + Dijsrta.getPath(edges, cnt, source, destination));
        });
    }
        //@ 银行职员管理
    static String[] colnumName = new String[]{"编号", "姓名", "性别", "年龄", "电话", "地址", "职位"};
    static String[][] rowData = new String[0][];
    static JTable table;
    JScrollPane scrollPane;
    static void Update(){
        try{
            ResultSet rs = stmt.executeQuery("select * from person");
            int row = 0;
            while(rs.next()){
                row++;
            }
            rowData = new String[row][7];
            rs = stmt.executeQuery("select * from person");
            row = 0;
            while(rs.next()){
                rowData[row][0] = rs.getString("ID");
                rowData[row][1] = rs.getString("Name");
//                rowData[row][2] = rs.getString("Sex");
                if(rs.getString("Sex").equals("1"))
                    rowData[row][2] = "男";
                else
                    rowData[row][2] = "女";
                rowData[row][3] = rs.getString("Age");
                rowData[row][4] = rs.getString("tele");
                rowData[row][5] = rs.getString("Address");
                rowData[row][6] = rs.getString("Posi");
                row++;
            }
            try{
                table.setModel(new DefaultTableModel(rowData, colnumName));
            }catch (Exception ignored){
            }
            try{
                table.getColumnModel().getColumn(0).setPreferredWidth(50);
                table.getColumnModel().getColumn(1).setPreferredWidth(50);
                table.getColumnModel().getColumn(2).setPreferredWidth(30);
                table.getColumnModel().getColumn(3).setPreferredWidth(30);
                table.getColumnModel().getColumn(4).setPreferredWidth(80);
                table.getColumnModel().getColumn(5).setPreferredWidth(150);
                table.getColumnModel().getColumn(6).setPreferredWidth(80);
            }catch (Exception ignored){
            }
            System.out.println("共查询到" + row + "条数据");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void Init_panel_0(){
        panel[0].setBorder(new EmptyBorder(5, 5, 5, 5));
        panel[0].setLayout(null);
        Update();
        table = new JTable(rowData, colnumName);
        scrollPane = new JScrollPane(table);
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.setBounds(0, 0, 560, 390);
        // 设置每一列的宽度
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(30);
        table.getColumnModel().getColumn(3).setPreferredWidth(30);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            String value = (String) table.getValueAt(row, col);
            String ID = (String) table.getValueAt(row, 0);
            if(col == 2){
                if(value.equals("男"))
                    value = "1";
                else if(value.equals("女"))
                    value = "0";
                else{
                    JOptionPane.showMessageDialog(null, "性别只能为男或女", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            System.out.println("修改了第" + row + "行，第" + col + "列的数据，修改后的数据为：" + value);
            try{
                stmt.executeUpdate("update person set " + GetSqlCol(col) + " = '" + value + "' where ID = '" + ID + "'");
                JOptionPane.showMessageDialog(null, "修改成功");
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JPopupMenu popupMenu1 = new JPopupMenu();
        JPopupMenu popupMenu2 = new JPopupMenu();
        JMenuItem add1 = new JMenuItem("添加");
        JMenuItem add2 = new JMenuItem("添加");
        JMenuItem delete = new JMenuItem("删除");
        JMenuItem refresh = new JMenuItem("刷新");
        popupMenu1.add(add1);
        popupMenu1.add(delete);
        popupMenu2.add(add2);
//        popupMenu2.add(delete);
        popupMenu2.add(refresh);

        scrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    popupMenu2.show(scrollPane, e.getX(), e.getY());
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    popupMenu1.show(table, e.getX(), e.getY());
                }
            }
        });
        // 添加按钮的监听
        add1.addActionListener(e -> new AddPerson(stmt));
        add2.addActionListener(e -> new AddPerson(stmt));
        refresh.addActionListener(e -> Update());
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            String ID = (String) table.getValueAt(row, 0);
            try{
                stmt.executeUpdate("delete from person where ID = '" + ID + "'");
            }catch (Exception e1){
                e1.printStackTrace();
            }
            Update();
            table.updateUI();
        });
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel[0].add(scrollPane);
        JLabel label = new JLabel("当前时间为：" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("宋体", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("当前时间为：" + new Date()));
        timer.start();
        panel[0].add(label);
    }
    String GetSqlCol(int row){
        String[] zidian = new String[]{"ID","Name","Sex","Age","tele","Address","Posi"};
        return zidian[row];
    }

    // @客户账户管理
    static String[] colnumName_custom = new String[]{"卡号","户名","密码","电话","身份","余额","贷款"};
    static String[][] rowData_custom = new String[0][];
    static JTable table_custom;
    JScrollPane scrollPane_custom;
    String GetSqlCol_custom(int row){
        String[] zidian = new String[]{"Card","Name","Pass","Tele","Posi","Res","loan"};
        return zidian[row];
    }
    static void Update_custom(){
        try{
            ResultSet rs = stmt.executeQuery("select * from custom");
            int row = 0;
            while(rs.next()){
                row++;
            }
            rowData_custom = new String[row][7];
            rs = stmt.executeQuery("select * from custom");
            row = 0;
            while(rs.next()){
                rowData_custom[row][0] = rs.getString("Card");
                rowData_custom[row][1] = rs.getString("Name");
                rowData_custom[row][2] = rs.getString("Pass");
                rowData_custom[row][3] = rs.getString("Tele");
                if(rs.getString("Posi").equals("0")){
                    rowData_custom[row][4] = "普通用户";
                }else{
                    rowData_custom[row][4] = "VIP用户";
                }
                // double转String
                rowData_custom[row][5] = String.valueOf(rs.getDouble("Res"));
                rowData_custom[row][6] = String.valueOf(rs.getDouble("loan"));
                row++;
            }
            try{
                table_custom.setModel(new DefaultTableModel(rowData_custom, colnumName_custom));
            }catch (Exception ignored){
            }
            try{
                table_custom.getColumnModel().getColumn(0).setPreferredWidth(50);
                table_custom.getColumnModel().getColumn(1).setPreferredWidth(50);
                table_custom.getColumnModel().getColumn(2).setPreferredWidth(30);
                table_custom.getColumnModel().getColumn(3).setPreferredWidth(30);
                table_custom.getColumnModel().getColumn(4).setPreferredWidth(80);
                table_custom.getColumnModel().getColumn(5).setPreferredWidth(20);
                table_custom.getColumnModel().getColumn(6).setPreferredWidth(20);
            }catch (Exception ignored){
            }
            System.out.println("客户管理共查询到" + row + "条数据");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void Init_panel_1(){
        panel[1].setBorder(new EmptyBorder(5, 5, 5, 5));
        panel[1].setLayout(null);
        Update_custom();
        table_custom = new JTable(rowData_custom, colnumName_custom);
        scrollPane_custom = new JScrollPane(table_custom);
        table_custom.getTableHeader().setReorderingAllowed(false);
        scrollPane_custom.setBounds(0, 0, 560, 390);
        table_custom.getColumnModel().getColumn(0).setPreferredWidth(50);
        table_custom.getColumnModel().getColumn(1).setPreferredWidth(50);
        table_custom.getColumnModel().getColumn(2).setPreferredWidth(30);
        table_custom.getColumnModel().getColumn(3).setPreferredWidth(30);
        table_custom.getColumnModel().getColumn(4).setPreferredWidth(80);
        table_custom.getColumnModel().getColumn(5).setPreferredWidth(20);
        table_custom.getColumnModel().getColumn(6).setPreferredWidth(20);
        table_custom.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            String value = (String) table_custom.getValueAt(row, col);
            String CARD = (String) table_custom.getValueAt(row, 0);
            if(col == 4){
                if(value.equals("普通用户"))
                    value = "0";
                else if(value.equals("VIP用户"))
                    value = "1";
                else{
                    JOptionPane.showMessageDialog(null, "身份只能为普通用户或VIP用户,修改失败,请及时修改！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if(col == 5 || col == 6){
                JOptionPane.showMessageDialog(null, "客户的钱你能随便动？", "大胆！", JOptionPane.ERROR_MESSAGE);
                Update_custom();
                return;
            }
            try{
                stmt.executeUpdate("update custom set " + GetSqlCol_custom(col) + " = '" + value + "' where Card = '" + CARD + "'");
                JOptionPane.showMessageDialog(null, "修改成功");
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JLabel now_custom = new JLabel("当前客户: ");
        JPopupMenu popupMenu1 = new JPopupMenu();
        JPopupMenu popupMenu2 = new JPopupMenu();
        JMenuItem add1 = new JMenuItem("添加");
        JMenuItem add2 = new JMenuItem("添加");
        JMenu solve = new JMenu("办理业务");
        JMenuItem cunkuan = new JMenuItem("存款");
        JMenuItem qukuan = new JMenuItem("取款");
        JMenuItem daikuan = new JMenuItem("贷款");
        JMenuItem lixi = new JMenuItem("利息计算");
        // 添加到solve的二级菜单
        solve.add(cunkuan);
        solve.add(qukuan);
        solve.add(daikuan);
        solve.add(lixi);
        final int[] now_row = {0};
        cunkuan.addActionListener(L->{
            new CunKuan(stmt, table_custom.getValueAt(now_row[0], 0).toString(), table_custom.getValueAt(now_row[0], 1).toString(), Double.parseDouble(table_custom.getValueAt(now_row[0], 5).toString()));
//            new CunKuan(stmt, table_custom.getValueAt(table_custom.getSelectedRow(), 0).toString(), table_custom.getValueAt(table_custom.getSelectedRow(), 1).toString(), Double.parseDouble(table_custom.getValueAt(table_custom.getSelectedRow(), 5).toString()));
        });
        qukuan.addActionListener(L->{
//            new QuKuan(stmt, table_custom.getValueAt(table_custom.getSelectedRow(), 0).toString(), table_custom.getValueAt(table_custom.getSelectedRow(), 1).toString(), Double.parseDouble(table_custom.getValueAt(table_custom.getSelectedRow(), 5).toString()));
            new QuKuan(stmt, table_custom.getValueAt(now_row[0], 0).toString(), table_custom.getValueAt(now_row[0], 1).toString(), Double.parseDouble(table_custom.getValueAt(now_row[0], 5).toString()));
        });
        daikuan.addActionListener(L->{
            new DaiKuan(stmt, table_custom.getValueAt(now_row[0], 0).toString(), table_custom.getValueAt(now_row[0], 1).toString(), Double.parseDouble(table_custom.getValueAt(now_row[0], 5).toString()));
//            new DaiKuan(stmt, table_custom.getValueAt(table_custom.getSelectedRow(), 0).toString(), table_custom.getValueAt(table_custom.getSelectedRow(), 1).toString(), Double.parseDouble(table_custom.getValueAt(table_custom.getSelectedRow(), 6).toString()));
        });
        lixi.addActionListener(L-> new LiXi());
        JMenuItem delete = new JMenuItem("删除");
        JMenuItem refresh = new JMenuItem("刷新");
        popupMenu1.add(now_custom);
        popupMenu1.add(add1);
        popupMenu1.add(delete);
        popupMenu1.add(solve);
        popupMenu2.add(add2);
        popupMenu2.add(refresh);

        scrollPane_custom.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    popupMenu2.show(scrollPane_custom, e.getX(), e.getY());
                }
            }
        });
        table_custom.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    now_custom.setText("  [+]当前客户: " + rowData_custom[table_custom.rowAtPoint(e.getPoint())][1]);
                    popupMenu1.show(table_custom, e.getX(), e.getY());
                    now_row[0] = table_custom.rowAtPoint(e.getPoint());
                }
            }
        });
        // 添加按钮的监听
        add1.addActionListener(e -> new Addcustom(stmt));
        add2.addActionListener(e -> new Addcustom(stmt));
        refresh.addActionListener(e -> Update_custom());
        delete.addActionListener(e -> {
            int row = table_custom.getSelectedRow();
            String ID = (String) table_custom.getValueAt(row, 0);
            try{
                stmt.executeUpdate("delete from custom where Card = '" + ID + "'");
            }catch (Exception e1){
                e1.printStackTrace();
            }
            Update_custom();
            table_custom.updateUI();
        });
        scrollPane_custom.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane_custom.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JLabel label = new JLabel("当前时间为：" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("宋体", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("当前时间为：" + new Date()));
        timer.start();
        panel[1].add(label);
        panel[1].add(scrollPane_custom);
    }

    String[] colnumName_yewu = {"时间","用户","操作"};
    String[][] rowData_yewu = new String[0][];
    JTable table_yewu;
    JScrollPane scrollPane_yewu;
    void Update_yewu(){
        try{
            ResultSet rs = stmt.executeQuery("select * from log");
            int row = 0;
            while(rs.next()){
                row++;
            }
//            System.out.println("row = " + row);
            rowData_yewu = new String[row][3];
            rs = stmt.executeQuery("select * from log");
            row = 0;
            while(rs.next()){
                rowData_yewu[row][0] = rs.getString("Date");
                rowData_yewu[row][1] = rs.getString("User");
                rowData_yewu[row][2] = rs.getString("Ope");
                row++;
            }
            try{
                table_yewu.setModel(new DefaultTableModel(rowData_yewu, colnumName_yewu));
            }catch (Exception ignored){
            }
            try{
                table_yewu.getColumnModel().getColumn(0).setPreferredWidth(20);
                table_yewu.getColumnModel().getColumn(1).setPreferredWidth(20);
                table_yewu.getColumnModel().getColumn(2).setPreferredWidth(80);
            }catch (Exception ignored){
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void init_panel2(){
        panel[2].setBorder(new EmptyBorder(5, 5, 5, 5));
        panel[2].setLayout(null);
        Update_yewu();
        table_yewu = new JTable(rowData_yewu, colnumName_yewu);
        scrollPane_yewu = new JScrollPane(table_yewu);
        table_yewu.getTableHeader().setReorderingAllowed(false);
        scrollPane_yewu.setBounds(0, 0, 560, 390);
        table_yewu.getColumnModel().getColumn(0).setPreferredWidth(20);
        table_yewu.getColumnModel().getColumn(1).setPreferredWidth(20);
        table_yewu.getColumnModel().getColumn(2).setPreferredWidth(80);
        scrollPane_yewu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane_yewu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel[2].add(scrollPane_yewu);
        JLabel label = new JLabel("当前时间为：" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("宋体", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("当前时间为：" + new Date()));
        timer.start();
        panel[2].add(label);

    }

    String[] colnumName_VIP = {"VIP窗口-客户姓名"};
    String[][] rowData_VIP = new String[0][];
    JTable table_VIP;
    JScrollPane scrollPane_VIP;
    String[] colnumName_novip = {"普通窗口-客户姓名"};
    String[][] rowData_novip = new String[0][];
    JTable table_novip;
    JScrollPane scrollPane_novip;
    void Update_VIP(){
        try{
            ResultSet rs = stmt.executeQuery("select * from que");
            int row = 0;
            int vip = 0;
            while(rs.next()){
                row++;
                if(rs.getString("isVip").equals("1")){
                    vip++;
                }
            }
            rowData_VIP = new String[vip][1];
            rs = stmt.executeQuery("select * from que");
            row = 0;
            while(rs.next()){
                if(rs.getString("isVip").equals("1")){
                    rowData_VIP[row][0] = rs.getString("Name");
                    row++;
                }
            }
            try {
                table_VIP.setModel(new DefaultTableModel(rowData_VIP, colnumName_VIP));
            }catch (Exception ignored){
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void Update_noVip(){
        try{
            ResultSet rs = stmt.executeQuery("select * from que");
            int row = 0;
            int row_no = 0;
            while(rs.next()){
                if(rs.getString("isVip").equals("0")){
                    row_no++;
                }
                row++;
            }
            rowData_novip = new String[row_no][1];
            rs = stmt.executeQuery("select * from que");
            row = 0;
            while(rs.next()){
                if(rs.getString("isVip").equals("0")){
                    rowData_novip[row][0] = rs.getString("Name");
                    row++;
                }
            }
            try {
                table_novip.setModel(new DefaultTableModel(rowData_novip, colnumName_novip));
            }catch (Exception ignored){
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void init_panel4(){
        panel[4].setBorder(new EmptyBorder(5, 5, 5, 5));
        panel[4].setLayout(null);
        Update_VIP();
        table_VIP = new JTable(rowData_VIP, colnumName_VIP);
        scrollPane_VIP = new JScrollPane(table_VIP);
        table_VIP.getTableHeader().setReorderingAllowed(false);
        scrollPane_VIP.setBounds(10, 10, 400, 180);
        table_VIP.getColumnModel().getColumn(0).setPreferredWidth(20);
        scrollPane_VIP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane_VIP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel[4].add(scrollPane_VIP);
        Update_noVip();
        table_novip = new JTable(rowData_novip, colnumName_novip);
        scrollPane_novip = new JScrollPane(table_novip);
        table_novip.getTableHeader().setReorderingAllowed(false);
        scrollPane_novip.setBounds(10, 200, 400, 180);
        table_novip.getColumnModel().getColumn(0).setPreferredWidth(20);
        scrollPane_novip.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane_novip.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel[4].add(scrollPane_novip);
        JLabel label = new JLabel("当前时间为：" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("宋体", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("当前时间为：" + new Date()));
        timer.start();
        panel[4].add(label);
        JButton jb1 = new JButton("VIP窗口取号");
        jb1.addActionListener(L->{
            // 弹窗输入取号人姓名
            String name = JOptionPane.showInputDialog("请输入取号人姓名");
            if(name == null || name.equals("")){
                JOptionPane.showMessageDialog(null, "姓名不能为空");
                return;
            }
            try{
                String sql1 = "insert into que value (?, ?)";
                PreparedStatement ps = stmt.getConnection().prepareStatement(sql1);
                ps.setString(1, name);
                ps.setString(2, "1");
                ps.executeUpdate();
                Update_VIP();
                Update_noVip();
        } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        jb1.setBounds(420, 10, 120, 30);
        JButton jb2 = new JButton("普通窗口取号");
        jb2.addActionListener(L->{
            // 弹窗输入取号人姓名
            String name = JOptionPane.showInputDialog("请输入取号人姓名");
            if(name == null || name.equals("")){
                JOptionPane.showMessageDialog(null, "姓名不能为空");
                return;
            }
            try{
                String sql1 = "insert into que value (?, ?)";
                PreparedStatement ps = stmt.getConnection().prepareStatement(sql1);
                ps.setString(1, name);
                ps.setString(2, "0");
                ps.executeUpdate();
                Update_VIP();
                Update_noVip();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        jb2.setBounds(420, 200, 120, 30);
        JButton jb3 = new JButton("VIP窗口叫号");
        jb3.addActionListener(L->{
            try{
                ResultSet rs = stmt.executeQuery("select * from que where isVip = 1");
                if(rs.next()){
                    JOptionPane.showMessageDialog(null, "请" + rs.getString("Name") + "到VIP窗口办理业务");
                    String sql1 = "delete from que where Name = ? and isVip = 1";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql1);
                    ps.setString(1, rs.getString("Name"));
                    ps.executeUpdate();
                    Update_VIP();
                    Update_noVip();
                    // 打分
                    String score = JOptionPane.showInputDialog("请对本次服务进行评分（1-5）");
                    if(score == null || score.equals("")){
                        JOptionPane.showMessageDialog(null, "评分不能为空");
                        return;
                    }
                    JOptionPane.showMessageDialog(null, "感谢您的评分");

                }else{
                    JOptionPane.showMessageDialog(null, "没有VIP客户在排队", "警告", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        jb3.setBounds(420, 130, 120, 30);
        JButton jb4 = new JButton("普通窗口叫号");
        jb4.addActionListener(L->{
            try{
                ResultSet rs = stmt.executeQuery("select * from que where isVip = 0");
                if(rs.next()){
                    JOptionPane.showMessageDialog(null, "请" + rs.getString("Name") + "到普通窗口办理业务");
                    String sql1 = "delete from que where Name = ? and isVip = 0";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql1);
                    ps.setString(1, rs.getString("Name"));
                    ps.executeUpdate();
                    Update_VIP();
                    Update_noVip();
                    // 打分
                    String score = JOptionPane.showInputDialog("请对本次服务进行评分（1-5）");
                    if(score == null || score.equals("")){
                        JOptionPane.showMessageDialog(null, "评分不能为空");
                        return;
                    }
                    JOptionPane.showMessageDialog(null, "感谢您的评分");
                }else{
                    JOptionPane.showMessageDialog(null, "没有普通客户在排队", "警告", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        jb4.setBounds(420, 330, 120, 30);
        panel[4].add(jb1);
        panel[4].add(jb2);
        panel[4].add(jb3);
        panel[4].add(jb4);
    }
}
