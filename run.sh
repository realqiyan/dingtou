#!/bin/sh
while true ; do
  if ! pidof "java" > /dev/null ; then
    echo "dingtou.jar try running"
    # fuTuApiIp指定富途服务api服务地址
    java -jar -DfuTuApiIp=172.18.0.1 -Duser.timezone=Asia/Shanghai dingtou.jar --spring.config.location=./config/application.yml > application.log
    RET=$?
    echo "RET CODE: ${RET}"
  fi
  sleep 30
done