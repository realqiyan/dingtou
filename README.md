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
3. 准备好`application.yml`配置文件，需要挂载到容器`/dingtou/config`下；
    ```
    # 数据源配置
    spring:
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        # schema: classpath:db/schema-mysql.sql
        # data: classpath:db/data-mysql.sql
        url: jdbc:mysql://127.0.0.1:3306/dingtou?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
        username: 数据库用户名
        password: 数据库密码
    
    
    
      # FREEMARKER (FreeMarkerAutoConfiguration)
      freemarker:
        allow-request-override: false
        cache: true
        check-template-location: true
        charset: UTF-8
        content-type: text/html
        expose-request-attributes: false
        expose-session-attributes: false
        expose-spring-macro-helpers: false
        suffix: .ftl
        template-loader-path: classpath:/templates/ #comma-separated list
    
      resources:
        static-locations: classpath:/static/
    
    
      jackson:
        generator:
          write_numbers_as_strings: true
        date-format: yyyy-MM-dd HH:mm:ss
        time-zone: Asia/Shanghai
        default-property-inclusion: non_null
    
    me:
      dingtou:
        #登陆配置
        login:
          needLogin: false
          defaultOwner: default
          #loginUrl: https://github.com/login/oauth/authorize?client_id=700a1ae2a42328279390
          loginUrl: https://api.weibo.com/oauth2/authorize?client_id=xxxxxxxxxxx&response_type=code&redirect_uri=https://dingtoume.cn/login/oauth_weibo
          secretKey: d7edf5704cae2e5105b2c99d6a4e32b655c36f8a3ae2dcc22ea4e7df1be8803e
        #集成登陆配置
        oauth:
          github:
            client_id: 700a1ae2a42328279390
            client_secret: bdca3d91e0867d5a1c4bbf01dc61a1f22a18eeff
            redirect_uri: https://dingtoume.cn/login/oauth_github
          weibo:
            client_id: xxxxxxxxxxx
            client_secret: xxxxxxxxxxxxxxxxxxxx
            redirect_uri: https://dingtoume.cn/login/oauth_weibo
    
    ```
4. 执行下面命令启动镜像；
    ```
    docker run -d \
        --restart=always \
        --name=dingtou-1 \
        --hostname dingtou-1 \
        -p 8080:8080 \
        -v /etc/localtime:/etc/localtime:ro \
        -v /home/admin/config:/dingtou/config \
        yhb3420/dingtou:v1
    ```
