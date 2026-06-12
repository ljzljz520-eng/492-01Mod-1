SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

-- 班组表
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_name` varchar(100) NOT NULL COMMENT '班组名称',
  `team_leader` varchar(50) DEFAULT NULL COMMENT '班组长',
  `team_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `quota` int(11) DEFAULT '0' COMMENT '招聘名额',
  `used_quota` int(11) DEFAULT '0' COMMENT '已用名额',
  `work_shift` varchar(20) DEFAULT NULL COMMENT '班次（早班/中班/晚班）',
  `description` varchar(500) DEFAULT NULL COMMENT '班组描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_team_name` (`team_name`),
  KEY `idx_work_shift` (`work_shift`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组表';

-- 候选人表
DROP TABLE IF EXISTS `candidate`;
CREATE TABLE `candidate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `candidate_name` varchar(50) NOT NULL COMMENT '候选人姓名',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证号',
  `position` varchar(100) DEFAULT NULL COMMENT '应聘岗位',
  `interview_status` varchar(20) DEFAULT 'pending' COMMENT '面试状态（pending-待面试，passed-通过，failed-未通过）',
  `interview_score` int(11) DEFAULT NULL COMMENT '面试评分',
  `interview_remark` varchar(500) DEFAULT NULL COMMENT '面试评语',
  `is_in_risk` tinyint(1) DEFAULT '0' COMMENT '是否在风险名单（0-否，1-是）',
  `absent_count` int(11) DEFAULT '0' COMMENT '爽约次数',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态（active-活跃，hired-已录用，rejected-已拒绝）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_candidate_name` (`candidate_name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_interview_status` (`interview_status`),
  KEY `idx_is_in_risk` (`is_in_risk`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人表';

-- 试岗安排表
DROP TABLE IF EXISTS `trial_assignment`;
CREATE TABLE `trial_assignment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `candidate_id` bigint(20) NOT NULL COMMENT '候选人ID',
  `candidate_name` varchar(50) DEFAULT NULL COMMENT '候选人姓名',
  `team_id` bigint(20) NOT NULL COMMENT '班组ID',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `trial_date` date NOT NULL COMMENT '试岗日期',
  `trial_shift` varchar(20) DEFAULT NULL COMMENT '试岗班次（早班/中班/晚班）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `arrange_user_id` bigint(20) DEFAULT NULL COMMENT '安排人ID',
  `arrange_user_name` varchar(50) DEFAULT NULL COMMENT '安排人姓名',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态（pending-待试岗，in_progress-试岗中，completed-已完成，cancelled-已取消，absent-未到岗）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_candidate_id` (`candidate_id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_trial_date` (`trial_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试岗安排表';

-- 试岗记录表
DROP TABLE IF EXISTS `trial_record`;
CREATE TABLE `trial_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assignment_id` bigint(20) NOT NULL COMMENT '试岗安排ID',
  `candidate_id` bigint(20) NOT NULL COMMENT '候选人ID',
  `candidate_name` varchar(50) DEFAULT NULL COMMENT '候选人姓名',
  `team_id` bigint(20) NOT NULL COMMENT '班组ID',
  `team_name` varchar(100) DEFAULT NULL COMMENT '班组名称',
  `trial_date` date DEFAULT NULL COMMENT '试岗日期',
  `is_arrived` tinyint(1) DEFAULT '0' COMMENT '是否到岗（0-否，1-是）',
  `arrive_time` datetime DEFAULT NULL COMMENT '到岗时间',
  `safety_training` tinyint(1) DEFAULT '0' COMMENT '是否完成安全培训（0-否，1-是）',
  `training_content` varchar(500) DEFAULT NULL COMMENT '培训内容',
  `performance_score` int(11) DEFAULT NULL COMMENT '试岗表现评分（1-10分）',
  `performance_remark` varchar(1000) DEFAULT NULL COMMENT '试岗表现评语',
  `can_convert` varchar(20) DEFAULT 'pending' COMMENT '能否转正（pending-待定，yes-可以，no-不可以）',
  `convert_remark` varchar(500) DEFAULT NULL COMMENT '转正意见',
  `record_user_id` bigint(20) DEFAULT NULL COMMENT '记录人ID',
  `record_user_name` varchar(50) DEFAULT NULL COMMENT '记录人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_assignment_id` (`assignment_id`),
  KEY `idx_candidate_id` (`candidate_id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_trial_date` (`trial_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试岗记录表';

-- 风险名单表
DROP TABLE IF EXISTS `risk_list`;
CREATE TABLE `risk_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `candidate_id` bigint(20) NOT NULL COMMENT '候选人ID',
  `candidate_name` varchar(50) DEFAULT NULL COMMENT '候选人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `absent_count` int(11) DEFAULT '0' COMMENT '爽约次数',
  `reason` varchar(500) DEFAULT NULL COMMENT '列入原因',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态（active-生效中，removed-已移除）',
  `remove_reason` varchar(500) DEFAULT NULL COMMENT '移除原因',
  `remove_time` datetime DEFAULT NULL COMMENT '移除时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_candidate_id` (`candidate_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险名单表';

-- 插入测试班组数据
INSERT INTO `team` (`team_name`, `team_leader`, `team_phone`, `quota`, `used_quota`, `work_shift`, `description`) VALUES
('生产一组', '张组长', '13800138001', 5, 0, 'morning', '负责产品组装'),
('生产二组', '李组长', '13800138002', 5, 0, 'afternoon', '负责产品测试'),
('包装组', '王组长', '13800138003', 3, 0, 'night', '负责产品包装');

-- 插入测试候选人数据
INSERT INTO `candidate` (`candidate_name`, `gender`, `age`, `phone`, `position`, `interview_status`, `interview_score`, `interview_remark`, `status`) VALUES
('张三', '男', 28, '13900139001', '普工', 'passed', 85, '表现良好，有相关经验', 'active'),
('李四', '女', 25, '13900139002', '普工', 'passed', 80, '沟通能力强', 'active'),
('王五', '男', 30, '13900139003', '普工', 'pending', NULL, '待面试', 'active'),
('赵六', '女', 27, '13900139004', '质检', 'passed', 90, '细心负责，有质检经验', 'active');

SET FOREIGN_KEY_CHECKS = 1;
