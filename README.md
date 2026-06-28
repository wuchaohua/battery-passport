 # Battery Passport Platform

 > 鐢垫睜鎶ょ収骞冲彴 鈥?闈㈠悜鍌ㄨ兘琛屼笟鐨勭數姹犲叏鐢熷懡鍛ㄦ湡绠＄悊骞冲彴
 >
 > 鎶€鏈爤: Spring Cloud Alibaba 2022.x + JDK 17 + React 18
 >
 > 浜や粯妯″紡: 绉佹湁鍖栭儴缃诧紙澶╁悎鍌ㄨ兘銆佹墭閭﹁偂浠斤級+ SaaS 澶氱鎴?
 ---

 ## 姒傝

 Battery Passport Platform 鏄竴濂楀熀浜庡井鏈嶅姟鏋舵瀯鐨勭數姹犳姢鐓х鐞嗙郴缁燂紝瑕嗙洊鐢垫睜浠庣敓浜с€佹祦閫氥€佷娇鐢ㄥ埌鍥炴敹鐨勫叏鐢熷懡鍛ㄦ湡鏁版嵁绠＄悊锛屾弧瓒虫鐩熺數姹犳硶瑙勶紙EU 2023/1542锛夊悎瑙勮姹傘€?
 鍚屼竴濂椾唬鐮佹敮鎸佷笁绉嶄氦浠樻ā寮忥細

 | 妯″紡 | 璇存槑 | 瀹㈡埛妗堜緥 |
 |------|------|---------|
 | 绉佹湁鍖栭儴缃?| 娣锋穯 jar + Docker 闀滃儚浜や粯锛屼笉鎻愪緵 Java 婧愮爜 | 澶╁悎鍌ㄨ兘銆佹墭閭﹁偂浠?|
 | SaaS 澶氱鎴?| 浜戝師鐢熼儴缃诧紝绉熸埛闅旂 | 涓皬鍨嬩紒涓氬鎴?|
 | 婧愮爜浜や粯 | 鍚?SPI 鎵╁睍鎺ュ彛婧愮爜锛屾敮鎸佸鎴蜂簩娆″紑鍙?| 鐗瑰畾澶у鎴?|

 ## 鏋舵瀯鎬昏

 瀹㈡埛绔?鈫?Gateway 鈫?Auth / Battery / Tenant / Integration 鈫?Nacos / MySQL / Redis / RocketMQ

 ## 寰湇鍔℃竻鍗?
 | 鏈嶅姟 | 绔彛 | 鑱岃矗 | SaaS | 绉佹湁鍖?|
 |------|------|------|------|-------|
 | gateway-service | 8080 | 缁熶竴缃戝叧銆佽矾鐢便€侀檺娴?| 鉁?| 鉁?|
 | auth-service | 8081 | OAuth2 鎺堟潈銆丼SO 閫傞厤鍣?| 鉁?| 鉁?|
 | battery-service | 8084 | 鐢垫睜鎶ょ収 CRUD銆佺敓鍛藉懆鏈熺鐞?| 鉁?| 鉁?|
 | tenant-service | 8083 | 绉熸埛绠＄悊銆佸椁愩€侀殧绂伙紙浠?SaaS锛?| 鉁?| 鉂?|
 | integration-service | 8089 | ERP 瀵规帴銆丼SO 閰嶇疆绠＄悊 | 鉁?| 鉁?|

 ## 鎶€鏈爤

 | 棰嗗煙 | 閫夊瀷 |
 |------|------|
 | 璇█ | JDK 17 (LTS) |
 | 寰湇鍔℃鏋?| Spring Boot 3.1 + Spring Cloud Alibaba 2022 |
 | 娉ㄥ唽/閰嶇疆 | Nacos |
 | 缃戝叧 | Spring Cloud Gateway + Sentinel |
 | ORM | MyBatis-Plus 3.5 |
 | 鏁版嵁搴?| MySQL 8.0 |
 | 缂撳瓨 | Redis 7 |
 | 娑堟伅 | RocketMQ 5.x |
 | 瀹夊叏 | Spring Security + JWT + 浼佷笟 SSO 閫傞厤 |
 | 鍓嶇 | React 18 + TypeScript + Vite 5 + Turborepo + Ant Design Pro 5 |

 ## 鐩綍缁撴瀯

 ```
 battery-passport/
 鈹溾攢鈹€ pom.xml                        # 鐖?POM锛堜緷璧栫鐞?+ Profile 鍒囨崲锛? 鈹溾攢鈹€ .gitlab-ci.yml                 # GitLab CI/CD 娴佹按绾? 鈹溾攢鈹€ Jenkinsfile                    # Jenkins Pipeline锛堝惈娣锋穯闃舵锛? 鈹溾攢鈹€ platform/                      # 鍏叡鍩虹妯″潡锛? 涓粍浠讹級
 鈹?  鈹溾攢鈹€ plugin-spi/                #   鎻掍欢 SPI 鎺ュ彛锛坆attery-plugin-api锛? 鈹?  鈹溾攢鈹€ common-core/               #   鏍稿績 DTO銆佸紓甯搞€佷笂涓嬫枃
 鈹?  鈹溾攢鈹€ common-security/           #   SSO 閫傞厤鍣ㄣ€丣WT銆佸畨鍏ㄨ繃婊? 鈹?  鈹斺攢鈹€ common-mybatis/            #   澶氱鎴锋嫤鎴櫒銆佸熀绫诲疄浣? 鈹溾攢鈹€ services/                      # 寰湇鍔″疄鐜帮紙5 涓級
 鈹?  鈹溾攢鈹€ gateway-service/           #   Spring Cloud Gateway
 鈹?  鈹溾攢鈹€ auth-service/              #   璁よ瘉涓績锛圫SO 閫傞厤鍣ㄦā寮忥級
 鈹?  鈹溾攢鈹€ battery-service/           #   鐢垫睜鎶ょ収鏍稿績锛堝惈 SPI 鐑姞杞斤級
 鈹?  鈹溾攢鈹€ tenant-service/            #   SaaS 绉熸埛绠＄悊
 鈹?  鈹斺攢鈹€ integration-service/       #   浼佷笟 ERP/SSO 闆嗘垚
 鈹溾攢鈹€ frontend/                      # React Turborepo Monorepo
 鈹溾攢鈹€ docker/local/                  # Docker Compose 寮€鍙戠幆澧? 鈹溾攢鈹€ scripts/db/                    # 鏁版嵁搴撳垵濮嬪寲鑴氭湰
 鈹斺攢鈹€ helm-charts/                   # Kubernetes 閮ㄧ讲缂栨帓
 ```

 ## 蹇€熷紑濮?
 ### 鐜瑕佹眰
 - JDK 17+
 - Maven 3.9+
 - Node.js 18+ (鍓嶇)
 - Docker & Docker Compose (鏈湴鐜)

 ### 鏈湴寮€鍙?
 ```bash
 # 1. 鍚姩鍩虹璁炬柦锛圢acos + MySQL + Redis锛? docker compose -f docker/local/docker-compose.yml up -d nacos mysql redis

 # 2. 鏋勫缓鍏叡妯″潡
 mvn clean install -f platform/plugin-spi/pom.xml -DskipTests
 mvn clean install -f platform/common-core/pom.xml -DskipTests
 mvn clean install -f platform/common-security/pom.xml -DskipTests
 mvn clean install -f platform/common-mybatis/pom.xml -DskipTests

 # 3. 骞惰鍚姩寰湇鍔? mvn spring-boot:run -f services/gateway-service/pom.xml &
 mvn spring-boot:run -f services/auth-service/pom.xml &
 mvn spring-boot:run -f services/battery-service/pom.xml &
 mvn spring-boot:run -f services/tenant-service/pom.xml &

 # 4. 鍚姩鍓嶇
 cd frontend && npm ci && npm run dev
 ```

 ### 鐢熶骇鏋勫缓

 ```bash
 # SaaS 鐢熶骇
 bash build.sh saas-prod

 # 绉佹湁鍖栦氦浠橈紙鍚贩娣嗭級
 bash build.sh onpremise-prod
 ```

 ## 閮ㄧ讲妯″紡

 ### 绉佹湁鍖栭儴缃?
 浜や粯鐗╀负娣锋穯鍚庣殑 jar 鍖?+ Docker 闀滃儚锛屼笉鍖呭惈 Java 婧愮爜銆傚鎴锋柟閫氳繃浠ヤ笅鏈哄埗瀹炵幇涓氬姟瀹氬埗锛?
 | 瀹氬埗鏂瑰紡 | 璇存槑 | 绀轰緥 |
 |---------|------|------|
 | SPI 鎻掍欢 | 瀹炵幇鍏紑鎺ュ彛娉ㄥ唽鎻掍欢 jar | 澶╁悎瀹归噺鏍￠獙銆佹墭閭?ERP 杞崲 |
 | REST API | 璋冪敤寮€鏀炬帴鍙?Webhook | ERP 鍚屾銆佹姤鍛婂鍑?|
 | 鑴氭湰寮曟搸 | Groovy 瑙勫垯鑴氭湰鐑姞杞?| 鏍￠獙瑙勫垯銆佹姤鍛婃按鍗?|
 | 鍓嶇鎵╁睍 | 杩滅▼ React 缁勪欢娉ㄥ叆 | 澶╁悎涓撳睘浠〃鐩?|
 | 閰嶇疆鍖?| yaml 瑕嗙洊瀹氬埗 | SSO 鍦板潃銆佸搧鐗岃壊銆侀檺娴?|

 璇﹁ [battery-passport-extension](https://github.com/wuchaohua/battery-passport-extension) 浜屾寮€鍙戠ず渚嬪伐绋嬨€?
 ### SaaS 澶氱鎴烽儴缃?
 澶氱鎴风壒鎬э細
 - MyBatis-Plus tenant_id 鍒楃骇鏁版嵁闅旂
 - Nginx 璇锋眰澶?X-Tenant-Id 璺敱鍒嗗彂
 - Kubernetes HPA 鑷姩寮规€т几缂? - 绉熸埛绮掑害鐨勫椁愬拰閰嶉绠＄悊

 ## 闃插弽缂栬瘧鎺柦

 | 灞傜骇 | 鎶€鏈?| 璇存槑 |
 |------|------|------|
 | 娣锋穯灞?| ProGuard / CandyGuard | 绫诲悕/鏂规硶鍚嶆贩娣?+ 瀛楃涓插姞瀵?|
 | 閰嶇疆灞?| Jasypt | 閰嶇疆鏂囦欢瀵嗙爜/secret 鍔犲瘑 |
 | 閮ㄧ讲灞?| JDK 17 瀵嗗皝绫?| 鏍稿績妯″潡闃叉缁ф壙 |
 | 杩愯鏃?| Plugin ClassLoader | 鎻掍欢娌欑闅旂 |

 ## 涓昏 API

 | 鏂规硶 | 绔偣 | 璇存槑 |
 |------|------|------|
 | GET | /api/v1/passports | 鎶ょ収鍒楄〃锛堝垎椤?绛涢€夛級 |
 | POST | /api/v1/passports | 鍒涘缓鐢垫睜鎶ょ収 |
 | GET | /api/v1/passports/{id} | 鎶ょ収璇︽儏 |
 | PUT | /api/v1/passports/{id} | 鏇存柊鎶ょ収 |
 | DELETE | /api/v1/passports/{id} | 鍒犻櫎鎶ょ収 |
 | GET | /api/v1/passports/search | 搴忓垪鍙锋悳绱?|
 | POST | /api/v1/auth/login | 鑾峰彇鐧诲綍 URL |
 | GET/POST | /api/v1/tenants | 绉熸埛绠＄悊锛圫aaS锛?|
 | POST | /api/v1/integration/erp/import | ERP 鏁版嵁瀵煎叆 |

 ## 椤圭洰璺嚎鍥?
 ```
 v1.0 --- 鏍稿績鎶ょ収 CRUD + 澶╁悎鍌ㄨ兘绉佹湁鍖栭儴缃? v1.1 --- 鎵橀偊鑲′唤閮ㄧ讲 + SSO 閫傞厤鍣ㄦ墿灞? v1.2 --- SaaS 澶氱鎴蜂笂绾?+ 杩愯惀鎺у埗鍙? v2.0 --- 鍖哄潡閾炬函婧?+ 鏀垮簻鐩戠涓婃姤
 v2.1 --- 娆х洘鐢垫睜娉曡鍚堣
 v2.2 --- AI 瀵垮懡棰勬祴 + 姊鍒╃敤寤鸿
 ```

 ## 璁稿彲璇?
 Copyright (c) 2026 鐢垫睜鎶ょ収骞冲彴銆備繚鐣欐墍鏈夋潈鍒┿€?
