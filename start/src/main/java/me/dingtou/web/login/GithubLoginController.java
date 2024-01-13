package me.dingtou.web.login;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.dingtou.web.model.LoginUser;
import me.dingtou.web.util.SessionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class GithubLoginController {

    @Value("${me.dingtou.oauth.github.client_id}")
    private String clientId;
    @Value("${me.dingtou.oauth.github.client_secret}")
    private String clientSecret;
    @Value("${me.dingtou.oauth.github.redirect_uri}")
    private String redirectUri;

    @Value("${me.dingtou.login.secretKey}")
    private String secretKey;

    /**
     * httpclient
     */
    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    @RequestMapping(value = "/login/oauth_github", method = RequestMethod.GET)
    public void login(@RequestParam(value = "code", required = true) String code, HttpServletResponse response) throws Exception {
        LoginUser loginUser = getUser(code);
        if (null != loginUser) {
            Cookie cookie = SessionUtils.buildCookie(secretKey, loginUser);
            if (null != cookie) {
                response.addCookie(cookie);
                response.sendRedirect("/");
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    /**
     * 获取github用户信息
     *
     * @param code
     * @return
     */
    private LoginUser getUser(String code) {
        if (null == code) {
            return null;
        }
        String accessToken = getAccessToken(code);
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet("https://api.github.com/user");
            httpGet.setHeader("Accept", "application/json");
            httpGet.addHeader("Authorization", "token " + accessToken);
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject jsonObject = JSON.parseObject(content);
            return new LoginUser("github", jsonObject.getString("name"), jsonObject.getString("id"));
        } catch (Exception e) {
            log.error("getAccessToken error.", e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    //
                }
            }
        }

        return null;

    }

    /**
     * 根据code换取accessToken
     *
     * @param code
     * @return
     */
    private String getAccessToken(String code) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost("https://github.com/login/oauth/access_token");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("code", code));
            nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
            nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
            nameValuePairs.add(new BasicNameValuePair("redirect_uri", redirectUri));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpPost.setHeader("Accept", "application/json");
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject jsonObject = JSON.parseObject(content);
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            log.error("getAccessToken error. code:" + code, e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        return null;
    }
}
