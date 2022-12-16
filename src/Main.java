/*
@ BankManager - Main.java
@ A JFrame for Index
@ Autor : YuRuiH
@ Data : 2022-11-11
 */
import java.sql.*;
import java.util.*;
import java.util.Date;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        FlatDarculaLaf.install();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            // 本地数据库
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/user", "root", "123321");
            // 远程数据库
//            Connection conn = DriverManager.getConnection("jdbc:mysql://43.129.173.73:3306/user", "user", "123321");
            Statement stmt = conn.createStatement();
            System.out.println("数据库连接成功");
            new Login(stmt);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static String[] xing = {"赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐", "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝", "邬", "安", "常", "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余", "元", "卜", "顾", "孟", "平", "黄", "和", "穆", "萧", "尹", "姚", "邵", "湛", "汪", "祁", "毛", "禹", "狄", "米", "贝", "明", "臧", "计", "伏", "成", "戴", "谈", "宋", "茅", "庞", "熊", "纪", "舒", "屈", "项", "祝", "董", "梁"};
    private static String[] ming = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "涛", "明", "超", "刚", "平", "辉", "霞", "秀兰", "桂英"};
    private static String[] province = {"河北省","山西省","辽宁省","吉林省","黑龙江省","江苏省","浙江省","安徽省","福建省","江西省","山东省","河南省","湖北省","湖南省","广东省","海南省","四川省","贵州省","云南省","陕西省","甘肃省","青海省","台湾省",};
    private static String[] city = {"安康市","安庆市","安顺市","安阳市","鞍山市","巴彦淖尔市","巴中市","白城市","白山市","白银市","百色市","蚌埠市","包头市","宝鸡市","保定市","保山市","北海市","本溪市","滨州市","沧州市","昌都地区","长春市","长沙市","长治市","常德市","常州市","巢湖市","朝阳市","潮州市","郴州市","成都市","承德市","池州市","赤峰市","崇左市","滁州市","达州市","大连市","大庆市","大同市","丹东市","德阳市","德州市","定西市","东莞市","东营市","鄂尔多斯市","鄂州市","防城港市","佛山市","福州市","抚顺市","抚州市","阜新市","阜阳市","甘南州","赣州市","固原市","广安市","广元市","广州市","贵港市","贵阳市","桂林市","哈尔滨市","哈密地区","海北藏族自治州","海东地区","海口市","邯郸市","汉中市","杭州市","毫州市","合肥市","河池市","河源市","菏泽市","贺州市","鹤壁市","鹤岗市","黑河市","衡水市","衡阳市","呼和浩特市","呼伦贝尔市","湖州市","葫芦岛市","怀化市","淮安市","淮北市","淮南市","黄冈市","黄山市","黄石市","惠州市","鸡西市","吉安市","吉林市","济南市","济宁市","佳木斯市","嘉兴市","嘉峪关市","江门市","焦作市","揭阳市","金昌市","金华市","锦州市","晋城市","晋中市","荆门市","荆州市","景德镇市","九江市","酒泉市","开封市","克拉玛依市","昆明市","拉萨市","来宾市","莱芜市","兰州市","廊坊市","乐山市","丽江市","丽水市","连云港市","辽阳市","辽源市","聊城市","临沧市","临汾市","临沂市","柳州市","六安市","六盘水市","龙岩市","陇南市","娄底市","泸州市","吕梁市","洛阳市","漯河市","马鞍山市","茂名市","眉山市","梅州市","绵阳市","牡丹江市","内江市","南昌市","南充市","南京市","南宁市","南平市","南通市","南阳市","宁波市","宁德市","攀枝花市","盘锦市","平顶山市","平凉市","萍乡市","莆田市","濮阳市","普洱市","七台河市","齐齐哈尔市","钦州市","秦皇岛市","青岛市","清远市","庆阳市","曲靖市","衢州市","泉州市","日照市","三门峡市","三明市","三亚市","汕头市","汕尾市","商洛市","商丘市","上饶市","韶关市","邵阳市","绍兴市","深圳市","沈阳市","十堰市","石家庄市","石嘴山市","双鸭山市","朔州市","四平市","松原市","苏州市","宿迁市","宿州市","绥化市","随州市","遂宁市","台州市","太原市","泰安市","泰州市","唐山市","天水市","铁岭市","通化市","通辽市","铜川市","铜陵市","铜仁市","吐鲁番地区","威海市","潍坊市","渭南市","温州市","乌海市","乌兰察布市","乌鲁木齐市","无锡市","吴忠市","芜湖市","梧州市","武汉市","武威市","西安市","西宁市","锡林郭勒盟","厦门市","咸宁市","咸阳市","湘潭市","襄樊市","孝感市","忻州市","新乡市","新余市","信阳市","兴安盟","邢台市","徐州市","许昌市","宣城市","雅安市","烟台市","延安市","盐城市","扬州市","阳江市","阳泉市","伊春市","伊犁哈萨克自治州","宜宾市","宜昌市","宜春市","益阳市","银川市","鹰潭市","营口市","永州市","榆林市","玉林市","玉溪市","岳阳市","云浮市","运城市","枣庄市","湛江市","张家界市","张家口市","张掖市","漳州市","昭通市","肇庆市","镇江市","郑州市","中山市","中卫市","舟山市","周口市","株洲市","珠海市","驻马店市","资阳市","淄博市","自贡市","遵义市",};
    private static String[] area = {"伊春区","带岭区","南岔区","金山屯区","西林区","美溪区","乌马河区","翠峦区","友好区","新青区","上甘岭区","五营区","红星区","汤旺河区","乌伊岭区","榆次区"};
    private static String[] road = {"黄河路","中原路","安波路","新四路","安汾路","安福路","安国路","安化路","安澜路","安龙路","安仁路","安顺路","安亭路","安图路","安业路","安义路","安远路","鞍山路","鞍山支路","澳门路","八一路","巴林路","白城路","白城南路","白渡路","白渡桥","白兰路","白水路","白玉路","百安路（方泰镇）","百官街","百花街","百色路","板泉路","半淞园路","包头路","包头南路","宝安公路","宝安路","宝昌路","宝联路","宝林路","宝祁路","宝山路","宝通路","宝杨路","宝源路","保德路","保定路","保屯路","保屯路","北艾路",};
    private static String[] home = {"金色家园","耀江花园","阳光翠竹苑","东新大厦","溢盈河畔别墅","真新六街坊","和亭佳苑","协通公寓","博泰新苑","菊园五街坊","住友嘉馨名园","复华城市花园","爱里舍花园"};
    private static String[] position = {"董事长","总经理","副总经理","总监","经理","主管","助理","员工","实习生"};
    static void rand_dom_Insert_person(Statement st) throws SQLException {
        // 随机生成职员信息插入到数据库中
        int cnt = 30;
        //查询现在的最大id
        int max_id = 0;
        try {
            ResultSet rs = st.executeQuery("select max(id) from person");
            while(rs.next()){
                max_id = Math.max(Integer.parseInt(rs.getString(1)),max_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(int i = 1; i <= 30; i++){
            String id = "00" + String.valueOf(max_id + i);
            //随机生成银行卡号
            String card = "";
            for(int j = 0; j < 16; j++){
                card += (int)(Math.random() * 10);
            }
            //随机生成中文姓名
            String name = "";
            name += xing[(int)(Math.random() * xing.length)];
            name += ming[(int)(Math.random() * ming.length)];
            //随机生成性别 0是女 1是男
            String sex = Integer.toString((int)(Math.random() * 2));
            //随机生成年龄
            String age = Integer.toString((int)(Math.random() * 50) + 18);
            //随机生成电话号码
            String phone = "1";
            for(int j = 1; j < 11; j++){
                phone += (int)(Math.random() * 10);
            }
            //随机生成地址
            String address = "";
            address += province[(int)(Math.random() * province.length)];
            address += city[(int)(Math.random() * city.length)];
            address += area[(int)(Math.random() * area.length)];
            address += road[(int)(Math.random() * road.length)];
            address += home[(int)(Math.random() * home.length)];
            //随机生成职位
            String posi = "";
            posi += position[(int)(Math.random() * position.length)];

            // 插入到数据库中
            String sql1 = "insert into person values(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pss = st.getConnection().prepareStatement(sql1);
            pss.setString(1, id);
            pss.setString(2, name);
            pss.setString(3, sex);
            pss.setString(4, age);
            pss.setString(5,phone);
            pss.setString(6, address);
            pss.setString(7, posi);
            pss.executeUpdate();
        }
    }
    static void rand_dom_Insert_custom(Statement st) throws SQLException{
        // 随机生成客户信息插入到数据库中
        int cnt = 30;
        for(int i = 1; i <= cnt; i++){
            String Card = "";
            for(int j = 0; j < 16; j++){
                Card += (int)(Math.random() * 10);
            }
            String Name = "";
            Name += xing[(int)(Math.random() * xing.length)];
            Name += ming[(int)(Math.random() * ming.length)];
            String pass = "";
            for(int j = 0; j < 6; j++){
                pass += (int)(Math.random() * 10);
            }
            String tele = "1";
            for(int j = 1; j < 11; j++){
                tele += (int)(Math.random() * 10);
            }
            String posi = Integer.toString((int)(Math.random() * 2));
            String Res = Double.toString((Math.random() * 1000000 * 1.0));
            String loan = Double.toString((Math.random() * 300 * 1.0));
            String sql1 = "insert into custom values(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pss = st.getConnection().prepareStatement(sql1);
            pss.setString(1, Card);
            pss.setString(2, Name);
            pss.setString(3, pass);
            pss.setString(4, tele);
            pss.setString(5, posi);
            pss.setString(6, Res);
            pss.setString(7, loan);
            pss.executeUpdate();

        }
    }
}
