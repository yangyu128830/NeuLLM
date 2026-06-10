package com.neusoft.edu.neullmdev.tool.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.edu.neullmdev.dto.document.WordWriteParams;
import com.neusoft.edu.neullmdev.model.mcp.McpCallContext;
import com.neusoft.edu.neullmdev.model.mcp.ToolResult;
import com.neusoft.edu.neullmdev.service.document.WordWriteService;
import com.neusoft.edu.neullmdev.service.mcp.McpToolHandler;
import com.neusoft.edu.neullmdev.tool.common.McpToolSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/** MCP 适配：Word 文档生成委托 {@link WordWriteService}。 */
@Component
public class WordWriteTool implements McpToolHandler {

    private final WordWriteService wordWriteService;
    private final ObjectMapper objectMapper;

    public WordWriteTool(WordWriteService wordWriteService, ObjectMapper objectMapper) {
        this.wordWriteService = wordWriteService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String toolName() {
        return "word_write";
    }

    @Override
    public ToolResult handle(Map<String, Object> arguments, McpCallContext context) {
        WordWriteParams params = objectMapper.convertValue(arguments, WordWriteParams.class);
        if (McpToolSupport.isBlank(params.getTitle()) && McpToolSupport.isBlank(params.getContent())) {
            return McpToolSupport.validationError(toolName(), "参数校验失败：title 或 content 至少需要提供一个");
        }
        return wordWriteService.write(params);
    }
}
