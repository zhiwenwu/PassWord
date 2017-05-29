package cn.xuemcu.password;

/**
 * Created by 朱红晨 on 2017/5/29.
 */

public class PassWord {
    private String websiteName;
    private String accounts;
    private String passWord;

    public PassWord(String websiteName) {
        this.websiteName = websiteName;
    }

    public PassWord(String websiteName,String accounts,String passWord) {
        this.websiteName = websiteName;
        this.accounts = accounts;
        this.passWord = passWord;
    }

    public String getWebsiteName() {
        return this.websiteName;
    }

    public String getAccounts() {
        return this.accounts;
    }

    public String getPassWord() {
        return this.passWord;
    }
}
