#!/usr/bin/env bash
nohup java -jar start/target/dingtou.jar --spring.config.location=../application.yml >> application.log  2>&1 &