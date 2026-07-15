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
INSERT INTO `credit_rule` (`id`, `event_code`, `event_name`, `credit_value`, `is_enabled`, `created_at`, `start_time`, `end_time`, `project_id`, `org_id`) VALUES
(1, 'COURSE_COMPLETE', '课程完成', 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, NULL),
(2, 'PROJECT_PARTICIPATE', '校园APP开发项目参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 1, 1),
(3, 'PROJECT_PARTICIPATE', '智能教室系统参与', 200, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 2, 1),
(4, 'PROJECT_PARTICIPATE', '工程模拟平台参与', 250, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 3, 2),
(5, 'PROJECT_EXCELLENT', '校园APP开发优秀', 500, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 1, 1),
(6, 'PROJECT_EXCELLENT', '数据分析大赛优秀', 600, 1, NOW(), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL 6 MONTH), 4, 2),
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

-- 学生证书发放记录初始数据
INSERT INTO `student_cert` (`id`, `student_id`, `cert_standard_id`, `application_id`, `cert_no`, `student_name`, `cert_name`, `org_name`, `verify_code`, `status`, `revoke_reason`, `revoked_at`, `issued_at`, `valid_until`) VALUES
(1, 8, 1, NULL, CONCAT('CB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0008-0001'), '小刚', '学生初级能力认证', '信息技术学院', 'DEMO20260713', 1, NULL, NULL, DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL 360 DAY));

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