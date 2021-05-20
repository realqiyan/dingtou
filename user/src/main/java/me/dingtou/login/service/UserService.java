package me.dingtou.login.service;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import me.dingtou.login.dao.UserDAO;
import me.dingtou.login.dao.dataobject.UserDO;
import me.dingtou.login.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author ken
 */
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<UserDO> query = new QueryWrapper<>();
        query.eq("username", username);
        UserDO userDO = userDAO.selectOne(query);

        if(Objects.isNull(userDO)){
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        String password = passwordEncoder.encode(userDO.getPassword());
        User user = new User(userDO.getUsername(), password, AuthorityUtils.commaSeparatedStringToAuthorityList(userDO.getRole()));
        user.setNickName(userDO.getNickname());
        return user;
    }
}
