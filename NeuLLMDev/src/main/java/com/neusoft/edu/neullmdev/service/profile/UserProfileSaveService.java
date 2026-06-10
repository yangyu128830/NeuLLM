package com.neusoft.edu.neullmdev.service.profile;

import com.neusoft.edu.neullmdev.dto.profile.UserProfileSaveParams;
import com.neusoft.edu.neullmdev.entity.profile.UserProfileEntity;
import com.neusoft.edu.neullmdev.mapper.profile.UserProfileMapper;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserProfileSaveService {

    private final UserProfileMapper userProfileMapper;

    public UserProfileSaveService(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    public ToolResult save(UserProfileSaveParams params, McpCallContext ctx) {
        if (ctx.commitSideEffects()) {
            return McpToolSupport.fromJsonString("save_user_profile", persist("save_user_profile", params));
        }
        JSONObject o = new JSONObject();
        o.put("functionName", "save_user_profile");
        o.put("name", "save_user_profile");
        o.put("externalId", McpToolSupport.trimOrEmpty(params.getExternalId()));
        o.put("displayName", McpToolSupport.trimOrEmpty(params.getDisplayName()));
        o.put("realName", McpToolSupport.trimOrEmpty(params.getRealName()));
        o.put("email", McpToolSupport.trimOrEmpty(params.getEmail()));
        o.put("phone", McpToolSupport.trimOrEmpty(params.getPhone()));
        o.put("address", McpToolSupport.trimOrEmpty(params.getAddress()));
        Object g = params.getGender();
        o.put("gender", g == null ? "" : String.valueOf(g).trim());
        o.put("avatarUrl", McpToolSupport.trimOrEmpty(params.getAvatarUrl()));
        o.put("remark", McpToolSupport.trimOrEmpty(params.getRemark()));
        return new ToolResult("save_user_profile", "用户资料预填卡片", o.toString());
    }

    public String persist(String functionName, UserProfileSaveParams params) {
        if (params == null) {
            return err("缺少参数");
        }
        String ext = trimToNull(params.getExternalId());
        String display = trimToNull(params.getDisplayName());
        String real = trimToNull(params.getRealName());
        String email = trimToNull(params.getEmail());
        String phone = trimToNull(params.getPhone());
        if (ext == null && display == null && real == null && email == null && phone == null) {
            return err("请至少提供 external_id、display_name、real_name、email、phone 之一");
        }

        UserProfileEntity e = new UserProfileEntity();
        e.setExternalId(ext);
        e.setDisplayName(display);
        e.setRealName(real);
        e.setEmail(email);
        e.setPhone(phone);
        e.setAddress(trimToNull(params.getAddress()));
        e.setGender(normalizeGender(params.getGender()));
        e.setAvatarUrl(trimToNull(params.getAvatarUrl()));
        e.setRemark(trimToNull(params.getRemark()));
        LocalDateTime now = LocalDateTime.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        try {
            userProfileMapper.insert(e);
        } catch (DataIntegrityViolationException ex) {
            return err("邮箱、手机号或 external_id 与已有记录冲突，未写入（同一邮箱/手机仅能存一条）。");
        }

        JSONObject o = new JSONObject();
        o.put("ok", true);
        o.put("function", functionName);
        o.put("id", e.getId());
        o.put("message", "已写入 user_profile 表");
        return o.toString();
    }

    private static String err(String msg) {
        return new JSONObject().put("ok", false).put("message", msg).toString();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer normalizeGender(Object g) {
        if (g == null) {
            return null;
        }
        if (g instanceof Number n) {
            int v = n.intValue();
            if (v >= 0 && v <= 2) {
                return v;
            }
            return null;
        }
        String s = g.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        if ("1".equals(s) || "男".equals(s)) {
            return 1;
        }
        if ("2".equals(s) || "女".equals(s)) {
            return 2;
        }
        if ("0".equals(s) || "未知".equals(s)) {
            return 0;
        }
        return null;
    }
}
