package me.dingtou.web.filter;

import me.dingtou.web.model.LoginUser;
import me.dingtou.web.util.SessionUtils;


import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登陆过滤
 */
public class LoginFilter implements Filter {

    public static final String OWNER = "owner";
    public static final String DEFAULT_SOURCE = "default";

    /**
     * 登陆白名单
     */
    private static final String LOGIN_PATH = "/login/.*";

    /**
     * 图片白名单
     */
    private static final String IMAGE_PATH = "/images/.*";

    /**
     * 是否需要登陆
     */
    private final boolean needLogin;

    /**
     * 登陆密钥
     */
    private final String secretKey;
    /**
     * 登陆URL
     */
    private final String loginUrl;

    /**
     * 默认用户
     */
    private final String defaultOwner;

    public LoginFilter(boolean needLogin, String defaultOwner, String secretKey, String loginUrl) {
        this.needLogin = needLogin;
        this.defaultOwner = defaultOwner;
        this.secretKey = secretKey;
        this.loginUrl = loginUrl;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            if (!(servletRequest instanceof HttpServletRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String pathInfo = httpServletRequest.getRequestURI();
            // 登陆白名单不拦截
            if (pathInfo.matches(IMAGE_PATH) || pathInfo.matches(LOGIN_PATH)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (!needLogin) {
                String owner = httpServletRequest.getParameter(OWNER);
                if (null == owner) {
                    owner = defaultOwner;
                }
                SessionUtils.setCurrentOwner(new LoginUser(DEFAULT_SOURCE, owner, owner));
            } else {
                LoginUser loginUser = getLoginUser(httpServletRequest.getCookies());
                if (null == loginUser) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                    httpServletResponse.sendRedirect(loginUrl);
                    return;
                }
                SessionUtils.setCurrentOwner(loginUser);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            SessionUtils.clear();
        }
    }

    private LoginUser getLoginUser(Cookie[] cookies) {
        return SessionUtils.parseCookie(secretKey, cookies);
    }

}
