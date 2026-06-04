#!/bin/bash
# 停止旧进程
ps -ef | grep perm-menu-0.0.1-SNAPSHOT.jar | grep -v grep | awk '{print $2}' | xargs -r kill -9
# 启动新进程，指定使用当前目录的 application.yml
nohup java -jar perm-menu-0.0.1-SNAPSHOT.jar --spring.config.location=./application.yml > app.log 2>&1 &

echo "后端服务已启动，日志输出在 app.log"