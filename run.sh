#!/bin/sh
# run.sh
pid=`ps -aux | grep run.sh | awk '{print $2}'`
echo $pid
for id in $pid
do
kill -9 $id
echo "kill $id"
done
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
    java -jar -DfuTuApiIp=172.18.0.1 -Duser.timezone=Asia/Shanghai dingtou.jar --spring.config.location=./config/application.yml > application.log
    RET=$?
    echo "RET CODE: ${RET}"
  fi
  sleep 30
done