package com.neusoft.edu.neullmdev.service.llm;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    /** 关键词 → 关联知识点简述（模拟知识图谱关联查询） */
    private static final Map<String, String> TOPIC_GRAPH;

    static {
        TOPIC_GRAPH = new LinkedHashMap<>();
        TOPIC_GRAPH.put("java", "JavaSE：面向对象、集合框架、异常、IO、泛型、反射");
        TOPIC_GRAPH.put("javase", "JavaSE：面向对象、集合框架、异常、IO、泛型、反射");
        TOPIC_GRAPH.put("多线程", "并发：线程创建、synchronized、volatile、线程池、JUC 常用类");
        TOPIC_GRAPH.put("并发", "并发：线程创建、synchronized、volatile、线程池、JUC 常用类");
        TOPIC_GRAPH.put("数据结构", "数据结构：线性表、栈队列、树与图、排序与查找");
        TOPIC_GRAPH.put("操作系统", "操作系统：进程线程、调度、内存、文件系统、死锁");
        TOPIC_GRAPH.put("计算机网络", "计算机网络：TCP/IP、HTTP、DNS、七层模型");
        TOPIC_GRAPH.put("数据库", "数据库：SQL、索引、事务、范式、锁");
        TOPIC_GRAPH.put("英语", "英语：词汇积累、语法结构、阅读与写作策略");
    }

    @Override
    public String enrichUserQuery(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return userInput;
        }
        String lower = userInput.toLowerCase();
        StringBuilder hints = new StringBuilder();
        for (Map.Entry<String, String> e : TOPIC_GRAPH.entrySet()) {
            String key = e.getKey();
            if (lower.contains(key.toLowerCase())) {
                if (hints.length() > 0) {
                    hints.append("；");
                }
                hints.append(e.getValue());
            }
        }
        if (hints.length() == 0) {
            return userInput;
        }
        return userInput + "\n\n【知识图谱关联知识点】" + hints;
    }
}
