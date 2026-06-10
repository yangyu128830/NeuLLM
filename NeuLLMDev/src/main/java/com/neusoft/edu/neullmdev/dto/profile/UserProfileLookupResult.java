package com.neusoft.edu.neullmdev.dto.profile;

/**
 * 按姓名查 user_profile，供发邮件卡片预填收件人。
 */
public class UserProfileLookupResult {

    private boolean ok;
    private String message;
    private Long id;
    private String realName;
    private String displayName;
    private String email;
    private String phone;

    public static UserProfileLookupResult missingName() {
        UserProfileLookupResult r = new UserProfileLookupResult();
        r.ok = false;
        r.message = "缺少 name 参数";
        return r;
    }

    public static UserProfileLookupResult notFound() {
        UserProfileLookupResult r = new UserProfileLookupResult();
        r.ok = false;
        r.message = "未找到该姓名在 user_profile 中的记录";
        return r;
    }

    public static UserProfileLookupResult found(
            Long id, String realName, String displayName, String email, String phone) {
        UserProfileLookupResult r = new UserProfileLookupResult();
        r.ok = true;
        r.id = id;
        r.realName = realName;
        r.displayName = displayName;
        r.email = email;
        r.phone = phone;
        return r;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
