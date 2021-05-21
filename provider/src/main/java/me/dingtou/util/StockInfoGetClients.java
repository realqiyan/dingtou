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
public class StockInfoGetClients {

    /**
     * 拉取证券数据专用httpclient
     */
    private static CloseableHttpClient httpclient = HttpClients.custom()
            .setMaxConnPerRoute(20)
            .setMaxConnTotal(20)
            .build();

    /**
     * get
     *
     * @param url
     * @return
     */
    public static String getUrlContent(String url) {
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            response = httpclient.execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            log.error("getUrlContent error, url:" + url, e);
            return null;
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

}
