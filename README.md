# BankManager

sdufe数据结构大作业，银行管理系统，javaGui实现

# 😮how to run?

1. `git clone` to local
2. Import jar package
3. Run `main.java`
4. `gbk` !



# 😈Mysql

- `version : 8.0`
- Import `backup.sql` to your database


# 😅tips

- `jdk16`

- Select skin in `mian.java`

  - ```java
    FlatDarculaLaf.install();
    
    ```

- Fake data can be generated in `mian.java`



# 😓Instructions when submitting homework

### 简介

这次大作业，我们使用的是`java`的`gui`工具包`swing`模块进行编写，满足了老师要求的几个要点。存储上数据使用了`mysql`数据库，较于文件存储是比较方便高效的。
使用的是`jdk16`,运行时请检查是否导入`jar`包！
感谢李恒武老师教授的课程提供的这次大作业机会让我能更好的了解到自己的缺点与不足，接下来我会更努力的学习，不辜负老师的期望。李恒武老师教授的课程深入浅出，让我学到了很多很多。

### 功能

- 首先从登陆页面登陆或者注册登陆，就可以看到银行职员管理，可以实现添加修改删除，
- 客户账户管理也是可以实现添加修改删除，
- 存取贷业务管理是可以在客户账户管理中右键客户进行操作的。
- 业务查询是根据客服账户管理中的办理业务存储的数据库读取来的
- 银行排队管理，可视化分为VIP窗口和普通窗口，可以实现取号叫号打分
- 银行网点查询，可以增删改，查询信息，导航

### TODO

- [x] 答辩之前紧急处理了选中行的`bug`
- [ ] 实现一下`tui`
- [ ] 可视化地图
- [ ] 用`vue` + `electron` 写

### 优缺点

#### 优点：

- 细节处理的很好，比如数据库已经存在、用户的余额等重要信息没办法直接修改
- 很人性化，我添加了右键菜单，可以选择客户或者职员进行操作，很方便快捷
- 界面简洁明了好看。
- 数据我使用的自己随机生成的，保证数据合法。

#### 缺点：

- 代码冗杂，希望学习之后用`springboot`框架进行更好的改进。

### ps

关于`swing`，我觉得原版皮肤界面不好看，我调用了`flatlaf`的`FlatDarculaLaf`皮肤，跟我们用的`idea`是很像的，视觉感很舒服，总之这次大作业我们觉得总体是很成功的！

### 部分程序界面

![,](https://www.educoder.net//api/attachments/4371307?type=image/png)
![,](https://www.educoder.net//api/attachments/4371333?type=image/png)
![,](https://www.educoder.net//api/attachments/4371386?type=image/png)
![,](https://www.educoder.net//api/attachments/4371400?type=image/png)
![,](https://www.educoder.net//api/attachments/4371419?type=image/png)
![,](https://www.educoder.net//api/attachments/4371425?type=image/png)

### 部分代码截图

![,](https://www.educoder.net//api/attachments/4371944?type=image/png)
![,](https://www.educoder.net//api/attachments/4371955?type=image/png)
![,](https://www.educoder.net//api/attachments/4371961?type=image/png)
![,](https://www.educoder.net//api/attachments/4371966?type=image/png)
![,](https://www.educoder.net//api/attachments/4371968?type=image/png)

### 数据库截图

![,](https://www.educoder.net//api/attachments/4371993?type=image/png)
![,](https://www.educoder.net//api/attachments/4371999?type=image/png)
![,](https://www.educoder.net//api/attachments/4372003?type=image/png)
![,](https://www.educoder.net//api/attachments/4372009?type=image/png)
![,](https://www.educoder.net//api/attachments/4372010?type=image/png)
![,](https://www.educoder.net//api/attachments/4372015?type=image/png)
