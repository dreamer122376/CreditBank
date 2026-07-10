-- 初始账户（余额都从 0 开始）
INSERT INTO t_account (user_name, balance) VALUES
 ('张三', 0),
 ('李四', 0),
 ('王五', 0);

-- 初始积分规则
INSERT INTO t_point_rule (rule_code, rule_name, points, enabled) VALUES
 ('COURSE_DONE',   '完成一门课程', 10, 1),
 ('ACTIVITY_JOIN', '参加平台活动',  5, 1),
 ('CERT_PASS',     '通过认证考核', 20, 1);
