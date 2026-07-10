-- 每次启动先删后建，保证从干净的初始状态开始，方便反复演示
DROP TABLE IF EXISTS t_point_transaction;
DROP TABLE IF EXISTS t_point_rule;
DROP TABLE IF EXISTS t_account;

-- 积分账户
CREATE TABLE t_account (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name  VARCHAR(64)    NOT NULL                COMMENT '用户名',
    balance    DECIMAL(15, 2) NOT NULL DEFAULT 0.00   COMMENT '积分余额',
    version    INT            NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT ='积分账户';

-- 积分规则（哪种行为奖励多少分）
CREATE TABLE t_point_rule (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(64)    NOT NULL              COMMENT '规则编码',
    rule_name VARCHAR(128)   NOT NULL              COMMENT '规则名称',
    points    DECIMAL(15, 2) NOT NULL              COMMENT '命中一次奖励的积分',
    enabled   INT            NOT NULL DEFAULT 1    COMMENT '1启用 0停用',
    UNIQUE KEY uk_rule_code (rule_code)
) COMMENT ='积分规则';

-- 积分交易流水（账本，只增不改）
CREATE TABLE t_point_transaction (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id    BIGINT         NOT NULL              COMMENT '账户ID',
    rule_code     VARCHAR(64)                          COMMENT '触发的规则编码',
    change_amount DECIMAL(15, 2) NOT NULL              COMMENT '本次变动积分(+/-)',
    balance_after DECIMAL(15, 2) NOT NULL              COMMENT '变动后余额快照',
    remark        VARCHAR(255)                         COMMENT '备注',
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_account (account_id)
) COMMENT ='积分交易流水';
