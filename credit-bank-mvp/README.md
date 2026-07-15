# 终身学习学分银行积分管理系统 · 最小可运行骨架

这是一个 **walking skeleton（能走通的骨架）**：只实现一个功能，但把从页面到数据库的整条链路全部打通。目的是让全组先跑一遍、理解"实际项目是怎么跑起来的"，再分头扩展。

实现的功能：**完成一件事 → 按积分规则算分 → 写一条流水 → 原子更新余额 → 前端展示余额和流水**。

它同时立好了两个核心样板：账本流水（余额每次变动都有不可修改的流水）、数据驱动规则（分值存在表里而非写死），以及全组要复用的公共层（统一响应、全局异常、乐观锁）。

---

## 一、环境要求

- JDK 8（或以上）
- Maven 3.6+
- MySQL 8.0（本机启动即可，无需手动建库）

## 二、运行步骤

1. 打开 `src/main/resources/application.yml`，把 `password` 改成你自己的 MySQL 密码。
2. 在 IDEA 里运行 `CreditBankMvpApplication` 的 `main` 方法（或命令行 `mvn spring-boot:run`）。
   - 首次启动会自动建库 `credit_bank_mvp`、建表、写入初始数据。
3. 打开浏览器：
   - 测试页：<http://localhost:8080/>
   - 接口文档（Swagger）：<http://localhost:8080/swagger-ui.html>

在测试页选一个账户，点几下规则按钮，看余额和流水变化，同时看 IDEA 控制台打印出来的 SQL。

### 前端页面访问

项目已新增完整的登录注册页面和多角色主页（系统管理员、机构管理员、学生、专家），可直接在浏览器访问：

- 登录注册页：<http://localhost:8080/login.html>
- 系统管理员主页：<http://localhost:8080/home-system-admin.html>
- 机构管理员主页：<http://localhost:8080/home-institution-admin.html>
- 学生主页：<http://localhost:8080/home-student.html>
- 专家主页：<http://localhost:8080/home-expert.html>

> 说明：当前登录逻辑为前端模拟（演示账号），后续接通后端登录接口后会替换为真实校验。

### 演示账号

以下账号密码可直接登录，登录后自动跳转到对应角色主页：

| 账号       | 密码     | 角色         | 说明                         |
| ---------- | -------- | ------------ | ---------------------------- |
| admin      | 123456   | 系统管理员   | 全局管理权限，可访问全部功能 |
| instadmin  | 123456   | 机构管理员   | 仅管理本机构数据和业务       |
| student    | 123456   | 学生         | 查看个人积分、报名项目、参与活动 |
| expert     | 123456   | 专家         | 评审项目、查看聘用机构       |

## 三、目录结构

```
com.creditbank.mvp
├── CreditBankMvpApplication   启动类
├── common/                    公共层：Result 统一响应、BizException、全局异常处理
├── config/                    MyBatisPlus 配置（乐观锁 + 分页插件）
├── entity/                    实体：Account 账户 / PointRule 规则 / PointTransaction 流水
├── mapper/                    数据访问层（继承 BaseMapper，自带 CRUD）
├── dto/                       接口入参对象
├── service/                   业务层：PointService（核心的 earn 方法在这里）
└── controller/                接口层：PointController
resources/
├── application.yml            配置
├── db/schema.sql              建表脚本
├── db/data.sql                初始化数据
└── static/index.html          最小前端测试页
```

## 四、一次"加分"请求是怎么一层层跑通的

这是这个骨架最该看懂的部分。以点击"完成一门课程"为例：

1. **前端** `index.html` 发起 `POST /api/points/earn`，body 是 `{accountId, ruleCode}`。
2. **Controller**（`PointController.earn`）接住请求，只负责收参数、调 Service、把结果包成 `Result`。
3. **Service**（`PointService.earn`，标了 `@Transactional`）做真正的业务：
   - 用 `AccountMapper` 查账户；
   - 用 `PointRuleMapper` 查规则拿到分值（分值来自表，不是写死的）；
   - 算出新余额，`updateById` 更新账户——因为实体有 `@Version`，SQL 会自动带上 `WHERE version = ?` 并把 version+1，防止并发覆盖；
   - 用 `PointTransactionMapper` 插入一条流水。
   - 这四步在同一个事务里，要么全成功、要么全回滚。
4. **Mapper → MySQL**：MyBatisPlus 把上面的调用翻译成 SQL 执行（控制台能看到）。
5. 结果沿原路返回，前端刷新余额和流水表。

对照之前的架构图：这条链路就是 `前端 → Controller 层 → Service 层 → Mapper 层 → MySQL`，只是暂时还没接 nginx / Redis / Kafka。

## 五、这个骨架对应完整系统的哪一块，怎么扩展

它是"账户交易 + 规则中心"两个模块的最小版。其余 8 个功能
（认证标准、业务流程、活动、项目、专家、机构、用户、兑换规则）都可以照着同一套分层套路复制：

- 新建 entity → mapper → service → controller，复用 `Result` 和全局异常。
- 权限：加 Spring Security 或简单的 JWT 拦截器。
- Redis：把规则、认证标准这类"读多写少"的数据加缓存；积分排行榜用 ZSet。
- Kafka：把"活动完成"做成事件，消费者异步调用这里的 `earn` 逻辑。
- 扣分 / 积分转换：在 `PointService` 里加 `deduct` / `convert` 方法，同样"改余额 + 写流水"。

> 正式开发时记得把 `application.yml` 里的 `spring.sql.init.mode` 从 `always` 改成 `never`，否则每次启动都会清库重建。
