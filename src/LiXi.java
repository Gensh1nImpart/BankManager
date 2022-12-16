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
        this.setTitle("���й���-��Ϣ������");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(400, 200);
        this.setResizable(false);
        Box box = Box.createVerticalBox();
        JLabel jl[] = new JLabel[2];
        JTextField jtf[] = new JTextField[2];
        JButton jb1 = new JButton("����");
        JButton jb2 = new JButton("ȡ��");
        jl[0] = new JLabel("�����");
        jl[1] = new JLabel("�������");
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
                    // �жϴ�����ǲ������ֲ���С����������λ
                    String str = jtf[0].getText();
                    Pattern pattern= Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
                    if(!pattern.matcher(str).matches()){
                        JOptionPane.showMessageDialog(null, "�����Ϸ�", "����", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // �жϴ�������ǲ�������
                    str = jtf[1].getText();
                    pattern= Pattern.compile("^\\d+$");
                    if(!pattern.matcher(str).matches()){
                        JOptionPane.showMessageDialog(null, "������޲��Ϸ�", "����", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    double money = Double.parseDouble(jtf[0].getText());
                    int year = Integer.parseInt(jtf[1].getText());
                    double lixi = money * year * 0.03;
                    JOptionPane.showMessageDialog(null, "��ϢΪ��" + lixi, "��Ϣ����", JOptionPane.INFORMATION_MESSAGE);
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
