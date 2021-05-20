package me.dingtou.login.dao.dataobject;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author ken
 */
@Data
@TableName(value = "sys_user")
public class UserDO {

    private String username;
    private String nickname;
    private String password;
    private String role;
}
