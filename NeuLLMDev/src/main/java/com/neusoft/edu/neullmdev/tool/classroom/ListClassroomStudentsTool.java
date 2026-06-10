package com.neusoft.edu.neullmdev.tool.classroom;

import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.classroom.ClassroomService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListClassroomStudentsTool implements McpToolHandler {

    private final ClassroomService classroomService;

    public ListClassroomStudentsTool(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Override
    public String toolName() {
        return "list_classroom_students";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        var students = classroomService.listStudents(stringArg(arguments, "classId"));
        return new ToolResult(toolName(), "共 " + students.size() + " 名学生", students);
    }

    static String stringArg(Map<String, Object> args, String key) {
        Object v = args == null ? null : args.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
