package me.dingtou.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * HttpClients
 */
@Slf4j
public class HttpUtils {

    /**
     * 拉取证券数据专用httpclient
     */
    private static final CloseableHttpClient httpclient = HttpClients.custom()
            .setMaxConnPerRoute(20)
            .setMaxConnTotal(20)
            .build();

    /**
     * 通过URL获取内容
     *
     * @param url URL地址
     * @return 返回URL内容，如果获取失败返回null
     */
    public static String getUrlContent(String url) {
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(new HttpGet(url));
            if (null == response || response.getStatusLine().getStatusCode() != 200) {
                String forwardUrl = "https://forward.myworker.win/" + url;
                response = httpclient.execute(new HttpGet(forwardUrl));
            }
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            log.error("getUrlContent error, url:" + url, e);
            return null;
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    // 忽略IO异常
                }
            }
        }
    }


}
