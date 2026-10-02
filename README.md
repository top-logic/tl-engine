# TopLogic AI Integration

[AI integration](https://github.com/top-logic/tl-ai) for [TopLogic](https://github.com/top-logic/tl-engine) applications with LLM support, MCP server capabilities, and chat functionality.

## Modules

### [tl-ai-tool-api](tl-ai-tool-api/)
Shared tool contracts (`Tool`, `ToolResult`, `ToolMode`, `@ToolMethod`, `@P`, `ToolSchema`, `ToolRegistryService`). No implementations — just interfaces, annotations, and the service-level registry.

### [tl-ai-core](tl-ai-core/)
Core AI framework with custom agent loop, multi-provider LLM support (OpenAI, Anthropic, Mistral, xAI), tool system, multi-agent orchestration, and TL-Script functions.

### [tl-ai-mcp-server](tl-ai-mcp-server/)
Model Context Protocol (MCP) server implementation enabling AI agents to interact with TopLogic's model structure, metadata, and business logic through standardized resources and tools.

**Switched off by default.** `/mcp/*` authenticates nobody, so enabling it publishes the application's model and business data to everyone who can reach the endpoint. [doc/security/mcp-endpoint.md](doc/security/mcp-endpoint.md) says what is exposed and how an administrator turns it on.

### [tl-chat](tl-chat/)
Chat UI components and model definitions for building conversational interfaces within TopLogic applications. Chat messages are handled by the entry-point agent (tl-ai-core), which routes to specialist agents (ModelExplorer, ModelEditor, TLScriptAgent) via delegation; the UI shows a unified execution timeline (AgentCall vs ToolCall). Includes persistent chat sessions with per-user history stored in the KnowledgeBase.

### [tl-chat-classic](tl-chat-classic/)
The React chat in a classic LayoutServlet application - the "Albert" tab. A `LayoutComponent` writes a React view into the classic page; an application gets the tab by depending on this module and chooses the view it shows. A temporary bridge with documented workarounds, see [tl-chat-classic/CLAUDE.md](tl-chat-classic/CLAUDE.md).

### [tl-ai-vector-index](tl-ai-vector-index/)
Semantic search via embedding models and vector stores, with background indexing daemon.

### [tl-ai-demo](tl-ai-demo/)
Demo application showcasing AI integration features including chat interactions, MCP server capabilities, and AI-powered business logic.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- TopLogic 8.0.0-alpha3
- Optional: API keys for AI providers (OpenAI, Anthropic, Mistral, or xAI)

### Build

```bash
# Build all modules
mvn clean install
```

### Configuration

Set your API keys as environment variables:

```bash
export tl_openai_api_key="sk-..."
export tl_anthropic_api_key="sk-ant-..."
export tl_mistral_api_key="..."
export tl_xai_api_key="..."
```

Configure AI models in `tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.conf.config.xml`.

#### Gitea Integration (optional)

The ReviewAgent uses the `create_gitea_issue` tool to file quality issues. To enable it, set the following environment variables:

```bash
export tl_gitea_url="https://gitea.example.com"
export tl_gitea_token="your-api-token"
export tl_gitea_repo_owner="org-or-user"
export tl_gitea_repo_name="repository-name"
```

Without these variables the tool returns an error message and no issues are created.

### Run Demo

```bash
cd tl-ai-demo
mvn
```

Access at: http://localhost:8080/tl-ai-demo/
