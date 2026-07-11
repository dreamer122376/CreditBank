-- 机构表初始数据（无外键依赖，最先插入）
INSERT INTO `organization` (`id`, `name`, `contact_person`, `contact_phone`, `address`, `status`, `created_at`, `updated_at`) VALUES
(1, '信息技术学院', '张三', '13800138001', '教学楼A栋', 1, NOW(), NOW()),
(2, '工程学院', '李四', '13800138002', '教学楼B栋', 1, NOW(), NOW()),
(3, '管理学院', '陈七', '13800138008', '教学楼C栋', 0, NOW(), NOW());

-- 系统用户表初始数据（依赖 organization.org_id）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role`, `org_id`, `expert_field`, `balance`, `status`, `created_at`) VALUES
(1, 'admin', '123456', '系统管理员', '13800138000', 'admin@creditbank.com', 'admin', NULL, NULL, 0, 1, NOW()),
(2, 'org_admin_1', '123456', '张三', '13800138001', 'zhangsan@org1.com', 'org_admin', 1, NULL, 0, 1, NOW()),
(3, 'org_admin_2', '123456', '李四', '13800138002', 'lisi@org2.com', 'org_admin', 2, NULL, 0, 1, NOW()),
(4, 'expert_1', '123456', '王五', '13800138003', 'wangwu@expert.com', 'expert', NULL, '计算机科学', 0, 1, NOW()),
(5, 'expert_2', '123456', '赵六', '13800138004', 'zhaoliu@expert.com', 'expert', NULL, '软件工程', 0, 1, NOW()),
(6, 'student_1', '123456', '小明', '13800138005', 'xiaoming@student.com', 'student', 1, NULL, 500, 1, NOW()),
(7, 'student_2', '123456', '小红', '13800138006', 'xiaohong@student.com', 'student', 2, NULL, 300, 1, NOW()),
(8, 'student_3', '123456', '小刚', '13800138007', 'xiaogang@student.com', 'student', 1, NULL, 800, 1, NOW());

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

-- 认证标准表初始数据（无外键依赖）
INSERT INTO `cert_standard` (`id`, `standard_name`, `min_credit`, `need_expert_approve`, `validity_days`, `is_enabled`, `created_at`) VALUES
(1, '初级认证', 500, 0, 365, 1, NOW()),
(2, '中级认证', 1000, 1, 365, 1, NOW()),
(3, '高级认证', 2000, 1, 365, 1, NOW());

-- 项目表初始数据（依赖 organization.org_id 和 sys_user.id）
INSERT INTO `project` (`id`, `org_id`, `expert_id`, `name`, `description`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, 4, '校园APP开发项目', '开发一款面向学生的校园服务APP', 1, NOW(), NOW()),
(2, 1, 5, '智能教室系统', '基于物联网的智能教室管理系统', 1, NOW(), NOW()),
(3, 2, 4, '工程模拟平台', '土木工程模拟仿真平台开发', 1, NOW(), NOW()),
(4, 2, 5, '数据分析大赛', '校园数据分析竞赛项目', 0, NOW(), NOW()),
(5, 1, NULL, '校园安全监测', '校园安全监测系统升级', 2, NOW(), NOW());

-- 积分规则表初始数据（依赖 project.id）
INSERT INTO `credit_rule` (`id`, `event_code`, `event_name`, `credit_value`, `is_enabled`, `created_at`, `project_id`) VALUES
(1, 'COURSE_COMPLETE', '课程完成', 100, 1, NOW(), NULL),
(2, 'PROJECT_PARTICIPATE', '校园APP开发项目参与', 200, 1, NOW(), 1),
(3, 'PROJECT_PARTICIPATE', '智能教室系统参与', 200, 1, NOW(), 2),
(4, 'PROJECT_PARTICIPATE', '工程模拟平台参与', 250, 1, NOW(), 3),
(5, 'PROJECT_EXCELLENT', '校园APP开发优秀', 500, 1, NOW(), 1),
(6, 'PROJECT_EXCELLENT', '数据分析大赛优秀', 600, 1, NOW(), 4),
(7, 'ONLINE_TEST', '在线测试', 50, 1, NOW(), NULL),
(8, 'ATTENDANCE', '签到打卡', 10, 1, NOW(), NULL),
(9, 'SHARE_CONTENT', '分享内容', 30, 1, NOW(), NULL);

-- 积分转换规则表初始数据（无外键依赖）
INSERT INTO `exchange_rule` (`id`, `item_name`, `item_icon`, `required_credit`, `stock`, `daily_limit`, `is_enabled`, `created_at`) VALUES
(1, '精美笔记本', 'icon_notebook', 100, 100, 1, 1, NOW()),
(2, '课程优惠券', 'icon_coupon', 200, 50, 1, 1, NOW()),
(3, '荣誉证书', 'icon_certificate', 500, 20, 1, 1, NOW()),
(4, '书籍借阅卡', 'icon_card', 300, 80, 1, 1, NOW()),
(5, '充电宝', 'icon_powerbank', 800, 30, 1, 1, NOW());

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

-- 交易流水表初始数据（依赖 sys_user.id）
INSERT INTO `transaction_log` (`id`, `user_id`, `amount`, `balance_after`, `biz_type`, `biz_id`, `description`, `created_at`) VALUES
(1, 6, 100, 100, 'REWARD', 'course_001', '完成课程获得积分', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(2, 6, 200, 300, 'REWARD', 'project_001', '参与项目获得积分', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(3, 6, 100, 400, 'REWARD', 'test_001', '在线测试获得积分', DATE_ADD(NOW(), INTERVAL -6 DAY)),
(4, 6, 100, 500, 'REWARD', 'course_002', '完成课程获得积分', DATE_ADD(NOW(), INTERVAL -4 DAY)),
(5, 7, 100, 100, 'REWARD', 'course_001', '完成课程获得积分', DATE_ADD(NOW(), INTERVAL -9 DAY)),
(6, 7, 200, 300, 'REWARD', 'project_002', '参与项目获得积分', DATE_ADD(NOW(), INTERVAL -5 DAY)),
(7, 8, 300, 300, 'REWARD', 'project_001', '参与项目获得积分', DATE_ADD(NOW(), INTERVAL -12 DAY)),
(8, 8, 200, 500, 'REWARD', 'course_003', '完成课程获得积分', DATE_ADD(NOW(), INTERVAL -10 DAY)),
(9, 8, 100, 600, 'REWARD', 'test_002', '在线测试获得积分', DATE_ADD(NOW(), INTERVAL -8 DAY)),
(10, 8, 200, 800, 'REWARD', 'project_003', '优秀项目奖励', DATE_ADD(NOW(), INTERVAL -6 DAY));
