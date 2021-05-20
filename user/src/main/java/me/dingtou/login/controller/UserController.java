package me.dingtou.login.controller;

import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import me.dingtou.login.domain.User;
import me.dingtou.login.utils.UserUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author ken
 */
@RestController
public class UserController {
    @GetMapping("/user/getCurrentUser")
    public Object getCurrentUser(Authentication authentication) {
        return authentication.getPrincipal();
    }

    @GetMapping("/api/currentUser")
    public JSONObject currentUser(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", UserUtils.userName());
        User user = UserUtils.userInfo();
        if(Objects.nonNull(user)) {
            jsonObject.put("nameNick", user.getNickName());
        }
        jsonObject.put("avatar", "https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png");
        return jsonObject;
    }

}
