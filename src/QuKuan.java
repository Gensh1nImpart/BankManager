import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
public class QuKuan extends JFrame{
    Statement stmt;
    String Card;
    String Name;
    JLabel[] jl = new JLabel[3];
    JTextField[] jtf = new JTextField[1];
    JButton jb1 = new JButton("取款");
    JButton jb2 = new JButton("取消");
    Box box = Box.createVerticalBox();
    QuKuan(Statement stmt, String Card, String Name, double Money){
        final double[] money = {Money};
        this.setTitle("银行管理-取款");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(400, 200);
        this.setResizable(false);
        this.stmt = stmt;
        this.Card = Card;
        this.Name = Name;
        jl[0] = new JLabel("取款金额");
        jl[1] = new JLabel("取款人姓名 :" + Name);
        jl[2] = new JLabel("当前余额 :" + Money);
        Box box0 = Box.createHorizontalBox();
        box0.add(jl[2]);
        box.add(box0);
        box.add(Box.createVerticalStrut(10));
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
                    if(str.contains(".")){
                        String str1 = str.substring(str.indexOf(".") + 1);
                        if(str1.length() > 2){
                            JOptionPane.showMessageDialog(null, "请输入两位小数", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    if(money[0] - Double.parseDouble(str) < 0){
                        JOptionPane.showMessageDialog(null, "余额不足", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    money[0] -= Double.parseDouble(str);
                    String sql = "update custom set Res = ? where Card = ?";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql);
                    ps.setDouble(1, money[0]);
                    ps.setString(2, Card);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "取款成功,剩余余额为" + money[0], "成功", JOptionPane.INFORMATION_MESSAGE);
                    BankManger.Update_custom();
                    Date date = new Date();
                    // 转化成mysql date
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    BankManger.Write_Log(sqlDate,Name,"用户" + Name + "取款" + str + "元");
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