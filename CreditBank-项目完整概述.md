# 终身学习学分银行积分管理系统（CreditBank MVP）项目完整概述

## 一、项目背景与目标

本项目是一个面向终身学习场景的**学分银行积分管理系统**，旨在为各类学习者（学生）、教育机构、评审专家和系统管理员提供一个统一的学习成果认证、积分累计、积分兑换、活动参与与项目学习平台。系统核心设计理念是"账本流水+数据驱动规则"，即每一次积分变动都产生不可修改的流水记录，所有分值规则存储于数据库中而非硬编码，确保系统可追溯、可配置、可扩展。

**项目名称**：credit-bank-mvp（终身学习学分银行积分管理系统最小可运行骨架）
**技术定位**：前后端分离的全栈Web应用
**运行端口**：后端8080，前端Vite开发模式默认端口5173

---

## 二、技术栈详情

### 2.1 后端技术栈

| 技术/框架 | 版本 | 用途 |
|---|---|---|
| Java | 8/9 | 核心开发语言 |
| Spring Boot | 2.7.18 | 后端主框架，提供Web、事务、自动配置等能力 |
| Spring Security | Boot自带 | 安全框架，提供BCrypt密码加密器 |
| MyBatis-Plus | 3.5.3.1 | ORM持久层框架，提供自动CRUD、乐观锁、分页插件 |
| MySQL | 8.0 | 关系型数据库，存储所有业务数据 |
| Redis | 5.x | 缓存中间件，存储签到状态、连续签到天数等高频数据 |
| JJWT (Java JWT) | 0.11.5 | JWT Token生成与解析，实现无状态认证 |
| SpringDoc (Swagger) | 1.7.0 | 自动生成OpenAPI接口文档 |
| Jackson (jsr310) | - | JSON序列化，支持Java 8时间类型 |
| Maven | 3.6+ | 项目构建与依赖管理 |

### 2.2 前端技术栈

| 技术/框架 | 版本 | 用途 |
|---|---|---|
| Vue | 3.4.21 | 前端主框架，采用Composition API编程范式 |
| Vue Router | 4.3.0 | 前端路由管理，含路由守卫与角色权限控制 |
| Element Plus | 2.5.3 | UI组件库，提供表格、表单、对话框等组件 |
| Element Plus Icons | 2.3.1 | 图标组件库 |
| Axios | 1.6.7 | HTTP客户端，封装请求/响应拦截器 |
| ECharts | 6.1.0 | 数据可视化图表库，用于Dashboard统计展示 |
| Vite | 5.1.6 | 前端构建工具与开发服务器 |
| LocalStorage | - | 前端存储登录态（用户信息+Token） |

### 2.3 项目配置（application.yml 关键配置）

```yaml
server.port: 8080
spring.datasource:
  url: jdbc:mysql://localhost:3306/credit_bank?createDatabaseIfNotExist=true&...
  username: root
  password: 123456
spring.redis:
  host: localhost
  port: 6379
  password: 123456
  database: 0
  timeout: 10000ms
jwt:
  secret: creditbank-jwt-secret-key-2026-07-11
  expire: 86400000  # 24小时过期
```

---

## 三、系统架构设计

### 3.1 整体架构（前后端分离）

```
┌─────────────────────────────────────────────────────────────┐
│                         前端 (Vue3)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────┐   │
│  │  登录注册 │  │工作台Dashboard│ │ 积分商城 │  │ 转换申请  │   │
│  │  用户管理 │  │ 项目管理   │ │ 机构管理 │  │ 证书认证  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └─────┬─────┘   │
│       │             │             │                │         │
│  ┌────┴─────────────┴─────────────┴────────────────┴─────┐  │
│  │              Axios (请求拦截器 + Token注入)             │  │
│  └──────────────────────────┬──────────────────────────────┘  │
└─────────────────────────────┼─────────────────────────────────┘
                              │ HTTPS (Authorization: Bearer xxx)
┌─────────────────────────────┼─────────────────────────────────┐
│                       后端 (Spring Boot)                      │
│  ┌──────────────────────────┴──────────────────────────────┐  │
│  │  三层拦截器链（按顺序执行）                               │  │
│  │  1. JwtAuthenticationInterceptor → 认证+写入用户上下文   │  │
│  │  2. RoleAuthorizationInterceptor → 角色权限控制         │  │
│  │  3. FreezePermissionInterceptor → 冻结用户读写限制      │  │
│  └──────────────────────────┬──────────────────────────────┘  │
│                             │                                 │
│  ┌──────────────────────────┴──────────────────────────────┐  │
│  │                    Controller层 (22个)                   │  │
│  │  统一返回 Result<T> {code, message, data}               │  │
│  └──────────────────────────┬──────────────────────────────┘  │
│                             │                                 │
│  ┌──────────────────────────┴──────────────────────────────┐  │
│  │              Service层 (@Transactional事务)             │  │
│  └──────────────────────────┬──────────────────────────────┘  │
│                             │                                 │
│  ┌──────────────────────────┴──────────────────────────────┐  │
│  │         Mapper层 (MyBatis-Plus BaseMapper)              │  │
│  └────┬───────────────────────┬────────────────────────────┘  │
│       │                       │                               │
│  ┌────┴────┐             ┌────┴────┐                          │
│  │  MySQL  │             │  Redis  │                          │
│  └─────────┘             └─────────┘                          │
└───────────────────────────────────────────────────────────────┘
```

