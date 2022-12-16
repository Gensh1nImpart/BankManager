// 导入mysql驱动
import java.sql.*;
import java.util.*;

public class mysqldemo {
    public static void main(String[] args) throws Exception{
        try{
            // 加载驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 连接数据库
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user", "root", "123321");
            // 创建statement
            Statement stmt = conn.createStatement();
            // 执行sql语句
            ResultSet rs = stmt.executeQuery("select * from user");
            // 遍历结果集
            while(rs.next()){
                System.out.println(rs.getString("UserName"));
                System.out.println(rs.getString("PassWord"));
            }
            // 关闭连接
            rs.close();
            stmt.close();
            conn.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
