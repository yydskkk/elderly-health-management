-- ============================================================
-- 社区老年人健康管理服务平台 - 初始化数据脚本
-- 数据库：elderly_health
-- 说明：包含内置角色、默认管理员、数据字典、辖区、预警规则、权限
-- ============================================================

USE `elderly_health`;

-- ============================================================
-- 一、内置角色数据
-- ============================================================
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `is_builtin`, `create_by`, `update_by`) VALUES
(1, 'ELDERLY', '老年用户', '社区老年用户', 1, 1, 1),
(2, 'FAMILY', '家属', '老人家属', 1, 1, 1),
(3, 'DOCTOR', '社区医护人员', '社区卫生服务中心医护人员', 1, 1, 1),
(4, 'ADMIN', '系统管理员', '系统运维管理员', 1, 1, 1);

-- ============================================================
-- 二、默认管理员账号
-- 密码：admin123（BCrypt 加密）
-- ============================================================
INSERT INTO `sys_user` (`id`, `email`, `password`, `name`, `gender`, `age`, `status`, `create_by`, `update_by`) VALUES
(1, 'admin@elderly.com', '$2a$10$ajPWOlQ/ptyI6K0aDwrGBOZCqDX8rfAMaV4canKM5QDRGGUnP9vG2', '系统管理员', 1, 30, 1, 1, 1),
(2, 'elderly@test.com', '$2a$10$ajPWOlQ/ptyI6K0aDwrGBOZCqDX8rfAMaV4canKM5QDRGGUnP9vG2', '张大爷', 1, 72, 1, 1, 1),
(3, 'family@test.com', '$2a$10$ajPWOlQ/ptyI6K0aDwrGBOZCqDX8rfAMaV4canKM5QDRGGUnP9vG2', '张小华', 0, 40, 1, 1, 1),
(4, 'doctor@test.com', '$2a$10$ajPWOlQ/ptyI6K0aDwrGBOZCqDX8rfAMaV4canKM5QDRGGUnP9vG2', '李医生', 1, 35, 1, 1, 1);

