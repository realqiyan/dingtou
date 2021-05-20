package me.dingtou.login.utils;

import me.dingtou.login.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author 上叶
 * @date 2021/5/16
 **/
public class UserUtils {
    public static String userName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static User userInfo(){
        Object details = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(details instanceof User){
            return (User)details;
        }

        return null;
    }
}
