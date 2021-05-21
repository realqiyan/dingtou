package me.dingtou.web.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.web.model.LoginUser;
import org.apache.commons.lang3.time.DateUtils;

import javax.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;

/**
 * @author yuanhongbo
 */
@Slf4j
public class SessionUtils {
    private static final ThreadLocal<LoginUser> sessionOwner = new ThreadLocal<>();
    public static final String JWT = "jwt";

    /**
     * 初始化当前登陆用户
     *
     * @param loginUser
     */
    public static void setCurrentOwner(LoginUser loginUser) {
        sessionOwner.set(loginUser);
    }

    /**
     * 获取当前登陆用户
     *
     * @return
     */
    public static String getCurrentOwner() {
        return sessionOwner.get().getOwnerCode();
    }

    /**
     * 获取当前登陆用户
     *
     * @return
     */
    public static LoginUser getCurrentLoginUser() {
        return sessionOwner.get();
    }

    /**
     * 构建cookie
     *
     * @param secretKey
     * @param loginUser
     * @return
     */
    public static Cookie buildCookie(String secretKey, LoginUser loginUser) {
        String jwt = buildJwt(secretKey, loginUser);
        Cookie cookie = new Cookie(JWT, jwt);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(3600);
        return cookie;
    }

    /**
     * 从cookies里面查找登陆用户
     *
     * @param secretKey
     * @param cookies
     * @return
     */
    public static LoginUser parseCookie(String secretKey, Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (JWT.equals(cookie.getName())) {
                return parseJwt(secretKey, cookie.getValue());
            }
        }
        return null;
    }

    /**
     * 根据用户信息生成jwt
     *
     * @param secretKey
     * @param loginUser
     * @return
     */
    public static String buildJwt(String secretKey, LoginUser loginUser) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String jwt = Jwts.builder()
                .setSubject(loginUser.getNick())
                .setIssuer(loginUser.getSource())
                .setIssuedAt(new Date())
                .setId(loginUser.getOuterId())
                .signWith(key)
                .compact();
        return jwt;
    }

    /**
     * 根据用户信息生成jwt
     *
     * @param secretKey
     * @param jwtStr
     * @return
     */
    public static LoginUser parseJwt(String secretKey, String jwtStr) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Jws<Claims> jwt = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwtStr);
            Claims body = jwt.getBody();
            Date issuedAt = body.getIssuedAt();
            // jwt当天有效
            Date now = new Date();
            if (!DateUtils.isSameDay(issuedAt, now)) {
                return null;
            }
            return new LoginUser(body.getIssuer(), body.getSubject(), body.getId());
        } catch (Exception e) {
            log.error("parseJwt error, jwtStr:" + jwtStr, e);
            return null;
        }
    }

    /**
     * 会话结束清理登陆用户
     */
    public static void clear() {
        sessionOwner.remove();
    }


}
