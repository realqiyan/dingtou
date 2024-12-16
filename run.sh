#!/bin/sh
# java
nohup java -jar -DfuTuApiIp=127.0.0.1 -Duser.timezone=Asia/Shanghai dingtou.jar --spring.config.location=config/application.yml &