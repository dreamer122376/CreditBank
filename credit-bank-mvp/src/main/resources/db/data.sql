-- 机构表初始数据（无外键依赖，最先插入）
INSERT INTO `organization` (`id`, `name`, `contact_person`, `contact_phone`, `address`, `status`, `created_at`, `updated_at`) VALUES
(1, '信息技术学院', '张三', '13800138001', '教学楼A栋', 1, NOW(), NOW()),
(2, '工程学院', '李四', '13800138002', '教学楼B栋', 1, NOW(), NOW()),
(3, '管理学院', '陈七', '13800138008', '教学楼C栋', 0, NOW(), NOW());

-- 系统用户表初始数据（依赖 organization.org_id）
-- 密码均为 123456（BCrypt加密）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role`, `org_id`, `expert_field`, `balance`, `status`, `created_at`) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '系统管理员', '13800138000', 'admin@creditbank.com', 'admin', NULL, NULL, 0, 1, NOW()),
(2, 'org_admin_1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '张三', '13800138001', 'zhangsan@org1.com', 'org_admin', 1, NULL, 5000, 1, NOW()),
(3, 'org_admin_2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '李四', '13800138002', 'lisi@org2.com', 'org_admin', 2, NULL, 8000, 1, NOW()),
(4, 'expert_1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '王五', '13800138003', 'wangwu@expert.com', 'expert', NULL, '计算机科学', 0, 1, NOW()),
(5, 'expert_2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '赵六', '13800138004', 'zhaoliu@expert.com', 'expert', NULL, '软件工程', 0, 1, NOW()),
(6, 'student_1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小明', '13800138005', 'xiaoming@student.com', 'student', 1, NULL, 500, 1, NOW()),
(7, 'student_2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小红', '13800138006', 'xiaohong@student.com', 'student', 2, NULL, 300, 1, NOW()),
(8, 'student_3', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小刚', '13800138007', 'xiaogang@student.com', 'student', 1, NULL, 800, 1, NOW());

-- 管理员角色菜单权限表初始数据（无外键依赖）
INSERT INTO `admin_role_menu` (`id`, `role_tag`, `menu_code`) VALUES
(1, 'admin', 'user_manage'),
(2, 'admin', 'org_manage'),
(3, 'admin', 'project_manage'),
(4, 'admin', 'rule_manage'),
(5, 'admin', 'campaign_manage'),
(6, 'admin', 'application_manage'),
(7, 'org_admin', 'project_create'),
(8, 'org_admin', 'project_list'),
(9, 'org_admin', 'application_review'),
(10, 'expert', 'application_review'),
(11, 'expert', 'project_list');

-- 认证标准表初始数据（无外键依赖，first_node_id 稍后由 cert_audit_flow 回填）
INSERT INTO `cert_standard` (`id`, `standard_name`, `version`, `org_id`, `target_role`, `requirement_text`, `need_manual_audit`, `first_node_id`, `is_enabled`, `created_at`) VALUES
(1, '学生初级能力认证', '1.0', NULL, 'student', '一、认证目标\n面向在校学生，验证其基础学习能力与实践参与度。\n\n二、认证要求\n1. 累计积分满 500 分\n2. 参与至少 1 个实践项目\n3. 完成基础课程学习\n\n三、考核方式\n系统自动核验积分达标情况，无需人工评审。', 0, NULL, 1, NOW()),
(2, '学生中级能力认证', '1.0', NULL, 'student', '一、认证目标\n面向优秀学生，验证其中等专业能力与项目实战能力。\n\n二、认证要求\n1. 累计积分满 1000 分\n2. 参与至少 2 个实践项目且至少 1 个获评优秀\n3. 通过技术面试\n\n三、考核方式\n机构初审 → 专家复审，两级人工审核。', 1, 101, 1, NOW()),
(3, '专家资质认证', '2.0', NULL, 'expert', '一、认证目标\n认定专家在某领域的评审资质，获得资质后可参与对应认证标准的复审工作。\n\n二、认证要求\n1. 具有相关专业背景，从事相关领域工作 3 年以上\n2. 发表过 2 篇以上相关论文或取得等效成果\n3. 通过管理员资质审核\n\n三、考核方式\n系统管理员终审。', 1, 103, 1, NOW()),
(4, '机构办学资质认证', '1.0', NULL, 'org_admin', '一、认证目标\n认定机构的办学/培训资质，获得认证后机构可在平台发布认证项目。\n\n二、认证要求\n1. 具备合法办学许可证\n2. 拥有至少 3 名持证专家\n3. 过去一年无重大违规记录\n\n三、考核方式\n系统管理员审核。', 1, 104, 1, NOW());

-- 认证审批流程节点表初始数据
-- 节点101→102：学生中级能力认证（机构管理员初审 → 专家复审 → 通过）
-- 节点103：专家资质认证（管理员终审 → 通过）
-- 节点104：机构办学资质认证（管理员终审 → 通过）
INSERT INTO `cert_audit_flow` (`id`, `cert_standard_id`, `auditor_id`, `next_node_id`, `created_at`) VALUES
(101, 2, 2, 102, NOW()),
(102, 2, 4, NULL, NOW()),
(103, 3, 1, NULL, NOW()),
(104, 4, 1, NULL, NOW());

-- 项目表初始数据（依赖 organization.org_id 和 sys_user.id）
INSERT INTO `project` (`id`, `org_id`, `expert_id`, `name`, `description`, `credit_reward`, `credit_price`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, 4, '校园APP开发项目', '开发一款面向学生的校园服务APP', 200, 0, 1, NOW(), NOW()),
(2, 1, 5, '智能教室系统', '基于物联网的智能教室管理系统', 150, 0, 1, NOW(), NOW()),
(3, 2, 4, '工程模拟平台', '土木工程模拟仿真平台开发', 250, 0, 1, NOW(), NOW()),
(4, 2, 5, '数据分析大赛', '校园数据分析竞赛项目', 300, 0, 0, NOW(), NOW()),
(5, 1, NULL, '校园安全监测', '校园安全监测系统升级', 100, 0, 2, NOW(), NOW());

-- 学生报名项目关系表初始数据（依赖 sys_user.id 和 project.id）
INSERT INTO `student_project` (`id`, `student_id`, `project_id`, `status`, `created_at`) VALUES
(1, 6, 1, '进行中', DATE_ADD(NOW(), INTERVAL -5 DAY)),
(2, 6, 2, '已报名', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(3, 7, 1, '已完成', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(4, 7, 3, '进行中', DATE_ADD(NOW(), INTERVAL -3 DAY)),
(5, 8, 1, '已完成', DATE_ADD(NOW(), INTERVAL -12 DAY)),
(6, 8, 2, '进行中', DATE_ADD(NOW(), INTERVAL -4 DAY)),
(7, 8, 3, '已报名', DATE_ADD(NOW(), INTERVAL -1 DAY));


-- 积分规则表初始数据（依赖 project.id）
INSERT INTO `credit_rule` (`id`, `event_code`, `event_name`, `credit_value`, `is_enabled`, `created_at`, `start_time`, `end_time`, `project_id`) VALUES
(1, 'COURSE_COMPLETE', '课程完成', 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL),
(2, 'PROJECT_PARTICIPATE', '校园APP开发项目参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 1),
(3, 'PROJECT_PARTICIPATE', '智能教室系统参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 2),
(4, 'PROJECT_PARTICIPATE', '工程模拟平台参与', 250, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 3),
(5, 'PROJECT_EXCELLENT', '校园APP开发优秀', 500, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 1),
(6, 'PROJECT_EXCELLENT', '数据分析大赛优秀', 600, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 4),
(7, 'ONLINE_TEST', '在线测试', 50, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL),
(8, 'ATTENDANCE', '签到打卡', 10, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL),
(9, 'SHARE_CONTENT', '分享内容', 30, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL);

-- 积分转换规则表初始数据（无外键依赖）
INSERT INTO `exchange_rule` (`id`, `item_name`, `item_icon`, `required_credit`, `stock`, `per_user_limit`, `is_enabled`, `org_id`, `created_at`) VALUES
(1, '精美笔记本', 'icon_notebook', 100, 100, 1, 1, NULL, NOW()),
(2, '课程优惠券', 'icon_coupon', 200, 50, 1, 1, NULL, NOW()),
(3, '荣誉证书', 'icon_certificate', 500, 20, 1, 1, NULL, NOW()),
(4, '书籍借阅卡', 'icon_card', 300, 80, 1, 1, 1, NOW()),
(5, '充电宝', 'icon_powerbank', 800, 30, 1, 1, 2, NOW());

-- 平台活动表初始数据（无外键依赖）
INSERT INTO `campaign` (`id`, `title`, `multiplier`, `start_time`, `end_time`, `status`, `created_at`, `description`, `cover_image`, `organizer`, `images`) VALUES
(1, '暑期学习季', 1.5, DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, NOW(), '暑期学习积分翻倍活动', 'campaign1.jpg', '信息技术学院', '["img1.jpg","img2.jpg"]'),
(2, '技能挑战赛', 2.0, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), 0, NOW(), '各类技能挑战赛活动', 'campaign2.jpg', '工程学院', '["img3.jpg"]'),
(3, '迎新活动', 1.2, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY), 2, NOW(), '新生入学迎新活动', 'campaign3.jpg', '校团委', '["img4.jpg","img5.jpg","img6.jpg"]');

-- 统一申请审批表初始数据（依赖 sys_user.id 和 organization.org_id 和 project.id）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(1, 'PROJECT_UP', 4, 2, 2, 4, '{"projectName":"数据分析大赛","description":"校园数据分析竞赛项目"}', 1, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), NOW()),
(2, 'EXCHANGE', NULL, 6, 1, NULL, '{"itemId":1,"quantity":1}', 0, NULL, DATE_ADD(NOW(), INTERVAL -2 HOUR), NOW()),
(3, 'CERT_APPLY', 2, 6, 1, 4, '{"standardId":2,"applyReason":"已完成1000积分要求"}', 2, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY), NOW()),
(4, 'PROJECT_UP', NULL, 3, 2, NULL, '{"projectName":"智能建筑项目","description":"智能建筑控制系统开发"}', 0, NULL, DATE_ADD(NOW(), INTERVAL -1 HOUR), NOW()),
(5, 'CERT_APPLY', 1, 8, 1, 5, '{"standardId":1,"applyReason":"已完成500积分要求"}', 3, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY), NOW()),
(6, 'EXCHANGE', NULL, 7, 2, NULL, '{"itemId":2,"quantity":1}', 4, '积分不足', DATE_ADD(NOW(), INTERVAL -4 DAY), NOW());

-- 交易流水表初始数据（依赖 sys_user.id 和 credit_rule.id）
-- REWARD 类型：平台通用规则只生成学生流水；机构专属规则同步生成机构管理员积分池扣减流水
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `related_rule_id`, `description`, `created_at`) VALUES
-- 12天前：通用 REWARD（student_3 在线测试 +50，初始800→850）
(1, 8, 50, 850, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -12 DAY)),
-- 10天前：通用 REWARD（student_1 完成课程 +100，初始500→600）
--         + 机构1专属 REWARD（student_3 校园APP项目 +200，850→1050；org_admin_1 扣减200，5000→4800）
(2, 6, 100, 600, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(3, 8, 200, 1050, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(4, 2, -200, 4800, 'REWARD', 2, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -10 DAY)),
-- 9天前：通用 REWARD（student_2 分享内容 +30，初始300→330）
(5, 7, 30, 330, 'REWARD', 9, '分享内容', DATE_ADD(NOW(), INTERVAL -9 DAY)),
-- 8天前：通用 REWARD（student_1 在线测试 +50，600→650）
--         + 机构1专属 REWARD（student_3 智能教室项目 +200，1050→1250；org_admin_1 扣减200，4800→4600）
(6, 6, 50, 650, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(7, 8, 200, 1250, 'REWARD', 3, '智能教室系统参与', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(8, 2, -200, 4600, 'REWARD', 3, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -8 DAY)),
-- 6天前：通用 REWARD（student_1 签到打卡 +10，650→660）
(9, 6, 10, 660, 'REWARD', 8, '签到打卡', DATE_ADD(NOW(), INTERVAL -6 DAY)),
-- 5天前：机构2专属 REWARD（student_2 工程模拟平台 +250，330→580；org_admin_2 扣减250，8000→7750）
(10, 7, 250, 580, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -5 DAY)),
(11, 3, -250, 7750, 'REWARD', 4, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -5 DAY)),
-- 4天前：机构1专属 REWARD（student_1 校园APP项目 +200，660→860；org_admin_1 扣减200，4600→4400）
(12, 6, 200, 860, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -4 DAY)),
(13, 2, -200, 4400, 'REWARD', 2, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -4 DAY)),
-- 2天前：机构1专属 REWARD（student_1 校园APP优秀 +500，860→1360；org_admin_1 扣减500，4400→3900）
(14, 6, 500, 1360, 'REWARD', 5, '校园APP开发优秀', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(15, 2, -500, 3900, 'REWARD', 5, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -2 DAY));


