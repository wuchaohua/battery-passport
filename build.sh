#!/bin/bash
# ========================================
# 电池护照平台 — 完整构建脚本
# 使用方式: bash build.sh [profile]
#   profile: dev | saas-prod | onpremise-prod
# ========================================
set -euo pipefail

PROFILE="${1:-dev}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MVN_OPTS="-DskipTests -q"

echo "===== 电池护照平台构建 [${PROFILE}] ====="

# 1. 构建平台公共模块
echo "[1/6] 编译公共模块..."
mvn clean install -f "${SCRIPT_DIR}/platform/plugin-spi/pom.xml" ${MVN_OPTS}
mvn clean install -f "${SCRIPT_DIR}/platform/common-core/pom.xml" ${MVN_OPTS}
mvn clean install -f "${SCRIPT_DIR}/platform/common-security/pom.xml" ${MVN_OPTS}
mvn clean install -f "${SCRIPT_DIR}/platform/common-mybatis/pom.xml" ${MVN_OPTS}
mvn clean install -f "${SCRIPT_DIR}/platform/common-mq/pom.xml" ${MVN_OPTS}

# 2. 并行构建业务服务
echo "[2/6] 并行编译业务服务..."
for service in gateway-service auth-service battery-service tenant-service integration-service; do
    (mvn clean package -f "${SCRIPT_DIR}/services/${service}/pom.xml" -P${PROFILE} ${MVN_OPTS}) &
done
wait
echo "  ✓ 服务编译完成"

# 3. 前端构建
echo "[3/6] 编译前端..."
cd "${SCRIPT_DIR}/frontend"
npm ci --silent
case "${PROFILE}" in
    saas-prod)      npm run build:saas ;;
    onpremise-prod) npm run build:onpremise ;;
    *)              npm run build ;;
esac
cd "${SCRIPT_DIR}"

# 4. 代码混淆（仅生产构建）
if [ "${PROFILE}" != "dev" ]; then
    echo "[4/6] 混淆核心服务..."
    for service in battery-service auth-service; do
        java -jar candyguard.jar \
            -in "services/${service}/target/${service}.jar" \
            -out "services/${service}/target/${service}-obfuscated.jar"
    done
fi

# 5. 打包交付物
echo "[5/6] 生成交付包..."
DELIVERY_DIR="${SCRIPT_DIR}/build/delivery/${PROFILE}"
mkdir -p "${DELIVERY_DIR}"

# 收集jar
for service in gateway-service auth-service battery-service tenant-service integration-service; do
    cp "services/${service}/target/${service}.jar" "${DELIVERY_DIR}/"
done

# 收集前端
cp -r "${SCRIPT_DIR}/frontend/packages/app-battery/dist" "${DELIVERY_DIR}/webapp"

echo "交付包位置: ${DELIVERY_DIR}"

# 6. Docker 镜像构建（生产环境）
if [ "${PROFILE}" != "dev" ]; then
    echo "[6/6] 构建Docker镜像..."
    TAG="v$(date +%Y%m%d%H%M)"
    for service in gateway-service auth-service battery-service tenant-service integration-service; do
        docker build -t "registry.battery.com/battery/${service}:${TAG}" "services/${service}/"
    done
fi

echo ""
echo "===== 构建完成 ====="
