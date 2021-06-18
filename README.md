# dingtoume v2版本
1. 支持场内基金定投；
2. 支持场内股票定投；
3. 简化算法和模型；

# 升级记录
## 2021-05-16版本
* 支持资产分析统计
* 升级脚本参考 start/src/main/resources/db/upgrade-20210516-mysql.sql


# docker
1. 执行 `package.sh` 打包；
2. 执行 `docker build -t yhb3420/dingtou:v1 .` 生成镜像；
3. 执行 `docker run --name=dingtou-1 --restart=always -d -p 8080:8080 yhb3420/dingtou:v1
` 启动镜像；
