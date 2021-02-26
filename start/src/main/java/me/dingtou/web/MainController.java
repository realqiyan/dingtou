package me.dingtou.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;

/**
 * @author qiyan
 * @date 2017/6/16
 */
@Controller
public class MainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void home(HttpServletResponse response) throws Exception {
        response.sendRedirect("index.html");
    }

//    @RequestMapping(value = "/trade", method = RequestMethod.GET)
//    public String trade(Map<String, Object> map) throws Exception {
//        map.put("time", System.currentTimeMillis());
//        return "trade";
//    }

}