### 3.2 后端代码包结构

```
com.creditbank.mvp
├── CreditBankMvpApplication.java          // 启动类
├── common/                                 // 公共层
│   ├── Result.java                        // 统一响应体 {code, message, data}
│   ├── BizException.java                  // 业务异常（受检→非受检）
│   └── GlobalExceptionHandler.java        // 全局异常兜底处理
├── config/                                 // 配置层
│   ├── AuthConstants.java                 // 权限路径常量集中管理
│   ├── DatabaseInitializer.java           // 启动时自动执行SQL+初始化管理员
│   ├── WebConfig.java                     // 注册3个拦截器+静态资源映射
│   ├── JwtAuthenticationInterceptor.java  // JWT认证拦截器
│   ├── RoleAuthorizationInterceptor.java  // 角色权限拦截器
│   ├── FreezePermissionInterceptor.java   // 冻结用户权限拦截器
│   ├── SecurityConfig.java                // BCryptPasswordEncoder Bean
│   ├── RedisConfig.java                   // RedisTemplate序列化配置
│   ├── MybatisPlusConfig.java             // 乐观锁+分页插件
│   ├── ResponseUtil.java                  // 拦截器内直接写JSON响应
│   └── OpenApiConfig.java                 // Swagger配置
├── controller/ (22个)                     // 接口层
├── service/ (23个)                        // 业务逻辑层
├── mapper/ (22个)                         // 数据访问层（继承BaseMapper）
├── entity/ (21个)                         // 数据库实体（@TableName + @TableId）
├── dto/ (23个)                            // 数据传输对象（接口入参/出参）
└── util/
    ├── JwtUtil.java                       // JWT生成/解析/验证
    └── CurrentUserUtil.java               // 从request attribute取当前用户
```

### 3.3 前端目录结构

```
frontend/src
├── api/                // 后端API封装（21个模块）
│   ├── request.js      // Axios实例 + 请求/响应拦截器
│   ├── user.js         // 用户登录/注册/管理
│   ├── profile.js      // 个人资料/密码修改
│   ├── point.js        // 积分交易
│   ├── transaction.js  // 流水管理
│   ├── conversionRule.js / conversionApplication.js
│   ├── project.js / studentCert.js / expertCert.js
│   ├── organization.js / expert.js / campaign.js
│   ├── notification.js / signin.js / stats.js ...
├── components/         // 公共组件
│   ├── CountTo.vue / EmptyState.vue / HeroCard.vue
│   ├── NotificationBell.vue / StatCard.vue
├── composables/        // Vue3组合式函数
│   ├── useAuth.js      // 登录/注册/登出 + 用户状态（localStorage持久化）
│   ├── useNotifications.js
│   └── useAuditTodos.js
├── layouts/
│   └── MainLayout.vue  // 主布局（侧边栏+顶栏+内容区）
├── router/
│   └── index.js        // 路由定义 + beforeEach路由守卫（认证+角色白名单）
├── styles/
│   └── design-system.css
├── views/ (30+个页面)  // 各功能页面
├── utils/
│   └── message.js
├── App.vue
└── main.js
```

---

## 四、数据库设计（共21张表）

### 4.1 用户与权限类（4张）

| 表名 | 用途 | 核心字段 |
|---|---|---|
| `sys_user` | 系统用户表（四种角色） | id, username, password(BCrypt), realName, phone, email, role(admin/org_admin/expert/student), org_id, expert_field, balance(积分余额), status(1正常/0冻结), frozen_at, frozen_by, last_login_at, created_at |
| `organization` | 机构表 | id, name, contact_person, contact_phone, address, province, status(0待审核/1启用/2禁用/3拒绝), reject_reason, created_at, updated_at |
| `admin_role_menu` | 管理员角色菜单权限表 | id, role_tag, menu_code |
| `user_op_log` | 用户操作日志表 | id, operator_id, operator_name, target_user_id, target_user_name, action(FREEZE/UNFREEZE/RESET_PW/.../EARN), module(USER/CAMPAIGN/...), detail, created_at |

### 4.2 积分账本类（3张核心表）

