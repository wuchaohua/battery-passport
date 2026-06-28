# Battery Passport Platform

电池护照平台 -- 面向储能行业的电池全生命周期管理平台

技术栈: Spring Cloud Alibaba 2022.x + JDK 17 + React 18
交付模式: 私有化部署 + SaaS 多租户

---

## 概览

Battery Passport Platform 是一套基于微服务架构的电池护照管理系统，
覆盖电池从生产到回收的全生命周期数据管理。

同一套代码支持三种交付模式:

- 私有化部署: 混淆 jar + Docker 镜像交付，不提供 Java 源码
- SaaS 多租户: 云原生部署，租户隔离
- 源码交付: 含 SPI 扩展接口源码，支持二次开发

## 微服务清单

服务 | 端口 | 职责
--- | --- | ---
gateway-service | 8080 | 统一网关、路由、限流
auth-service | 8081 | OAuth2 授权、SSO 适配器
battery-service | 8084 | 电池护照 CRUD、生命周期管理
tenant-service | 8083 | 租户管理(仅SaaS)
integration-service | 8089 | ERP 对接、SSO 配置

## 技术栈

### 后端
- JDK 17 (LTS)
- Spring Boot 3.1 + Spring Cloud Alibaba 2022
- Nacos (注册/配置)
- Spring Cloud Gateway + Sentinel
- MyBatis-Plus 3.5
- MySQL 8.0 | Redis 7 | RocketMQ 5
- Spring Security + JWT + 企业 SSO 适配

### 前端
- React 18 + TypeScript
- Vite 5 + Turborepo
- Ant Design Pro 5

## 快速开始

```bash
# 1. 启动基础设施
docker compose -f docker/local/docker-compose.yml up -d nacos mysql redis

# 2. 构建公共模块
mvn clean install -f platform/plugin-spi/pom.xml -DskipTests
mvn clean install -f platform/common-core/pom.xml -DskipTests
mvn clean install -f platform/common-security/pom.xml -DskipTests
mvn clean install -f platform/common-mybatis/pom.xml -DskipTests

# 3. 并行启动微服务
mvn spring-boot:run -f services/gateway-service/pom.xml &
mvn spring-boot:run -f services/auth-service/pom.xml &
mvn spring-boot:run -f services/battery-service/pom.xml &
mvn spring-boot:run -f services/tenant-service/pom.xml &

# 4. 启动前端
cd frontend && npm ci && npm run dev
```

## 私有化部署定制方式

客户方在不获取 Java 源码的前提下，通过以下机制实现业务定制:

- SPI 插件: 实现公开接口注册插件 jar (热加载)
- REST API: 调用开放接口 / Webhook
- 脚本引擎: Groovy 规则脚本热加载
- 前端扩展: 远程 React 组件注入
- 配置化: yaml 覆盖定制

详见: battery-passport-extension 二次开发示例工程

## 项目链接

- 主项目: https://github.com/wuchaohua/battery-passport
- 扩展示例: https://github.com/wuchaohua/battery-passport-extension