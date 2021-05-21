package me.dingtou.web.model;

import me.dingtou.web.filter.LoginFilter;

/**
 * @author yuanhongbo
 */
public class LoginUser {
    /**
     * 来源
     */
    private String source;
    /**
     * 昵称
     */
    private String nick;
    /**
     * 外部Id
     */
    private String outerId;

    public LoginUser(String source, String nick, String outerId) {
        this.source = source;
        this.nick = nick;
        this.outerId = outerId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getOuterId() {
        return outerId;
    }

    public void setOuterId(String outerId) {
        this.outerId = outerId;
    }

    public String getOwnerCode() {
        if (LoginFilter.DEFAULT.equals(source)) {
            return outerId;
        }
        return String.format("%s_%s", source, outerId);
    }
}
