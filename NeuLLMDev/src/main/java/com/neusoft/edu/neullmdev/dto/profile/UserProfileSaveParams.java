package com.neusoft.edu.neullmdev.dto.profile;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileSaveParams {

    @JsonAlias({"externalId", "external_id", "userId", "user_id"})
    private String externalId;

    @JsonAlias({"displayName", "display_name", "nickname"})
    private String displayName;

    @JsonAlias({"realName", "real_name", "name", "fullName", "full_name"})
    private String realName;

    private String email;

    @JsonAlias({"phone", "mobile", "phoneNumber", "phone_number", "tel"})
    private String phone;

    private String address;

    /** 0 未知 / 1 男 / 2 女；也可由模型传字符串「男」「女」由后端归一 */
    private Object gender;

    @JsonAlias({"avatarUrl", "avatar_url"})
    private String avatarUrl;

    private String remark;
}
