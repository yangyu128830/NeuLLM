package com.neusoft.edu.neullmdev.config.auth;

import com.neusoft.edu.neullmdev.entity.auth.SysUserEntity;
import com.neusoft.edu.neullmdev.mapper.auth.SysUserMapper;
import com.neusoft.edu.neullmdev.service.auth.PasswordHasher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class AuthDataInitializer implements ApplicationRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordHasher passwordHasher;

    public AuthDataInitializer(SysUserMapper sysUserMapper, PasswordHasher passwordHasher) {
        this.sysUserMapper = sysUserMapper;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (sysUserMapper.count() > 0) {
            return;
        }
        seedTeacher("teacher", "王老师", "T2024001", "计算机学院", "副教授",
                "teacher@example.com", "13800138000");
        seed("student1", "STUDENT", "张三", "20240001", "软件工程", "2024级", "软工2401班", null, null);
        seed("student2", "STUDENT", "李四", "20240002", "软件工程", "2024级", "软工2401班", null, null);
        seed("student3", "STUDENT", "王五", "20240003", "人工智能", "2024级", "智能2401班", null, null);
        seed("student4", "STUDENT", "赵六", "20240004", "软件工程", "2024级", "软工2401班", null, null);
    }

    private void seed(String username, String role, String displayName, String studentNo,
                      String major, String grade, String className, String email, String phone) {
        SysUserEntity row = new SysUserEntity();
        row.setUsername(username);
        row.setPasswordHash(passwordHasher.encode("123456"));
        row.setRole(role);
        row.setDisplayName(displayName);
        row.setStudentNo(studentNo);
        row.setClassId("CLASS-01");
        row.setMajor(major);
        row.setGrade(grade);
        row.setClassName(className);
        row.setEmail(email);
        row.setPhone(phone);
        sysUserMapper.insert(row);
    }

    private void seedTeacher(String username, String displayName, String employeeNo, String department,
                             String title, String email, String phone) {
        SysUserEntity row = new SysUserEntity();
        row.setUsername(username);
        row.setPasswordHash(passwordHasher.encode("123456"));
        row.setRole("TEACHER");
        row.setDisplayName(displayName);
        row.setStudentNo(employeeNo);
        row.setClassId("CLASS-01");
        row.setMajor(department);
        row.setGrade(title);
        row.setEmail(email);
        row.setPhone(phone);
        row.setTaughtSubjects("[\"人工智能\",\"软件工程\"]");
        row.setTeachingScopes("[{\"major\":\"软件工程\",\"grade\":\"2024级\",\"className\":\"软工2401班\"},"
                + "{\"major\":\"人工智能\",\"grade\":\"2024级\",\"className\":\"智能2401班\"}]");
        sysUserMapper.insert(row);
    }
}
