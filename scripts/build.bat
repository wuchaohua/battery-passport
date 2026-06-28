@echo off
REM ========================================
REM 电池护照平台 — Windows 构建脚本
REM 使用: build.bat [profile]
REM   profile: dev | saas-prod | onpremise-prod
REM ========================================
setlocal enabledelayedexpansion

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=dev

echo ==== 电池护照平台构建 [%PROFILE%] ====

REM 1. 构建公共模块
echo [1/6] 编译公共模块...
call mvn clean install -f platform\plugin-spi\pom.xml -DskipTests -q
call mvn clean install -f platform\common-core\pom.xml -DskipTests -q
call mvn clean install -f platform\common-security\pom.xml -DskipTests -q
call mvn clean install -f platform\common-mybatis\pom.xml -DskipTests -q

REM 2. 并行构建服务（Windows用start）
echo [2/6] 编译业务服务...
start /B "mvn clean package -f services\gateway-service\pom.xml -DskipTests -q"
start /B "mvn clean package -f services\auth-service\pom.xml -DskipTests -q"
start /B "mvn clean package -f services\battery-service\pom.xml -DskipTests -q"
start /B "mvn clean package -f services\tenant-service\pom.xml -DskipTests -q"
start /B "mvn clean package -f services\integration-service\pom.xml -DskipTests -q"
timeout /T 60 /NOBREAK >nul
echo   ✓ 服务编译完成

REM 3. 前端
echo [3/6] 编译前端...
cd frontend
call npm ci --silent
if "%PROFILE%"=="saas-prod" (call npm run build:saas) else (call npm run build)
cd ..

REM 4. 交付包
echo [4/6] 生成交付包...
set DELIVERY_DIR=build\delivery\%PROFILE%
if not exist %DELIVERY_DIR% mkdir %DELIVERY_DIR%

for %%s in (gateway-service auth-service battery-service tenant-service integration-service) do (
    if exist services\%%s\target\%%s.jar (
        copy services\%%s\target\%%s.jar %DELIVERY_DIR%\ /Y
    )
)
xcopy /E /I /Y frontend\packages\app-battery\dist %DELIVERY_DIR%\webapp

echo ==== 构建完成 ====
echo 交付包位置: %DELIVERY_DIR%
