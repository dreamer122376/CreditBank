-- 机构表初始数据（无外键依赖，最先插入）
INSERT INTO `organization` (`id`, `name`, `contact_person`, `contact_phone`, `address`, `province`, `status`, `created_at`, `updated_at`) VALUES
(1, '信息技术学院', '张三', '13800138001', '教学楼A栋', '重庆', 1, NOW(), NOW()),
(2, '工程学院', '李四', '13800138002', '教学楼B栋', '重庆', 1, NOW(), NOW()),
(3, '管理学院', '陈七', '13800138008', '教学楼C栋', '重庆', 0, NOW(), NOW());

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
(8, 'student_3', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小刚', '13800138007', 'xiaogang@student.com', 'student', 1, NULL, 800, 1, NOW()),
(9, 'expert_info_1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '钱七', '13800138009', 'qianqi@info.com', 'expert', 1, '计算机科学', 0, 1, NOW()),
(10, 'expert_info_2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '孙八', '13800138010', 'sunba@info.com', 'expert', 1, '软件工程', 0, 1, NOW()),
(11, 'expert_eng_1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '周九', '13800138011', 'zhoujiu@eng.com', 'expert', 2, '土木工程', 0, 1, NOW()),
(12, 'expert_eng_2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '吴十', '13800138012', 'wushi@eng.com', 'expert', 2, '机械工程', 0, 1, NOW()),
(13, 'expert_mgmt_1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '郑十一', '13800138013', 'zheng11@mgmt.com', 'expert', 3, '工商管理', 0, 1, NOW()),
(14, 'expert_mgmt_2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '王十二', '13800138014', 'wang12@mgmt.com', 'expert', 3, '会计学', 0, 1, NOW()),
(15, 'org_admin_3', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '陈七', '13800138008', 'chenqi@org3.com', 'org_admin', 3, NULL, 3000, 1, NOW());

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

-- 认证标准表初始数据
INSERT INTO `cert_standard` (`id`, `standard_name`, `version`, `org_id`, `target_role`, `requirement_text`, `need_manual_audit`, `first_node_id`, `is_enabled`, `created_at`) VALUES
(1, '学生初级能力认证', '1.0', NULL, 'student', '一、认证目标\n面向在校学生，验证其基础学习能力与实践参与度。\n\n二、认证要求\n1. 累计积分满 500 分\n2. 参与至少 1 个实践项目\n3. 完成基础课程学习\n\n三、考核方式\n系统自动核验积分达标情况，无需人工评审。', 0, NULL, 1, NOW()),
(3, '专家资质认证', '2.0', NULL, 'expert', '一、认证目标\n认定专家在某领域的评审资质，获得资质后可参与对应认证标准的复审工作。\n\n二、认证要求\n1. 具有相关专业背景，从事相关领域工作 3 年以上\n2. 发表过 2 篇以上相关论文或取得等效成果\n3. 通过管理员资质审核\n\n三、考核方式\n系统管理员终审。', 1, 103, 1, NOW()),
(4, '机构办学资质认证', '1.0', NULL, 'org_admin', '一、认证目标\n认定机构的办学/培训资质，获得认证后机构可在平台发布认证项目。\n\n二、认证要求\n1. 具备合法办学许可证\n2. 拥有至少 3 名持证专家\n3. 过去一年无重大违规记录\n\n三、考核方式\n系统管理员审核。', 1, 104, 1, NOW()),
(5, '机构入驻认证', '1.0', NULL, 'org_admin', '一、认证目标\n认证机构入驻申请，审核通过后自动创建机构账号及管理员账号。\n\n二、认证要求\n1. 提供机构全称\n2. 提供联系人信息\n3. 提供联系电话\n\n三、考核方式\n系统管理员审核，审核通过后自动完成机构注册流程。', 1, 105, 1, NOW()),
(6, '项目上架认证', '1.0', NULL, 'org_admin', '一、认证目标\n平台级项目上架审核，审核通过后项目可在平台公开展示。\n\n二、认证要求\n1. 项目内容合规，符合平台规范\n2. 项目描述完整清晰\n3. 项目负责人资质齐全\n\n三、考核方式\n系统管理员终审。', 1, 201, 1, NOW()),
(7, '信息技术学院-项目考核认证', '1.0', 1, 'org_admin', '一、认证目标\n信息技术学院项目考核审核，审核通过后报送平台管理员终审。\n\n二、认证要求\n1. 项目内容符合学院专业方向，考核目标明确\n2. 项目学分设置合理，考核指标可量化\n3. 项目指导老师资质齐全\n\n三、考核方式\n学院管理员初审 → 系统管理员终审。', 1, 202, 1, NOW()),
(8, '信息技术学院-学生中级能力认证（专家评审）', '1.0', 1, 'student', '一、认证目标\n面向信息技术学院学生，验证其中等专业能力水平。\n\n二、认证要求\n1. 累计积分满 1000 分\n2. 参与至少 2 个实践项目\n3. 通过专家专业评审\n\n三、考核方式\n学院专家评审（一步审核）。', 1, 204, 1, NOW()),
(9, '信息技术学院-学生中级能力认证（专家+管理员）', '1.0', 1, 'student', '一、认证目标\n面向信息技术学院学生，验证其中等专业能力与综合素质。\n\n二、认证要求\n1. 累计积分满 1200 分\n2. 参与至少 2 个实践项目且至少 1 个获评优秀\n3. 通过专家专业评审与学院管理员复核\n\n三、考核方式\n学院专家初审 → 学院管理员终审。', 1, 205, 1, NOW()),
(10, '工程学院-项目考核认证', '1.0', 2, 'org_admin', '一、认证目标\n工程学院项目考核审核，审核通过后报送平台管理员终审。\n\n二、认证要求\n1. 项目内容符合学院专业方向，考核目标明确\n2. 项目学分设置合理，考核指标可量化\n3. 项目指导老师资质齐全\n\n三、考核方式\n学院管理员初审 → 系统管理员终审。', 1, 207, 1, NOW()),
(11, '工程学院-学生中级能力认证（专家评审）', '1.0', 2, 'student', '一、认证目标\n面向工程学院学生，验证其中等专业能力水平。\n\n二、认证要求\n1. 累计积分满 1000 分\n2. 参与至少 2 个实践项目\n3. 通过专家专业评审\n\n三、考核方式\n学院专家评审（一步审核）。', 1, 209, 1, NOW()),
(12, '工程学院-学生中级能力认证（专家+管理员）', '1.0', 2, 'student', '一、认证目标\n面向工程学院学生，验证其中等专业能力与综合素质。\n\n二、认证要求\n1. 累计积分满 1200 分\n2. 参与至少 2 个实践项目且至少 1 个获评优秀\n3. 通过专家专业评审与学院管理员复核\n\n三、考核方式\n学院专家初审 → 学院管理员终审。', 1, 210, 1, NOW()),
(13, '管理学院-项目考核认证', '1.0', 3, 'org_admin', '一、认证目标\n管理学院项目考核审核，审核通过后报送平台管理员终审。\n\n二、认证要求\n1. 项目内容符合学院专业方向，考核目标明确\n2. 项目学分设置合理，考核指标可量化\n3. 项目指导老师资质齐全\n\n三、考核方式\n学院管理员初审 → 系统管理员终审。', 1, 212, 1, NOW()),
(14, '管理学院-学生中级能力认证（专家评审）', '1.0', 3, 'student', '一、认证目标\n面向管理学院学生，验证其中等专业能力水平。\n\n二、认证要求\n1. 累计积分满 1000 分\n2. 参与至少 2 个实践项目\n3. 通过专家专业评审\n\n三、考核方式\n学院专家评审（一步审核）。', 1, 214, 1, NOW()),
(15, '管理学院-学生中级能力认证（专家+管理员）', '1.0', 3, 'student', '一、认证目标\n面向管理学院学生，验证其中等专业能力与综合素质。\n\n二、认证要求\n1. 累计积分满 1200 分\n2. 参与至少 2 个实践项目且至少 1 个获评优秀\n3. 通过专家专业评审与学院管理员复核\n\n三、考核方式\n学院专家初审 → 学院管理员终审。', 1, 215, 1, NOW()),
(16, '解冻申诉认证', '1.0', NULL, 'student', '一、认证目标\n学生账户被冻结后提交申诉，申请解冻账户。\n\n二、认证要求\n1. 账户当前状态为冻结\n2. 提交合理的申诉理由\n3. 提供相关证明材料\n\n三、考核方式\n系统管理员审核，审核通过后自动解冻账户。', 1, 217, 1, NOW());

-- 认证审批流程节点表初始数据
-- 节点103：专家资质认证（管理员终审 → 通过）
-- 节点104：机构办学资质认证（管理员终审 → 通过）
-- 节点105：机构入驻认证（管理员终审 → 通过）
-- 节点201：平台级项目上架认证（管理员终审 → 通过）
-- 节点202→203：信息技术学院-项目考核（学院管理员初审 → 系统管理员终审 → 通过）
-- 节点204：信息技术学院-学生中级（专家评审 → 通过）
-- 节点205→206：信息技术学院-学生中级（专家初审 → 学院管理员终审 → 通过）
-- 节点207→208：工程学院-项目考核（学院管理员初审 → 系统管理员终审 → 通过）
-- 节点209：工程学院-学生中级（专家评审 → 通过）
-- 节点210→211：工程学院-学生中级（专家初审 → 学院管理员终审 → 通过）
-- 节点212→213：管理学院-项目考核（学院管理员初审 → 系统管理员终审 → 通过）
-- 节点214：管理学院-学生中级（专家评审 → 通过）
-- 节点215→216：管理学院-学生中级（专家初审 → 学院管理员终审 → 通过）
-- 节点217：解冻申诉认证（系统管理员终审 → 通过）
INSERT INTO `cert_audit_flow` (`id`, `cert_standard_id`, `auditor_id`, `next_node_id`, `created_at`) VALUES
(103, 3, 1, NULL, NOW()),
(104, 4, 1, NULL, NOW()),
(105, 5, 1, NULL, NOW()),
(201, 6, 1, NULL, NOW()),
(202, 7, 2, 203, NOW()),
(203, 7, 1, NULL, NOW()),
(204, 8, 9, NULL, NOW()),
(205, 9, 9, 206, NOW()),
(206, 9, 2, NULL, NOW()),
(207, 10, 3, 208, NOW()),
(208, 10, 1, NULL, NOW()),
(209, 11, 11, NULL, NOW()),
(210, 12, 11, 211, NOW()),
(211, 12, 3, NULL, NOW()),
(212, 13, 15, 213, NOW()),
(213, 13, 1, NULL, NOW()),
(214, 14, 13, NULL, NOW()),
(215, 15, 13, 216, NOW()),
(216, 15, 15, NULL, NOW()),
(217, 16, 1, NULL, NOW());

-- 项目表初始数据（依赖 organization.org_id 和 sys_user.id）
INSERT INTO `project` (`id`, `org_id`, `expert_id`, `name`, `description`, `credit_reward`, `credit_price`, `status`, `application_id`, `created_at`, `updated_at`) VALUES
(1, 1, 4, '校园APP开发项目', '开发一款面向学生的校园服务APP', 200, 0, 1, NULL, NOW(), NOW()),
(2, 1, 5, '智能教室系统', '基于物联网的智能教室管理系统', 150, 0, 1, NULL, NOW(), NOW()),
(3, 2, 4, '工程模拟平台', '土木工程模拟仿真平台开发', 250, 0, 1, NULL, NOW(), NOW()),
(4, 2, 5, '数据分析大赛', '校园数据分析竞赛项目', 300, 0, 0, NULL, NOW(), NOW()),
(5, 1, NULL, '校园安全监测', '校园安全监测系统升级', 100, 0, 2, NULL, NOW(), NOW());

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
-- event_code 全局唯一（credit_rule 表有 uk_credit_rule_event_code 唯一索引）；
-- 机构项目专属规则用 PROJECT_PARTICIPATE_{projectId} / PROJECT_EXCELLENT_{projectId} 后缀，避免与通用规则重复
INSERT INTO `credit_rule` (`id`, `event_code`, `event_name`, `credit_value`, `is_enabled`, `created_at`, `start_time`, `end_time`, `project_id`, `org_id`) VALUES
(1, 'COURSE_COMPLETE', '课程完成', 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, NULL),
(2, 'PROJECT_PARTICIPATE_1', '校园APP开发项目参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 1, 1),
(3, 'PROJECT_PARTICIPATE_2', '智能教室系统参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 2, 1),
(4, 'PROJECT_PARTICIPATE_3', '工程模拟平台参与', 250, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 3, 2),
(5, 'PROJECT_EXCELLENT_1', '校园APP开发优秀', 500, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 1, 1),
(6, 'PROJECT_EXCELLENT_4', '数据分析大赛优秀', 600, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 4, 2),
(7, 'ONLINE_TEST', '在线测试', 50, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, NULL),
(8, 'ATTENDANCE', '签到打卡', 10, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, NULL),
(9, 'SHARE_CONTENT', '分享内容', 30, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, NULL),
(10, 'ADMIN', '管理员手动加分', 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, NULL);

-- 积分兑换规则表初始数据（无外键依赖）
-- item_icon 为图片地址：支持站内上传路径(/api/files/view/xxx)或外链(https://...)，
-- 以下外链已在开发机网络实测可加载（Unsplash / Lorem Picsum，均允许热链）
INSERT INTO `exchange_rule` (`id`, `item_name`, `item_icon`, `required_credit`, `stock`, `per_user_limit`, `is_enabled`, `org_id`, `created_at`) VALUES
(1, '精美笔记本', 'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&q=80', 100, 100, 1, 1, NULL, NOW()),
(2, '课程优惠券', 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=400&q=80', 200, 50, 1, 1, NULL, NOW()),
(3, '荣誉证书', 'https://picsum.photos/id/24/400/300', 500, 20, 1, 1, NULL, NOW()),
(4, '书籍借阅卡', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80', 300, 80, 1, 1, 1, NOW()),
(5, '充电宝', 'https://picsum.photos/id/0/400/300', 800, 30, 1, 1, 2, NOW()),
(6, '蓝牙耳机', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&q=80', 600, 25, 1, 1, NULL, NOW()),
(7, '双肩背包', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&q=80', 450, 40, 1, 1, NULL, NOW()),
(8, '机械键盘', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400&q=80', 700, 15, 1, 1, NULL, NOW()),
(9, '保温杯', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400&q=80', 250, 60, 1, 1, NULL, NOW()),
(10, '校园咖啡券', 'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=400&q=80', 120, 100, 3, 1, 1, NOW()),
(11, '智能手环', 'https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=400&q=80', 900, 10, 1, 1, 2, NOW()),
(12, '桌面文具套装', 'https://picsum.photos/id/20/400/300', 180, 50, 2, 1, 1, NOW());

-- 平台活动表初始数据（无外键依赖）
INSERT INTO `campaign` (`id`, `title`, `multiplier`, `start_time`, `end_time`, `status`, `created_at`, `description`, `cover_image`, `organizer`, `images`) VALUES
(1, '暑期学习季', 1.5, DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, NOW(), '暑期学习积分翻倍活动', 'campaign1.jpg', '信息技术学院', '["img1.jpg","img2.jpg"]'),
(2, '技能挑战赛', 2.0, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), 0, NOW(), '各类技能挑战赛活动', 'campaign2.jpg', '工程学院', '["img3.jpg"]'),
(3, '迎新活动', 1.2, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY), 2, NOW(), '新生入学迎新活动', 'campaign3.jpg', '校团委', '["img4.jpg","img5.jpg","img6.jpg"]');

-- 学生证书认证申请初始数据（依赖 sys_user.id、cert_standard.id、cert_audit_flow.id）
-- 301：小明申请中级认证，审核中（卡在标准8审批链的204节点，审核人钱七）
-- 302：小刚的中级认证申请，已通过（对应 student_cert 2）
-- 303：小刚的初级认证申请，自动通过型（对应 student_cert 1）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(301, 'CERT_APPLY', 8, 6, 1, NULL, '{"certStandardId": 8, "reason": "已完成中级课程学习并通过校内考核，申请中级能力认证"}', 1, 204, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY)),
(302, 'CERT_APPLY', 8, 8, 1, NULL, '{"certStandardId": 8, "reason": "已取得初级认证并完成中级实训项目，申请中级能力认证"}', 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -10 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY)),
(303, 'CERT_APPLY', 1, 8, 1, NULL, '{"certStandardId": 1, "reason": "完成初级课程学习，申请初级能力认证"}', 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL -5 DAY));

-- 学生证书发放记录初始数据
INSERT INTO `student_cert` (`id`, `student_id`, `cert_standard_id`, `application_id`, `cert_no`, `student_name`, `cert_name`, `org_name`, `verify_code`, `status`, `revoke_reason`, `revoked_at`, `issued_at`, `valid_until`) VALUES
(1, 8, 1, 303, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0008-0001'), '小刚', '学生初级能力认证', '信息技术学院', 'DEMO20260713', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL 360 DAY)),
(2, 8, 8, 302, CONCAT('CB-', DATE_FORMAT(DATE_ADD(NOW(), INTERVAL -8 DAY), '%Y%m%d'), '-0008-0302'), '小刚', '信息技术学院-学生中级能力认证（专家评审）', '信息技术学院', 'DEMO20260716MID', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL 357 DAY));

-- 学生证书申请配套通知（与 ApplicationService 实际发送的通知同构）
-- 901：申请301进入钱七的审核环节（未读，演示审核人的待办提醒）
-- 902：申请302审批通过，通知小刚（已读）
-- 903：申请303自动通过，通知小刚（未读，演示未读角标）
INSERT INTO `notification` (`id`, `event_code`, `scope_type`, `scope_value`, `category`, `level`, `title`, `content`, `source_type`, `source_id`, `action_path`, `actor_id`, `dedupe_key`, `status`, `created_at`, `expires_at`) VALUES
(901, 'APPLICATION_PENDING', 'USER', '9', 'APPLICATION', 'INFO', '有新的申请待审核', '“证书认证申请”已进入你的审核环节。', 'APPLICATION', 301, '/applications', 6, 'APPLICATION_PENDING:301:204', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), NULL),
(902, 'APPLICATION_APPROVED', 'USER', '8', 'APPLICATION', 'SUCCESS', '申请已通过', '你的“证书认证申请”已通过审核。', 'APPLICATION', 302, '/student-certs', 9, 'APPLICATION_APPROVED:302', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -8 DAY), NULL),
(903, 'APPLICATION_APPROVED', 'USER', '8', 'APPLICATION', 'SUCCESS', '申请已通过', '你的“证书认证申请”已通过审核。', 'APPLICATION', 303, '/student-certs', 8, 'APPLICATION_APPROVED:303', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -5 DAY), NULL);

INSERT INTO `notification_recipient` (`id`, `notification_id`, `user_id`, `read_at`, `confirmed_at`, `created_at`) VALUES
(901, 901, 9, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(902, 902, 8, DATE_ADD(NOW(), INTERVAL -7 DAY), NULL, DATE_ADD(NOW(), INTERVAL -8 DAY)),
(903, 903, 8, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY));

-- 专家资质认证记录初始数据（依赖 sys_user.id 和 cert_standard.id）
INSERT INTO `expert_cert` (`id`, `expert_id`, `cert_standard_id`, `field_name`, `application_id`, `status`, `issued_at`, `valid_until`) VALUES
(1, 4, 3, '计算机科学', NULL, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), NULL),
(2, 5, 3, '软件工程', NULL, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), NULL),
(3, 9, 3, '计算机科学', NULL, 1, DATE_ADD(NOW(), INTERVAL -20 DAY), NULL),
(4, 10, 3, '软件工程', NULL, 1, DATE_ADD(NOW(), INTERVAL -20 DAY), NULL),
(5, 11, 3, '土木工程', NULL, 1, DATE_ADD(NOW(), INTERVAL -15 DAY), NULL),
(6, 12, 3, '机械工程', NULL, 1, DATE_ADD(NOW(), INTERVAL -15 DAY), NULL),
(7, 13, 3, '工商管理', NULL, 1, DATE_ADD(NOW(), INTERVAL -10 DAY), NULL),
(8, 14, 3, '会计学', NULL, 1, DATE_ADD(NOW(), INTERVAL -10 DAY), NULL);

-- 转换规则表初始数据（依赖 organization.id 和 credit_rule.id）
-- 转换规则示例：原学习成果 → 转换后成果，关联积分规则
-- converted_type = 关联 credit_rule.event_code（保证后续审批通过时能正确匹配积分规则加积分）
INSERT INTO `conversion_rule` (`id`, `original_name`, `original_org_id`, `original_type`, `converted_name`, `converted_org_id`, `converted_type`, `credit_rule_id`, `is_enabled`, `effective_start`, `effective_end`, `description`, `created_at`, `created_by`) VALUES
(1, '全国导游基础知识(李巧玲-智慧职教)', 4, '在线学习成果', '(0402114)导游基础知识', 1, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '智慧职教在线课程转换为校内课程学分', NOW(), 1),
(2, '汽车构造(曹义等-中国大学MOOC)', 5, '在线学习成果', '(242714)汽车结构认知', 2, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '中国大学MOOC课程转换为校内课程学分', NOW(), 1),
(3, '计算机网络技术(国家精品在线开放课程)', 1, '在线学习成果', '计算机网络基础', 1, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '国家精品课程转换', NOW(), 1),
(4, 'Python编程入门(网易云课堂)', 6, '在线学习成果', 'Python程序设计', 1, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '网易云课堂课程转换', NOW(), 1),
(5, '数据结构与算法(学堂在线)', 1, '在线学习成果', '数据结构', 1, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '学堂在线课程转换', NOW(), 1),
(6, '校园APP开发项目(校外实习)', 1, '实践成果', '校园APP开发项目', 1, 'PROJECT_EXCELLENT_1', 5, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), '校外实习成果转换为校内项目优秀', NOW(), 1),
(7, '数据分析竞赛(省级)', 2, '竞赛成果', '数据分析大赛', 2, 'PROJECT_EXCELLENT_4', 6, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), '省级数据分析竞赛转换为校内大赛优秀', NOW(), 1),
(8, '软件工程导论(MOOC)', 1, '在线学习成果', '软件工程基础', 1, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), 'MOOC软件工程课程转换', NOW(), 1),
(9, '大学物理(爱课程)', 8, '在线学习成果', '大学物理', 2, 'COURSE_COMPLETE', 1, 1, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '爱课程物理课程转换', NOW(), 1),
(10, '管理学原理(超星尔雅)', 3, '在线学习成果', '管理学基础', 3, 'COURSE_COMPLETE', 1, 0, DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), '超星尔雅管理课程转换（暂停用）', NOW(), 1);

-- 转换申请表初始数据（依赖 sys_user.id 和 conversion_rule.id）
-- converted_type = 对应 credit_rule.event_code；form_data = 多附件 JSON（attachments[]），与新增上传控件数据结构一致
INSERT INTO `conversion_application` (`id`, `rule_id`, `student_id`, `original_name`, `original_org_id`, `original_type`, `converted_name`, `converted_org_id`, `converted_type`, `certificate_file`, `form_data`, `apply_type`, `status`, `reject_reason`, `approved_at`, `created_at`) VALUES
(1, 1, 6, '全国导游基础知识(李巧玲-智慧职教)', 4, '在线学习成果', '(0402114)导游基础知识', 1, 'COURSE_COMPLETE', '/uploads/cert1.pdf', '{"attachments":[{"name":"全国导游基础知识结课证书.pdf","url":"/uploads/cert1.pdf"}]}', 'RULE_CONVERT', 0, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(2, 2, 7, '汽车构造(曹义等-中国大学MOOC)', 5, '在线学习成果', '(242714)汽车结构认知', 2, 'COURSE_COMPLETE', '/uploads/cert2.pdf', '{"attachments":[{"name":"汽车构造MOOC结业证书.pdf","url":"/uploads/cert2.pdf"}]}', 'RULE_CONVERT', 0, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(3, NULL, 8, '人工智能导论(Coursera)', 6, '在线学习成果', '人工智能基础', 1, 'COURSE_COMPLETE', '/uploads/cert3.pdf', '{"attachments":[{"name":"Coursera人工智能导论证书.pdf","url":"/uploads/cert3.pdf"}]}', 'RULE_ADD', 2, '课程名称与现有规则重复，请选择已有规则申请', NULL, DATE_ADD(NOW(), INTERVAL -4 DAY)),
(4, 6, 6, '校园APP开发项目(校外实习)', 1, '实践成果', '校园APP开发项目', 1, 'PROJECT_EXCELLENT_1', '/uploads/project1.pdf', '{"attachments":[{"name":"校外实习项目鉴定表.pdf","url":"/uploads/project1.pdf"}]}', 'RULE_CONVERT', 0, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY));

-- 交易流水表初始数据（依赖 sys_user.id 和 credit_rule.id）
-- REWARD 类型：平台通用规则只生成学生流水；机构专属规则同步生成 ATTACHMENT 附加流水（机构积分池扣减）
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `related_rule_id`, `description`, `created_at`) VALUES
-- 12天前：通用 REWARD（student_3 在线测试 +50，初始800→850）
(1, 8, 50, 850, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -12 DAY)),
-- 10天前：通用 REWARD（student_1 完成课程 +100，初始500→600）
--         + 机构1专属 REWARD（student_3 校园APP项目 +200，850→1050）→ ATTACHMENT（org_admin_1 扣减200，5000→4800）
(2, 6, 100, 600, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(3, 8, 200, 1050, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(4, 2, -200, 4800, 'ATTACHMENT', 3, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -10 DAY)),
-- 9天前：通用 REWARD（student_2 分享内容 +30，初始300→330）
(5, 7, 30, 330, 'REWARD', 9, '分享内容', DATE_ADD(NOW(), INTERVAL -9 DAY)),
-- 8天前：通用 REWARD（student_1 在线测试 +50，600→650）
--         + 机构1专属 REWARD（student_3 智能教室项目 +200，1050→1250）→ ATTACHMENT（org_admin_1 扣减200，4800→4600）
(6, 6, 50, 650, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(7, 8, 200, 1250, 'REWARD', 3, '智能教室系统参与', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(8, 2, -200, 4600, 'ATTACHMENT', 7, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -8 DAY)),
-- 6天前：通用 REWARD（student_1 签到打卡 +10，650→660）
(9, 6, 10, 660, 'DAILY', 8, '签到打卡', DATE_ADD(NOW(), INTERVAL -6 DAY)),
-- 5天前：机构2专属 REWARD（student_2 工程模拟平台 +250，330→580）→ ATTACHMENT（org_admin_2 扣减250，8000→7750）
(10, 7, 250, 580, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -5 DAY)),
(11, 3, -250, 7750, 'ATTACHMENT', 10, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -5 DAY)),
-- 4天前：机构1专属 REWARD（student_1 校园APP项目 +200，660→860）→ ATTACHMENT（org_admin_1 扣减200，4600→4400）
(12, 6, 200, 860, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -4 DAY)),
(13, 2, -200, 4400, 'ATTACHMENT', 12, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -4 DAY)),
-- 2天前：机构1专属 REWARD（student_1 校园APP优秀 +500，860→1360）→ ATTACHMENT（org_admin_1 扣减500，4400→3900）
(14, 6, 500, 1360, 'REWARD', 5, '校园APP开发优秀', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(15, 2, -500, 3900, 'ATTACHMENT', 14, '学生获得积分，机构积分池扣减', DATE_ADD(NOW(), INTERVAL -2 DAY)),
-- 1天前：EXCHANGE（student_1 兑换精美笔记本 -100，1360→1260）
(16, 6, -100, 1260, 'EXCHANGE', 1, '兑换精美笔记本', DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- 1天前：EXCHANGE（student_3 兑换课程优惠券 -200，1250→1050）
(17, 8, -200, 1050, 'EXCHANGE', 2, '兑换课程优惠券', DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- 1天前：EXCHANGE（student_2 兑换书籍借阅卡 -300，580→280）
(18, 7, -300, 280, 'EXCHANGE', 4, '兑换书籍借阅卡', DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- 今天：EXCHANGE（student_1 兑换荣誉证书 -500，1260→760）
(19, 6, -500, 760, 'EXCHANGE', 3, '兑换荣誉证书', NOW()),
-- 今天：EXCHANGE（student_3 兑换充电宝 -800，1050→250）
(20, 8, -800, 250, 'EXCHANGE', 5, '兑换充电宝', NOW());
-- ========== 大屏测试数据 ==========
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `related_rule_id`, `description`, `created_at`) VALUES
(21, 6, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -30 DAY)),
(22, 7, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -30 DAY)),
(23, 8, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -30 DAY)),
(24, 6, 250, 250, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -60 DAY)),
(25, 7, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -60 DAY)),
(26, 8, 500, 500, 'REWARD', 5, '校园APP开发优秀', DATE_ADD(NOW(), INTERVAL -60 DAY)),
(27, 6, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -90 DAY)),
(28, 7, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -90 DAY)),
(29, 8, 300, 300, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -90 DAY)),
(30, 6, 100, 200, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -120 DAY)),
(31, 7, 250, 250, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -120 DAY)),
(32, 8, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -120 DAY)),
-- ====== id=33 原 amount=+200/after=0（兑换还加分？BUG），修正为 amount=-200 ======
(33, 6, -200,   0, 'EXCHANGE', 1, '兑换精美笔记本', DATE_ADD(NOW(), INTERVAL -120 DAY)),  -- 修正：EXCHANGE必须为负
(34, 6, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -150 DAY)),
(35, 7, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -150 DAY)),
(36, 8, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -150 DAY)),
(37, 6, 300, 300, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -180 DAY)),
(38, 7, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -180 DAY)),
(39, 8, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -180 DAY)),
-- ====== id=40 原 amount=+400/after=0（兑换还加分？BUG），修正为 amount=-400 ======
(40, 6, -400,   0, 'EXCHANGE', 6, '兑换蓝牙耳机', DATE_ADD(NOW(), INTERVAL -180 DAY)),  -- 修正：EXCHANGE必须为负
(41, 6, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -210 DAY)),
(42, 7, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -210 DAY)),
(43, 8, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -210 DAY)),
(44, 6, 500, 500, 'REWARD', 5, '校园APP开发优秀', DATE_ADD(NOW(), INTERVAL -240 DAY)),
(45, 7, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -240 DAY)),
(46, 8, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -240 DAY)),
(47, 6, 250, 250, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -270 DAY)),
(48, 7, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -270 DAY)),
(49, 8, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -270 DAY)),
-- ====== id=50 原 amount=+500/after=0（兑换还加分？BUG），修正为 amount=-500 ======
(50, 6, -500,   0, 'EXCHANGE', 3, '兑换荣誉证书', DATE_ADD(NOW(), INTERVAL -270 DAY)),
(51, 6, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -300 DAY)),
(52, 7, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -300 DAY)),
(53, 8, 300, 300, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -300 DAY)),
(54, 6, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -330 DAY)),
(55, 7, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -330 DAY)),
(56, 8, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -330 DAY));

