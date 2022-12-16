import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class Addcustom extends JFrame{
    Statement stmt;
    JLabel jl[] = new JLabel[7];
    JTextField jtf[] = new JTextField[7];
    JPasswordField jpf = new JPasswordField();
    JButton jb1 = new JButton("添加");
    JButton jb2 = new JButton("取消");
    Box box = Box.createVerticalBox();
    Addcustom(Statement stmt){
        this.setTitle("银行管理-添加客户");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocation(400, 200);
        this.setResizable(false);
        this.stmt = stmt;
        jl[0] = new JLabel("卡号");
        jl[1] = new JLabel("户名");
        jl[2] = new JLabel("密码");
        jl[3] = new JLabel("电话");
        jl[4] = new JLabel("身份");
        for(int i = 0; i < 4; i++){
            jtf[i] = new JTextField();
            Box box1 = Box.createHorizontalBox();
            box1.add(jl[i]);
            box1.add(Box.createHorizontalStrut(10));
            if(i == 2) {
                box1.add(jpf);
            }else{
                box1.add(jtf[i]);
            }
            box.add(box1);
        }
        String[] str = {"普通用户", "VIP用户"};
        JComboBox jcb = new JComboBox(str);
        Box box1 = Box.createHorizontalBox();
        box1.add(jl[4]);
        box1.add(Box.createHorizontalStrut(10));
        box1.add(jcb);
        box.add(box1);
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
                    // 判断是否为空
                    for(int i = 0; i < 5; i++){
                        if(i == 2){
                            System.out.println(i);
                            String now = new String(jpf.getPassword());
                            if(now.equals("")){
                                JOptionPane.showMessageDialog(null, "请填写完整信息", "提示", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        else if(i == 4){
                            System.out.println(i);
                            if(jcb.getSelectedItem().equals("")){
                                JOptionPane.showMessageDialog(null, "请填写完整信息", "提示", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        else if(jtf[i].getText().equals("")){
                            System.out.println(i);
                            JOptionPane.showMessageDialog(null, "请填写完整信息", "提示", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                    }
                    String sql = "select * from custom where Card = ?";
                    PreparedStatement ps = stmt.getConnection().prepareStatement(sql);
                    ps.setString(1, jtf[0].getText());
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        JOptionPane.showMessageDialog(null, "卡号已存在");
                        dispose();
                        return;
                    }
                    String isvip = "0";
                    if(jcb.getSelectedItem().equals("VIP用户")){
                        isvip = "1";
                    }
                    String sql1 = "insert into custom values(?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement pss = stmt.getConnection().prepareStatement(sql1);
                    pss.setString(1, jtf[0].getText());
                    pss.setString(2, jtf[1].getText());
                    pss.setString(3, new String(jpf.getPassword()));
                    pss.setString(4, jtf[3].getText());
                    pss.setString(5, isvip);
                    pss.setString(6, Double.toString(0.0));
                    pss.setString(7, Double.toString(0.0));
//                    pss.setString(5, jtf[4].getText());
                    pss.executeUpdate();
                    BankManger.Update_custom();
                    JOptionPane.showMessageDialog(null, "添加成功");

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
}