# NeuLLMDev 架构说明

## 分层约定

```
com.neusoft.edu.neullmdev
├── config/              # Spring 配置（llm、web）
├── controller/          # HTTP 入口（按域分包，统一 /api）
├── dto/                 # MCP 入参、管理页 Request/Response（与 tool 同域）
├── entity/              # 数据库实体（*Entity 命名）
├── mapper/              # MyBatis Mapper（注解 SQL，与 entity 同域）
├── model/               # 运行时模型：model.mcp、model.agent、model.api
├── service/
│   ├── agent/           # 对话编排、意图协调（intent/*Override）
│   ├── llm/             # 大模型调用
│   ├── mcp/             # MCP 总线、McpToolCatalog、McpCommitService
│   ├── travel/          # 天气、行程、目的地、地点坐标
│   ├── food/            # 美食搜索（腾讯地图）
│   ├── hotel/           # 酒店推荐/预订
│   ├── communication/   # 邮件、短信、邮箱别名
│   ├── document/        # Word 文档生成
│   ├── schedule/        # 日程（演示）
│   ├── profile/         # 用户资料查询与保存
│   ├── prompt/          # 系统提示（AgentSystemPrompt、AgentIntentRulesSection、reviewrhythm/*）
│   └── reminder/        # CRUD、MCP 编排、调度、internal/*
└── tool/                # MCP 薄适配（实现 McpToolHandler，委托 service）
    ├── travel/ food/ hotel/ communication/ document/ profile/ schedule/ reminder/
    └── common/          # McpToolSupport 等
```

需 preview/committed 的工具有：`send_email`、`save_user_profile`、`create_travel_reminder`、`hotel_book`。

## MCP 工具扩展（标准两步）

1. **入参** `dto.{domain}.*Params`
2. **注册** 在 `McpToolCatalog` 增加工具定义（协议与 LLM 提示共用）
3. **适配** `tool.{domain}.*` 实现 `McpToolHandler`：Map→DTO、校验、委托 `service.{domain}`

`McpToolHandler` 由 Spring 注入 `McpServiceImpl`（`List<McpToolHandler>` 自动注册，工具名不可重复）。

复杂逻辑放在 `service.*`，不要堆在 `tool` 里。

## 工具元数据单一来源

| 用途 | 类 |
|------|-----|
| MCP `tools/list` | `McpToolCatalog.listTools()` |
| LLM 系统提示工具表 | `McpToolCatalog.renderPromptToolList()` |
| 意图规则与 JSON 示例 | `AgentIntentRulesSection` |
| 完整系统提示 | `AgentSystemPrompt`（人设 + 意图 + 工具表） |

## 预览与确认（preview / committed）

| 阶段 | `McpCallContext` | 行为 |
|------|------------------|------|
| 聊天首次调用 | `preview(userInput)` | 只返回卡片 JSON，不写库、不发信 |
| 用户点确认 | `committed("")` | 真正执行副作用 |

业务 REST 统一前缀 `/api`（MCP 协议：`POST /mcp/jsonrpc`）。

确认接口（`McpConfirmController` → `McpCommitService`）：

- `POST /api/sendEmail` → `EmailParams`
- `POST /api/userProfile` → `UserProfileSaveParams`（见 `UserProfileController`）
- `POST /api/travelReminder/confirm` → `TravelReminderParams`
- `POST /api/hotelBook/confirm` → `HotelBookingParams`
- `POST /api/prompt/stream` → Agent SSE

管理页 CRUD（`model.api.ApiResponse` + `TravelReminderRequest`/`Response`）：

- `POST /api/travelReminder`、`GET /api/travelReminders`、`GET|PUT|DELETE /api/travelReminder/{id}`

**不要**在聊天确认流中使用 `POST /api/travelReminder`。

## 数据持久化

| 表 | entity | mapper |
|----|--------|--------|
| travel_reminder | entity.reminder.TravelReminderEntity | mapper.reminder |
| user_profile | entity.profile.UserProfileEntity | mapper.profile |
| hotel_order | entity.hotel.HotelOrderEntity | mapper.hotel |
| locations | entity.travel.LocationEntity | mapper.travel |
| emailaddress | entity.communication.EmailAddressEntity | mapper.communication |

## 与其它模块的边界

| 组件 | 职责 |
|------|------|
| `tool.*`（`McpToolHandler`） | MCP 入参适配；委托 `service.*` |
| `service.{domain}` | 业务逻辑、外部 API、落库 |
| `service.agent` | SSE 对话编排、意图纠偏 |
| `service.mcp` | 工具路由、提交、元数据目录 |

不要把 HTTP 写在 `tool` 中；不要把表实体放在 `service` 根包。