-- 用户角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`, `update_by`) VALUES
(1, 1, 4, 1, 1),
(2, 2, 1, 1, 1),
(3, 3, 2, 1, 1),
(4, 4, 3, 1, 1);

-- 老年用户分配到阳光社区
INSERT INTO `sys_user_region` (`id`, `user_id`, `region_id`, `user_type`, `create_by`, `update_by`) VALUES
(1, 2, 1, 1, 1, 1);

-- 医护人员分配到阳光社区
INSERT INTO `sys_user_region` (`id`, `user_id`, `region_id`, `user_type`, `create_by`, `update_by`) VALUES
(2, 4, 1, 2, 1, 1);

-- 家属与老人关联
INSERT INTO `family_elderly_relation` (`id`, `elderly_id`, `family_id`, `relation_type`, `status`, `create_by`, `update_by`) VALUES
(1, 2, 3, '子女', 1, 1, 1);

-- 老年用户健康档案
INSERT INTO `elderly_profile` (`id`, `user_id`, `id_card`, `medical_history`, `allergy_history`, `family_history`, `create_by`, `update_by`) VALUES
(1, 2, '450102195401011234', '高血压5年', '青霉素过敏', '父亲有糖尿病史', '1', '1');

-- ============================================================
-- 三、健康数据类型字典
-- ============================================================
INSERT INTO `health_data_type` (`id`, `code`, `name`, `unit`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(1, 'BLOOD_PRESSURE', '血压', 'mmHg', '', 1, 1, 1),
(2, 'BLOOD_SUGAR', '血糖', 'mmol/L', '', 2, 1, 1),
(3, 'HEART_RATE', '心率', '次/分', '', 3, 1, 1),
(4, 'BLOOD_OXYGEN', '血氧', '%', '', 4, 1, 1);

-- ============================================================
-- 四、默认辖区
-- ============================================================
INSERT INTO `sys_region` (`id`, `region_name`, `description`, `status`, `create_by`, `update_by`) VALUES
(1, '阳光社区', '阳光社区辖区', 1, 1, 1),
(2, '幸福社区', '幸福社区辖区', 1, 1, 1);

-- ============================================================
-- 五、默认全局预警规则
-- ============================================================
INSERT INTO `alert_rule` (`id`, `data_type`, `name`, `min_value`, `max_value`, `min_value_high`, `max_value_high`, `min_value_low`, `max_value_low`, `alert_level`, `description`, `status`, `create_by`, `update_by`) VALUES
(1, 'BLOOD_PRESSURE', '血压正常范围', NULL, NULL, 90, 140, 60, 90, 2, '血压高压90-140mmHg，低压60-90mmHg，超出范围预警', 1, 1, 1),
(2, 'BLOOD_SUGAR', '血糖正常范围', 3.9, 6.1, NULL, NULL, NULL, NULL, 2, '空腹血糖3.9-6.1mmol/L，超出范围预警', 1, 1, 1),
(3, 'HEART_RATE', '心率正常范围', 60, 100, NULL, NULL, NULL, NULL, 2, '心率60-100次/分，超出范围预警', 1, 1, 1),
(4, 'BLOOD_OXYGEN', '血氧正常范围', 95, 100, NULL, NULL, NULL, NULL, 3, '血氧95-100%，低于95%为高危预警', 1, 1, 1);

-- ============================================================
-- 六、权限数据
-- 一级菜单（parent_id=0）：用户管理、健康档案管理、健康数据监测、预警通知、健康干预建议、系统管理
-- 二级菜单：各模块子菜单
-- 三级按钮：各菜单下的操作按钮
-- ============================================================

-- ---------- 一级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
-- 用户管理
(100, 0, 'user', '用户管理', 1, '/user', 'Layout', 'User', 1, 1, 1),
-- 健康档案管理
(200, 0, 'profile', '健康档案管理', 1, '/profile', 'Layout', 'Document', 2, 1, 1),
-- 健康数据监测
(300, 0, 'health-data', '健康数据监测', 1, '/health-data', 'Layout', 'Monitor', 3, 1, 1),
-- 预警通知
(400, 0, 'alert', '预警通知', 1, '/alert', 'Layout', 'Bell', 4, 1, 1),
-- 健康干预建议
(500, 0, 'advice', '健康干预建议', 1, '/advice', 'Layout', 'ChatDotRound', 5, 1, 1),
-- 系统管理
(600, 0, 'system', '系统管理', 1, '/system', 'Layout', 'Setting', 6, 1, 1);

-- ---------- 用户管理 - 二级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(101, 100, 'user:list', '用户列表', 1, 'list', 'user/UserList', '', 1, 1, 1),
(102, 100, 'user:role', '角色管理', 1, 'role', 'user/RoleList', '', 2, 1, 1),
(103, 100, 'user:relation', '家属老人关联', 1, 'relation', 'user/RelationList', '', 3, 1, 1);

-- ---------- 用户管理 - 三级按钮 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(1011, 101, 'user:add', '新增用户', 2, '', '', '', 1, 1, 1),
(1012, 101, 'user:edit', '编辑用户', 2, '', '', '', 2, 1, 1),
(1013, 101, 'user:delete', '删除用户', 2, '', '', '', 3, 1, 1),
(1014, 101, 'user:reset', '重置密码', 2, '', '', '', 4, 1, 1),
(1015, 101, 'user:status', '启用/禁用', 2, '', '', '', 5, 1, 1),
(1021, 102, 'role:add', '新增角色', 2, '', '', '', 1, 1, 1),
(1022, 102, 'role:edit', '编辑角色', 2, '', '', '', 2, 1, 1),
(1023, 102, 'role:delete', '删除角色', 2, '', '', '', 3, 1, 1),
(1024, 102, 'role:permission', '分配权限', 2, '', '', '', 4, 1, 1),
(1031, 103, 'relation:invite', '邀请关联', 2, '', '', '', 1, 1, 1),
(1032, 103, 'relation:apply', '申请关联', 2, '', '', '', 2, 1, 1),
(1033, 103, 'relation:confirm', '确认关联', 2, '', '', '', 3, 1, 1),
(1034, 103, 'relation:reject', '拒绝关联', 2, '', '', '', 4, 1, 1),
(1035, 103, 'relation:delete', '解除关联', 2, '', '', '', 5, 1, 1);

-- ---------- 健康档案管理 - 二级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(201, 200, 'profile:list', '档案列表', 1, 'list', 'profile/ProfileList', '', 1, 1, 1),
(202, 200, 'profile:mine', '我的档案', 1, 'mine', 'profile/MyProfile', '', 2, 1, 1);

-- ---------- 健康档案管理 - 三级按钮 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(2011, 201, 'profile:view', '查看档案', 2, '', '', '', 1, 1, 1),
(2012, 201, 'profile:edit', '编辑档案', 2, '', '', '', 2, 1, 1),
(2013, 201, 'profile:export', '导出档案', 2, '', '', '', 3, 1, 1),
(2021, 202, 'profile:view:mine', '查看我的档案', 2, '', '', '', 1, 1, 1);

-- ---------- 健康数据监测 - 二级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(301, 300, 'health-data:input', '数据录入', 1, 'input', 'healthdata/DataInput', '', 1, 1, 1),
(302, 300, 'health-data:view', '数据查看', 1, 'view', 'healthdata/DataView', '', 2, 1, 1),
(303, 300, 'health-data:type', '数据类型管理', 1, 'type', 'healthdata/DataType', '', 3, 1, 1);

-- ---------- 健康数据监测 - 三级按钮 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(3011, 301, 'health-data:add', '录入数据', 2, '', '', '', 1, 1, 1),
(3012, 301, 'health-data:edit', '编辑数据', 2, '', '', '', 2, 1, 1),
(3013, 301, 'health-data:delete', '删除数据', 2, '', '', '', 3, 1, 1),
(3021, 302, 'health-data:trend', '查看趋势图', 2, '', '', '', 1, 1, 1),
(3022, 302, 'health-data:statistics', '查看统计', 2, '', '', '', 2, 1, 1),
(3023, 302, 'health-data:export', '导出数据', 2, '', '', '', 3, 1, 1),
(3031, 303, 'datatype:add', '新增数据类型', 2, '', '', '', 1, 1, 1),
(3032, 303, 'datatype:edit', '编辑数据类型', 2, '', '', '', 2, 1, 1),
(3033, 303, 'datatype:delete', '删除数据类型', 2, '', '', '', 3, 1, 1);

-- ---------- 预警通知 - 二级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(401, 400, 'alert:rule', '预警规则', 1, 'rule', 'alert/AlertRule', '', 1, 1, 1),
(402, 400, 'alert:personal', '个性化规则', 1, 'personal', 'alert/PersonalRule', '', 2, 1, 1),
(403, 400, 'alert:record', '预警记录', 1, 'record', 'alert/AlertRecord', '', 3, 1, 1),
(404, 400, 'alert:notification', '预警通知', 1, 'notification', 'alert/AlertNotification', '', 4, 1, 1);

-- ---------- 预警通知 - 三级按钮 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(4011, 401, 'rule:add', '新增规则', 2, '', '', '', 1, 1, 1),
(4012, 401, 'rule:edit', '编辑规则', 2, '', '', '', 2, 1, 1),
(4013, 401, 'rule:delete', '删除规则', 2, '', '', '', 3, 1, 1),
(4014, 401, 'rule:status', '启用/禁用规则', 2, '', '', '', 4, 1, 1),
(4021, 402, 'personal:add', '新增个性化规则', 2, '', '', '', 1, 1, 1),
(4022, 402, 'personal:edit', '编辑个性化规则', 2, '', '', '', 2, 1, 1),
(4023, 402, 'personal:delete', '删除个性化规则', 2, '', '', '', 3, 1, 1),
(4031, 403, 'record:handle', '处理预警', 2, '', '', '', 1, 1, 1),
(4032, 403, 'record:view', '查看预警详情', 2, '', '', '', 2, 1, 1),
(4041, 404, 'notification:read', '标记已读', 2, '', '', '', 1, 1, 1),
(4042, 404, 'notification:view', '查看通知', 2, '', '', '', 2, 1, 1);

-- ---------- 健康干预建议 - 二级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(501, 500, 'advice:list', '健康建议', 1, 'list', 'advice/AdviceList', '', 1, 1, 1),
(502, 500, 'advice:plan', '干预方案', 1, 'plan', 'advice/InterventionPlan', '', 2, 1, 1),
(503, 500, 'advice:feedback', '执行反馈', 1, 'feedback', 'advice/FeedbackList', '', 3, 1, 1);

-- ---------- 健康干预建议 - 三级按钮 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(5011, 501, 'advice:add', '发布建议', 2, '', '', '', 1, 1, 1),
(5012, 501, 'advice:edit', '编辑建议', 2, '', '', '', 2, 1, 1),
(5013, 501, 'advice:delete', '删除建议', 2, '', '', '', 3, 1, 1),
(5014, 501, 'advice:view', '查看建议', 2, '', '', '', 4, 1, 1),
(5021, 502, 'plan:add', '新增方案', 2, '', '', '', 1, 1, 1),
(5022, 502, 'plan:edit', '编辑方案', 2, '', '', '', 2, 1, 1),
(5023, 502, 'plan:terminate', '终止方案', 2, '', '', '', 3, 1, 1),
(5024, 502, 'plan:view', '查看方案', 2, '', '', '', 4, 1, 1),
(5031, 503, 'feedback:add', '提交反馈', 2, '', '', '', 1, 1, 1),
(5032, 503, 'feedback:view', '查看反馈', 2, '', '', '', 2, 1, 1);

-- ---------- 系统管理 - 二级菜单 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(601, 600, 'system:region', '辖区管理', 1, 'region', 'system/RegionList', '', 1, 1, 1),
(602, 600, 'system:statistics', '数据统计', 1, 'statistics', 'system/Statistics', '', 2, 1, 1),
(603, 600, 'system:log', '操作日志', 1, 'log', 'system/OperationLog', '', 3, 1, 1),
(604, 600, 'system:profile', '个人信息', 1, 'profile', 'system/Profile', '', 4, 1, 1);

-- ---------- 系统管理 - 三级按钮 ----------
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_code`, `permission_name`, `type`, `path`, `component`, `icon`, `sort`, `create_by`, `update_by`) VALUES
(6011, 601, 'region:add', '新增辖区', 2, '', '', '', 1, 1, 1),
(6012, 601, 'region:edit', '编辑辖区', 2, '', '', '', 2, 1, 1),
(6013, 601, 'region:delete', '删除辖区', 2, '', '', '', 3, 1, 1),
(6014, 601, 'region:status', '启用/禁用辖区', 2, '', '', '', 4, 1, 1),
(6015, 601, 'region:assign', '分配用户', 2, '', '', '', 5, 1, 1),
(6021, 602, 'statistics:user', '用户统计', 2, '', '', '', 1, 1, 1),
(6022, 602, 'statistics:health', '健康数据统计', 2, '', '', '', 2, 1, 1),
(6023, 602, 'statistics:alert', '预警统计', 2, '', '', '', 3, 1, 1),
(6031, 603, 'log:view', '查看日志', 2, '', '', '', 1, 1, 1),
(6032, 603, 'log:export', '导出日志', 2, '', '', '', 2, 1, 1),
(6033, 603, 'log:delete', '删除日志', 2, '', '', '', 3, 1, 1),
(6041, 604, 'profile:edit:mine', '编辑个人信息', 2, '', '', '', 1, 1, 1),
(6042, 604, 'profile:avatar', '修改头像', 2, '', '', '', 2, 1, 1),
(6043, 604, 'profile:password', '修改密码', 2, '', '', '', 3, 1, 1);

