---
name: add-ai-provider
description: This skill should be used when adding a new AI provider, LLM integration, or ChatModelFactory implementation to tl-ai-service.
version: 1.0.0
---

# Adding a New AI Provider

To add support for a new AI provider to `tl-ai-service`:

## Step 1: Create Factory Class

Create a class implementing `ChatModelFactory` in `tl-ai-service/src/main/java/com/top_logic/ai/service/providers/`:

```java
public class MyProviderChatModelFactory implements ChatModelFactory {
    @Override
    public ChatModel createChatModel() {
        // Initialize provider-specific ChatModel
    }

    @Override
    public ObjectPool<ChatModel> createPool(String modelName) {
        // Create and configure model pool
    }
}
```

## Step 2: Add Configuration Interface

Extend `ChatModelFactory.Config`:

```java
public interface Config<I extends MyProviderChatModelFactory>
        extends ChatModelFactory.Config<I> {
    // Provider-specific configuration properties
}
```

## Step 3: Register in Configuration

Add to `tl-ai-service/src/main/webapp/WEB-INF/conf/tl-ai-openai.conf.config.xml`:

```xml
<factory
    class="com.top_logic.ai.service.providers.MyProviderChatModelFactory"
    models="model-name-1, model-name-2"
    api-key="%MY_PROVIDER_API_KEY%"
/>
```

## Factory Configuration Properties

Each `ChatModelFactory` supports:
- **models**: Comma-separated list of model names (leave empty for auto-discovery)
- **api-key**: API key (use environment variable references like `%MY_PROVIDER_API_KEY%`)
- **base-url**: Provider API endpoint
- **max-pool-size**: Maximum number of model instances in pool
- **max-idle-models**: Maximum idle instances to keep

## Existing Providers for Reference

- `OpenAIChatModelFactory` — GPT-3.5, GPT-4, GPT-4o models
- `AnthropicChatModelFactory` — Claude 3 (Opus, Sonnet, Haiku)
- `MistralChatModelFactory` — Mistral AI models (with auto-discovery)
