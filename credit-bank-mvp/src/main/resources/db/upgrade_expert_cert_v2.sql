-- 专家评审资质业务补全（2026-07-14）
-- 注意：本项目后端每次启动会重放 db/credit_bank.sql（含 DROP TABLE），
-- 因此 schema 变更必须同步写进 credit_bank.sql 的建表语句（已改），本文件仅作变更记录。
-- 1) 资质有效期：valid_until，NULL 表示长期有效；发证时写入 issued_at + 3 年
-- 2) 资质撤销：revoke_reason / revoked_at，配合 status 置 0
-- 3) 撤销/过期后允许重新发证：同一专家+标准会存多条历史记录，
--    故原唯一键 uk_expert_standard 改为普通索引
ALTER TABLE expert_cert
    ADD COLUMN valid_until datetime NULL COMMENT '资质有效期截止，NULL为长期' AFTER issued_at,
  ADD COLUMN revoke_reason varchar(255) NULL COMMENT '撤销原因' AFTER valid_until,
  ADD COLUMN revoked_at datetime NULL COMMENT '撤销时间' AFTER revoke_reason,
DROP INDEX uk_expert_standard,
  ADD INDEX idx_expert_standard (expert_id, cert_standard_id);
