#!/bin/sh
# java
pid=`ps -aux | grep dingtou | grep java | awk '{print $2}'`
echo $pid
for id in $pid
do
kill -9 $id
echo "kill $id"
done
echo "dingtou.jar try starting"
while true ; do
if ! pidof "java" > /dev/null ; then
  echo "dingtou.jar try running"
  # fuTuApiIp指定富途服务api服务地址
  java -jar -DfuTuApiIp=127.0.0.1 -Duser.timezone=Asia/Shanghai dingtou/start/target/dingtou.jar --spring.config.location=dingtou/config/application.yml > application.log
  RET=$?
  echo "dingtou.jar stopped. RET CODE: ${RET}"
fi
sleep 30
done