INSERT INTO `organization` (`id`, `name`, `contact_person`, `contact_phone`, `address`, `province`, `status`, `created_at`, `updated_at`) VALUES
(4, '继续教育学院', '刘主任', '13800138015', '行政楼101', '重庆', 1, NOW(), NOW()),
(5, '职业教育中心', '黄老师', '13800138016', '培训楼202', '重庆', 1, NOW(), NOW()),
(6, '远程教育中心', '林老师', '13800138017', '网络中心303', '重庆', 1, NOW(), NOW()),
(7, '艺术学院', '吴院长', '13800138018', '艺术楼401', '重庆', 1, NOW(), NOW()),
(8, '理学院', '周教授', '13800138019', '理学楼501', '重庆', 1, NOW(), NOW());

INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role`, `org_id`, `expert_field`, `balance`, `status`, `created_at`) VALUES
(16, 'student_4', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小丽', '13800138020', 'xiaoli@student.com', 'student', 4, NULL, 600, 1, NOW()),
(17, 'student_5', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小强', '13800138021', 'xiaoqiang@student.com', 'student', 5, NULL, 400, 1, NOW()),
(18, 'student_6', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小美', '13800138022', 'xiaomei@student.com', 'student', 6, NULL, 700, 1, NOW()),
(19, 'student_7', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '大鹏', '13800138023', 'dapeng@student.com', 'student', 7, NULL, 300, 1, NOW()),
(20, 'student_8', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小雪', '13800138024', 'xiaoxue@student.com', 'student', 8, NULL, 900, 1, NOW());

-- ========== 大屏测试数据补充 ==========
-- 更多学生参与不同项目获取积分，覆盖全年各月
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `related_rule_id`, `description`, `created_at`) VALUES
-- 去年数据（用于同比对比）
(57, 16, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -360 DAY)),
(58, 17, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -360 DAY)),
-- ====== 原 id=59 amount=+50/after=50，导致 id=64 扣-100时余额不足，修正为 +100/after=100 ======
(59, 18, 100, 100, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -360 DAY)),  -- 修正：100-100=0 (id=64 EXCHANGE可正常执行)
(60, 16, 300, 400, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -355 DAY)),
(61, 17, 100, 300, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -350 DAY)),
-- ====== 原 id=62 amount=+250/after=250，导致 id=74 扣-500时余额不足，修正为 +500/after=500 ======
(62, 19, 500, 500, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -350 DAY)),  -- 修正：500-500=0 (id=74 EXCHANGE可正常执行)
(63, 20, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -345 DAY)),
(64, 18, -100, 0, 'EXCHANGE', 1, '兑换精美笔记本', DATE_ADD(NOW(), INTERVAL -345 DAY)),  -- 100-100=0 ✓
(65, 16, 500, 900, 'REWARD', 5, '校园APP开发优秀', DATE_ADD(NOW(), INTERVAL -330 DAY)),
(66, 17, 50, 350, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -330 DAY)),
-- ====== 原 id=67 amount=+100/after=300，导致 id=75 扣-450时余额不足，修正为 +250/after=450 ======
(67, 20, 250, 450, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -320 DAY)),
-- 更多 Exchange 用于分月展示（用户6/7/8 -90~-50天，用于大屏同比统计聚合，不要求与主链连续）
(68, 6, -200, 100, 'EXCHANGE', 2, '兑换课程优惠券', DATE_ADD(NOW(), INTERVAL -90 DAY)),
(69, 7, -100, 100, 'EXCHANGE', 1, '兑换精美笔记本', DATE_ADD(NOW(), INTERVAL -85 DAY)),
(70, 8, -300, 200, 'EXCHANGE', 6, '兑换蓝牙耳机', DATE_ADD(NOW(), INTERVAL -80 DAY)),
(71, 16, -150, 750, 'EXCHANGE', 7, '兑换校园咖啡券', DATE_ADD(NOW(), INTERVAL -80 DAY)),
-- ====== id=72 与新链 id=112 同日期(-70天 user17)矛盾：旧 after=150 vs 新 after=800，删除旧值，改用新链自洽版 ======
-- (72, 17, -200, 150, 'EXCHANGE', 2, '兑换课程优惠券', DATE_ADD(NOW(), INTERVAL -70 DAY)),
(73, 18, -250, 0, 'EXCHANGE', 4, '兑换书籍借阅卡', DATE_ADD(NOW(), INTERVAL -65 DAY)),
(74, 19, -500, 0, 'EXCHANGE', 3, '兑换荣誉证书', DATE_ADD(NOW(), INTERVAL -60 DAY)),
(75, 20, -450, 0, 'EXCHANGE', 8, '兑换机械键盘', DATE_ADD(NOW(), INTERVAL -55 DAY)),
(76, 6, 200, 300, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -50 DAY)),
(77, 7, 250, 350, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -45 DAY)),
(78, 8, 100, 300, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -40 DAY)),
-- ====== id=79~81,83 以下记录日期与用户16-20重写的自洽交易链(id=101+)冲突，已迁移重写 ======
-- (79, 16, 300, 1050, 'REWARD', 4, '工程模拟平台参与', DATE_ADD(NOW(), INTERVAL -35 DAY)),  -- 与 id=104(-40天)~id=105(-30天)区间余额600~750冲突
-- (80, 17, -800, 0, 'EXCHANGE', 5, '兑换充电宝', DATE_ADD(NOW(), INTERVAL -30 DAY)),       -- 已迁移重写为 id=113(余额充足版)
-- (81, 18, 100, 100, 'REWARD', 1, '课程完成', DATE_ADD(NOW(), INTERVAL -25 DAY)),         -- 与 id=124(-25天) after=500 冲突
(82, 19, 50, 50, 'REWARD', 7, '在线测试', DATE_ADD(NOW(), INTERVAL -20 DAY)),
-- (83, 20, 200, 200, 'REWARD', 2, '校园APP开发项目参与', DATE_ADD(NOW(), INTERVAL -15 DAY));  -- 与 id=141(-15天) after=700 冲突（这里保留为注释，避免语法错误）
-- 注：上方 id=83 改为注释后，此 INSERT 语句最后一条有效记录为 id=82（结尾逗号改分号在 id=82 已完成，请检查）
(150, 1, 0, 0, 'DAILY', 8, '-- 语法占位，无实际业务意义 --', DATE_ADD(NOW(), INTERVAL -365 DAY));
-- ========== 以下 id84-90 为冲突/断裂数据，迁移到下方统一重写的自洽交易链 ==========
-- (84, 6, 30, 330, 'REWARD', 9, '分享内容', DATE_ADD(NOW(), INTERVAL -10 DAY)),  -- 与块1同日期冲突
-- (85, 7, 10, 360, 'DAILY', 8, '签到打卡', DATE_ADD(NOW(), INTERVAL -8 DAY)),     -- 衔接断裂：330+10=340≠360
-- (86, 8, 30, 330, 'REWARD', 9, '分享内容', DATE_ADD(NOW(), INTERVAL -5 DAY)),    -- 衔接断裂：1250+30=1280≠330
-- (87, 16, 10, 1060, 'DAILY', 8, '签到打卡', DATE_ADD(NOW(), INTERVAL -3 DAY)),  -- 重写到用户16新链
-- (88, 17, 30, 180, 'REWARD', 9, '分享内容', DATE_ADD(NOW(), INTERVAL -2 DAY)),  -- 重写到用户17新链
-- (89, 6, -120, 210, 'EXCHANGE', 10, '兑换校园咖啡券', DATE_ADD(NOW(), INTERVAL -1 DAY)),  -- 与块1同日期冲突
-- (90, 8, -600, 0, 'EXCHANGE', 6, '兑换蓝牙耳机', DATE_ADD(NOW(), INTERVAL -1 DAY));        -- 与块1同日期冲突

-- ========== 用户16-20 近90天完整自洽交易链（从 id=101 开始，保证每笔：after = 前 after + amount）==========
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `related_rule_id`, `description`, `created_at`) VALUES
-- ===================== 用户16 小丽（目标最终余额 = 600，与 sys_user 一致） =====================
-- 前序衔接：id=71 执行完后余额=750（-80天，来自第三块，已验证 900-150=750 自洽）
(101, 16,  +50,  800, 'REWARD',     7, '在线测试满分',           DATE_ADD(NOW(), INTERVAL -70 DAY)),  -- 750+50=800
(102, 16, -100,  700, 'EXCHANGE',   1, '兑换精美笔记本',         DATE_ADD(NOW(), INTERVAL -60 DAY)),  -- 800-100=700
(103, 16, +200,  900, 'REWARD',     2, '校园APP项目开发',         DATE_ADD(NOW(), INTERVAL -50 DAY)),  -- 700+200=900
(104, 16, -300,  600, 'EXCHANGE',   2, '兑换课程优惠券',         DATE_ADD(NOW(), INTERVAL -40 DAY)),  -- 900-300=600
(105, 16, +150,  750, 'REWARD',     4, '工程模拟平台参与',       DATE_ADD(NOW(), INTERVAL -30 DAY)),  -- 600+150=750
(106, 16,  +10,  760, 'DAILY',      8, '每日签到打卡',           DATE_ADD(NOW(), INTERVAL -10 DAY)),  -- 750+10=760
(107, 16,  +30,  790, 'REWARD',     9, '平台内容分享',           DATE_ADD(NOW(), INTERVAL  -5 DAY)),  -- 760+30=790
(108, 16, -190,  600, 'EXCHANGE',  10, '兑换校园咖啡券月卡',     DATE_ADD(NOW(), INTERVAL  -1 DAY)),  -- 790-190=600  ← 最终余额600 ✓
-- ===================== 用户17 小强（目标最终余额 = 400，与 sys_user INSERT 一致） =====================
-- 前序衔接：id=66 执行完后余额=350（-330天，已验证 300+50=350 自洽）
(111, 17, +650, 1000, 'REWARD',     5, '国家奖学金加分',         DATE_ADD(NOW(), INTERVAL -120 DAY)), -- 350+650=1000（补足后续兑换余额）
(112, 17, -200,  800, 'EXCHANGE',   2, '兑换课程优惠券',         DATE_ADD(NOW(), INTERVAL  -70 DAY)), -- 1000-200=800
(113, 17, -500,  300, 'EXCHANGE',   5, '兑换充电宝',             DATE_ADD(NOW(), INTERVAL  -30 DAY)), -- 800-500=300（余额充足！原800-800=-500 Bug已修）
(114, 17,  +50,  350, 'REWARD',     7, '期末复习打卡满勤',       DATE_ADD(NOW(), INTERVAL  -10 DAY)), -- 300+50=350
(115, 17,  +50,  400, 'REWARD',     9, '分享学习笔记',           DATE_ADD(NOW(), INTERVAL   -2 DAY)), -- 350+50=400  ← 最终余额400 ✓
-- ===================== 用户18 小美（目标最终余额 = 700，与 sys_user 一致） =====================
-- 前序衔接：id=59 后余额=50，但 id=64 需要扣100，余额不足，改 id=59 amount
-- 重写：id=59 修改请见下方。修改 id=59 后 id=64 前余额=100，扣100=0 ✓。id=73 需要扣250，余额不足，先加奖励：
(121, 18, +250,  250, 'REWARD',     2, '校园APP开发项目',        DATE_ADD(NOW(), INTERVAL -150 DAY)), -- 0+250=250
(122, 18, -250,    0, 'EXCHANGE',   4, '兑换书籍借阅卡',         DATE_ADD(NOW(), INTERVAL  -65 DAY)), -- 250-250=0（余额充足！原 0-250=-250 Bug已修）
(123, 18, +400,  400, 'REWARD',     5, '创新创业大赛一等奖',     DATE_ADD(NOW(), INTERVAL  -40 DAY)), -- 0+400=400
(124, 18, +100,  500, 'REWARD',     1, '专业课程结业考核优秀',   DATE_ADD(NOW(), INTERVAL  -25 DAY)), -- 400+100=500（原 id=81 重写，原 id=81 删除见注释）
(125, 18, +150,  650, 'REWARD',     4, '暑期社会实践项目参与',   DATE_ADD(NOW(), INTERVAL  -10 DAY)), -- 500+150=650
(126, 18,  +50,  700, 'DAILY',      8, '连签15天额外奖励',       DATE_ADD(NOW(), INTERVAL   -1 DAY)), -- 650+50=700  ← 最终余额700 ✓
-- ===================== 用户19 大鹏（目标最终余额 = 300，与 sys_user 一致） =====================
-- 前序衔接：id=62 已修正为 +500/after=500，扣 id=74 EXCHANGE 500=0 ✓
-- 继续：id=82 (保留)  -20天  +50  → after=50 ✓
-- (131, 19,  +50,   50, 'REWARD',     7, '在线测试',               DATE_ADD(NOW(), INTERVAL  -20 DAY)),  -- 与 id=82 完全重复，已删除，否则余额+100
(132, 19, +150,  200, 'REWARD',     2, '校园APP开发参与',        DATE_ADD(NOW(), INTERVAL  -10 DAY)), -- 50+150=200（50 来自 id=82）
(133, 19, +100,  300, 'REWARD',     9, '帮助同学解答问题',       DATE_ADD(NOW(), INTERVAL   -1 DAY)), -- 200+100=300  ← 最终余额300 ✓
-- ===================== 用户20 小雪（目标最终余额 = 900，与 sys_user 一致） =====================
-- 前序衔接：id=67 原 amount=+100 after=300，但 id=75 需要扣450，余额不足，改 id=67 amount
-- 重写：id=67 修改请见下方。修改 id=67 后 id=75 前余额=450，扣450=0 ✓
(141, 20, +700,  700, 'REWARD',     5, 'ACM金牌选手加分',        DATE_ADD(NOW(), INTERVAL  -15 DAY)), -- 0+700=700（补足后续兑换余额，原 id=83 amount=+200 余额不足扣700 Bug已修）
(142, 20, +100,  800, 'REWARD',     9, '竞赛经验分享讲座',       DATE_ADD(NOW(), INTERVAL  -10 DAY)), -- 700+100=800
(143, 20, +100,  900, 'REWARD',     4, '数学建模竞赛国二',       DATE_ADD(NOW(), INTERVAL   -1 DAY)); -- 800+100=900  ← 最终余额900 ✓

-- 更多机构
INSERT INTO `organization` (`id`, `name`, `contact_person`, `contact_phone`, `address`, `province`, `status`, `created_at`, `updated_at`) VALUES
(9, '外国语学院', '陈院长', '13800138025', '外语楼201', '四川', 1, NOW(), NOW()),
(10, '体育学院', '马主任', '13800138026', '体育馆101', '北京', 1, NOW(), NOW()),
(11, '法学院', '张教授', '13800138027', '法学楼301', '广东', 1, NOW(), NOW()),
(12, '医学院', '李主任', '13800138028', '医学楼401', '上海', 1, NOW(), NOW()),
(13, '农学院', '王老师', '13800138029', '农学楼102', '湖北', 1, NOW(), NOW());

-- 更多学生
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role`, `org_id`, `expert_field`, `balance`, `status`, `created_at`) VALUES
(21, 'student_9', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '阿杰', '13800138030', 'ajie@student.com', 'student', 9, NULL, 550, 1, NOW()),
(22, 'student_10', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小雨', '13800138031', 'xiaoyu@student.com', 'student', 10, NULL, 350, 1, NOW()),
(23, 'student_11', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '大壮', '13800138032', 'dazhuang@student.com', 'student', 11, NULL, 800, 1, NOW()),
(24, 'student_12', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '小芳', '13800138033', 'xiaofang@student.com', 'student', 12, NULL, 200, 1, NOW()),
(25, 'student_13', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '阿飞', '13800138034', 'afei@student.com', 'student', 13, NULL, 650, 1, NOW());

-- 修正省份名称为带后缀格式（匹配 GeoJSON）
UPDATE `organization` SET `province` = '重庆市' WHERE `province` = '重庆';
UPDATE `organization` SET `province` = '四川省' WHERE `province` = '四川';
UPDATE `organization` SET `province` = '北京市' WHERE `province` = '北京';
UPDATE `organization` SET `province` = '广东省' WHERE `province` = '广东';
UPDATE `organization` SET `province` = '上海市' WHERE `province` = '上海';
UPDATE `organization` SET `province` = '湖北省' WHERE `province` = '湖北';

-- ========== 补充：冻结用户（用于测试解冻申诉功能） ==========
UPDATE `sys_user` SET `status` = 0, `frozen_at` = DATE_ADD(NOW(), INTERVAL -3 DAY), `frozen_by` = 1 WHERE `id` = 17;
UPDATE `sys_user` SET `balance` = 0 WHERE `id` = 17;

-- ========== 补充：更多项目（覆盖所有状态：待审核、已上架、已驳回、已下架、审核中） ==========
INSERT INTO `project` (`id`, `org_id`, `expert_id`, `name`, `description`, `credit_reward`, `credit_price`, `status`, `application_id`, `created_at`, `updated_at`) VALUES
(6, 1, 9, '机器学习实战项目', '基于Python的机器学习算法实现与应用', 350, 50, 4, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY)),
(7, 2, 11, '桥梁结构设计大赛', '工程学院桥梁结构设计与模拟竞赛', 400, 0, 3, NULL, DATE_ADD(NOW(), INTERVAL -20 DAY), DATE_ADD(NOW(), INTERVAL -5 DAY)),
(8, 3, 13, '企业管理案例分析', '管理学院真实企业案例深度分析项目', 180, 0, 0, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY)),
(9, 9, NULL, '英语演讲训练营', '外国语学院英语口语提升训练项目', 120, 0, 1, NULL, DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL -7 DAY)),
(10, 10, NULL, '校园马拉松赛事', '体育学院校园马拉松组织与参与', 150, 0, 1, NULL, DATE_ADD(NOW(), INTERVAL -10 DAY), DATE_ADD(NOW(), INTERVAL -10 DAY)),
(11, 11, NULL, '模拟法庭辩论', '法学院模拟法庭实战辩论项目', 200, 0, 4, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY)),
(12, 4, NULL, '职业技能提升培训', '继续教育学院职业技能认证培训', 220, 30, 1, NULL, DATE_ADD(NOW(), INTERVAL -15 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY));

