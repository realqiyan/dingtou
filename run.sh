#!/usr/bin/env bash
nohup java -jar start/target/start-0.0.1-SNAPSHOT.jar --spring.config.name=application.yml > application.log  2>&1 &