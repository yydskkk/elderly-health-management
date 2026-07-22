-- ============================================================
-- 社区老年人健康管理服务平台 - 数据库表结构创建脚本
-- 数据库：elderly_health
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_general_ci
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `elderly_health` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `elderly_health`;

-- ============================================================
-- 一、用户权限表
-- ============================================================

-- 1. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `gender` TINYINT DEFAULT NULL COMMENT '性别：0女1男',
  `age` INT DEFAULT NULL COMMENT '年龄',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用1启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 2. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `is_builtin` TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置：0否1是（内置角色不可删）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- 3. 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID，0表示顶级',
  `permission_code` VARCHAR(100) DEFAULT NULL COMMENT '权限编码',
  `permission_name` VARCHAR(50) DEFAULT NULL COMMENT '权限名称',
  `type` TINYINT DEFAULT NULL COMMENT '类型：1菜单2按钮',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- 4. 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- 5. 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- ============================================================
-- 二、辖区与关联关系表
-- ============================================================

-- 6. 辖区表
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_name` VARCHAR(100) NOT NULL COMMENT '辖区名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='辖区表';

-- 7. 用户辖区关联表
DROP TABLE IF EXISTS `sys_user_region`;
CREATE TABLE `sys_user_region` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `region_id` BIGINT NOT NULL COMMENT '辖区ID',
  `user_type` TINYINT NOT NULL COMMENT '用户类型：1老人2医护',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_region_id` (`region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户辖区关联表';

-- 8. 家属老人关联表
DROP TABLE IF EXISTS `family_elderly_relation`;
CREATE TABLE `family_elderly_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` BIGINT NOT NULL COMMENT '家属用户ID',
  `elderly_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `relation_type` VARCHAR(20) DEFAULT NULL COMMENT '关系类型，如父子、母女',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待确认1已关联2已拒绝',
  `invite_way` TINYINT DEFAULT NULL COMMENT '关联方式：1老人邀请2家属申请3代关联',
  `initiator_id` BIGINT DEFAULT NULL COMMENT '发起人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_elderly_id` (`elderly_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家属老人关联表';

-- ============================================================
-- 三、健康档案与数据表
-- ============================================================

-- 9. 健康档案表
DROP TABLE IF EXISTS `elderly_profile`;
CREATE TABLE `elderly_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '关联sys_user的用户ID',
  `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '居住地址',
  `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
  `medical_history` TEXT COMMENT '既往病史',
  `allergy_history` TEXT COMMENT '过敏史',
  `family_history` TEXT COMMENT '家族病史',
  `blood_type` VARCHAR(10) DEFAULT NULL COMMENT '血型',
  `height` DECIMAL(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健康档案表';

-- 10. 健康数据记录表
DROP TABLE IF EXISTS `health_data`;
CREATE TABLE `health_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型code（health_data_type的code）',
  `value_high` DECIMAL(10,2) DEFAULT NULL COMMENT '高压值（血压专用）',
  `value_low` DECIMAL(10,2) DEFAULT NULL COMMENT '低压值（血压专用）',
  `value` DECIMAL(10,2) DEFAULT NULL COMMENT '单值指标（血糖、心率、血氧等）',
  `measure_time` DATETIME NOT NULL COMMENT '测量时间',
  `recorder_id` BIGINT NOT NULL COMMENT '录入人ID',
  `is_abnormal` TINYINT NOT NULL DEFAULT 0 COMMENT '是否异常：0正常1异常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_measure_time` (`measure_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健康数据记录表';

-- 11. 数据类型字典表
DROP TABLE IF EXISTS `health_data_type`;
CREATE TABLE `health_data_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(20) NOT NULL COMMENT '类型编码，如BLOOD_PRESSURE',
  `name` VARCHAR(50) NOT NULL COMMENT '类型名称，如血压',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位，如mmHg',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort` INT DEFAULT NULL COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据类型字典表';

-- ============================================================
-- 四、预警相关表
-- ============================================================

-- 12. 全局预警规则表
DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型code',
  `name` VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
  `min_value` DECIMAL(10,2) DEFAULT NULL COMMENT '单值指标下限',
  `max_value` DECIMAL(10,2) DEFAULT NULL COMMENT '单值指标上限',
  `min_value_high` DECIMAL(10,2) DEFAULT NULL COMMENT '血压高压下限',
  `max_value_high` DECIMAL(10,2) DEFAULT NULL COMMENT '血压高压上限',
  `min_value_low` DECIMAL(10,2) DEFAULT NULL COMMENT '血压低压下限',
  `max_value_low` DECIMAL(10,2) DEFAULT NULL COMMENT '血压低压上限',
  `alert_level` TINYINT DEFAULT NULL COMMENT '预警等级：1低2中3高',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='全局预警规则表';

-- 13. 个性化预警规则表
DROP TABLE IF EXISTS `alert_rule_personal`;
CREATE TABLE `alert_rule_personal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型code',
  `name` VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
  `min_value` DECIMAL(10,2) DEFAULT NULL COMMENT '单值指标下限',
  `max_value` DECIMAL(10,2) DEFAULT NULL COMMENT '单值指标上限',
  `min_value_high` DECIMAL(10,2) DEFAULT NULL COMMENT '血压高压下限',
  `max_value_high` DECIMAL(10,2) DEFAULT NULL COMMENT '血压高压上限',
  `min_value_low` DECIMAL(10,2) DEFAULT NULL COMMENT '血压低压下限',
  `max_value_low` DECIMAL(10,2) DEFAULT NULL COMMENT '血压低压上限',
  `alert_level` TINYINT DEFAULT NULL COMMENT '预警等级：1低2中3高',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_data_type` (`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个性化预警规则表';

-- 14. 预警记录表
DROP TABLE IF EXISTS `alert_record`;
CREATE TABLE `alert_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `data_type` VARCHAR(20) DEFAULT NULL COMMENT '数据类型code',
  `data_id` BIGINT DEFAULT NULL COMMENT '关联health_data的ID',
  `value` VARCHAR(100) DEFAULT NULL COMMENT '异常值描述',
  `alert_level` TINYINT DEFAULT NULL COMMENT '预警等级：1低2中3高',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待处理1已处理',
  `handle_opinion` TEXT COMMENT '处理意见',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警记录表';

-- 15. 预警通知表
DROP TABLE IF EXISTS `alert_notification`;
CREATE TABLE `alert_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_record_id` BIGINT NOT NULL COMMENT '预警记录ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读1已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_alert_record_id` (`alert_record_id`),
  KEY `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预警通知表';

-- ============================================================
-- 五、健康建议与干预方案表
-- ============================================================

-- 16. 健康建议表
DROP TABLE IF EXISTS `health_advice`;
CREATE TABLE `health_advice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `doctor_id` BIGINT NOT NULL COMMENT '发布医生ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '建议标题',
  `content` TEXT NOT NULL COMMENT '建议内容',
  `advice_type` VARCHAR(50) DEFAULT NULL COMMENT '建议类型，如饮食/运动/用药',
  `expire_time` DATETIME DEFAULT NULL COMMENT '有效期',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0失效1有效',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健康建议表';

-- 17. 干预方案表
DROP TABLE IF EXISTS `intervention_plan`;
CREATE TABLE `intervention_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `advice_id` BIGINT NOT NULL COMMENT '关联health_advice的ID',
  `user_id` BIGINT NOT NULL COMMENT '老人用户ID',
  `content` TEXT NOT NULL COMMENT '干预内容',
  `start_date` DATE DEFAULT NULL COMMENT '开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期',
  `cycle` VARCHAR(50) DEFAULT NULL COMMENT '执行周期，如每日/每周',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0已终止1进行中2已完成',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_advice_id` (`advice_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='干预方案表';

-- 18. 干预方案执行反馈表
DROP TABLE IF EXISTS `intervention_feedback`;
CREATE TABLE `intervention_feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` BIGINT NOT NULL COMMENT '干预方案ID',
  `user_id` BIGINT NOT NULL COMMENT '反馈人ID',
  `is_executed` TINYINT DEFAULT NULL COMMENT '是否执行：0未执行1已执行',
  `execution_feeling` TEXT COMMENT '执行感受',
  `abnormal_situation` TEXT COMMENT '异常情况',
  `feedback_time` DATETIME DEFAULT NULL COMMENT '反馈时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='干预方案执行反馈表';

-- ============================================================
-- 六、系统管理表
-- ============================================================

-- 19. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
  `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作内容',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '操作地点',
  `time` BIGINT DEFAULT NULL COMMENT '耗时(ms)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- 20. 验证码表
DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `purpose` VARCHAR(20) NOT NULL COMMENT '用途：REGISTER/FORGET_PASSWORD',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用：0未使用1已使用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='验证码表';
