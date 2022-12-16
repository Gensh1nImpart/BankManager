import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.*;
import java.awt.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.sql.PreparedStatement;
public class AddPerson extends JFrame{
    Statement stmt;
    JLabel jl[] = new JLabel[7];
    JTextField jtf[] = new JTextField[7];
    JButton jb1 = new JButton("添加");
    JButton jb2 = new JButton("取消");
    Box box = Box.createVerticalBox();
    AddPerson(Statement stmt){
        this.setTitle("银行管理-添加职员");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(400, 200);
        this.setResizable(false);
        this.stmt = stmt;
        jl[0] = new JLabel("职员编号");
        jl[1] = new JLabel("职员姓名");
        jl[2] = new JLabel("职员性别");
        jl[3] = new JLabel("职员年龄");
        jl[4] = new JLabel("职员电话");
        jl[5] = new JLabel("职员地址");
        jl[6] = new JLabel("职员职位");
        String[] str = {"男", "女"};
        JComboBox jcb = new JComboBox(str);
        for(int i = 0; i < 7; i++){
            jtf[i] = new JTextField();
            Box box1 = Box.createHorizontalBox();
            if(i == 2){
                box1.add(jl[i]);
                box1.add(Box.createHorizontalStrut(10));
                box1.add(jcb);

            }
            else {
                box1.add(jl[i]);
                box1.add(Box.createHorizontalStrut(10));
                box1.add(jtf[i]);
            }
            box.add(box1);
        }
        box.add(Box.createVerticalStrut(10));
        Box box2 = Box.createHorizontalBox();
        box2.add(jb1);
        box2.add(Box.createHorizontalStrut(10));
        box2.add(jb2);
        box.add(box2);
        this.add(box);
        this.pack();
        this.setVisible(true);
        jb1.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                try{
                    // 判断是否在数据库中存在
                    String sql = "select * from person where id = ?";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql);
                    ps.setString(1, jtf[0].getText());
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        JOptionPane.showMessageDialog(null, "职员编号已存在");
                        dispose();
                        return;
                    }
                    String sql1 = "insert into person values(?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement pss = stmt.getConnection().prepareStatement(sql1);
                    pss.setString(1, jtf[0].getText());
                    pss.setString(2, jtf[1].getText());
                    String SexValue = "0";
                    if(jcb.getSelectedItem().equals("男")){
                        SexValue = "1";
                    }
                    pss.setString(3, SexValue);
                    pss.setString(4, jtf[3].getText());
                    pss.setString(5, jtf[4].getText());
                    pss.setString(6, jtf[5].getText());
                    pss.setString(7, jtf[6].getText());
                    pss.executeUpdate();
                    BankManger.Update();
                    JOptionPane.showMessageDialog(null, "添加成功");
                    // 调用BankManager的刷新方法

                    dispose();
                }catch(SQLException e1){
                    e1.printStackTrace();
                }
            }
        });
        jb2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                dispose();
            }
        });
        // 如果关掉不会退出程序
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }
    String GetSqlCol(int row){
        String[] zidian = new String[]{"ID","Name","Sex","Age","tele","Address","Posi"};
        return zidian[row];
    }
}