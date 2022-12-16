/*
@ BankManager - Login.java
@ A JFrame for login
@ Autor : YuRuiH
@ Data : 2022-11-11
 */
import java.sql.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField textField;
    private JPasswordField passwordField;
    private JButton btnNewButton[] = new JButton[5];
    private String btnName[] = {"��¼", "����", "ע��", "�˳�"};
    Box box = Box.createVerticalBox();
    Box box1 = Box.createHorizontalBox();
    Box box2 = Box.createHorizontalBox();
    Box box3 = Box.createHorizontalBox();
    Statement stmt;
    Login(Statement stmt){
        this.setTitle("���й���-��¼");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, 450, 300);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.init();
        this.stmt = stmt;
        box1.add(new JLabel("�û���:  "));
        box1.add(textField);
        box.add(box1);
        box.add(Box.createVerticalStrut(10));
        box2.add(new JLabel("��    ��:  "));
        box2.add(passwordField);
        box.add(box2);
        box.add(Box.createVerticalStrut(10));
        box.add(box3);
        /*
        @ Add buttons
        @ btnNewButton[0] - ��¼
        @ btnNewButton[1] - ����
        @ btnNewButton[2] - ע��
        @ btnNewButton[3] - �˳�
         */
        btnNewButton[0].addActionListener(L -> {
            try{
                boolean flag = false;
                String User_user = textField.getText();
                String User_password = new String(passwordField.getPassword());
                ResultSet rs = stmt.executeQuery("select * from user");
                while(rs.next()){
                    if(User_user.equals(rs.getString("UserName")) && User_password.equals(rs.getString("PassWord"))){
                        flag = true;
                        System.out.println("��¼�ɹ�");
                        JOptionPane.showMessageDialog(null, "��¼�ɹ�", "��ʾ", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        new BankManger(stmt, User_user);
                        break;
                    }
                }
                if(!flag) {
                    System.out.println("��¼ʧ��");
                    JOptionPane.showMessageDialog(null, "�û������������", "��ʾ", JOptionPane.ERROR_MESSAGE);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        btnNewButton[1].addActionListener(L->{
            textField.setText("");
            passwordField.setText("");
        });
        btnNewButton[2].addActionListener(L->{
            this.dispose();
            new Register(stmt);
        });
        btnNewButton[3].addActionListener(L->{
            System.exit(0);
        });
        this.add(box);
        this.setVisible(true);
        this.pack();
    }
    void init(){
        box3.add(Box.createHorizontalStrut(10));
        textField = new JTextField();
        passwordField = new JPasswordField();
        for(int i = 0; i < 4; i++){
            btnNewButton[i] = new JButton(btnName[i]);
            box3.add(btnNewButton[i]);
            box3.add(Box.createHorizontalStrut(10));
        }
    }
}
