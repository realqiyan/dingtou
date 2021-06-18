#!/bin/sh
while true ; do
  if ! pidof "java" > /dev/null ; then
    echo "dingtou.jar try running"
    java -jar dingtou.jar --spring.config.location=./application.yml > application.log
    RET=$?
    echo "RET CODE: ${RET}"
  fi
  sleep 30
done