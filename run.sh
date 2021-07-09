#!/bin/sh
while true ; do
  if ! pidof "java" > /dev/null ; then
    echo "dingtou.jar try running"
    /dingtou/FutuOpenD/FutuOpenD &
    java -jar -Duser.timezone=Asia/Shanghai dingtou.jar --spring.config.location=./config/application.yml > application.log
    RET=$?
    echo "RET CODE: ${RET}"
  fi
  sleep 30
done