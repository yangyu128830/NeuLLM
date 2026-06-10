package com.neusoft.edu.neullmdev.service.llm;

/**
 * 知识图谱增强：根据用户问题抽取关联知识点提示，供答疑与规划时约束输出（轻量实现，可替换为图数据库）。
 */
public interface KnowledgeService {

    /**
     * 若命中预设知识点，则在用户输入后追加「关联知识点」提示；否则原样返回。
     */
    String enrichUserQuery(String userInput);
}
