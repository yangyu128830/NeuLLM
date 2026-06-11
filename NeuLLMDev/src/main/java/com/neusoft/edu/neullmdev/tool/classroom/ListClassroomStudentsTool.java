package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomStudentService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListClassroomStudentsTool implements McpToolHandler {

    private final ClassroomStudentService studentService;

    public ListClassroomStudentsTool(ClassroomStudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public String toolName() {
        return "list_classroom_students";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        var students = studentService.listStudents(stringArg(arguments, "classId"));
        return new ToolResult(toolName(), "班级学生 " + students.size() + " 人", students);
    }

    static String stringArg(Map<String, Object> arguments, String key) {
        if (arguments == null) {
            return null;
        }
        Object v = arguments.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }
}
