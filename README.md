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

## Vercel 部署（仅前端）

仓库根目录已包含 `vercel.json`，从 GitHub 导入后 **Root Directory 保持默认（仓库根目录）** 即可，Vercel 会自动构建 `neullmfront/` 并输出到 `neullmfront/dist`。

若已在 Vercel 中将 Root Directory 设为 `neullmfront`，同样可用（该目录下也有 `vercel.json` 处理 SPA 路由）。

### 环境变量（Vercel → Settings → Environment Variables）

| 变量 | 说明 |
|------|------|
| `VITE_API_BASE` | **必填**。已部署的后端公网地址，如 `https://api.example.com`（不要末尾斜杠） |
| `VITE_AMAP_KEY` | 可选。高德地图 JS API Key（行程/路线地图） |
| `VITE_AMAP_SECURITY_CODE` | 可选。高德安全密钥 |

配置完成后在 Deployments 页 **Redeploy** 一次。

> **说明**：Vercel 只能托管 Vue 静态前端。`NeuLLMDev`（Spring Boot + MySQL）需单独部署到云服务器、Railway、Render 等平台；前端通过 `VITE_API_BASE` 连接后端。

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
