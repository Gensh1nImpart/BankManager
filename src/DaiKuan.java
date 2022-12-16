import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.*;
import java.awt.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.sql.PreparedStatement;

public class DaiKuan extends JFrame{
    Statement stmt;
    String Card;
    String Name;
    JLabel jl[] = new JLabel[2];
    JTextField jtf[] = new JTextField[1];
    JButton jb1 = new JButton("存款");
    JButton jb2 = new JButton("取消");
    Box box = Box.createVerticalBox();
    DaiKuan(Statement stmt, String Card, String Name, double Money){
        final double[] money = {Money};
        this.setTitle("银行管理-贷款");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(400, 200);
        this.setResizable(false);
        this.stmt = stmt;
        this.Card = Card;
        this.Name = Name;
        jl[0] = new JLabel("贷款金额");
        jl[1] = new JLabel("贷款人姓名 :" + Name);
        Box box1 = Box.createHorizontalBox();
        box1.add(jl[1]);
        box.add(box1);
        Box box2 = Box.createHorizontalBox();
        box.add(Box.createVerticalStrut(10));
        box2.add(jl[0]);
        box2.add(Box.createHorizontalStrut(10));
        jtf[0] = new JTextField();
        box2.add(jtf[0]);
        box.add(box2);
        box.add(Box.createVerticalStrut(10));
        Box box3 = Box.createHorizontalBox();
        box3.add(jb1);
        box3.add(Box.createHorizontalStrut(10));
        box3.add(jb2);
        box.add(box3);
        this.add(box);
        this.pack();
        this.setVisible(true);
        jb1.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                try{
                    // 更新custom的res
                    // 判断是不是两位小数
                    String str = jtf[0].getText();
                    if(str.indexOf(".") != -1){
                        String str1 = str.substring(str.indexOf(".") + 1);
                        if(str1.length() > 2){
                            JOptionPane.showMessageDialog(null, "最多输入两位小数", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    System.out.println(Double.parseDouble(str) + " " + money[0]);
                    money[0] += Double.parseDouble(str);
                    System.out.println(money[0]);
                    String sql = "update custom set Loan = ? where Card = ?";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql);
                    ps.setDouble(1, money[0]);
                    ps.setString(2, Card);
                    ps.executeUpdate();;
                    JOptionPane.showMessageDialog(null, "贷款成功，请及时还款");
//                    new BankManger(stmt, Card);
                    Date date = new Date();
                    // 转化成mysql date
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    BankManger.Write_Log(sqlDate,Name,"用户" + Name + "贷款" + str + "元");
                    BankManger.Update_custom();
                    dispose();
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });
        jb2.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
//                new BankManger(stmt, Card);
                dispose();
            }
        });
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}