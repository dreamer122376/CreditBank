INSERT INTO sys_user (username, password, real_name, phone, email, role, org_id, expert_field, balance, status) VALUES
('admin', '123456', '超级管理员', '13800000000', 'admin@creditbank.com', 'admin', NULL, NULL, 0, 1),
('instadmin', '123456', '李机构', '13800000001', 'inst@creditbank.com', 'org_admin', 1, NULL, 28600, 1),
('student', '123456', '张三', '13800000002', 'student@creditbank.com', 'student', 1, NULL, 1280, 1),
('expert', '123456', '王教授', '13800000003', 'expert@creditbank.com', 'expert', NULL, '计算机科学', 3200, 1);

INSERT INTO credit_rule (event_code, event_name, credit_value, scope, is_enabled) VALUES
('COURSE_DONE', '完成一门课程', 10, 'STUDENT', 1),
('PROJECT_COMPLETE', '完成项目', 200, 'STUDENT', 1),
('ACTIVITY_JOIN', '参与活动', 50, 'ALL', 1),
('CREDIT_EXCHANGE', '学分转换', 180, 'STUDENT', 1),
('EXPERT_REVIEW', '专家评审', 100, 'EXPERT', 1);

INSERT INTO organization (name, contact_person, contact_phone, address, status) VALUES
('北京开放大学', '张校长', '13800138000', '北京市海淀区', 1),
('上海培训中心', '李主任', '13900139000', '上海市浦东新区', 0),
('广州继续教育学校', '王主任', '13700137000', '广州市天河区', 1);

INSERT INTO application (biz_type, biz_key, applicant_id, org_id, expert_id, current_status, reject_reason, applied_at) VALUES
('PROJECT_UP', 1, 2, 1, 4, 1, NULL, '2026-07-08 10:22:00'),
('EXCHANGE', NULL, 3, 1, 4, 2, NULL, '2026-07-08 09:15:00'),
('CERT_APPLY', NULL, 3, 1, 4, 1, NULL, '2026-07-07 16:40:00'),
('PROJECT_UP', 2, 2, 1, 4, 2, NULL, '2026-07-07 14:00:00'),
('EXCHANGE', NULL, 3, 1, 4, 0, NULL, '2026-07-06 11:30:00');

INSERT INTO transaction_log (user_id, amount, balance_after, biz_type, biz_id, description, created_at) VALUES
(3, 50, 50, 'REWARD', '1', '参与活动', '2026-07-03 10:00:00'),
(3, 200, 250, 'REWARD', '2', '完成项目', '2026-07-04 10:00:00'),
(3, 180, 430, 'EXCHANGE', '3', '学分转换', '2026-07-05 14:20:00'),
(3, 10, 440, 'REWARD', '4', '完成一门课程', '2026-07-06 09:00:00'),
(3, 50, 490, 'REWARD', '5', '参与活动', '2026-07-06 15:30:00'),
(3, 200, 690, 'REWARD', '6', '完成项目', '2026-07-07 10:00:00'),
(3, 180, 870, 'EXCHANGE', '7', '学分转换', '2026-07-07 14:20:00'),
(3, 10, 880, 'REWARD', '8', '完成一门课程', '2026-07-08 09:00:00'),
(3, 50, 930, 'REWARD', '9', '参与活动', '2026-07-08 15:30:00'),
(3, 350, 1280, 'REWARD', '10', '暑期读书会活动奖励', '2026-07-09 16:00:00'),
(2, -200, 28400, 'REWARD', '11', '项目奖励发放', '2026-07-04 10:00:00'),
(2, -150, 28250, 'REWARD', '12', '活动奖励发放', '2026-07-05 15:00:00'),
(2, 5000, 33250, 'REWARD', '13', '充值入账', '2026-07-06 09:00:00'),
(2, -300, 32950, 'REWARD', '14', '项目奖励发放', '2026-07-07 10:00:00'),
(2, -100, 32850, 'REWARD', '15', '活动奖励发放', '2026-07-08 15:00:00'),
(2, -200, 28600, 'REWARD', '16', '积分池调整', '2026-07-09 12:00:00'),
(4, 100, 100, 'REWARD', '17', '专家评审', '2026-07-04 11:00:00'),
(4, 200, 300, 'REWARD', '18', '专家评审', '2026-07-05 11:00:00'),
(4, 300, 600, 'REWARD', '19', '专家评审', '2026-07-06 11:00:00'),
(4, 200, 800, 'REWARD', '20', '专家评审', '2026-07-07 11:00:00'),
(4, 400, 1200, 'REWARD', '21', '专家评审', '2026-07-08 11:00:00'),
(4, 500, 1700, 'REWARD', '22', '专家评审', '2026-07-08 16:00:00'),
(4, 600, 2300, 'REWARD', '23', '专家评审', '2026-07-09 10:00:00'),
(4, 900, 3200, 'REWARD', '24', '月度评审奖励', '2026-07-09 17:00:00');
