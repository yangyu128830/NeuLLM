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

## 线上部署（推荐方案）

整项目需要 **两个平台**：Vercel 放前端，Railway（或云服务器）放后端 + 数据库。

```
用户 → Vercel（neullmfront）→ Railway（NeuLLMDev + MySQL）
```

### 第一步：Vercel 部署前端（你已完成）

- 仓库根目录 `vercel.json` 会自动构建 `neullmfront/`
- 站点示例：https://neu-llm.vercel.app

### 第二步：Railway 部署后端 + MySQL

1. 打开 [Railway](https://railway.app)，用 GitHub 登录
2. **New Project** → **Deploy from GitHub repo** → 选择 `NeuLLM`
3. **Add Service** → **Database** → **MySQL**（记下连接信息）
4. 再 **Add Service** → 同一仓库（NeuLLM 后端），**Root Directory 留空**（使用根目录 `Dockerfile`）

5. 在后端服务 **Variables** 里，点击 **Add Reference** 引用 MySQL 服务的变量（假设 MySQL 服务名为 `MySQL`）：

| 变量 | 值（Reference） |
|------|----------------|
| `MYSQLHOST` | `${{MySQL.MYSQLHOST}}` |
| `MYSQLPORT` | `${{MySQL.MYSQLPORT}}` |
| `MYSQLUSER` | `${{MySQL.MYSQLUSER}}` |
| `MYSQLPASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `MYSQLDATABASE` | `${{MySQL.MYSQLDATABASE}}` |
| `DASHSCOPE_API_KEY` | 阿里云百炼 API Key（手动填写） |
| `APP_CORS_ORIGINS` | `https://neu-llm.vercel.app` |

> 也可用手动 `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD` 覆盖，不必重复配置。

6. **Settings** → **Networking** → **Generate Domain**，得到公网地址

> **Crashed 排查**：构建成功但状态 Crashed，通常是 (1) **未配置 MySQL 变量**（默认连 localhost 会失败），(2) 未 Generate Domain。应用已支持 Railway 的 `PORT` 环境变量。

7. 浏览器访问 `https://你的后端域名/api/auth/login`，GET 返回 405 说明后端已上线

### 第三步：Vercel 连接后端

Vercel → 项目 **Settings** → **Environment Variables**：

| 变量 | 值 |
|------|-----|
| `VITE_API_BASE` | `https://你的后端域名.railway.app`（无末尾斜杠） |
| `VITE_AMAP_KEY` | 可选，高德地图 Key |
| `VITE_AMAP_SECURITY_CODE` | 可选，高德安全密钥 |

保存后在 **Deployments** 页 **Redeploy** 前端。

### 第四步：验证

1. 打开 https://neu-llm.vercel.app/login
2. 登录 `student1` / `123456`
3. 若失败：F12 → Network，确认请求发往 Railway 而非 `localhost:8082`

### 其他部署方式

| 平台 | 适合 |
|------|------|
| **Railway** | 省心，Spring Boot + MySQL 一键部署（推荐） |
| **Render** | 类似 Railway，免费档有休眠限制 |
| **云服务器**（阿里云/腾讯云） | 装 JDK 17 + MySQL，手动 `java -jar` 或使用 `NeuLLMDev/Dockerfile` |

本地开发不受影响，仍按下方「本地启动」运行即可。

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
