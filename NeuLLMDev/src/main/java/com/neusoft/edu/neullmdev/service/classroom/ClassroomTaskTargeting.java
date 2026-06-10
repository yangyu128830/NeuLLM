package com.neusoft.edu.neullmdev.service.classroom;

import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.entity.classroom.ClassroomTaskEntity;

public final class ClassroomTaskTargeting {

    private ClassroomTaskTargeting() {
    }

    public static boolean matches(ClassroomTaskEntity task, SysUserEntity student) {
        if (task == null || student == null) {
            return false;
        }
        if (task.getTargetMajor() != null && !task.getTargetMajor().isBlank()) {
            if (!task.getTargetMajor().equals(student.getMajor())) {
                return false;
            }
        }
        if (task.getTargetGrade() != null && !task.getTargetGrade().isBlank()) {
            if (!task.getTargetGrade().equals(student.getGrade())) {
                return false;
            }
        }
        if (task.getTargetClassName() != null && !task.getTargetClassName().isBlank()) {
            if (!task.getTargetClassName().equals(student.getClassName())) {
                return false;
            }
        }
        return true;
    }
}