| 表名 | 用途 | 核心字段 |
|---|---|---|
| `credit_rule` | 积分规则表（数据驱动，非硬编码） | id, **event_code(唯一索引)**, event_name, credit_value(+加/-扣), is_enabled, created_at, start_time, end_time, project_id(外键→project), org_id |
| `transaction_log` | 交易流水表（**核心账本，不可修改**） | id, user_id, amount(±变动值), balance_after(变动后余额快照), **biz_type(ENUM: REWARD/EXCHANGE/REFUND/ADMIN/ATTACHMENT/UPDATE_ADJUST/DAILY)**, related_rule_id(关联规则/原流水ID), description, created_at |
| `exchange_rule` | 积分兑换规则表 | id, item_name, item_icon, required_credit, stock, per_user_limit, is_enabled, org_id, created_at |

### 4.3 项目与活动类（4张）

| 表名 | 用途 | 核心字段 |
|---|---|---|
| `project` | 项目表 | id, org_id, expert_id, name, description, credit_reward(完成奖励), credit_price(报名费用, 0免费), status(0待审/1上架/2驳回/3下架/4审核中), application_id, created_at, updated_at |
| `student_project` | 学生报名项目关系表 | id, student_id, project_id, status(已报名/进行中/已完成/已取消), created_at | 联合唯一键(student_id, project_id) |
| `campaign` | 平台活动表 | id, title, description, cover_image, images, organizer, **multiplier(积分倍率如1.5)**, project_ids(适用项目), start_time, end_time, status(0未开始/1进行中/2已结束) |
| `campaign_enrollment` | 活动报名记录 | id, campaign_id, user_id, enrolled_at | 联合唯一键(user_id, campaign_id) |

### 4.4 认证与审批类（6张）

| 表名 | 用途 | 核心字段 |
|---|---|---|
| `cert_standard` | 认证标准表 | id, standard_name, version, org_id(NULL=通用), target_role(student/expert/org_admin), requirement_text, need_manual_audit, first_node_id, is_enabled |
| `cert_audit_flow` | 认证审批流程节点表（链表结构） | id, cert_standard_id, auditor_id, **next_node_id(NULL=流程结束)**, created_at |
| `application` | **统一申请审批表**（承载所有申请业务：项目上架/兑换/证书申请） | id, biz_type(PROJECT_UP/EXCHANGE/CERT_APPLY), biz_key, applicant_id, org_id, expert_id, form_data(JSON), current_status(0草稿/1审核中/3通过/4驳回), current_node_id, reject_reason, applied_at, updated_at |
| `application_audit_log` | 申请审批业务记录表 | id, application_id, node_id, status(2通过/3驳回), reject_reason, created_at |
| `student_cert` | 学生证书发放记录表 | id, student_id, cert_standard_id, application_id, **cert_no(唯一)**, student_name(快照), cert_name(快照), org_name(快照), **verify_code(核验码)**, status(1有效/0撤销), revoke_reason, revoked_at, issued_at, valid_until |
| `expert_cert` | 专家认证记录表 | id, expert_id, cert_standard_id, field_name(快照), application_id, status(1有效/0撤销), issued_at, valid_until, revoke_reason, revoked_at |

### 4.5 成果转换类（2张）

| 表名 | 用途 | 核心字段 |
|---|---|---|
| `conversion_rule` | 转换规则表 | id, original_name, original_org_id, original_type, converted_name, converted_org_id, converted_type, **credit_rule_id(必填外键→credit_rule.id, ON DELETE RESTRICT)**, is_enabled, effective_start, effective_end, description, created_by |
| `conversion_application` | 转换申请表 | id, rule_id, student_id, original_name/org_id/type, converted_name/org_id/type, certificate_file, **form_data(证明材料JSON)**, credit_rule_id(外键), apply_type(RULE_CONVERT/RULE_ADD), status(0待审/1通过/2驳回), reject_reason, approved_at, created_at |

### 4.6 通知消息类（2张）

| 表名 | 用途 | 核心字段 |
|---|---|---|
| `notification` (system_notification) | 站内通知消息表 | id, event_code, scope_type(USER/ORG/ROLE/ALL), scope_value, category(SYSTEM/APPLICATION/POINT/MALL/CONVERSION), level(INFO/SUCCESS/WARNING), title, content, source_type, source_id, action_path, actor_id, dedupe_key(幂等唯一键), status(PUBLISHED/REVOKED), created_at, expires_at |
| `notification_recipient` | 通知接收与已读状态表 | id, notification_id, user_id, read_at, confirmed_at, created_at | 联合唯一键(notification_id, user_id)，索引idx_recipient_unread(user_id, read_at) |

---

## 五、用户角色体系（4种角色）

系统严格区分四种用户角色，权限由三层拦截器 + 前端路由守卫双重控制：

