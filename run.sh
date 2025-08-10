#!/bin/sh

# 应用名称
APP_NAME="dingtou"
# jar包名称
JAR_NAME="dingtou.jar"
# 配置文件路径
CONFIG_PATH="config/application.yml"
# 日志文件
LOG_FILE="nohup.out"
# PID文件
PID_FILE="app.pid"

# 启动应用
start() {
  # 检查是否已经运行
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null; then
      echo "$APP_NAME is already running (pid: $PID)"
      return 1
    else
      # 如果PID文件存在但进程不存在，则删除PID文件
      rm -f "$PID_FILE"
    fi
  fi
  
  echo "Starting $APP_NAME ..."
  nohup java -jar -DfuTuApiIp=127.0.0.1 -Duser.timezone=Asia/Shanghai "$JAR_NAME" --spring.config.location="$CONFIG_PATH" > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  echo "$APP_NAME started successfully"
}

# 停止应用
stop() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null; then
      echo "Stopping $APP_NAME ..."
      kill "$PID"
      # 等待进程结束
      while ps -p "$PID" > /dev/null; do
        sleep 1
      done
      rm -f "$PID_FILE"
      echo "$APP_NAME stopped successfully"
    else
      echo "$APP_NAME is not running but pid file exists"
      rm -f "$PID_FILE"
    fi
  else
    echo "$APP_NAME is not running"
  fi
}

# 重启应用
restart() {
  stop
  start
}

# 检查应用状态
status() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null; then
      echo "$APP_NAME is running (pid: $PID)"
    else
      echo "$APP_NAME is not running but pid file exists"
    fi
  else
    echo "$APP_NAME is not running"
  fi
}

# 根据参数执行相应操作
case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    restart
    ;;
  status)
    status
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
esac

exit 0