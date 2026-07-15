-- MySQL dump 10.13  Distrib 8.4.8, for Win64 (x86_64)
--
-- Host: localhost    Database: credit_bank
-- ------------------------------------------------------
-- Server version	8.4.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_role_menu`
--
DROP DATABASE IF EXISTS `credit_bank`;
CREATE DATABASE IF NOT EXISTS `credit_bank` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci;
USE `credit_bank`;


DROP TABLE IF EXISTS `admin_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_role_menu` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                                   `role_tag` varchar(30) NOT NULL COMMENT '角色标签',
                                   `menu_code` varchar(50) NOT NULL COMMENT '菜单编码',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_role_menu` (`role_tag`,`menu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员角色菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_role_menu`
--

LOCK TABLES `admin_role_menu` WRITE;
/*!40000 ALTER TABLE `admin_role_menu` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application`
--

DROP TABLE IF EXISTS `application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `application` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请单ID',
                               `biz_type` varchar(30) NOT NULL COMMENT '业务类型：PROJECT_UP/EXCHANGE/CERT_APPLY',
                               `biz_key` bigint DEFAULT NULL COMMENT '关联的具体业务主键ID',
                               `applicant_id` bigint NOT NULL COMMENT '申请人ID',
                               `org_id` bigint DEFAULT NULL COMMENT '申请所属机构ID',
                               `expert_id` bigint DEFAULT NULL COMMENT '指派的专家审批人ID',
                               `form_data` json DEFAULT NULL COMMENT '前端表单的JSON数据',
                               `current_status` tinyint DEFAULT '0' COMMENT '状态：0草稿/1审核中/3通过/4驳回（2为旧数据，兼容为审核中）',
                               `current_node_id` bigint DEFAULT NULL COMMENT '认证业务当前审批节点ID（cert_audit_flow.id，非认证业务为NULL）',
                               `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
                               `applied_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                               `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_applicant_id` (`applicant_id`),
                               KEY `idx_current_status` (`current_status`),
                               KEY `idx_current_node_id` (`current_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一申请审批表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application`
--

LOCK TABLES `application` WRITE;
/*!40000 ALTER TABLE `application` DISABLE KEYS */;
/*!40000 ALTER TABLE `application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application_audit_log`
--

DROP TABLE IF EXISTS `application_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `application_audit_log` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                          `application_id` bigint NOT NULL COMMENT '申请单ID（关联application.id）',
                                          `node_id` bigint NOT NULL COMMENT '审批节点ID（关联cert_audit_flow.id）',
                                          `status` tinyint NOT NULL COMMENT '审核结果：2通过/3驳回',
                                          `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
                                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
                                          PRIMARY KEY (`id`),
                                          KEY `idx_application_id` (`application_id`),
                                          KEY `idx_node_id` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='申请审批业务记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application_audit_log`
--

LOCK TABLES `application_audit_log` WRITE;
/*!40000 ALTER TABLE `application_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `application_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campaign`
--

DROP TABLE IF EXISTS `campaign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaign` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
                            `title` varchar(100) NOT NULL COMMENT '活动标题',
                            `description` text COMMENT '活动简讯/详情描述',
                            `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图URL',
                            `images` text COMMENT '多张活动图片，JSON数组',
                            `organizer` varchar(100) DEFAULT NULL COMMENT '主办方/组织者',
                            `multiplier` decimal(3,1) DEFAULT '1.0' COMMENT '积分倍率（如1.5表示1.5倍）',
                            `project_ids` text COMMENT '适用项目ID，逗号分隔（空代表全平台）',
                            `start_time` datetime NOT NULL COMMENT '活动开始时间',
                            `end_time` datetime NOT NULL COMMENT '活动结束时间',
                            `status` tinyint DEFAULT '0' COMMENT '状态：0未开始，1进行中，2已结束',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campaign`
--

LOCK TABLES `campaign` WRITE;
/*!40000 ALTER TABLE `campaign` DISABLE KEYS */;
/*!40000 ALTER TABLE `campaign` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cert_standard`
--

DROP TABLE IF EXISTS `cert_standard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cert_standard` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证标准ID',
                                 `standard_name` varchar(100) NOT NULL COMMENT '认证名称',
                                 `version` varchar(20) DEFAULT '1.0' COMMENT '版本号',
                                 `org_id` bigint DEFAULT NULL COMMENT '归属机构ID（NULL=平台通用）',
                                 `target_role` varchar(20) NOT NULL COMMENT '适用对象：student/expert/org_admin（与 sys_user.role 枚举一致）',
                                 `requirement_text` text COMMENT '认证要求表述（执行标准正文）',
                                 `need_manual_audit` tinyint DEFAULT '1' COMMENT '是否需要人工审核：0否（自动通过） 1是',
                                 `first_node_id` bigint DEFAULT NULL COMMENT '审批流程第一步节点ID（仅 need_manual_audit=1 时有效）',
                                 `is_enabled` tinyint DEFAULT '1' COMMENT '是否启用',
                                 `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_org_id` (`org_id`),
                                 KEY `idx_target_role` (`target_role`),
                                 KEY `idx_first_node_id` (`first_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认证标准表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cert_standard`
--

LOCK TABLES `cert_standard` WRITE;
/*!40000 ALTER TABLE `cert_standard` DISABLE KEYS */;
/*!40000 ALTER TABLE `cert_standard` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cert_audit_flow`
--

DROP TABLE IF EXISTS `cert_audit_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cert_audit_flow` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流程节点ID',
                                   `cert_standard_id` bigint NOT NULL COMMENT '所属认证标准ID',
                                   `auditor_id` bigint NOT NULL COMMENT '审核人员用户ID（sys_user.id）',
                                   `next_node_id` bigint DEFAULT NULL COMMENT '下一步流程节点ID（NULL表示流程结束，即审批通过）',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_cert_standard_id` (`cert_standard_id`),
                                   KEY `idx_auditor_id` (`auditor_id`),
                                   KEY `idx_next_node_id` (`next_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认证审批流程节点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cert_audit_flow`
--

LOCK TABLES `cert_audit_flow` WRITE;
/*!40000 ALTER TABLE `cert_audit_flow` DISABLE KEYS */;
/*!40000 ALTER TABLE `cert_audit_flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_rule`
--

DROP TABLE IF EXISTS `credit_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_rule` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
                               `event_code` varchar(50) NOT NULL COMMENT '事件编码',
                               `event_name` varchar(100) NOT NULL COMMENT '事件中文名',
                               `credit_value` int NOT NULL COMMENT '变动值（正数=加分，负数=扣分）',
                               `is_enabled` tinyint DEFAULT '1' COMMENT '是否启用：1启用，0停用',
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `start_time` datetime COMMENT '规则生效开始时间',
                               `end_time` datetime COMMENT '规则生效结束时间',
                               `project_id` bigint DEFAULT NULL,
                               `org_id` bigint DEFAULT NULL COMMENT '所属机构ID（NULL=平台通用）',
                               PRIMARY KEY (`id`),
                               KEY `fk_credit_rule_project` (`project_id`),
                               KEY `idx_org_id` (`org_id`),
                               CONSTRAINT `fk_credit_rule_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_rule`
--

LOCK TABLES `credit_rule` WRITE;
/*!40000 ALTER TABLE `credit_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `credit_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exchange_rule`
--

DROP TABLE IF EXISTS `exchange_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exchange_rule` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '兑换规则ID',
                                 `item_name` varchar(100) NOT NULL COMMENT '兑换品名称',
                                 `item_icon` varchar(200) DEFAULT NULL COMMENT '商品图标URL',
                                 `required_credit` int NOT NULL COMMENT '兑换所需积分数量',
                                 `stock` int DEFAULT '9999' COMMENT '总库存数量',
                                 `per_user_limit` int DEFAULT '1' COMMENT '每人限兑次数',
                                 `is_enabled` tinyint DEFAULT '1' COMMENT '是否启用',
                                 `org_id` bigint DEFAULT NULL COMMENT '所属机构ID（NULL=平台通用）',
                                 `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分兑换规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exchange_rule`
--

LOCK TABLES `exchange_rule` WRITE;
/*!40000 ALTER TABLE `exchange_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `exchange_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organization` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机构唯一ID',
                                `name` varchar(100) NOT NULL COMMENT '机构全称',
                                `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
                                `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                                `address` varchar(200) DEFAULT NULL COMMENT '机构地址',
                                `province` varchar(20) DEFAULT NULL COMMENT '所在省份',
                                `status` tinyint DEFAULT '0' COMMENT '状态：0待审核，1启用，2禁用，3已拒绝',
                                `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机构表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organization`
--

LOCK TABLES `organization` WRITE;
/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
                           `org_id` bigint NOT NULL COMMENT '发起该项目的机构ID',
                           `expert_id` bigint DEFAULT NULL COMMENT '负责该项目的专家ID',
                           `name` varchar(100) NOT NULL COMMENT '项目名称',
                           `description` text COMMENT '项目简介',
                           `credit_reward` int DEFAULT '0' COMMENT '完成项目获得的积分奖励',
                           `credit_price` int DEFAULT '0' COMMENT '报名项目需要消耗的积分费用，0表示免费',
                           `status` tinyint DEFAULT '0' COMMENT '状态：0待审核，1已上架，2已驳回，3已下架，4审核中',
                           `application_id` bigint DEFAULT NULL COMMENT '关联申请单ID',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_org_id` (`org_id`),
                           KEY `idx_expert_id` (`expert_id`),
                           KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_project`
--

DROP TABLE IF EXISTS `student_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_project` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名记录ID',
                                   `student_id` bigint NOT NULL COMMENT '学生ID，关联sys_user(id)',
                                   `project_id` bigint NOT NULL COMMENT '项目ID，关联project(id)',
                                   `status` varchar(20) DEFAULT '已报名' COMMENT '报名状态：已报名，进行中，已完成，已取消',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_student_project` (`student_id`,`project_id`),
                                   KEY `idx_project_id` (`project_id`),
                                   CONSTRAINT `fk_student_project_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_student_project_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生报名项目关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_project`
--

LOCK TABLES `student_project` WRITE;
/*!40000 ALTER TABLE `student_project` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` varchar(50) NOT NULL COMMENT '登录账号',
                            `password` varchar(255) NOT NULL COMMENT '加密后的密码',
                            `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
                            `role` varchar(20) NOT NULL COMMENT '角色：admin/org_admin/expert/student',
                            `org_id` bigint DEFAULT NULL COMMENT '所属机构ID',
                            `expert_field` varchar(100) DEFAULT NULL COMMENT '专家擅长领域',
                            `balance` int DEFAULT '0' COMMENT '当前可用总积分',
                            `status` tinyint DEFAULT '1' COMMENT '账号状态：1正常，0冻结',
                            `frozen_at` datetime DEFAULT NULL COMMENT '被冻结的时间',
                            `frozen_by` bigint DEFAULT NULL COMMENT '冻结操作人ID',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                            `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                                `event_code` varchar(50) NOT NULL COMMENT '业务事件编码',
                                `scope_type` varchar(20) NOT NULL COMMENT '范围：USER/ORG/ROLE/ALL',
                                `scope_value` varchar(50) DEFAULT NULL COMMENT '用户、机构或角色标识',
                                `category` varchar(20) NOT NULL COMMENT '分类：SYSTEM/APPLICATION/POINT/MALL',
                                `level` varchar(20) NOT NULL DEFAULT 'INFO' COMMENT '级别：INFO/SUCCESS/WARNING',
                                `title` varchar(100) NOT NULL COMMENT '通知标题',
                                `content` varchar(500) NOT NULL COMMENT '通知正文',
                                `source_type` varchar(30) DEFAULT NULL COMMENT '关联业务类型',
                                `source_id` bigint DEFAULT NULL COMMENT '关联业务ID',
                                `action_path` varchar(200) DEFAULT NULL COMMENT '前端跳转路径',
                                `actor_id` bigint DEFAULT NULL COMMENT '触发操作人ID',
                                `dedupe_key` varchar(150) DEFAULT NULL COMMENT '业务幂等键',
                                `status` varchar(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED/REVOKED',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `expires_at` datetime DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_notification_dedupe` (`dedupe_key`),
                                KEY `idx_notification_created` (`created_at`),
                                KEY `idx_notification_source` (`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内通知消息表';

--
-- Table structure for table `notification_recipient`
--

DROP TABLE IF EXISTS `notification_recipient`;
CREATE TABLE `notification_recipient` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '接收记录ID',
                                          `notification_id` bigint NOT NULL COMMENT '通知ID',
                                          `user_id` bigint NOT NULL COMMENT '接收用户ID',
                                          `read_at` datetime DEFAULT NULL COMMENT '阅读时间，NULL为未读',
                                          `confirmed_at` datetime DEFAULT NULL COMMENT '重要通知确认时间',
                                          `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `uk_notification_user` (`notification_id`,`user_id`),
                                          KEY `idx_recipient_unread` (`user_id`,`read_at`,`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知接收与已读状态表';

--
-- Table structure for table `campaign_enrollment`
--

DROP TABLE IF EXISTS `campaign_enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campaign_enrollment` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `campaign_id` bigint NOT NULL COMMENT '活动ID',
                                       `user_id` bigint NOT NULL COMMENT '用户ID',
                                       `enrolled_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_user_campaign` (`user_id`,`campaign_id`),
                                       KEY `idx_campaign` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动报名记录';

--
-- Dumping data for table `campaign_enrollment`
--

LOCK TABLES `campaign_enrollment` WRITE;
/*!40000 ALTER TABLE `campaign_enrollment` DISABLE KEYS */;
/*!40000 ALTER TABLE `campaign_enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_op_log`
--

DROP TABLE IF EXISTS `user_op_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_op_log` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `operator_id` bigint NOT NULL COMMENT '操作人ID',
                               `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
                               `target_user_id` bigint DEFAULT NULL COMMENT '被操作的用户ID，批量操作时为空',
                               `target_user_name` varchar(50) DEFAULT NULL COMMENT '被操作的用户名',
                               `action` varchar(30) NOT NULL COMMENT '操作类型：FREEZE/UNFREEZE/RESET_PW/BATCH_FREEZE/BATCH_UNFREEZE/CREATE/UPDATE/DELETE/PROJECT_AUDIT/PROJECT_OFFLINE/CAMPAIGN_ENROLL/CAMPAIGN_LEAVE/PROJECT_ENROLL/PROJECT_LEAVE/EARN',
                               `module` varchar(30) DEFAULT 'USER' COMMENT '模块：USER/CAMPAIGN/PROJECT/POINT/ENROLL',
                               `detail` varchar(255) DEFAULT NULL COMMENT '操作详情',
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               KEY `idx_operator` (`operator_id`),
                               KEY `idx_target` (`target_user_id`),
                               KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户操作日志';

--
-- Dumping data for table `user_op_log`
--

LOCK TABLES `user_op_log` WRITE;
/*!40000 ALTER TABLE `user_op_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_op_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_log`
--

DROP TABLE IF EXISTS `transaction_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_log` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
                                   `user_id` bigint NOT NULL COMMENT '用户ID',
                                   `amount` int NOT NULL COMMENT '变动金额（+增加，-减少）',
                                   `balance_after` int NOT NULL COMMENT '变动后的即时余额快照',
                                   `biz_type` ENUM('REWARD', 'EXCHANGE', 'REFUND', 'ADMIN', 'ATTACHMENT', 'UPDATE_ADJUST', 'DAILY') NOT NULL COMMENT '业务类型：REWARD/EXCHANGE/REFUND/ADMIN/ATTACHMENT(附加流水)/UPDATE_ADJUST(更新补差)/DAILY(每日打卡)',
                                   `related_rule_id` bigint DEFAULT NULL COMMENT '相关规则ID：REWARD对应积分规则id，EXCHANGE对应兑换规则id，REFUND对应原流水id，ATTACHMENT对应生成其的流水id，ADMIN无意义',
                                   `description` varchar(200) DEFAULT NULL COMMENT '备注说明',
                                   `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '交易发生时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_user_id` (`user_id`),
                                   KEY `idx_created_at` (`created_at`),
                                   KEY `idx_related_rule_id` (`related_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_log`
--

LOCK TABLES `transaction_log` WRITE;
/*!40000 ALTER TABLE `transaction_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `transaction_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expert_cert`
--

DROP TABLE IF EXISTS `expert_cert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expert_cert` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证记录ID',
  `expert_id` bigint NOT NULL COMMENT '专家用户ID',
  `cert_standard_id` bigint NOT NULL COMMENT '认证标准ID',
  `field_name` varchar(100) DEFAULT NULL COMMENT '认证领域名（快照）',
  `application_id` bigint DEFAULT NULL COMMENT '来源申请单ID',
  `status` tinyint DEFAULT '1' COMMENT '1有效 0撤销',
  `issued_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '颁发时间',
  `valid_until` datetime DEFAULT NULL COMMENT '资质有效期截止，NULL为长期',
  `revoke_reason` varchar(255) DEFAULT NULL COMMENT '撤销原因',
  `revoked_at` datetime DEFAULT NULL COMMENT '撤销时间',
  PRIMARY KEY (`id`),
  KEY `idx_expert_standard` (`expert_id`,`cert_standard_id`),
  KEY `idx_expert_id` (`expert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专家认证记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `student_cert`
--

DROP TABLE IF EXISTS `student_cert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_cert` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生证书ID',
  `student_id` bigint NOT NULL COMMENT '学生用户ID',
  `cert_standard_id` bigint NOT NULL COMMENT '认证标准ID',
  `application_id` bigint DEFAULT NULL COMMENT '来源申请单ID',
  `cert_no` varchar(50) NOT NULL COMMENT '证书编号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名快照',
  `cert_name` varchar(100) NOT NULL COMMENT '证书名称快照',
  `org_name` varchar(100) DEFAULT NULL COMMENT '发证机构快照',
  `verify_code` varchar(32) NOT NULL COMMENT '核验码',
  `status` tinyint DEFAULT '1' COMMENT '1有效 0撤销',
  `revoke_reason` varchar(200) DEFAULT NULL COMMENT '作废原因',
  `revoked_at` datetime DEFAULT NULL COMMENT '作废时间',
  `issued_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '颁发时间',
  `valid_until` datetime DEFAULT NULL COMMENT '有效期至',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_standard` (`student_id`,`cert_standard_id`),
  UNIQUE KEY `uk_cert_no` (`cert_no`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生证书发放记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'credit_bank'
--

--
-- Dumping routines for database 'credit_bank'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-10 21:06:15
