package me.dingtou.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * HttpClients
 */
public class HttpClients {

    /**
     * get
     *
     * @param url
     * @return
     */
    public static StringBuffer getUrlContent(String url) {
        try {
            URL urlObj = new URL(url);
            InputStream inputStream = urlObj.openConnection().getInputStream();
            StringBuffer content = new StringBuffer();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
                br.close();
            } finally {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
            return content;
        } catch (Exception e) {
            return null;
        }
    }

}
