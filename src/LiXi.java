import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.*;
import java.awt.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.sql.PreparedStatement;
import java.util.regex.Pattern;

public class LiXi extends JFrame{
    LiXi(){
        this.setTitle("银行管理-利息计算器");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(400, 200);
        this.setResizable(false);
        Box box = Box.createVerticalBox();
        JLabel jl[] = new JLabel[2];
        JTextField jtf[] = new JTextField[2];
        JButton jb1 = new JButton("计算");
        JButton jb2 = new JButton("取消");
        jl[0] = new JLabel("存款金额");
        jl[1] = new JLabel("存款年限");
        Box box1 = Box.createHorizontalBox();
        box1.add(jl[0]);
        box1.add(Box.createHorizontalStrut(10));
        jtf[0] = new JTextField();
        box1.add(jtf[0]);
        box.add(box1);
        box.add(Box.createVerticalStrut(10));
        Box box2 = Box.createHorizontalBox();
        box2.add(jl[1]);
        box2.add(Box.createHorizontalStrut(10));
        jtf[1] = new JTextField();
        box2.add(jtf[1]);
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
        jb1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    // 判断存款金额是不是数字并且小数不超过两位
                    String str = jtf[0].getText();
                    Pattern pattern= Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
                    if(!pattern.matcher(str).matches()){
                        JOptionPane.showMessageDialog(null, "存款金额不合法", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // 判断存款年限是不是数字
                    str = jtf[1].getText();
                    pattern= Pattern.compile("^\\d+$");
                    if(!pattern.matcher(str).matches()){
                        JOptionPane.showMessageDialog(null, "存款年限不合法", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    double money = Double.parseDouble(jtf[0].getText());
                    int year = Integer.parseInt(jtf[1].getText());
                    double lixi = money * year * 0.03;
                    JOptionPane.showMessageDialog(null, "利息为：" + lixi, "利息计算", JOptionPane.INFORMATION_MESSAGE);
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });
        jb2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
