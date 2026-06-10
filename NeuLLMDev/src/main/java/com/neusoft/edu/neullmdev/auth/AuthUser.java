package com.neusoft.edu.neullmdev.auth;

import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;

public record AuthUser(
        Long id,
        String username,
        UserRole role,
        String displayName,
        String studentNo,
        String classId
) {
    public static AuthUser from(SysUserEntity entity) {
        return new AuthUser(
                entity.getId(),
                entity.getUsername(),
                UserRole.from(entity.getRole()),
                entity.getDisplayName(),
                entity.getStudentNo(),
                entity.getClassId()
        );
    }
}
