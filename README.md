 # Battery Passport Platform

 > 电池护照平台 — 面向储能行业的电池全生命周期管理平台
 >
 > 技术栈: Spring Cloud Alibaba 2022.x + JDK 17 + React 18
 >
 > 交付模式: 私有化部署（天合储能、托邦股份）+ SaaS 多租户

 ---

 ## 概览

 Battery Passport Platform 是一套基于微服务架构的电池护照管理系统，覆盖电池从生产、流通、使用到回收的全生命周期数据管理，满足欧盟电池法规（EU 2023/1542）合规要求。

 同一套代码支持三种交付模式：

 | 模式 | 说明 | 客户案例 |
 |------|------|---------|
 | 私有化部署 | 混淆 jar + Docker 镜像交付，不提供 Java 源码 | 天合储能、托邦股份 |
 | SaaS 多租户 | 云原生部署，租户隔离 | 中小型企业客户 |
 | 源码交付 | 含 SPI 扩展接口源码，支持客户二次开发 | 特定大客户 |

 ## 架构总览

 客户端 → Gateway → Auth / Battery / Tenant / Integration → Nacos / MySQL / Redis / RocketMQ

 ## 微服务清单

 | 服务 | 端口 | 职责 | SaaS | 私有化 |
 |------|------|------|------|-------|
 | gateway-service | 8080 | 统一网关、路由、限流 | ✅ | ✅ |
 | auth-service | 8081 | OAuth2 授权、SSO 适配器 | ✅ | ✅ |
 | battery-service | 8084 | 电池护照 CRUD、生命周期管理 | ✅ | ✅ |
 | tenant-service | 8083 | 租户管理、套餐、隔离（仅 SaaS） | ✅ | ❌ |
 | integration-service | 8089 | ERP 对接、SSO 配置管理 | ✅ | ✅ |

 ## 技术栈

 | 领域 | 选型 |
 |------|------|
 | 语言 | JDK 17 (LTS) |
 | 微服务框架 | Spring Boot 3.1 + Spring Cloud Alibaba 2022 |
 | 注册/配置 | Nacos |
 | 网关 | Spring Cloud Gateway + Sentinel |
 | ORM | MyBatis-Plus 3.5 |
 | 数据库 | MySQL 8.0 |
 | 缓存 | Redis 7 |
 | 消息 | RocketMQ 5.x |
 | 安全 | Spring Security + JWT + 企业 SSO 适配 |
 | 前端 | React 18 + TypeScript + Vite 5 + Turborepo + Ant Design Pro 5 |

 ## 目录结构

 ```
 battery-passport/
 ├── pom.xml                        # 父 POM（依赖管理 + Profile 切换）
 ├── .gitlab-ci.yml                 # GitLab CI/CD 流水线
 ├── Jenkinsfile                    # Jenkins Pipeline（含混淆阶段）
 ├── platform/                      # 公共基础模块（6 个组件）
 │   ├── plugin-spi/                #   插件 SPI 接口（battery-plugin-api）
 │   ├── common-core/               #   核心 DTO、异常、上下文
 │   ├── common-security/           #   SSO 适配器、JWT、安全过滤
 │   └── common-mybatis/            #   多租户拦截器、基类实体
 ├── services/                      # 微服务实现（5 个）
 │   ├── gateway-service/           #   Spring Cloud Gateway
 │   ├── auth-service/              #   认证中心（SSO 适配器模式）
 │   ├── battery-service/           #   电池护照核心（含 SPI 热加载）
 │   ├── tenant-service/            #   SaaS 租户管理
 │   └── integration-service/       #   企业 ERP/SSO 集成
 ├── frontend/                      # React Turborepo Monorepo
 ├── docker/local/                  # Docker Compose 开发环境
 ├── scripts/db/                    # 数据库初始化脚本
 └── helm-charts/                   # Kubernetes 部署编排
 ```

 ## 快速开始

 ### 环境要求
 - JDK 17+
 - Maven 3.9+
 - Node.js 18+ (前端)
 - Docker & Docker Compose (本地环境)

 ### 本地开发

 ```bash
 # 1. 启动基础设施（Nacos + MySQL + Redis）
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

 ### 生产构建

 ```bash
 # SaaS 生产
 bash build.sh saas-prod

 # 私有化交付（含混淆）
 bash build.sh onpremise-prod
 ```

 ## 部署模式

 ### 私有化部署

 交付物为混淆后的 jar 包 + Docker 镜像，不包含 Java 源码。客户方通过以下机制实现业务定制：

 | 定制方式 | 说明 | 示例 |
 |---------|------|------|
 | SPI 插件 | 实现公开接口注册插件 jar | 天合容量校验、托邦 ERP 转换 |
 | REST API | 调用开放接口/Webhook | ERP 同步、报告导出 |
 | 脚本引擎 | Groovy 规则脚本热加载 | 校验规则、报告水印 |
 | 前端扩展 | 远程 React 组件注入 | 天合专属仪表盘 |
 | 配置化 | yaml 覆盖定制 | SSO 地址、品牌色、限流 |

 详见 [battery-passport-extension](https://github.com/wuchaohua/battery-passport-extension) 二次开发示例工程。

 ### SaaS 多租户部署

 多租户特性：
 - MyBatis-Plus tenant_id 列级数据隔离
 - Nginx 请求头 X-Tenant-Id 路由分发
 - Kubernetes HPA 自动弹性伸缩
 - 租户粒度的套餐和配额管理

 ## 防反编译措施

 | 层级 | 技术 | 说明 |
 |------|------|------|
 | 混淆层 | ProGuard / CandyGuard | 类名/方法名混淆 + 字符串加密 |
 | 配置层 | Jasypt | 配置文件密码/secret 加密 |
 | 部署层 | JDK 17 密封类 | 核心模块防止继承 |
 | 运行时 | Plugin ClassLoader | 插件沙箱隔离 |

 ## 主要 API

 | 方法 | 端点 | 说明 |
 |------|------|------|
 | GET | /api/v1/passports | 护照列表（分页/筛选） |
 | POST | /api/v1/passports | 创建电池护照 |
 | GET | /api/v1/passports/{id} | 护照详情 |
 | PUT | /api/v1/passports/{id} | 更新护照 |
 | DELETE | /api/v1/passports/{id} | 删除护照 |
 | GET | /api/v1/passports/search | 序列号搜索 |
 | POST | /api/v1/auth/login | 获取登录 URL |
 | GET/POST | /api/v1/tenants | 租户管理（SaaS） |
 | POST | /api/v1/integration/erp/import | ERP 数据导入 |

 ## 项目路线图

 ```
 v1.0 --- 核心护照 CRUD + 天合储能私有化部署
 v1.1 --- 托邦股份部署 + SSO 适配器扩展
 v1.2 --- SaaS 多租户上线 + 运营控制台
 v2.0 --- 区块链溯源 + 政府监管上报
 v2.1 --- 欧盟电池法规合规
 v2.2 --- AI 寿命预测 + 梯次利用建议
 ```

 ## 许可证

 Copyright (c) 2026 电池护照平台。保留所有权利。
