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
    private String btnName[] = {"登录", "重置", "注册", "退出"};
    Box box = Box.createVerticalBox();
    Box box1 = Box.createHorizontalBox();
    Box box2 = Box.createHorizontalBox();
    Box box3 = Box.createHorizontalBox();
    Statement stmt;
    Login(Statement stmt){
        this.setTitle("银行管理-登录");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, 450, 300);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.init();
        this.stmt = stmt;
        box1.add(new JLabel("用户名:  "));
        box1.add(textField);
        box.add(box1);
        box.add(Box.createVerticalStrut(10));
        box2.add(new JLabel("密    码:  "));
        box2.add(passwordField);
        box.add(box2);
        box.add(Box.createVerticalStrut(10));
        box.add(box3);
        /*
        @ Add buttons
        @ btnNewButton[0] - 登录
        @ btnNewButton[1] - 重置
        @ btnNewButton[2] - 注册
        @ btnNewButton[3] - 退出
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
                        System.out.println("登录成功");
                        JOptionPane.showMessageDialog(null, "登录成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        new BankManger(stmt, User_user);
                        break;
                    }
                }
                if(!flag) {
                    System.out.println("登录失败");
                    JOptionPane.showMessageDialog(null, "用户名或密码错误", "提示", JOptionPane.ERROR_MESSAGE);
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
