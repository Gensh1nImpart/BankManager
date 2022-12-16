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
        this.setTitle("���й���-����Ա" + " - " + now_user);
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
            System.out.println("д����־�ɹ�");
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
        tabbedPane.addTab("����ְԱ����", null, panel[0], null);
        tabbedPane.addTab("�ͻ��˻�����", null, panel[1], null);
        tabbedPane.addTab("��ȡ��ҵ�����", null, panel[2], null);
        tabbedPane.addTab("�����Ŷӹ���", null, panel[4], null);
        tabbedPane.addTab("���������ѯ", null, panel[5], null);
        tabbedPane.addTab("����", null, panel[7], null);
        Init_panel_0();
        tabbedPane.setSelectedIndex(0);
        //�л���ǩҳʱ����
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

    // ����
    void init_panel6(){
        // ���ڽ���
        panel[7].setLayout(null);
        JLabel label = new JLabel("���ݽṹ����ҵ-���й���ϵͳ");
        label.setFont(new Font("����", Font.PLAIN, 20));
        label.setBounds(150, 50, 300, 50);
        panel[7].add(label);
        JLabel label_1 = new JLabel("���ߣ�YuRuiH & �κ���");
        label_1.setFont(new Font("����", Font.PLAIN, 20));
        label_1.setBounds(150, 100, 300, 50);
        panel[7].add(label_1);
        JLabel label_4 = new JLabel("ѧ�ţ�2021181405 + 34/33");
        label_4.setFont(new Font("����", Font.PLAIN, 20));
        label_4.setBounds(150, 150, 300, 50);
        panel[7].add(label_4);
        JLabel label_2 = new JLabel("�汾��1.0");
        label_2.setFont(new Font("����", Font.PLAIN, 20));
        label_2.setBounds(150, 200, 300, 50);
        panel[7].add(label_2);
        JLabel label_3 = new JLabel("���ڣ�2022-11-29");
        label_3.setFont(new Font("����", Font.PLAIN, 20));
        label_3.setBounds(150, 250, 300, 50);
        panel[7].add(label_3);
        JLabel label_5 = new JLabel("����ջ: JavaSwing + MySQL");
        label_5.setFont(new Font("����", Font.PLAIN, 20));
        label_5.setBounds(150, 300, 300, 50);
        panel[7].add(label_5);


    }
    //@ ��ͼ
    JLabel label = new JLabel("����");
    Edge[] edges = new Edge[100];
    JScrollPane scrollpan_map = new JScrollPane();
    JTable table_map = new JTable();
    int cnt = 0;
    void update_map(DefaultTableModel model){
        // ������
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
        label.setText("����" + cnt/2 + "����");
        panel[5].add(label);

        scrollpan_map.setBounds(10, 40, 550, 300);
        panel[5].add(scrollpan_map);

        table_map.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "���", "�յ�", "����"
                }
        ));
        table_map.getColumnModel().getColumn(0).setPreferredWidth(100);
        table_map.getColumnModel().getColumn(1).setPreferredWidth(100);
        table_map.getColumnModel().getColumn(2).setPreferredWidth(100);
        scrollpan_map.setViewportView(table_map);
        DefaultTableModel model = (DefaultTableModel) table_map.getModel();
        update_map(model);
        // ����ͷ�����ɶ�
        table_map.getTableHeader().setReorderingAllowed(false);
        // ���õ�Ԫ�񲻿ɱ༭
        table_map.setDefaultEditor(Object.class, null);
        JButton btn_add = new JButton("���");
        btn_add.setBounds(30, 350, 120, 30);
        panel[5].add(btn_add);
        JButton btn_delete = new JButton("��ѯ");
        btn_delete.setBounds(400, 350, 120, 30);
        panel[5].add(btn_delete);
        btn_add.addActionListener(L->{
            String source = JOptionPane.showInputDialog("���������");
            String destination = JOptionPane.showInputDialog("�������յ�");
            String weight = JOptionPane.showInputDialog("���������");
            if(source == null || destination == null || weight == null){
                // ����
                JOptionPane.showMessageDialog(null, "���벻��Ϊ��", "����", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sql_add = "insert into grap values ('" + source + "', '" + destination + "', " + weight + ")";
            try {
                stmt.executeUpdate(sql_add);
                JOptionPane.showMessageDialog(null, "��ӳɹ�");
                edges[cnt++] = new Edge(source, destination, Double.parseDouble(weight));
                edges[cnt++] = new Edge(destination, source, Double.parseDouble(weight));
                this.init_panel5();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        btn_delete.addActionListener(L->{
            String source = JOptionPane.showInputDialog("���������");
            String destination = JOptionPane.showInputDialog("�������յ�");
            // �ж��Ƿ����
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
                JOptionPane.showMessageDialog(null, "�����ڸ�·��");
                return;
            }
            // �����·��
            JOptionPane.showMessageDialog(null, "���·��Ϊ��" + Dijsrta.getPath(edges, cnt, source, destination));
        });
    }
        //@ ����ְԱ����
    static String[] colnumName = new String[]{"���", "����", "�Ա�", "����", "�绰", "��ַ", "ְλ"};
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
                    rowData[row][2] = "��";
                else
                    rowData[row][2] = "Ů";
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
            System.out.println("����ѯ��" + row + "������");
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
        // ����ÿһ�еĿ��
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
                if(value.equals("��"))
                    value = "1";
                else if(value.equals("Ů"))
                    value = "0";
                else{
                    JOptionPane.showMessageDialog(null, "�Ա�ֻ��Ϊ�л�Ů", "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            System.out.println("�޸��˵�" + row + "�У���" + col + "�е����ݣ��޸ĺ������Ϊ��" + value);
            try{
                stmt.executeUpdate("update person set " + GetSqlCol(col) + " = '" + value + "' where ID = '" + ID + "'");
                JOptionPane.showMessageDialog(null, "�޸ĳɹ�");
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JPopupMenu popupMenu1 = new JPopupMenu();
        JPopupMenu popupMenu2 = new JPopupMenu();
        JMenuItem add1 = new JMenuItem("���");
        JMenuItem add2 = new JMenuItem("���");
        JMenuItem delete = new JMenuItem("ɾ��");
        JMenuItem refresh = new JMenuItem("ˢ��");
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
        // ��Ӱ�ť�ļ���
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
        JLabel label = new JLabel("��ǰʱ��Ϊ��" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("����", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("��ǰʱ��Ϊ��" + new Date()));
        timer.start();
        panel[0].add(label);
    }
    String GetSqlCol(int row){
        String[] zidian = new String[]{"ID","Name","Sex","Age","tele","Address","Posi"};
        return zidian[row];
    }

    // @�ͻ��˻�����
    static String[] colnumName_custom = new String[]{"����","����","����","�绰","���","���","����"};
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
                    rowData_custom[row][4] = "��ͨ�û�";
                }else{
                    rowData_custom[row][4] = "VIP�û�";
                }
                // doubleתString
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
            System.out.println("�ͻ�������ѯ��" + row + "������");
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
                if(value.equals("��ͨ�û�"))
                    value = "0";
                else if(value.equals("VIP�û�"))
                    value = "1";
                else{
                    JOptionPane.showMessageDialog(null, "���ֻ��Ϊ��ͨ�û���VIP�û�,�޸�ʧ��,�뼰ʱ�޸ģ�", "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if(col == 5 || col == 6){
                JOptionPane.showMessageDialog(null, "�ͻ���Ǯ������㶯��", "�󵨣�", JOptionPane.ERROR_MESSAGE);
                Update_custom();
                return;
            }
            try{
                stmt.executeUpdate("update custom set " + GetSqlCol_custom(col) + " = '" + value + "' where Card = '" + CARD + "'");
                JOptionPane.showMessageDialog(null, "�޸ĳɹ�");
            }catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JLabel now_custom = new JLabel("��ǰ�ͻ�: ");
        JPopupMenu popupMenu1 = new JPopupMenu();
        JPopupMenu popupMenu2 = new JPopupMenu();
        JMenuItem add1 = new JMenuItem("���");
        JMenuItem add2 = new JMenuItem("���");
        JMenu solve = new JMenu("����ҵ��");
        JMenuItem cunkuan = new JMenuItem("���");
        JMenuItem qukuan = new JMenuItem("ȡ��");
        JMenuItem daikuan = new JMenuItem("����");
        JMenuItem lixi = new JMenuItem("��Ϣ����");
        // ��ӵ�solve�Ķ����˵�
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
        JMenuItem delete = new JMenuItem("ɾ��");
        JMenuItem refresh = new JMenuItem("ˢ��");
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
                    now_custom.setText("  [+]��ǰ�ͻ�: " + rowData_custom[table_custom.rowAtPoint(e.getPoint())][1]);
                    popupMenu1.show(table_custom, e.getX(), e.getY());
                    now_row[0] = table_custom.rowAtPoint(e.getPoint());
                }
            }
        });
        // ��Ӱ�ť�ļ���
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

        JLabel label = new JLabel("��ǰʱ��Ϊ��" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("����", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("��ǰʱ��Ϊ��" + new Date()));
        timer.start();
        panel[1].add(label);
        panel[1].add(scrollPane_custom);
    }

    String[] colnumName_yewu = {"ʱ��","�û�","����"};
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
        JLabel label = new JLabel("��ǰʱ��Ϊ��" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("����", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("��ǰʱ��Ϊ��" + new Date()));
        timer.start();
        panel[2].add(label);

    }

    String[] colnumName_VIP = {"VIP����-�ͻ�����"};
    String[][] rowData_VIP = new String[0][];
    JTable table_VIP;
    JScrollPane scrollPane_VIP;
    String[] colnumName_novip = {"��ͨ����-�ͻ�����"};
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
        JLabel label = new JLabel("��ǰʱ��Ϊ��" + new Date());
        label.setBounds(0, 400, 560, 20);
        label.setFont(new Font("����", Font.BOLD, 20));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> label.setText("��ǰʱ��Ϊ��" + new Date()));
        timer.start();
        panel[4].add(label);
        JButton jb1 = new JButton("VIP����ȡ��");
        jb1.addActionListener(L->{
            // ��������ȡ��������
            String name = JOptionPane.showInputDialog("������ȡ��������");
            if(name == null || name.equals("")){
                JOptionPane.showMessageDialog(null, "��������Ϊ��");
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
        JButton jb2 = new JButton("��ͨ����ȡ��");
        jb2.addActionListener(L->{
            // ��������ȡ��������
            String name = JOptionPane.showInputDialog("������ȡ��������");
            if(name == null || name.equals("")){
                JOptionPane.showMessageDialog(null, "��������Ϊ��");
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
        JButton jb3 = new JButton("VIP���ڽк�");
        jb3.addActionListener(L->{
            try{
                ResultSet rs = stmt.executeQuery("select * from que where isVip = 1");
                if(rs.next()){
                    JOptionPane.showMessageDialog(null, "��" + rs.getString("Name") + "��VIP���ڰ���ҵ��");
                    String sql1 = "delete from que where Name = ? and isVip = 1";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql1);
                    ps.setString(1, rs.getString("Name"));
                    ps.executeUpdate();
                    Update_VIP();
                    Update_noVip();
                    // ���
                    String score = JOptionPane.showInputDialog("��Ա��η���������֣�1-5��");
                    if(score == null || score.equals("")){
                        JOptionPane.showMessageDialog(null, "���ֲ���Ϊ��");
                        return;
                    }
                    JOptionPane.showMessageDialog(null, "��л��������");

                }else{
                    JOptionPane.showMessageDialog(null, "û��VIP�ͻ����Ŷ�", "����", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        jb3.setBounds(420, 130, 120, 30);
        JButton jb4 = new JButton("��ͨ���ڽк�");
        jb4.addActionListener(L->{
            try{
                ResultSet rs = stmt.executeQuery("select * from que where isVip = 0");
                if(rs.next()){
                    JOptionPane.showMessageDialog(null, "��" + rs.getString("Name") + "����ͨ���ڰ���ҵ��");
                    String sql1 = "delete from que where Name = ? and isVip = 0";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql1);
                    ps.setString(1, rs.getString("Name"));
                    ps.executeUpdate();
                    Update_VIP();
                    Update_noVip();
                    // ���
                    String score = JOptionPane.showInputDialog("��Ա��η���������֣�1-5��");
                    if(score == null || score.equals("")){
                        JOptionPane.showMessageDialog(null, "���ֲ���Ϊ��");
                        return;
                    }
                    JOptionPane.showMessageDialog(null, "��л��������");
                }else{
                    JOptionPane.showMessageDialog(null, "û����ͨ�ͻ����Ŷ�", "����", JOptionPane.WARNING_MESSAGE);
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