-- ============================================================
-- 七、管理员角色权限分配（角色ID=4 拥有全部权限）
-- ============================================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_by`, `update_by`) VALUES
-- 一级菜单
(4, 100, 1, 1), (4, 200, 1, 1), (4, 300, 1, 1), (4, 400, 1, 1), (4, 500, 1, 1), (4, 600, 1, 1),
-- 用户管理子菜单及按钮
(4, 101, 1, 1), (4, 102, 1, 1), (4, 103, 1, 1),
(4, 1011, 1, 1), (4, 1012, 1, 1), (4, 1013, 1, 1), (4, 1014, 1, 1), (4, 1015, 1, 1),
(4, 1021, 1, 1), (4, 1022, 1, 1), (4, 1023, 1, 1), (4, 1024, 1, 1),
(4, 1031, 1, 1), (4, 1032, 1, 1), (4, 1033, 1, 1), (4, 1034, 1, 1), (4, 1035, 1, 1),
-- 健康档案管理子菜单及按钮
(4, 201, 1, 1), (4, 202, 1, 1),
(4, 2011, 1, 1), (4, 2012, 1, 1), (4, 2013, 1, 1), (4, 2021, 1, 1),
-- 健康数据监测子菜单及按钮
(4, 301, 1, 1), (4, 302, 1, 1), (4, 303, 1, 1),
(4, 3011, 1, 1), (4, 3012, 1, 1), (4, 3013, 1, 1),
(4, 3021, 1, 1), (4, 3022, 1, 1), (4, 3023, 1, 1),
(4, 3031, 1, 1), (4, 3032, 1, 1), (4, 3033, 1, 1),
-- 预警通知子菜单及按钮
(4, 401, 1, 1), (4, 402, 1, 1), (4, 403, 1, 1), (4, 404, 1, 1),
(4, 4011, 1, 1), (4, 4012, 1, 1), (4, 4013, 1, 1), (4, 4014, 1, 1),
(4, 4021, 1, 1), (4, 4022, 1, 1), (4, 4023, 1, 1),
(4, 4031, 1, 1), (4, 4032, 1, 1),
(4, 4041, 1, 1), (4, 4042, 1, 1),
-- 健康干预建议子菜单及按钮
(4, 501, 1, 1), (4, 502, 1, 1), (4, 503, 1, 1),
(4, 5011, 1, 1), (4, 5012, 1, 1), (4, 5013, 1, 1), (4, 5014, 1, 1),
(4, 5021, 1, 1), (4, 5022, 1, 1), (4, 5023, 1, 1), (4, 5024, 1, 1),
(4, 5031, 1, 1), (4, 5032, 1, 1),
-- 系统管理子菜单及按钮
(4, 601, 1, 1), (4, 602, 1, 1), (4, 603, 1, 1), (4, 604, 1, 1),
(4, 6011, 1, 1), (4, 6012, 1, 1), (4, 6013, 1, 1), (4, 6014, 1, 1), (4, 6015, 1, 1),
(4, 6021, 1, 1), (4, 6022, 1, 1), (4, 6023, 1, 1),
(4, 6031, 1, 1), (4, 6032, 1, 1), (4, 6033, 1, 1),
(4, 6041, 1, 1), (4, 6042, 1, 1), (4, 6043, 1, 1);
