package me.dingtou.config;

import me.dingtou.web.filter.LoginFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    public static final String DEFAULT = "default";
    
    @Value("${me.dingtou.login.needLogin}")
    private boolean needLogin;

    @Value("${me.dingtou.login.loginUrl}")
    private String loginUrl;

    @Value("${me.dingtou.login.secretKey}")
    private String secretKey;


    @Value("${me.dingtou.login.defaultOwner}")
    private String defaultOwner;

    @Bean
    public FilterRegistrationBean<LoginFilter> filterRegistrationBean() {

        FilterRegistrationBean<LoginFilter> registrationBean = new FilterRegistrationBean<>();
        String defaultOwner = this.defaultOwner;
        defaultOwner = ((null == defaultOwner) ? DEFAULT : defaultOwner);
        LoginFilter filter = new LoginFilter(needLogin, defaultOwner, secretKey, loginUrl);
        registrationBean.setFilter(filter);
        //设置过滤器拦截请求
        List<String> urls = new ArrayList<>();
        urls.add("/*");
        registrationBean.setUrlPatterns(urls);
        return registrationBean;
    }
}