| 角色 | role值 | 主要功能 | 对应前端页面 |
|---|---|---|---|
| **系统管理员** | admin | 全局管理权限：用户CRUD、冻结/解冻、重置密码；机构审核；积分规则调整；活动创建/编辑/删除；专家证书审核；数据统计；交易流水撤销 | 用户管理、机构管理、积分规则、交易管理、活动管理、操作日志、数据统计、转换规则管理 |
| **机构管理员** | org_admin | 管理本机构数据：本机构项目上架申请、本机构专家管理；创建兑换规则；发布机构级通知；创建/修改转换规则；审核转换申请；证书审核 | 项目管理、专家管理、兑换规则、通知中心、转换规则、转换申请审核、证书认证 |
| **学生** | student | 查看积分余额与流水；报名/参与项目；参加活动；每日签到；提交成果转换申请；申请证书；积分商城兑换；查看通知 | 工作台、项目报名、我的项目、参与活动、积分商城、转换申请、学生证书、通知中心、我的资料 |
| **专家** | expert | 评审机构上架的项目；查看被分配项目；查看个人资质；证书认证 | 项目评审、我的资质、工作台、通知中心 |

---

## 六、后端22个Controller功能清单

| 序号 | Controller | 核心接口 | 功能描述 |
|---|---|---|---|
| 1 | UserController | POST /login, /register, /create, /test-login | 用户登录/注册/管理；冻结/批量冻结/重置密码 |
| 2 | ProfileController | GET/PUT /profile, PUT /change-password | 个人资料查看/修改；密码修改（从JWT取userId，非请求体） |
| 3 | PointController | POST /earn, /deduct, /adjust | 积分赚取/扣除/管理员调账 |
| 4 | TransactionLogController | GET /page, /{id}, /export, POST /{id}/revert | 交易流水分页/详情/CSV导出/管理员撤销流水（REFUND类型） |
| 5 | CreditRuleController | GET /list, POST /create, /update, /{id}/toggle, /{id}/adjust, DELETE /{id} | 积分规则CRUD+启停+管理员临时调分 |
| 6 | ExchangeRuleController | GET /list, POST /create, /update, /{id}/toggle | 兑换规则CRUD+启停 |
| 7 | ConversionRuleController | GET /list, POST /create, /update, /{id}/toggle, DELETE /{id} | 转换规则管理（admin/org_admin） |
| 8 | ConversionApplicationController | GET /list, /mine, POST /submit, /{id}/audit | 转换申请提交+审核 |
| 9 | OrganizationController | GET /list, POST /create, /update, /{id}/status | 机构管理+审核 |
| 10 | ExpertController | GET /list, POST /create, /update, /{id}/status, /{id}/assign | 专家管理+分配项目 |
| 11 | ProjectController | GET /active, /org, /my, /{id}, POST /{id}/enroll, /create, /{id}/audit, /{id}/offline | 项目上架/报名/审核/下架（学生报名扣积分、完成后加积分） |
| 12 | StudentProjectController | POST /{id}/submit, /{id}/cancel | 学生提交项目成果/取消报名 |
| 13 | CampaignController | GET /active, /{id}, POST /{id}/enroll, /create, PUT /{id}, DELETE /{id} | 活动创建/参与（支持积分倍率multiplier） |
| 14 | SignInController | POST /sign-in, GET /status, /history | 每日签到（Redis缓存签到状态+连续天数，签到写入DAILY流水） |
| 15 | CertStandardController | GET /list, /{id}, POST /create, /update, /{id}/toggle | 认证标准管理 |
| 16 | CertAuditFlowController | POST /save/{certStandardId}, DELETE /{certStandardId} | 审批流程链表配置 |
| 17 | ApplicationController | POST /submit, GET /list, /mine, /{id}/resubmit | 统一申请单（承载项目上架/证书申请/申诉） |
| 18 | StudentCertController | GET /list, /verify(公开), POST /{id}/submit, /{id}/cancel | 学生证书颁发+公开核验接口 |
| 19 | ExpertCertController | GET /list, POST /audit | 专家资质认证+审核 |
| 20 | NotificationController | GET /list, /unread-count, /published, PUT /read, /read-all, /{id}/confirm, /{id}/revoke, POST /publish | 站内通知发布/已读/撤回/重要确认（启动时ensureWelcomeNotificationsForAllUsers生成欢迎通知） |
| 21 | StatsController | GET /dashboard(公开), GET /stats(admin) | 工作台统计Dashboard（积分趋势、用户分布等，ECharts） |
| 22 | FileController | POST /upload, GET /preview/**, /download/**, /view/** | 文件上传（≤10MB）/预览/下载/查看（uploads目录） |

---

## 七、前端30+页面功能清单（按路由分组）

### 7.1 公开页面（无需登录）
| 路由路径 | 页面组件 | 功能 |
|---|---|---|
| `/login` | Login.vue | 用户登录（支持角色选择+测试账号快捷登录） |
| `/org-register` | OrgRegister.vue | 机构注册申请 |
| `/` | PublicHome.vue | 公共首页（展示活动/项目/规则，无管理操作按钮） |
| `/dashboard-map` | DashboardMap.vue | 数据大屏（机构分布地图+统计图表） |
| `/certificate-verify` | CertificateVerify.vue | 证书公开核验（输入cert_no+verify_code查询） |

### 7.2 登录后通用页面
| 路由路径 | 页面组件 | meta.roles | 功能 |
|---|---|---|---|
| `/dashboard` | Dashboard.vue | - | 工作台（积分余额、签到、统计卡片、最近流水） |
| `/profile` | Profile.vue | - | 个人资料编辑+密码修改 |
| `/notifications` | Notifications.vue | - | 通知中心（分类筛选/已读/全部已读/重要确认） |
| `/point-mall` | PointMall.vue | - | 积分商城（兑换品列表+兑换操作） |
| `/conversion-apply` | ConversionApply.vue | - | 成果转换申请（支持已有规则转换/新增规则申请） |

### 7.3 管理员专用页面
| 路由路径 | 页面组件 | 功能 |
|---|---|---|
| `/users` | Accounts.vue | 用户列表（冻结/批量冻结/重置密码/编辑） |
| `/account/:id` | AccountDetail.vue | 用户详情（积分流水、参与项目、证书） |
| `/op-logs` | UserOpLog.vue | 操作日志查询 |
| `/rules` | PointRules.vue | 积分规则管理（CRUD+启停） |
| `/transactions` | Transactions.vue | 交易流水（撤销+CSV导出，标记已撤销状态） |
| `/organizations` | Organizations.vue | 机构审核/管理 |
| `/experts` | Experts.vue | 专家管理+分配机构 |
| `/projects/manage` | ProjectManage.vue | 项目审核/下架 |
| `/campaigns` | CampaignManage.vue | 活动管理（创建/编辑/删除） |
| `/cert-standards` | CertStandards.vue | 认证标准列表 |
| `/cert-standards/:id/flow` | CertFlowManage.vue | 审批流程节点配置（链表拖拽排序） |
| `/applications` | Applications.vue | 统一审核管理（项目上架/证书申请/申诉） |

### 7.4 机构管理员/管理员共用页面
| 路由路径 | 页面组件 | roles白名单 | 功能 |
|---|---|---|---|
| `/conversion-rules` | ConversionRules.vue | admin, org_admin | 转换规则管理（公共页面只读，无操作按钮） |
| `/exchange-rules` | ExchangeRules.vue | admin, org_admin | 兑换规则管理 |

### 7.5 学生/专家页面
| 路由路径 | 页面组件 | 面向角色 | 功能 |
|---|---|---|---|
| `/projects` | ProjectStudent.vue | 学生 | 可报名项目列表（报名扣积分） |
| `/project/:id` | ProjectDetail.vue | 学生 | 项目详情（机构/专家/已报名学生/报名状态） |
| `/my-projects` | MyProjects.vue | 学生 | 我的项目（已报名/进行中/已完成，提交成果） |
| `/campaigns/student` | CampaignStudent.vue | 学生/所有登录用户 | 参与活动（活动列表+活动详情+报名） |
| `/campaign/:id` | CampaignDetail.vue | 学生 | 活动详情 |
| `/student-certs` | StudentCerts.vue | 学生 | 我的证书列表+申请新证书 |
| `/student-certificate/:id` | StudentCertificate.vue | 学生 | 证书详情（含证书编号+核验码） |
| `/my-certs` | ExpertCerts.vue | 专家 | 专家资质认证列表 |
| `/cert-standards/:id/requirement` | CertRequirement.vue | 专家/学生 | 认证标准执行文件查看 |

---

## 八、核心业务流程详解

### 8.1 用户认证与安全流程（三层拦截器链）

```
HTTP请求到达
    ↓
① JwtAuthenticationInterceptor
   ├─ 检查是否是公开路径（PUBLIC_PATHS / PUBLIC_PATH_PREFIXES / FILE_ACCESS_PREFIXES）
   ├─ 从Header取 Authorization: Bearer <token>
   ├─ JwtUtil.parseToken() 解析出 userId + role
   ├─ 查询 sys_user 验证用户存在性
   └─ 写入 request.setAttribute: currentUserId / currentUserRole / frozen(冻结标记)
    ↓
② RoleAuthorizationInterceptor
   ├─ ADMIN_ONLY_PATHS（14条）：仅 role=admin 允许
   │   例：/api/users/create、/api/organization/{id}/status、/api/campaigns POST
   ├─ ADMIN_OR_ORG_ADMIN_PATHS（30+条）：admin或org_admin允许
   │   例：/api/projects POST、/api/conversion-rule/*、/api/notifications/publish
   └─ 不匹配则返回 403 "无权限"
    ↓
③ FreezePermissionInterceptor（用户status=0时触发）
   ├─ GET 请求 → 放行（只读）
   ├─ PUT 通知已读/确认 → 放行
   ├─ POST 白名单（login/register/提交申诉） → 放行
   └─ 其他写操作 → 返回 403 "账户已被冻结，请提交解冻申诉"
    ↓
进入 Controller
```

**前端路由守卫对应逻辑**：
```js
router.beforeEach：
  公开路径 → next()
  无currentUser → 跳/login
  路由meta.roles白名单存在且用户角色不在其中 → 跳/dashboard + 控制台警告
  否则 → next()
```

### 8.2 积分赚取核心流程（原子性+账本不可修改）

以"学生完成项目获得积分"或"每日签到"为例：

```
Service层 @Transactional
  ① 校验用户存在 + 规则存在且启用
  ② 查询用户当前余额 balance
  ③ 计算 newBalance = balance + creditValue
     （签到/项目完成：+；兑换/报名：-；兑换扣积分需newBalance≥0）
  ④ sys_user.balance = newBalance → updateById(user)
     （可选：实体类含@Version乐观锁，防止并发覆盖）
  ⑤ 插入一条 transaction_log：
     - amount = 变动值（正/负）
     - balance_after = newBalance（**快照，不可推导**）
     - biz_type = REWARD / DAILY / EXCHANGE / ...
     - related_rule_id = 关联的规则ID（REFUND时=原流水ID）
     - description = 业务描述
  ⑥ 可选：记录 user_op_log（审计追踪）
  ⑦ 可选：触发通知（站内消息异步/同步发送）
事务提交，要么4步全成，要么全回滚
```

**关键设计原则**：
- **余额不做计算**：每次都更新 user.balance 字段，流水表的 balance_after 是**快照**，不是通过SUM推导的（避免历史流水变动影响当前余额）
- **流水不可修改**：发现错误流水时，不是改原记录，而是插入一条 **REFUND** 类型的反向流水（`TransactionLogService.revert()`），原流水保持不变
- **附加流水级联撤销**：若源流水带 ATTACHMENT 附加流水，撤销源流水时会自动生成对应的REFUND流水级联撤销

### 8.3 签到流程（Redis + DB双写防重）

```
SignInService.signIn(userId) @Transactional：
  ├─ Redis查 "sign:today:{userId}" → 若已存在 → 抛"今日已签到"（性能优先）
  ├─ 从DB查签到规则(event_code=ATTENDANCE) → 不存在则报错
  ├─ DB查今日DAILY类型流水数 → >0 → 抛"今日已签到"（最终兜底防重）
  ├─ 加积分 = signInRule.creditValue → 更新balance → 写DAILY流水
  ├─ Redis写签到标记 true（TTL=当天结束剩余秒数）
  ├─ 计算连续签到天数streak（倒推365天内签到日期集合）
  ├─ Redis缓存连续天数
  └─ 记录user_op_log
返回：{creditEarned, newBalance, streak, message}
```

### 8.4 转换申请审核流程

```
学生提交申请 (ConversionApplicationService.submit)
  ├─ RULE_CONVERT：按rule_id→conversion_rule→credit_rule_id绑定，强制覆盖converted_type=event_code
  ├─ RULE_ADD：按converted_type(event_code/event_name模糊匹配) → credit_rule_id
  ├─ 校验 credit_rule_id 非空（方案A：所有申请必有精确关联的积分规则）
  └─ 初始 status=0 待审核
    ↓
管理员/机构管理员审核 (POST /{id}/audit)
  ├─ 驳回：status=2 + 写reject_reason + 发驳回通知
  └─ 通过：status=1
        ├─ PointService.earn() 按credit_rule_id精确加积分
        ├─ 写通知给学生
        └─ 记录操作日志
```

### 8.5 通知系统发布流程

```
管理员发布通知 (NotificationService.publish)
  ├─ 生成 dedupe_key（业务幂等键：防止重复发送）
  ├─ 插入 notification 记录 (status=PUBLISHED)
  ├─ 按 scope_type 展开接收用户集合：
  │   USER→单个用户 / ORG→该机构所有用户 / ROLE→该角色所有 / ALL→全体
  ├─ 批量插入 notification_recipient：
  │   先 selectCount(notification_id+user_id) 检查存在性，避免DuplicateKeyException
  │   （这是一个必须注意的坑：并发下唯一键冲突，必须先查再插）
  └─ 返回成功统计
```

**启动时动态生成欢迎通知**：
`DatabaseInitializer.run()` → `notificationService.ensureWelcomeNotificationsForAllUsers()`
（不是在data.sql预置，避免通知ID与接收人不匹配；新用户注册后也会补发）

### 8.6 数据库启动自动初始化流程

```
DatabaseInitializer implements CommandLineRunner
  ① executeSqlFile("/db/credit_bank.sql")
     ├─ DROP+CREATE数据库+所有表（含索引/外键）
     └─ 执行Plan-A幂等迁移脚本（6步骤，INFORMATION_SCHEMA检查存在性→PREPARE/EXECUTE动态SQL）
         Step1: credit_rule.event_code加唯一索引，event_name加普通索引
         Step2: conversion_application加form_data TEXT列（迁移历史certificate_file→attachments JSON）
         Step3: 回填conversion_rule.credit_rule_id（按converted_type匹配event_code→event_name）
         Step4: conversion_rule.credit_rule_id改为NOT NULL
         Step5: 加外键fk_conversion_rule_credit_rule (ON DELETE RESTRICT)
         Step6: conversion_application加credit_rule_id列+回填+加外键
     注：单条SQL失败 catch warn跳过，不阻塞整体启动
  ② executeSqlFile("/db/data.sql")  → 初始化演示数据
  ③ ensureAdminUserExists()
     ├─ id=1不存在则插admin/123456(BCrypt)
     └─ 存在但密码不是BCrypt(123456) → 全部重置为BCrypt加密（兼容老明文）
  ④ ensureWelcomeNotificationsForAllUsers() → 动态生成欢迎通知
```

---

## 九、关键技术实现与工程化亮点

### 9.1 JWT无状态认证实现

- **JwtUtil.java**：使用 HMAC-SHA256 签名，Claims包含 userId/username/role，过期时间24h
- **Token注入**：前端Axios请求拦截器自动从localStorage取cb_token，注入`Authorization: Bearer xxx`
- **Token失效响应**：后端返回401 → 前端响应拦截器清除localStorage + ElMessageBox提示"重新登录"

### 9.2 Result统一响应体约定

```java
// 所有Controller接口返回 Result<T>
{
  "code": 200,       // 200成功，401未认证，403无权限，500业务/系统异常
  "message": "success",
  "data": T          // 任意类型的业务数据
}
// GlobalExceptionHandler：BizException→Result.fail(message)；Exception→记日志+Result.fail(通用提示)
```

### 9.3 Redis缓存策略

- **RedisConfig**：key用StringRedisSerializer，value用GenericJackson2JsonRedisSerializer（注册JavaTimeModule）
- **RedisService**：封装String/Hash/List操作+TTL；`execute()`时显式转`RedisCallback<Object>`避免方法歧义
- **典型场景**：
  - 签到状态：`sign:today:{userId}` → TTL=当日剩余秒数
  - 连续签到天数：`sign:streak:{userId}` → TTL=当日剩余秒数
  - 未来扩展：积分规则缓存、认证标准缓存（读多写少场景）

### 9.4 密码安全

- **SecurityConfig**：注册BCryptPasswordEncoder Bean
- 所有用户密码存储为BCrypt哈希（DatabaseInitializer启动时统一重置为BCrypt加密的123456）
- 演示账号密码：admin/instadmin/student/expert 均为 123456

### 9.5 数据库幂等迁移设计

credit_bank.sql末尾的Plan-A迁移脚本（6步骤）是**可重跑**的：
- 每条DDL前先查询INFORMATION_SCHEMA判断索引/列/外键是否存在
- 用MySQL的`IF(@cond, 'ALTER...', 'SELECT 1')` + PREPARE/EXECUTE动态SQL
- 数据回填用`WHERE xxx IS NULL`保证只执行一次（UPDATE幂等）
- DatabaseInitializer.executeSql() catch异常warn跳过，不阻塞启动

### 9.6 测试覆盖（14个测试类）

| 测试类 | 覆盖模块 |
|---|---|
| UserServiceTest | 用户管理/登录/注册/冻结 |
| ProfileServiceTest | 个人资料/密码修改 |
| PointServiceTest | 积分赚取/扣除/调账（核心账本） |
| TransactionLogServiceTest | 流水撤销/CSV导出 |
| OrganizationServiceTest | 机构审核/管理 |
| ProjectServiceTest | 项目CRUD/审核/上下架 |
| StudentProjectServiceTest | 学生报名/取消/提交成果 |
| CampaignServiceTest | 活动CRUD/报名/积分倍率 |
| SignInServiceTest | 签到防重/连续天数计算 |
| NotificationServiceTest / PointNotificationTest | 通知发布/已读/欢迎通知 |
| StatsServiceTest | Dashboard统计 |
| RedisConnectionTest | Redis连通性 |
| CreditRuleTest / ProjectTest | 规则/项目单元测试 |

---

## 十、项目启动方式

### 10.1 环境要求
- JDK 8+、Maven 3.6+
- MySQL 8.0（root/123456，无需手动建库，自动创建credit_bank）
- Redis 5.x（localhost:6379，密码123456）

### 10.2 后端启动
```bash
cd credit-bank-mvp
mvn spring-boot:run
# 或IDEA运行 CreditBankMvpApplication.main()
```
启动后访问：
- Swagger接口文档：http://localhost:8080/swagger-ui.html
- 后端根路径：http://localhost:8080/

### 10.3 前端启动
```bash
cd credit-bank-mvp/frontend
npm install      # 首次安装依赖
npm run dev      # 开发模式 → http://localhost:5173
npm run build    # 生产构建 → dist/
```

### 10.4 演示账号（密码均为123456）
| 账号 | 角色 |
|---|---|
| admin | 系统管理员 |
| instadmin | 机构管理员 |
| student | 学生 |
| expert | 专家 |

---

## 十一、项目开发历程中的关键技术难点与解决方案

| 技术难点 | 问题描述 | 解决方案 |
|---|---|---|
| **并发余额更新安全** | 多请求同时加分可能导致余额覆盖 | 实体类加@Version乐观锁，MyBatis-Plus自动拼接`WHERE version=?`+`version=version+1` |
| **流水撤销设计** | 财务账本原记录不可修改原则 | 新增REFUND类型反向流水，related_rule_id指向原流水，级联处理ATTACHMENT附加流水 |
| **签到防重** | 同一用户并发提交多次签到 | Redis+DB双重防重：Redis快速返回，DB流水count作为最终兜底 |
| **通知接收人唯一键冲突** | 批量插入时并发重复(notification_id,user_id) | 插入前先selectCount检查存在性，存在则跳过，避免DuplicateKeyException |
| **前端登录态过期** | 401响应处理不当导致死循环 | Axios响应拦截器判断非公开API时才清除localStorage+弹窗，防止死循环 |
| **Element Plus按钮间距** | flex容器中相邻按钮margin-left:12px错位 | 对相邻按钮显式设置`margin-left: 0 !important`重置 |
| **数据库升级兼容** | 老库已有数据不能DROP重跑 | 6步幂等迁移脚本+INFORMATION_SCHEMA存在性检查+PREPARE动态SQL |
| **转换申请加积分精确性** | converted_type中文可能不匹配event_code | 强制绑定credit_rule_id外键，RULE_CONVERT时覆盖前端converted_type为积分规则的event_code |
| **欢迎通知生成时机** | data.sql预置导致接收人ID错位 | 改为启动时ensureWelcomeNotificationsForAllUsers()动态按当前用户生成 |
| **冻结用户操作控制** | 前后端双重控制粒度不一 | 后端三层拦截器FreezePermissionInterceptor细粒度控制GET放行+POST白名单+前端isFrozen响应式禁用按钮 |
| **公共页面权限泄漏** | localStorage残留登录态导致公共页显示管理按钮 | PublicHome.vue显式条件渲染+ConversionRules readonly prop隐藏操作列+不调用管理接口 |
| **文件访问带不上Token** | img/window.open无法注入Authorization头 | 文件路径前缀/FILE_ACCESS_PREFIXES加入认证白名单，文件名随机串保证安全 |

---

## 十二、可扩展的未来方向

1. **分布式架构**：当前单体应用，可拆分为用户服务、积分服务、通知服务等微服务，引入Nacos注册配置中心 + Spring Cloud Gateway + OpenFeign
2. **消息队列解耦**：项目完成发通知、签到写流水等耗时操作用RocketMQ/Kafka异步化
3. **Redis深化**：排行榜用ZSet、积分规则缓存失效策略、分布式锁替代DB乐观锁
4. **数据库优化**：分库分表（transaction_log按年月分表）、慢SQL优化、读写分离
5. **AI集成**：智能推荐项目/活动、证书自动图像识别审核、学习路径规划
6. **移动端适配**：uni-app/Taro开发小程序版，支持扫码签到、证书核验
7. **容器化部署**：Docker Compose + K8s，ELK日志收集，Prometheus监控

---

## 文档用途说明

本文档已完整覆盖**终身学习学分银行积分管理系统（CreditBank MVP）**的所有技术栈、架构设计、数据库模型、功能模块、核心业务流程、工程化实践与关键技术难点。可将全文复制交付给AI，用于生成：

- **每日/每周实习日志**（按功能模块拆成每日开发任务）
- **实习周/月总结**（按技术学习、功能开发、问题解决三个维度）
- **实习鉴定个人总结**（可参考本文档架构+功能+技术难点深化论述）
- **实训报告/毕业设计说明**（系统设计章节可直接引用架构图+ER图+模块描述）
- **面试准备要点**（第十一节"关键技术难点"可直接作为项目亮点回答素材）
