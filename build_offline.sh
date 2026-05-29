#!/bin/bash

# 退出遇到任何错误
set -e

echo "========================================="
echo "开始构建前后端一体化独立离线包 (Fat JAR)..."
echo "========================================="

PROJECT_ROOT=$(pwd)
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_DIR="$PROJECT_ROOT/backend"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"

echo "1. 构建前端项目 (Vue)..."
cd "$FRONTEND_DIR"
npm run build

echo "2. 将前端构建产物移动到后端的静态资源目录..."
# 如果静态目录已存在则先清空，如果不存在则创建
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -r "$FRONTEND_DIR"/dist/* "$STATIC_DIR"/

echo "3. 构建后端项目 (Spring Boot)..."
cd "$BACKEND_DIR"
./mvnw clean package -DskipTests

echo "========================================="
echo "构建成功！🎉"
echo "您的独立运行包已生成在："
echo "  $BACKEND_DIR/target/perm-menu-manager-1.0.0.jar"
echo "您可以直接将此 jar 文件拷贝到离线 Windows 机器上运行。"
echo "========================================="
