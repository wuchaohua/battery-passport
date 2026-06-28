#!/bin/bash
# ========================================
# 电池护照平台 — GitHub 推送脚本 (MacOS/Linux)
# 使用方式:
#   1. 先创建 GitHub 仓库:
#      https://github.com/new → wuchaohua/dpp-project
#   2. 生成 Personal Access Token
#   3. 运行: ./push-to-github.sh
# ========================================

set -euo pipefail

echo "===== 电池护照平台 — 推送到 GitHub ====="
echo ""
echo "请确认:"
echo "  1. 已在 https://github.com/new 创建仓库 wuchaohua/dpp-project"
echo "  2. 已生成 GitHub Personal Access Token"
echo ""

read -p "请输入 GitHub Personal Access Token: " token

if [ -z "$token" ]; then
    echo "错误: 未输入 Token"
    exit 1
fi

git remote set-url origin "https://wuchaohua:${token}@github.com/wuchaohua/dpp-project.git"

git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "===== 推送成功！====="
    echo "仓库地址: https://github.com/wuchaohua/dpp-project"
else
    echo ""
    echo "推送失败，请检查后重试。"
    echo ""
    echo "手动执行:"
    echo "  git push -u origin main"
fi
