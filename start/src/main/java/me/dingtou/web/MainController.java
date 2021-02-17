package me.dingtou.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author qiyan
 * @date 2017/6/16
 */
@Controller
public class MainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody
    String home() throws Exception {
        return "success";
    }

    @RequestMapping(value = "/trade", method = RequestMethod.GET)
    public String trade(Map<String, Object> map) throws Exception {
        map.put("time", System.currentTimeMillis());
        return "trade";
    }

}