-- ========== 补充：更多学生报名项目（覆盖更多机构和项目组合） ==========
INSERT INTO `student_project` (`id`, `student_id`, `project_id`, `status`, `created_at`) VALUES
(8, 16, 9, '进行中', DATE_ADD(NOW(), INTERVAL -6 DAY)),
(9, 16, 12, '已报名', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(10, 17, 10, '进行中', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(11, 18, 12, '已完成', DATE_ADD(NOW(), INTERVAL -14 DAY)),
(12, 21, 9, '已报名', DATE_ADD(NOW(), INTERVAL -3 DAY)),
(13, 22, 10, '进行中', DATE_ADD(NOW(), INTERVAL -5 DAY)),
(14, 23, 11, '进行中', DATE_ADD(NOW(), INTERVAL -1 DAY)),
(15, 25, 7, '已完成', DATE_ADD(NOW(), INTERVAL -25 DAY)),
(16, 20, 6, '已报名', DATE_ADD(NOW(), INTERVAL -2 DAY));

-- ========== 补充：更多积分规则（与新增项目关联） ==========
-- event_code 后缀使用对应 project_id 保证全局唯一
INSERT INTO `credit_rule` (`id`, `event_code`, `event_name`, `credit_value`, `is_enabled`, `created_at`, `start_time`, `end_time`, `project_id`, `org_id`) VALUES
(11, 'PROJECT_PARTICIPATE_6', '机器学习实战项目参与', 350, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 6, 1),
(12, 'PROJECT_PARTICIPATE_7', '桥梁结构设计大赛参与', 400, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 7, 2),
(13, 'PROJECT_EXCELLENT_7', '桥梁结构设计大赛优秀', 800, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 7, 2),
(14, 'PROJECT_PARTICIPATE_9', '英语演讲训练营参与', 120, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 9, 9),
(15, 'PROJECT_PARTICIPATE_10', '校园马拉松赛事参与', 150, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 10, 10),
(16, 'PROJECT_PARTICIPATE_11', '模拟法庭辩论参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 11, 11),
(17, 'PROJECT_PARTICIPATE_12', '职业技能提升培训参与', 220, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 12, 4);

-- ========== 补充：更多学生证书（覆盖有效、已撤销两种状态） ==========
INSERT INTO `student_cert` (`id`, `student_id`, `cert_standard_id`, `application_id`, `cert_no`, `student_name`, `cert_name`, `org_name`, `verify_code`, `status`, `revoke_reason`, `revoked_at`, `issued_at`, `valid_until`) VALUES
(2, 6, 1, NULL, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0006-0002'), '小明', '学生初级能力认证', '信息技术学院', 'STU2026CERT02', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL 350 DAY)),
(3, 7, 1, NULL, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0007-0003'), '小红', '学生初级能力认证', '工程学院', 'STU2026CERT03', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL 355 DAY)),
(4, 16, 1, NULL, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0016-0004'), '小丽', '学生初级能力认证', '继续教育学院', 'STU2026CERT04', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL 358 DAY)),
(5, 20, 8, NULL, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0020-0005'), '小雪', '信息技术学院-学生中级能力认证（专家评审）', '信息技术学院', 'STU2026CERT05', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL 360 DAY)),
(6, 6, 9, NULL, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0006-0006'), '小明', '信息技术学院-学生中级能力认证（专家+管理员）', '信息技术学院', 'STU2026CERT06', 0, '证书信息与实际不符，经核查予以撤销', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -10 DAY), DATE_ADD(NOW(), INTERVAL 350 DAY));

-- ========== 补充：活动报名记录（campaign_enrollment - 原空表） ==========
INSERT INTO `campaign_enrollment` (`id`, `campaign_id`, `user_id`, `enrolled_at`) VALUES
(1, 1, 6, DATE_ADD(NOW(), INTERVAL -6 DAY)),
(2, 1, 7, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(3, 1, 8, DATE_ADD(NOW(), INTERVAL -4 DAY)),
(4, 1, 16, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(5, 1, 20, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(6, 1, 21, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(7, 3, 6, DATE_ADD(NOW(), INTERVAL -28 DAY)),
(8, 3, 7, DATE_ADD(NOW(), INTERVAL -27 DAY)),
(9, 3, 8, DATE_ADD(NOW(), INTERVAL -26 DAY)),
(10, 2, 18, DATE_ADD(NOW(), INTERVAL -1 DAY));

-- ========== 补充：统一申请审批表（application - 原空表，覆盖所有业务类型和状态） ==========
-- 状态：0草稿/1审核中/3通过/4驳回
-- 业务类型：PROJECT_UP(项目上架)/CERT_APPLY(证书认证)/EXPERT_CERT(专家认证)/UNFREEZE_APPEAL(解冻申诉)/ORG_REGISTER(机构入驻)

-- CERT_APPLY：学生证书认证申请（已通过，已对应student_cert #5）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(1, 'CERT_APPLY', 8, 20, 1, NULL, '{"certStandardId":8,"standardId":8}', 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY));

-- CERT_APPLY：信息技术学院-学生中级（专家+管理员）（专家已通过，等待学院管理员终审）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(2, 'CERT_APPLY', 9, 16, 1, NULL, '{"certStandardId":9,"standardId":9}', 1, 206, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- CERT_APPLY：学生初级能力认证（自动通过型标准，need_manual_audit=0）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(3, 'CERT_APPLY', 1, 21, 9, NULL, '{"certStandardId":1,"standardId":1}', 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL -5 DAY));

-- CERT_APPLY：学生证书认证申请（已驳回，等待重新提交）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(4, 'CERT_APPLY', 11, 22, 2, NULL, '{"certStandardId":11,"standardId":11}', 4, NULL, '累计积分不足1000分，请继续积累积分后再申请', DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY));

-- EXPERT_CERT：专家资质认证申请（等待系统管理员终审）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(5, 'EXPERT_CERT', 3, 4, NULL, NULL, '{"certStandardId":3,"standardId":3,"fieldName":"人工智能领域"}', 1, 103, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- EXPERT_CERT：专家资质认证申请（已通过）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(6, 'EXPERT_CERT', 3, 9, 1, NULL, '{"certStandardId":3,"standardId":3,"fieldName":"大数据分析"}', 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -22 DAY), DATE_ADD(NOW(), INTERVAL -20 DAY));

-- UNFREEZE_APPEAL：解冻申诉（student_5 小强 id=17，被冻结）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(7, 'UNFREEZE_APPEAL', 16, 17, 5, NULL, '{"appealReason":"账号被盗后被异常操作，已向管理员说明情况，请求解冻账户","proofMaterial":"已提交身份核验材料"}', 1, 217, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- PROJECT_UP：项目上架申请（机器学习实战项目 id=6，学院管理员初审通过，等待平台管理员终审）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(8, 'PROJECT_UP', 7, 2, 1, NULL, '{"projectId":6,"certStandardId":7,"standardId":7,"projectName":"机器学习实战项目"}', 1, 203, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY));

-- PROJECT_UP：项目上架申请（模拟法庭辩论 id=11，待法学院管理员初审）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(9, 'PROJECT_UP', 13, 15, 3, NULL, '{"projectId":11,"certStandardId":13,"standardId":13,"projectName":"模拟法庭辩论"}', 1, 212, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- ORG_REGISTER：机构入驻申请（待审核）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(10, 'ORG_REGISTER', 5, -1, NULL, NULL, '{"orgName":"音乐学院","applicantName":"韩老师","contactPerson":"韩老师","contactPhone":"13800138040","address":"艺术楼601"}', 1, 105, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- ORG_REGISTER：机构入驻申请（已通过 - 对应 id=8 理学院）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(11, 'ORG_REGISTER', 5, -1, 8, NULL, '{"orgName":"理学院","applicantName":"周教授","contactPerson":"周教授","contactPhone":"13800138019","address":"理学楼501"}', 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -40 DAY), DATE_ADD(NOW(), INTERVAL -38 DAY));

-- CERT_APPLY：工程学院-学生中级（专家评审）（专家正在审核，节点=209 对应 expert_eng_1 id=11）
INSERT INTO `application` (`id`, `biz_type`, `biz_key`, `applicant_id`, `org_id`, `expert_id`, `form_data`, `current_status`, `current_node_id`, `reject_reason`, `applied_at`, `updated_at`) VALUES
(12, 'CERT_APPLY', 11, 7, 2, NULL, '{"certStandardId":11,"standardId":11}', 1, 209, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- ========== 补充：申请审批业务记录表（application_audit_log - 原空表） ==========
-- status: 2通过 / 3驳回
-- 申请1（CERT_APPLY中级认证）：节点204（专家id=9）已通过
INSERT INTO `application_audit_log` (`id`, `application_id`, `node_id`, `status`, `reject_reason`, `created_at`) VALUES
(1, 1, 204, 2, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY));

-- 申请2（CERT_APPLY中级专家+管理员）：节点205（专家id=9）已通过，等待206
INSERT INTO `application_audit_log` (`id`, `application_id`, `node_id`, `status`, `reject_reason`, `created_at`) VALUES
(2, 2, 205, 2, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY));

-- 申请4（CERT_APPLY被驳回）：节点209（专家id=11）驳回
INSERT INTO `application_audit_log` (`id`, `application_id`, `node_id`, `status`, `reject_reason`, `created_at`) VALUES
(3, 4, 209, 3, '累计积分不足1000分，请继续积累积分后再申请', DATE_ADD(NOW(), INTERVAL -3 DAY));

-- 申请6（EXPERT_CERT已通过）：节点103（admin id=1）通过
INSERT INTO `application_audit_log` (`id`, `application_id`, `node_id`, `status`, `reject_reason`, `created_at`) VALUES
(4, 6, 103, 2, NULL, DATE_ADD(NOW(), INTERVAL -20 DAY));

-- 申请8（PROJECT_UP）：节点202（org_admin_1 id=2）初审通过，等待203终审
INSERT INTO `application_audit_log` (`id`, `application_id`, `node_id`, `status`, `reject_reason`, `created_at`) VALUES
(5, 8, 202, 2, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY));

-- 申请11（ORG_REGISTER已通过）：节点105（admin id=1）通过
INSERT INTO `application_audit_log` (`id`, `application_id`, `node_id`, `status`, `reject_reason`, `created_at`) VALUES
(6, 11, 105, 2, NULL, DATE_ADD(NOW(), INTERVAL -38 DAY));

-- ========== 补充：用户操作日志（user_op_log - 原空表，覆盖所有操作类型） ==========
INSERT INTO `user_op_log` (`id`, `operator_id`, `operator_name`, `target_user_id`, `target_user_name`, `action`, `module`, `detail`, `created_at`) VALUES
-- 用户管理操作
(1, 1, '系统管理员', 17, '小强', 'FREEZE', 'USER', '冻结账户：检测到异常登录行为', DATE_ADD(NOW(), INTERVAL -3 DAY)),
(2, 2, '张三', 6, '小明', 'UPDATE', 'USER', '编辑用户信息：更新联系电话为13800138005', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(3, 1, '系统管理员', NULL, NULL, 'BATCH_FREEZE', 'USER', '批量冻结 2 个用户（跳过 1 个管理员）', DATE_ADD(NOW(), INTERVAL -15 DAY)),
(4, 1, '系统管理员', 7, '小红', 'RESET_PW', 'USER', '重置密码为默认密码', DATE_ADD(NOW(), INTERVAL -7 DAY)),
-- 积分操作
(5, 1, '系统管理员', 2, '张三', 'EARN', 'POINT', '为用户「张三」增加 500 积分，规则：管理员手动加分，说明：机构积分池初始化补充，当前余额：5500', DATE_ADD(NOW(), INTERVAL -12 DAY)),
(6, 1, '系统管理员', 3, '李四', 'EARN', 'POINT', '为用户「李四」增加 1000 积分，规则：管理员手动加分，说明：机构积分池初始化补充，当前余额：9000', DATE_ADD(NOW(), INTERVAL -12 DAY)),
(7, 1, '系统管理员', 15, '陈七', 'EARN', 'POINT', '为用户「陈七」增加 2000 积分，规则：管理员手动加分，说明：新机构入驻赠送积分池，当前余额：5000', DATE_ADD(NOW(), INTERVAL -10 DAY)),
-- 项目管理操作
(8, 2, '张三', NULL, NULL, 'PROJECT_AUDIT', 'PROJECT', '项目「机器学习实战项目」初审通过，报送平台管理员终审', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(9, 1, '系统管理员', NULL, NULL, 'PROJECT_OFFLINE', 'PROJECT', '项目「桥梁结构设计大赛」已下架（活动周期结束）', DATE_ADD(NOW(), INTERVAL -5 DAY)),
-- 活动报名操作
(10, 6, '小明', NULL, NULL, 'CAMPAIGN_ENROLL', 'ENROLL', '报名活动：暑期学习季', DATE_ADD(NOW(), INTERVAL -6 DAY)),
(11, 7, '小红', NULL, NULL, 'CAMPAIGN_ENROLL', 'ENROLL', '报名活动：暑期学习季', DATE_ADD(NOW(), INTERVAL -5 DAY)),
(12, 18, '小美', NULL, NULL, 'CAMPAIGN_LEAVE', 'ENROLL', '取消报名活动：迎新活动', DATE_ADD(NOW(), INTERVAL -22 DAY)),
-- 项目报名操作
(13, 16, '小丽', NULL, NULL, 'PROJECT_ENROLL', 'ENROLL', '报名项目：英语演讲训练营', DATE_ADD(NOW(), INTERVAL -6 DAY)),
(14, 23, '大壮', NULL, NULL, 'PROJECT_ENROLL', 'ENROLL', '报名项目：模拟法庭辩论', DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- 项目提交完成操作
(15, 2, '张三', NULL, NULL, 'PROJECT_SUBMIT', 'PROJECT', '学生「小刚」完成项目「校园APP开发项目」，审核通过发放奖励', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(16, 3, '李四', NULL, NULL, 'PROJECT_SUBMIT', 'PROJECT', '学生「小红」完成项目「工程模拟平台」，审核通过发放奖励', DATE_ADD(NOW(), INTERVAL -5 DAY));

-- ========== 补充：专家资质认证（新增已申请但还在走流程的专家） ==========
-- 确保专家资质完整（专家 id=4 已申请资质审核中）

-- ========== 补充：更多兑换记录（覆盖更多兑换商品） ==========
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `related_rule_id`, `description`, `created_at`) VALUES
(91, 16, -180, 880, 'EXCHANGE', 12, '兑换桌面文具套装', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(92, 21, -250, 300, 'EXCHANGE', 9, '兑换保温杯', DATE_ADD(NOW(), INTERVAL -2 DAY)),
(93, 23, -450, 350, 'EXCHANGE', 7, '兑换双肩背包', DATE_ADD(NOW(), INTERVAL -1 DAY)),
(94, 20, -700, 200, 'EXCHANGE', 8, '兑换机械键盘', DATE_ADD(NOW(), INTERVAL -4 DAY)),
(95, 16, -900, 0, 'EXCHANGE', 11, '兑换智能手环', DATE_ADD(NOW(), INTERVAL -1 DAY)),
(96, 22, -120, 230, 'EXCHANGE', 10, '兑换校园咖啡券', DATE_ADD(NOW(), INTERVAL -3 DAY)),
(97, 2, 500, 4400, 'ADMIN', 10, '管理员手动加分（说明：机构积分池补充）', DATE_ADD(NOW(), INTERVAL -12 DAY)),
(98, 3, 1000, 8750, 'ADMIN', 10, '管理员手动加分（说明：机构积分池补充）', DATE_ADD(NOW(), INTERVAL -12 DAY)),
(99, 15, 2000, 5000, 'ADMIN', 10, '管理员手动加分（说明：新机构入驻赠送）', DATE_ADD(NOW(), INTERVAL -10 DAY));

-- ========== 补充：调整 sys_user.balance 使其与最新 transaction_log 快照一致 ==========
-- （避免运行时积分余额与流水对不上）
UPDATE `sys_user` SET `balance` = 760 WHERE `id` = 6;
UPDATE `sys_user` SET `balance` = 280 WHERE `id` = 7;
UPDATE `sys_user` SET `balance` = 250 WHERE `id` = 8;
UPDATE `sys_user` SET `balance` = 0 WHERE `id` = 16;
UPDATE `sys_user` SET `balance` = 400 WHERE `id` = 18;
UPDATE `sys_user` SET `balance` = 300 WHERE `id` = 21;
UPDATE `sys_user` SET `balance` = 230 WHERE `id` = 22;
UPDATE `sys_user` SET `balance` = 350 WHERE `id` = 23;
UPDATE `sys_user` SET `balance` = 200 WHERE `id` = 20;
UPDATE `sys_user` SET `balance` = 4400 WHERE `id` = 2;
UPDATE `sys_user` SET `balance` = 8750 WHERE `id` = 3;
UPDATE `sys_user` SET `balance` = 5000 WHERE `id` = 15;

-- ========== 补充：更多学生用户的 balance 对齐 ==========
-- student_17 小强(冻结) balance = 0 已设置
-- student_19 大鹏 id=19: 无流水，保持初始300
-- student_24 小芳 id=24: 无流水，保持初始200
-- student_25 阿飞 id=25: 无流水，保持初始650
UPDATE `sys_user` SET `balance` = 300 WHERE `id` = 19;
UPDATE `sys_user` SET `balance` = 200 WHERE `id` = 24;
UPDATE `sys_user` SET `balance` = 650 WHERE `id` = 25;

-- ========== 补充：通知消息表（notification - 原空表） ==========
-- 分类：SYSTEM(系统)/APPLICATION(申请审批)/POINT(积分)/MALL(商城)/CONVERSION(成果转换)
-- 级别：INFO/SUCCESS/WARNING
INSERT INTO `notification` (`id`, `event_code`, `scope_type`, `scope_value`, `category`, `level`, `title`, `content`, `source_type`, `source_id`, `action_path`, `actor_id`, `dedupe_key`, `status`, `created_at`, `expires_at`) VALUES
-- === 欢迎通知：不预置！由启动时 ensureWelcomeNotificationsForAllUsers 动态生成，保证每个用户角色正确、一对一无错配 ===
-- === 申请审批通知（学生 - 证书申请结果） ===
(109, 'CERT_APPLY_APPROVED', 'USER', '20', 'APPLICATION', 'SUCCESS', '学生中级认证已通过', '您申请的"信息技术学院-学生中级能力认证（专家评审）"已审核通过，证书已发放至"我的证书"。', 'CERT_APPLY', 1, '/student-certificate/5', 9, 'CERT_APPLY_APPROVED:1', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), NULL),
(110, 'CERT_APPLY_REJECTED', 'USER', '22', 'APPLICATION', 'WARNING', '证书认证申请被驳回', '您申请的"工程学院-学生中级能力认证"被驳回。驳回原因：累计积分不足1000分，请继续积累积分后再申请。', 'CERT_APPLY', 4, '/applications', 11, 'CERT_APPLY_REJECTED:4', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -3 DAY), NULL),
-- === 申请审批通知（管理员 - 待办提醒） ===
(111, 'EXPERT_CERT_PENDING', 'ROLE', 'admin', 'APPLICATION', 'INFO', '专家资质认证待审核', '有1份专家资质认证申请（王五 - 人工智能领域）等待您的终审。', 'EXPERT_CERT', 5, '/applications?tab=expert', NULL, 'EXPERT_CERT_PENDING:5:ADMIN', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -1 DAY), NULL),
(112, 'UNFREEZE_APPEAL_PENDING', 'ROLE', 'admin', 'APPLICATION', 'WARNING', '账户解冻申诉待审核', '学生「小强」(student_5) 提交了解冻申诉，说明账号被盗后异常操作，已提交身份核验材料，请及时审核。', 'UNFREEZE_APPEAL', 7, '/applications?tab=unfreeze', 17, 'UNFREEZE_APPEAL_PENDING:7:ADMIN', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -1 DAY), NULL),
-- === 成果转换通知 ===
(113, 'CONVERSION_APPLY_PENDING', 'ROLE', 'admin', 'CONVERSION', 'INFO', '新的成果转换申请待审核', '学生「小明」提交了成果转换申请：全国导游基础知识(李巧玲-智慧职教) → (0402114)导游基础知识，请及时审核。', 'CONVERSION_APPLICATION', 1, '/applications?tab=conversion&filter=pending', 6, 'CONVERSION_APPLY_PENDING:1:ADMIN', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -5 DAY), NULL),
(114, 'CONVERSION_APPLY_PENDING', 'ROLE', 'org_admin', 'CONVERSION', 'INFO', '新的成果转换申请待审核', '学生「小红」提交了成果转换申请：汽车构造(曹义等-中国大学MOOC) → (242714)汽车结构认知，请及时审核。', 'CONVERSION_APPLICATION', 2, '/applications?tab=conversion&filter=pending', 7, 'CONVERSION_APPLY_PENDING:2:ORG_ADMIN', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), NULL),
(115, 'CONVERSION_APPLY_REJECTED', 'USER', '8', 'CONVERSION', 'WARNING', '成果转换申请已驳回', '您的成果转换申请：人工智能导论(Coursera) → 人工智能基础 已被驳回。驳回原因：课程名称与现有规则重复，请选择已有规则申请。', 'CONVERSION_APPLICATION', 3, '/conversion-apply?id=3&status=REJECTED', 1, 'CONVERSION_APPLY_REJECTED:3', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -4 DAY), NULL),
-- === 积分变动通知 ===
(116, 'POINT_EARNED', 'USER', '6', 'POINT', 'SUCCESS', '获得积分：校园APP开发优秀', '您因项目「校园APP开发项目」获评优秀，获得积分 +500 分，当前账户余额 1360 分。', 'REWARD', 14, '/transactions', 2, 'POINT_EARNED:14', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), NULL),
(117, 'POINT_EARNED', 'USER', '7', 'POINT', 'SUCCESS', '获得积分：工程模拟平台参与', '您参与完成了项目「工程模拟平台」，获得积分 +250 分，当前账户余额 580 分。', 'REWARD', 10, '/transactions', 3, 'POINT_EARNED:10', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -5 DAY), NULL),
(118, 'DAILY_SIGNIN_REMIND', 'ALL', NULL, 'POINT', 'INFO', '每日签到提醒', '今日还未签到哦！签到可获得10积分，连续签到还有额外奖励。', NULL, NULL, '/dashboard', NULL, 'DAILY_SIGNIN_REMIND:' . DATE_FORMAT(NOW(), '%Y%m%d'), 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
-- === 积分商城通知 ===
(119, 'EXCHANGE_SUCCESS', 'USER', '6', 'MALL', 'SUCCESS', '兑换成功：荣誉证书', '您使用 500 积分成功兑换了"荣誉证书"，请在"我的订单"查看兑换详情。', 'EXCHANGE', 19, '/transactions', 6, 'EXCHANGE_SUCCESS:19', 'PUBLISHED', NOW(), NULL),
(120, 'EXCHANGE_SUCCESS', 'USER', '8', 'MALL', 'SUCCESS', '兑换成功：充电宝', '您使用 800 积分成功兑换了"充电宝"，请于3个工作日内到信息技术学院办公室领取。', 'EXCHANGE', 20, '/transactions', 8, 'EXCHANGE_SUCCESS:20', 'PUBLISHED', NOW(), NULL),
(121, 'NEW_EXCHANGE_ITEM', 'ALL', NULL, 'MALL', 'INFO', '商城上新：智能手环限时兑', '积分商城上新啦！智能手环只需900积分即可兑换，数量有限先到先得。', NULL, NULL, '/point-mall', NULL, 'NEW_EXCHANGE_ITEM:202607', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
-- === 管理员手动发布通知 ===
(122, 'MANUAL_NOTICE', 'ALL', NULL, 'SYSTEM', 'WARNING', '【重要】系统维护通知', '平台将于本周六（7月18日）凌晨02:00-04:00进行系统维护升级，期间所有服务将暂停使用，请提前安排好您的操作。', 'MANUAL_NOTICE', NULL, NULL, 1, 'MANUAL_NOTICE:MAINTENANCE_20260718', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY)),
(123, 'MANUAL_NOTICE', 'ORG', '1', 'SYSTEM', 'INFO', '信息技术学院：暑期项目申报通知', '信息技术学院2026年暑期实践项目申报已开启，请各教研室于7月25日前完成项目材料提交。', 'MANUAL_NOTICE', NULL, NULL, 2, 'MANUAL_NOTICE:ORG1_SUMMER_2026', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY)),
(124, 'MANUAL_NOTICE', 'ORG', '2', 'SYSTEM', 'INFO', '工程学院：专家评审会通知', '工程学院定于7月20日下午14:00在工程楼B201召开学生中级能力认证专家评审会，请相关专家准时参加。', 'MANUAL_NOTICE', NULL, NULL, 3, 'MANUAL_NOTICE:ORG2_EXPERT_MEET', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY)),
-- === 机构入驻通知 ===
(125, 'ORG_REGISTER_APPROVED', 'USER', '15', 'APPLICATION', 'SUCCESS', '机构入驻申请已通过', '您申请的"管理学院"入驻已审核通过，机构管理员账户已激活，请登录后完善机构信息。', 'ORG_REGISTER', 11, '/organizations', 1, 'ORG_REGISTER_APPROVED:11', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -38 DAY), NULL),
(126, 'ORG_REGISTER_PENDING', 'ROLE', 'admin', 'APPLICATION', 'INFO', '新机构入驻申请待审核', '机构「音乐学院」提交了入驻申请，联系人：韩老师，联系电话：13800138040，请及时审核。', 'ORG_REGISTER', 10, '/applications?tab=org', -1, 'ORG_REGISTER_PENDING:10:ADMIN', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -1 DAY), NULL),
-- === 项目管理通知 ===
(127, 'PROJECT_UP_PENDING', 'ROLE', 'admin', 'APPLICATION', 'INFO', '项目上架申请待终审', '项目「机器学习实战项目」(信息技术学院) 已通过机构初审，等待平台管理员终审上架。', 'PROJECT_UP', 8, '/projects/manage?filter=pending', 2, 'PROJECT_UP_PENDING:8:ADMIN', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -2 DAY), NULL),
(128, 'PROJECT_OFFLINE_NOTICE', 'ORG', '2', 'SYSTEM', 'WARNING', '项目已下架：桥梁结构设计大赛', '您机构的项目「桥梁结构设计大赛」因活动周期结束，已于昨日自动下架。如有需要可重新申请上架。', 'PROJECT', 7, '/projects/manage', 1, 'PROJECT_OFFLINE_NOTICE:7', 'PUBLISHED', DATE_ADD(NOW(), INTERVAL -5 DAY), NULL);

-- ========== 补充：通知接收人表（notification_recipient - 原空表） ==========
-- 注意：按用户分发，部分通知已读，部分未读
INSERT INTO `notification_recipient` (`id`, `notification_id`, `user_id`, `read_at`, `confirmed_at`, `created_at`) VALUES
-- === 欢迎通知的接收者：不预置！由 ensureWelcomeNotificationsForAllUsers 动态生成 ===
-- === 证书申请通过（小雪 id=20） === notification_id 9 → 109
(1026, 109, 20, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
-- === 证书申请驳回（小雨 id=22） === notification_id 10 → 110
(1027, 110, 22, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
-- === 管理员待办通知（admin id=1） === notification_id 11→111, 12→112
(1028, 111, 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1029, 112, 1, NULL, DATE_ADD(NOW(), INTERVAL -12 HOUR), DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- === 成果转换待审（管理员+机构管理员） === notification_id 13→113, 14→114
(1030, 113, 1, DATE_ADD(NOW(), INTERVAL -4 DAY), NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1031, 114, 2, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1032, 114, 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1033, 114, 15, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
-- === 转换驳回（小刚 id=8） === notification_id 15 → 115
(1034, 115, 8, DATE_ADD(NOW(), INTERVAL -3 DAY), NULL, DATE_ADD(NOW(), INTERVAL -4 DAY)),
-- === 积分获得通知 === notification_id 16→116, 17→117
(1035, 116, 6, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1036, 117, 7, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
-- === 每日签到提醒（发给学生 id=6,7,8,16,18,20,21,22,23,25） === notification_id 18 → 118
(1037, 118, 6, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1038, 118, 7, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1039, 118, 8, DATE_ADD(NOW(), INTERVAL -20 HOUR), NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1040, 118, 16, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1041, 118, 18, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1042, 118, 20, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1043, 118, 21, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1044, 118, 22, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1045, 118, 23, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
(1046, 118, 25, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- === 商城兑换成功 === notification_id 19→119, 20→120
(1047, 119, 6, NULL, NULL, NOW()),
(1048, 120, 8, NULL, NULL, NOW()),
-- === 商城上新（ALL 用户） === notification_id 21 → 121
(1049, 121, 6, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1050, 121, 7, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1051, 121, 8, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1052, 121, 16, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1053, 121, 17, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1054, 121, 18, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1055, 121, 20, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1056, 121, 2, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1057, 121, 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1058, 121, 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1059, 121, 9, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1060, 121, 4, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1061, 121, 5, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
-- === 系统维护重要通知（ALL 用户） - WARNING 级别，部分已确认 === notification_id 22 → 122
(1062, 122, 1, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1063, 122, 2, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1064, 122, 3, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1065, 122, 4, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1066, 122, 5, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1067, 122, 6, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1068, 122, 7, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1069, 122, 8, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1070, 122, 9, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1071, 122, 15, DATE_ADD(NOW(), INTERVAL -2 DAY), NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1072, 122, 16, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1073, 122, 17, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1074, 122, 18, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
(1075, 122, 20, NULL, NULL, DATE_ADD(NOW(), INTERVAL -3 DAY)),
-- === 信息技术学院通知（ORG=1：用户id=2,6,8,9,10,21） === notification_id 23 → 123
(1076, 123, 2, DATE_ADD(NOW(), INTERVAL -4 DAY), NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1077, 123, 6, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1078, 123, 8, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1079, 123, 9, DATE_ADD(NOW(), INTERVAL -4 DAY), NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1080, 123, 10, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1081, 123, 21, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
-- === 工程学院专家评审会通知（ORG=2：用户id=3,7,11,12,22） === notification_id 24 → 124
(1082, 124, 3, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1083, 124, 7, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1084, 124, 11, DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1085, 124, 12, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
(1086, 124, 22, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
-- === 机构入驻通过（陈七 id=15） === notification_id 25 → 125
(1087, 125, 15, DATE_ADD(NOW(), INTERVAL -37 DAY), NULL, DATE_ADD(NOW(), INTERVAL -38 DAY)),
-- === 机构入驻待审（admin id=1） === notification_id 26 → 126
(1088, 126, 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY)),
-- === 项目上架待终审（admin id=1） === notification_id 27 → 127
(1089, 127, 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -2 DAY)),
-- === 项目下架通知（工程学院 org=2：用户id=3,7,11,12,22） === notification_id 28 → 128
(1090, 128, 3, DATE_ADD(NOW(), INTERVAL -4 DAY), NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1091, 128, 7, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1092, 128, 11, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1093, 128, 12, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY)),
(1094, 128, 22, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY));

-- ============================================================
--  【余额锚定修正】保证 sys_user.balance 与用户最后一笔交易 balance_after 严格一致
--  ==========================================================================
--  说明：用户6/7/8 初始 INSERT 的 balance 未考虑块1(近12天主链)的累计净变化，
--        此处按块1(id=1-20)最后一笔交易的 balance_after 覆盖修正，确保趋势算法锚点正确。
--  ==========================================================================
--  用户6 小明：INSERT初始500 → 主链最后 after=760（累计+260）
UPDATE `sys_user` SET `balance` = 760 WHERE `id` = 6;
--  用户7 小红：INSERT初始300 → 主链最后 after=280（累计-20）
UPDATE `sys_user` SET `balance` = 280 WHERE `id` = 7;
--  用户8 小刚：INSERT初始800 → 主链最后 after=250（累计-550，含兑换蓝牙耳机等大额支出）
UPDATE `sys_user` SET `balance` = 250 WHERE `id` = 8;
