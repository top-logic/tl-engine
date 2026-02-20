# TopLogic AI Integration

[AI integration](https://github.com/top-logic/tl-ai) for [TopLogic](https://github.com/top-logic/tl-engine) applications with LLM support, MCP server capabilities, and chat functionality.

## Modules

### [tl-ai-service](tl-ai-service/)
AI service integration providing LLM access through LangChain4j with support for OpenAI, Anthropic, and Mistral models. Includes TL-Script functions for chat, structured JSON output, and multimodal content.

### [tl-ai-mcp-server](tl-ai-mcp-server/)
Model Context Protocol (MCP) server implementation enabling AI agents to interact with TopLogic's model structure, metadata, and business logic through standardized resources and tools.

### [tl-chat](tl-chat/)
Chat UI components and model definitions for building conversational interfaces within TopLogic applications. Chat messages are handled by **OrchestratorAgent** (tl-ai-service), which routes to specialist agents (ModelExplorer, ModelEditor, TLScriptAgent) via **AgentInvoker** and a structured work-order handoff; the UI shows a unified execution timeline (AgentCall vs ToolCall). Includes persistent chat sessions with per-user history stored in the KnowledgeBase.

### [tl-ai-demo](tl-ai-demo/)
Demo application showcasing AI integration features including chat interactions, MCP server capabilities, and AI-powered business logic.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- TopLogic 7.10.0-SNAPSHOT
- Optional: API keys for AI providers (OpenAI, Anthropic, or Mistral)

### Build

```bash
# Build all modules
mvn clean install
```

### Configuration

Set your API keys as environment variables:

```bash
export tl_openai_apikey="sk-..."
export tl_anthropic_api_key="sk-ant-..."
export tl_mistral_api_key="..."
```

Configure AI models in `tl-ai-service/src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml`.

### Run Demo

```bash
cd tl-ai-demo
mvn
```

Access at: http://localhost:8080/tl-ai-demo/
