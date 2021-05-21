package me.dingtou.web.filter;

import me.dingtou.web.model.LoginUser;
import me.dingtou.web.util.SessionUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登陆过滤
 */
public class LoginFilter implements Filter {

    public static final String OWNER = "owner";
    public static final String DEFAULT = "default";

    /**
     * 登陆白名单
     */
    private static final String LOGIN_PATH = "/login/.*";

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

    public LoginFilter(boolean needLogin, String secretKey, String loginUrl) {
        this.needLogin = needLogin;
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
            if (pathInfo.matches(LOGIN_PATH)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (!needLogin) {
                String owner = httpServletRequest.getParameter(OWNER);
                if (null == owner) {
                    owner = DEFAULT;
                }
                SessionUtils.setCurrentOwner(new LoginUser(DEFAULT, owner, owner));
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
