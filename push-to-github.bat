@echo off
REM ========================================
REM 电池护照平台 — GitHub 推送脚本
REM 使用方式: 
REM   1. 先在浏览器创建 GitHub 仓库:
REM      https://github.com/new → 输入 wuchaohua/dpp-project → Create repository
REM   2. 运行本脚本:
REM      push-to-github.bat
REM      （会弹出 GitHub 登录窗口，输入 Personal Access Token）
REM ========================================

echo.
echo ==== 电池护照平台 — 推送到 GitHub ====
echo.
echo 请确认:
echo   1. 已在 https://github.com/new 创建仓库 wuchaohua/dpp-project
echo   2. 已生成 GitHub Personal Access Token
echo      (Settings → Developer settings → Personal access tokens → Fine-grained tokens)
echo.

set /p "token=请输入 GitHub Personal Access Token (输入后回车): "

if "%token%"=="" (
    echo 错误: 未输入 Token
    exit /b 1
)

echo.
echo 正在配置远程仓库...
git remote set-url origin https://wuchaohua:%token%@github.com/wuchaohua/dpp-project.git

echo 正在推送代码...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ==== 推送成功！====
    echo 仓库地址: https://github.com/wuchaohua/dpp-project
) else (
    echo.
    echo 推送失败，请检查:
    echo   1. Token 是否有 repo 权限
    echo   2. 仓库是否已创建
    echo   3. 网络是否能访问 github.com
    echo.
    echo 也可以手动执行:
    echo   git push -u origin main
)

pause
