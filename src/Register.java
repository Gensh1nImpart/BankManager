/*
@ BankManager - Register.java
@ A JFrame for Register
@ Autor : YuRuiH
@ Data : 2022-11-11
 */
import java.sql.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Register extends JFrame{
    Statement stmt;
    private static final long serialVersionUID = 1L;
    private JTextField textField;
    private JPasswordField passwordField;
    private JPasswordField passwordField_1;
    private JButton btnNewButton[] = new JButton[5];
    private String btnName[] = {"ע��", "����", "����"};
    Box box = Box.createVerticalBox();
    Box box1 = Box.createHorizontalBox();
    Box box2 = Box.createHorizontalBox();
    Box box3 = Box.createHorizontalBox();
    Box box4 = Box.createHorizontalBox();
    Register(Statement stmt){
        this.setTitle("���й���-ע��");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, 450, 300);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.init();
        this.stmt = stmt;
        textField = new JTextField();
//        textField.setColumns(10);
        passwordField = new JPasswordField();
//        passwordField.setColumns(10);
        passwordField_1 = new JPasswordField();
        box1.add(new JLabel("�û���:  "));
        box1.add(textField);
        box.add(box1);
        box.add(Box.createVerticalStrut(10));
        box2.add(new JLabel("��    ��:  "));
        box2.add(passwordField);
        box.add(box2);
        box.add(Box.createVerticalStrut(10));
        box3.add(new JLabel("ȷ������:  "));
        box3.add(passwordField_1);
        box.add(box3);
        box.add(Box.createVerticalStrut(10));
        box.add(box4);
        /*
        @ Add buttons
        @ btnNewButton[0] - ע��
        @ btnNewButton[1] - ����
        @ btnNewButton[2] - ����
         */
        btnNewButton[0].addActionListener(L->{
            try{
                boolean flag = false;
                String User_user = textField.getText();
                String User_password = new String(passwordField.getPassword());
                String User_password_1 = new String(passwordField_1.getPassword());
                System.out.println(User_password + "  " + User_password_1);
                ResultSet rs = stmt.executeQuery("select * from user");
                if(!User_password.equals(User_password_1)){
                    System.out.println("�������벻һ��");
                    JOptionPane.showMessageDialog(null, "�������벻һ��", "��ʾ", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    while(rs.next()){
                        if(rs.getString("UserName").equals(User_user)){
                            JOptionPane.showMessageDialog(null, "�û����Ѵ���", "��ʾ", JOptionPane.INFORMATION_MESSAGE);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        stmt.executeUpdate("insert into user values('"+User_user+"', '"+User_password+"')");
                        JOptionPane.showMessageDialog(null, "ע��ɹ�", "��ʾ", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        new Login(stmt);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        });
        btnNewButton[1].addActionListener(L->{
            textField.setText("");
            passwordField.setText("");
            passwordField_1.setText("");
        });
        btnNewButton[2].addActionListener(L->{
            this.dispose();
            new Login(stmt);
        });
        this.add(box);
        this.setVisible(true);
        this.pack();
    }
    void init(){
        box4.add(Box.createHorizontalStrut(10));
        for(int i = 0; i < 3; i++){
            btnNewButton[i] = new JButton(btnName[i]);
            box4.add(btnNewButton[i]);
            box4.add(Box.createHorizontalStrut(10));
        }
    }
}
