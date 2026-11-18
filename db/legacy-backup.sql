/*
 Navicat Premium Data Transfer

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306
 Source Schema         : user

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 07/12/2022 14:29:00
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for custom
-- ----------------------------
DROP TABLE IF EXISTS `custom`;
CREATE TABLE `custom`  (
  `Card` varchar(3000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Name` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Pass` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Tele` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Posi` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Res` double(16, 2) NULL DEFAULT NULL,
  `loan` double(16, 2) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of custom
-- ----------------------------
INSERT INTO `custom` VALUES ('9428837788447430', '湛敏', '324397', '10992551472', '1', 585371.87, 46.91);
INSERT INTO `custom` VALUES ('4077743179952141', '鲁超', '689545', '17818415832', '1', 903279.45, 281.00);
INSERT INTO `custom` VALUES ('8035913024106047', '邬刚', '037394', '12090358182', '1', 122058.55, 82.87);
INSERT INTO `custom` VALUES ('9762004136144022', '俞艳', '979262', '11076170338', '1', 188076.05, 160.34);
INSERT INTO `custom` VALUES ('1370541925775144', '卫秀英', '209404', '17374738753', '0', 293267.32, 36.14);
INSERT INTO `custom` VALUES ('7205270872634466', '许磊', '296481', '10643560841', '0', 85776.38, 176.16);
INSERT INTO `custom` VALUES ('1538195856971031', '喻桂英', '804719', '10931700838', '1', 789073.56, 70.23);
INSERT INTO `custom` VALUES ('7862566568989252', '计秀兰', '660651', '17128345415', '1', 417692.76, 243.70);
INSERT INTO `custom` VALUES ('0690799223213977', '奚强', '124351', '15673895990', '0', 796050.72, 70.25);
INSERT INTO `custom` VALUES ('2159992550949859', '谢强', '424674', '11366302962', '1', 395177.99, 29.07);
INSERT INTO `custom` VALUES ('2813039143069847', '康丽', '487273', '17456797153', '1', 711716.22, 145.04);
INSERT INTO `custom` VALUES ('1216353491861042', '郝洋', '609836', '19163968390', '0', 59229.50, 286.64);
INSERT INTO `custom` VALUES ('3108824244022471', '茅静', '348005', '10969548976', '0', 592521.10, 134.54);
INSERT INTO `custom` VALUES ('3652111189781053', '吴强', '165296', '14499610593', '0', 867993.93, 89.59);
INSERT INTO `custom` VALUES ('9821122642296485', '平霞', '536193', '17537706477', '0', 850900.29, 255.57);
INSERT INTO `custom` VALUES ('2298743790103735', '罗霞', '270494', '11099553340', '0', 896731.37, 92.93);
INSERT INTO `custom` VALUES ('8763949284538246', '皮超', '050597', '18560025590', '0', 911506.46, 78.60);
INSERT INTO `custom` VALUES ('1592201360016678', '陈桂英', '579479', '13581554950', '1', 564050.45, 98.45);
INSERT INTO `custom` VALUES ('7427135071793817', '何强', '201099', '15911204138', '1', 479259.89, 146.04);
INSERT INTO `custom` VALUES ('4815558862112024', '姚秀兰', '193608', '15606445519', '1', 311981.51, 67.95);
INSERT INTO `custom` VALUES ('6768200640186196', '沈洋', '906193', '15658539102', '0', 518546.14, 50.70);
INSERT INTO `custom` VALUES ('6767273469937087', '尤艳', '415244', '12929059850', '1', 296043.24, 154.48);
INSERT INTO `custom` VALUES ('4086949654369863', '平洋', '670950', '18912340902', '0', 162053.39, 231.95);
INSERT INTO `custom` VALUES ('4033984053374216', '窦涛', '990867', '13375897312', '1', 296863.59, 144.11);
INSERT INTO `custom` VALUES ('1536997356400633', '祝超', '848592', '14594169405', '1', 947792.58, 157.07);
INSERT INTO `custom` VALUES ('3085080914526811', '秦芳', '358778', '16291690643', '1', 97359.06, 39.92);
INSERT INTO `custom` VALUES ('4888582486324963', '毕艳', '675692', '15574769005', '0', 859484.73, 47.82);
INSERT INTO `custom` VALUES ('4891118769004402', '王磊', '180535', '17235122259', '0', 883964.52, 252.48);
INSERT INTO `custom` VALUES ('2095048536620244', '苗洋', '711828', '15708323783', '1', 158992.74, 249.76);
INSERT INTO `custom` VALUES ('11111111', '于正', 'y12332', '11111111', '1', 100000.00, 800.00);

-- ----------------------------
-- Table structure for grap
-- ----------------------------
DROP TABLE IF EXISTS `grap`;
CREATE TABLE `grap`  (
  `p1` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `p2` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dis` double(16, 2) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of grap
-- ----------------------------
INSERT INTO `grap` VALUES ('大门', '网点1', 20.00);
INSERT INTO `grap` VALUES ('大门', '网点2', 40.00);
INSERT INTO `grap` VALUES ('大门', '网点3', 40.00);
INSERT INTO `grap` VALUES ('大门', '网点4', 30.00);
INSERT INTO `grap` VALUES ('大门', '网点5', 35.00);
INSERT INTO `grap` VALUES ('大门', '网点6', 41.00);
INSERT INTO `grap` VALUES ('网点2', '网点3', 44.00);
INSERT INTO `grap` VALUES ('网点2', '网点6', 77.00);
INSERT INTO `grap` VALUES ('网点2', '网点5', 12.00);
INSERT INTO `grap` VALUES ('网点3', '网点4', 33.00);
INSERT INTO `grap` VALUES ('网点3', '网点6', 100.00);
INSERT INTO `grap` VALUES ('网点4', '网点6', 2.00);
INSERT INTO `grap` VALUES ('网点4', '网点2', 2.00);
INSERT INTO `grap` VALUES ('null', 'null', NULL);

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log`  (
  `Date` date NULL DEFAULT NULL,
  `User` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Ope` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of log
-- ----------------------------
INSERT INTO `log` VALUES ('2022-11-13', '1', '用户1存款2元');
INSERT INTO `log` VALUES ('2022-11-13', '1', '用户1存款2元');
INSERT INTO `log` VALUES ('2022-11-13', '1', '用户1取款22元');
INSERT INTO `log` VALUES ('2022-11-13', '1', '用户1存款2元');
INSERT INTO `log` VALUES ('2022-11-13', '2', '用户2存款2222元');
INSERT INTO `log` VALUES ('2022-11-13', '鲁超', '用户鲁超存款333元');
INSERT INTO `log` VALUES ('2022-11-14', '鲁超', '用户鲁超取款90000元');
INSERT INTO `log` VALUES ('2022-11-14', '于正', '用户于正存款100000元');
INSERT INTO `log` VALUES ('2022-11-14', '于正', '用户于正贷款800元');
INSERT INTO `log` VALUES ('2022-11-19', '许磊', '用户许磊取款1111元');
INSERT INTO `log` VALUES ('2022-11-29', '康丽', '用户康丽取款22元');
INSERT INTO `log` VALUES ('2022-11-29', '许磊', '用户许磊存款11元');
INSERT INTO `log` VALUES ('2022-11-29', '计秀兰', '用户计秀兰取款2222元');
INSERT INTO `log` VALUES ('2022-11-29', '俞艳', '用户俞艳贷款22元');
INSERT INTO `log` VALUES ('2022-11-29', '卫秀英', '用户卫秀英取款222元');
INSERT INTO `log` VALUES ('2022-11-29', '喻桂英', '用户喻桂英取款222元');

-- ----------------------------
-- Table structure for person
-- ----------------------------
DROP TABLE IF EXISTS `person`;
CREATE TABLE `person`  (
  `ID` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Sex` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Age` int NULL DEFAULT NULL,
  `tele` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Address` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Posi` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of person
-- ----------------------------
INSERT INTO `person` VALUES ('001', '于正', '0', 19, '15665861738', '山东财经大学6405', '董事长');
INSERT INTO `person` VALUES ('002', '于瑞寒', '1', 19, '18764148156', '山东财经大学7305', '董事长');
INSERT INTO `person` VALUES ('003', '于瑞寒1', '1', 19, '18764148155', '山东财经大学7305', '董事长');
INSERT INTO `person` VALUES ('004', '于瑞寒2', '1', 19, '18764148156', '山东财经大学7305', '董事长');
INSERT INTO `person` VALUES ('005', '于瑞寒3', '1', 19, '18764148156', '山东财经大学7305', '董事长');
INSERT INTO `person` VALUES ('006', '于瑞寒4', '1', 19, '18764148156', '山东财经大学7305', '董事长');
INSERT INTO `person` VALUES ('007', '计平', '1', 38, '15129637505', '广东省南阳市友好区安化路协通公寓', '经理');
INSERT INTO `person` VALUES ('008', '舒杰', '0', 33, '15012294507', '福建省东莞市西林区安福路菊园五街坊', '助理');
INSERT INTO `person` VALUES ('009', '舒杰', '1', 25, '15024595703', '安徽省金昌市友好区白渡桥金色家园', '实习生');
INSERT INTO `person` VALUES ('0010', '酆伟', '0', 38, '13144184574', '海南省黄山市南岔区半淞园路博泰新苑', '副总经理');
INSERT INTO `person` VALUES ('0011', '任超', '1', 56, '14572435321', '江苏省长沙市伊春区安波路阳光翠竹苑', '经理');
INSERT INTO `person` VALUES ('0012', '花军', '1', 18, '12105391823', '河南省铁岭市乌伊岭区安顺路阳光翠竹苑', '董事长');
INSERT INTO `person` VALUES ('0013', '谢静', '1', 38, '13211468103', '山西省周口市上甘岭区宝林路爱里舍花园', '经理');
INSERT INTO `person` VALUES ('0014', '毛涛', '1', 35, '11021057581', '湖南省成都市友好区百官街博泰新苑', '副总经理');
INSERT INTO `person` VALUES ('0015', '顾刚', '1', 19, '14601477497', '山东省庆阳市上甘岭区宝联路博泰新苑', '总经理');
INSERT INTO `person` VALUES ('0016', '邬霞', '0', 41, '17176551202', '甘肃省乌鲁木齐市南岔区八一路菊园五街坊', '员工');
INSERT INTO `person` VALUES ('0017', '葛伟', '1', 63, '12607717129', '河南省榆林市南岔区安远路爱里舍花园', '董事长');
INSERT INTO `person` VALUES ('0018', '黄辉', '0', 57, '14976944208', '浙江省雅安市南岔区板泉路金色家园', '总经理');
INSERT INTO `person` VALUES ('0019', '廉刚', '0', 42, '18805053434', '河南省上饶市翠峦区宝昌路东新大厦', '助理');
INSERT INTO `person` VALUES ('0020', '奚静', '0', 23, '17917293076', '江西省白城市乌伊岭区安义路溢盈河畔别墅', '总经理');
INSERT INTO `person` VALUES ('0021', '郎勇', '1', 65, '10770876001', '山东省伊春市红星区白玉路金色家园', '员工');
INSERT INTO `person` VALUES ('0022', '陶娜', '0', 61, '15631833074', '安徽省营口市带岭区安波路真新六街坊', '主管');
INSERT INTO `person` VALUES ('0023', '滕秀兰', '0', 62, '14908757318', '湖北省临沂市汤旺河区白兰路阳光翠竹苑', '经理');
INSERT INTO `person` VALUES ('0024', '姚磊', '1', 42, '16208365829', '辽宁省通辽市带岭区安汾路复华城市花园', '员工');
INSERT INTO `person` VALUES ('0025', '滕桂英', '1', 37, '14015880364', '甘肃省石家庄市金山屯区宝通路博泰新苑', '员工');
INSERT INTO `person` VALUES ('0026', '穆芳', '1', 56, '14841400145', '云南省赤峰市五营区巴林路耀江花园', '实习生');
INSERT INTO `person` VALUES ('0027', '余秀兰', '1', 49, '19278642053', '四川省邯郸市南岔区宝杨路复华城市花园', '实习生');
INSERT INTO `person` VALUES ('0028', '乐勇', '0', 44, '17203083114', '四川省宜宾市带岭区安龙路和亭佳苑', '实习生');
INSERT INTO `person` VALUES ('0029', '施秀英', '0', 33, '12407438112', '安徽省延安市红星区百色路阳光翠竹苑', '经理');
INSERT INTO `person` VALUES ('0030', '俞芳', '1', 52, '10271043266', '山西省海口市伊春区保德路复华城市花园', '主管');
INSERT INTO `person` VALUES ('0031', '伍秀英', '0', 52, '18918536403', '陕西省丽江市红星区安波路协通公寓', '助理');
INSERT INTO `person` VALUES ('0032', '奚桂英', '1', 55, '12280196606', '山西省盐城市翠峦区宝祁路复华城市花园', '经理');
INSERT INTO `person` VALUES ('0033', '花芳', '1', 39, '17721811481', '湖北省枣庄市新青区安澜路协通公寓', '总监');
INSERT INTO `person` VALUES ('0034', '葛军', '0', 28, '19803760665', '黑龙江省潍坊市乌伊岭区八一路爱里舍花园', '主管');
INSERT INTO `person` VALUES ('0035', '袁桂英', '0', 67, '17010618425', '山东省石嘴山市金山屯区百花街协通公寓', '经理');

-- ----------------------------
-- Table structure for que
-- ----------------------------
DROP TABLE IF EXISTS `que`;
CREATE TABLE `que`  (
  `Name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `isVip` tinyint(1) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of que
-- ----------------------------
INSERT INTO `que` VALUES ('2', 0);
INSERT INTO `que` VALUES ('于瑞寒1', 0);
INSERT INTO `que` VALUES ('于瑞寒1', 0);
INSERT INTO `que` VALUES ('123', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `UserName` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `PassWord` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('root', 'root');
INSERT INTO `user` VALUES ('1', '1');
INSERT INTO `user` VALUES ('于正', '555');

SET FOREIGN_KEY_CHECKS = 1;
