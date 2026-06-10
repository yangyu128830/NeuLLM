package com.neusoft.edu.neullmdev.service.classroom;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 内置课堂任务模板，供教师一键选用。 */
@Service
public class ClassroomTaskTemplateService {

    private static final List<Map<String, Object>> TEMPLATES = List.of(
            template("agent-design", "自主决策 Agent 设计", """
                    围绕多智能体协作完成需求分析、工具设计与成果交付。
                    """, List.of(
                    sub("需求分析", "完成需求分析文档，含用户故事与约束。"),
                    sub("方案设计", "提交工具清单与 Plan 设计。"),
                    sub("成果交付", "提交可演示成果与说明。")
            )),
            template("weekly-report", "周学习报告", """
                    每周提交学习进度与反思，教师跟进完成情况。
                    """, List.of(
                    sub("本周总结", "200 字以内本周学习要点。"),
                    sub("问题与计划", "列出困难与下周计划。")
            )),
            template("lab-report", "实验报告", """
                    完成实验操作并提交报告与原始数据说明。
                    """, List.of(
                    sub("实验记录", "提交实验步骤与关键截图。"),
                    sub("分析与结论", "数据分析与结论，附参考文献。")
            ))
    );

    public List<Map<String, Object>> listTemplates() {
        return TEMPLATES;
    }

    public Map<String, Object> getTemplate(String id) {
        return TEMPLATES.stream()
                .filter(t -> id.equals(t.get("id")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知模板：" + id));
    }

    private static Map<String, Object> template(String id, String title, String description,
                                                 List<Map<String, String>> subTasks) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("description", description.trim());
        m.put("subTasks", subTasks);
        return m;
    }

    private static Map<String, String> sub(String title, String description) {
        Map<String, String> s = new LinkedHashMap<>();
        s.put("title", title);
        s.put("description", description);
        return s;
    }
}
