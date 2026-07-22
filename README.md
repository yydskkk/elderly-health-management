# 社区老年人健康管理服务平台

> 基于 SpringBoot + Vue3 前后端分离的社区老年人健康管理系统

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | SpringBoot + MyBatis-Plus + MySQL + JWT + Spring Security |
| 前端 | Vue3 + Element Plus + ECharts + Axios |
| 文档 | Knife4j (Swagger) |
| 工具 | Maven + IDEA + VS Code |

---

## 功能模块

- **用户权限**：基于 RBAC 模型的权限管理，支持多角色（管理员、医生、家属、老人）
- **健康档案**：老人基本信息管理、家属关联
- **健康数据**：血压、血糖、心率、血氧等数据录入与记录
- **预警系统**：全局预警规则 + 个性化预警规则，异常数据自动预警
- **干预方案**：医生制定健康干预方案，跟踪执行反馈
- **数据统计**：ECharts 可视化展示健康数据趋势
- **操作日志**：AOP 记录关键操作，便于审计

---

## 数据库设计

共设计 **20 张业务表**，涵盖：
- 用户权限体系（RBAC：用户、角色、权限、辖区）
- 健康档案与数据管理
- 预警规则与通知
- 干预方案与反馈
- 系统日志与验证码

> 提供 `db/schema.sql` 和 `db/data.sql` 一键建库建表

---

## 快速开始

### 1. 创建数据库
```sql
mysql -u root -p
source backend/src/main/resources/db/schema.sql;
source backend/src/main/resources/db/data.sql;
2. 配置后端
在 backend/src/main/resources/ 下创建 application.yml：
yaml
server:
  port: 8080

spring:
  application:
    name: elderly-health
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/elderly_health?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: 你的数据库用户名
    password: 你的数据库密码
  mail:
    host: smtp.qq.com
    port: 587
    username: 你的邮箱
    password: 你的邮箱授权码

jwt:
  secret: 你的JWT密钥
  expiration: 86400000

knife4j:
  enable: true
3. 启动后端
bash
cd backend
mvn spring-boot:run
4. 启动前端
bash
cd frontend
npm install
npm run dev
接口文档
启动后端后访问：http://localhost:8080/doc.html
