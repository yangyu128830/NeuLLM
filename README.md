# NeuLLM

智能体对话与 MCP 工具平台（旅游、天气、酒店、提醒等）。

## 项目组成

| 目录 | 说明 | 技术栈 |
|------|------|--------|
| [NeuLLMDev](./NeuLLMDev/) | **主后端**：聊天 Agent、MCP 工具、提醒调度 | Spring Boot 3 + MyBatis |
| [neullmfront](./neullmfront/) | **前端**：对话 UI、SSE 流式展示 | Vue 3 + Vite |
| [database](./database/) | 数据库初始化脚本 | SQL |

## 本地启动

### 1. 数据库

执行 `database/init-full.sql`（或 NeuLLMDev 自带的 `schema.sql`），配置 `NeuLLMDev/src/main/resources/application.yml` 中的数据源。

### 2. 后端

```bash
cd NeuLLMDev
./mvnw spring-boot:run
```

### 3. 前端

```bash
cd neullmfront
npm install
npm run dev
```

前端通过 Vite 代理或配置的后端地址访问 NeuLLMDev API。

## 请求链路（主应用）

```
用户 → neullmfront → PromptController
                    → AgentService（意图 + 函数调用解析）
                    → McpService → Tool（McpToolHandler）→ Service → 外部 API / DB
                    → FinalAnswerService → SSE 流式返回
```

后端包结构与扩展约定见 [NeuLLMDev/ARCHITECTURE.md](./NeuLLMDev/ARCHITECTURE.md)。

## 命名说明

- **NeuLLMDev**：主 Spring Boot 应用（包名 `neullmdev`）
