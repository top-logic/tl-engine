/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.ChatModelFactory;
import com.top_logic.ai.service.providers.AnthropicChatModelFactory;
import com.top_logic.ai.service.providers.MistralChatModelFactory;
import com.top_logic.ai.service.providers.OpenAIChatModelFactory;
import com.top_logic.ai.service.tools.registry.ToolImplementation;
import com.top_logic.basic.Logger;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentConfig;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;
import com.top_logic.tl.ai.core.provider.mistral.MistralProvider;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;
import com.top_logic.tl.ai.core.tool.Tool;
import com.top_logic.tl.ai.core.tool.ToolRegistry;

/**
 * Builds {@link AgentLoop} instances from agent definitions stored in the knowledge base.
 *
 * <p>
 * This is the primary entry point for constructing agents from model-based definitions
 * ({@code tl.ai.core:Agent} instances). It replaces the XML-config-based approach of
 * {@link AgentRegistry} with direct KB access.
 * </p>
 *
 * @see AgentDefinitionQuery
 * @see AgentLoop
 */
public class AgentFactory {

	private static final AgentFactory INSTANCE = new AgentFactory();

	/** */
	public static AgentFactory getInstance() {
		return INSTANCE;
	}

	private AgentFactory() {
		// Singleton.
	}

	/**
	 * Returns the name of the entry point agent (the agent with {@code entryPoint=true}).
	 *
	 * @return the agent name
	 * @throws IllegalStateException if no entry point agent is defined
	 */
	public String getEntryPointAgentName() {
		for (TLObject agent : AgentDefinitionQuery.getInstance().getAllAgents()) {
			Boolean entryPoint = (Boolean) agent.tValueByName("entryPoint");
			if (Boolean.TRUE.equals(entryPoint)) {
				return (String) agent.tValueByName("name");
			}
		}
		throw new IllegalStateException("No entry point agent defined in the knowledge base.");
	}

	/**
	 * Builds an {@link AgentLoop} for the agent with the given name.
	 *
	 * @param name
	 *        The agent name as stored in the KB.
	 * @return A ready-to-run agent loop.
	 * @throws IllegalArgumentException
	 *         if no agent with the given name exists
	 */
	public AgentLoop buildAgentLoop(String name) {
		TLObject agent = AgentDefinitionQuery.getInstance().findAgentByName(name);
		if (agent == null) {
			throw new IllegalArgumentException("No agent found with name: " + name);
		}

		AgentConfig config = buildConfig(agent);
		LLMProvider provider = buildProvider(config.model());
		ToolRegistry registry = buildToolRegistry(agent);

		return new AgentLoop(config, provider, registry);
	}

	/**
	 * Builds the {@link AgentConfig} record from a KB agent object.
	 */
	AgentConfig buildConfig(TLObject agent) {
		String model = (String) agent.tValueByName("model");
		if (model == null || model.isBlank()) {
			AIService service = AIService.getInstance();
			model = service != null ? service.getConfig().getDefaultModel() : null;
		}
		if (model == null) {
			throw new IllegalStateException(
				"Agent '" + agent.tValueByName("name") + "' has no model and no default model is configured.");
		}

		String systemPrompt = (String) agent.tValueByName("systemPrompt");
		systemPrompt = injectSkills(agent, systemPrompt);

		Integer maxTokens = (Integer) agent.tValueByName("maxTokens");
		int maxOutputTokens = maxTokens != null ? maxTokens : AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS;

		Map<String, Object> modelOptions = buildModelOptions(agent);

		return new AgentConfig(model, systemPrompt, AgentConfig.DEFAULT_MAX_TURNS, maxOutputTokens, modelOptions);
	}

	/**
	 * Builds provider-specific model options from agent attributes.
	 */
	private Map<String, Object> buildModelOptions(TLObject agent) {
		Map<String, Object> options = new HashMap<>();

		Double temperature = (Double) agent.tValueByName("temperature");
		if (temperature != null) {
			options.put("temperature", temperature);
		}

		Double topP = (Double) agent.tValueByName("topP");
		if (topP != null) {
			options.put("topP", topP);
		}

		return options.isEmpty() ? Map.of() : Map.copyOf(options);
	}

	/**
	 * Creates an {@link LLMProvider} for the given model name.
	 *
	 * <p>
	 * Looks up the {@link ChatModelFactory} from {@link AIService} and extracts connection details
	 * to create a native {@link LLMProvider} (bypassing LangChain4j).
	 * </p>
	 */
	LLMProvider buildProvider(String modelName) {
		AIService service = AIService.getInstance();
		if (service == null) {
			throw new IllegalStateException("AIService is not available.");
		}

		ChatModelFactory factory = service.getFactory(modelName);
		if (factory == null) {
			throw new IllegalStateException(
				"No ChatModelFactory registered for model: " + modelName
					+ ". Available: " + service.getModelNames());
		}

		if (factory instanceof AnthropicChatModelFactory) {
			AnthropicChatModelFactory.Config config =
				(AnthropicChatModelFactory.Config) factory.getConfig();

			// LangChain4j uses base URL with /v1 suffix (e.g. "https://api.anthropic.com/v1"),
			// but AnthropicProvider appends /v1/messages itself, so strip the trailing /v1.
			String baseUrl = config.getBaseUrl();
			if (baseUrl.endsWith("/v1")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
			}

			String version = config.getVersion();
			if (version == null) {
				version = "2023-06-01";
			}

			return new AnthropicProvider(config.getApiKey(), baseUrl, version);
		}

		if (factory instanceof OpenAIChatModelFactory) {
			OpenAIChatModelFactory.Config config =
				(OpenAIChatModelFactory.Config) factory.getConfig();

			String baseUrl = config.getBaseUrl();
			if (baseUrl.endsWith("/v1")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
			}

			return new OpenAIProvider(config.getApiKey(), baseUrl);
		}

		if (factory instanceof MistralChatModelFactory) {
			MistralChatModelFactory.Config config =
				(MistralChatModelFactory.Config) factory.getConfig();

			String baseUrl = config.getBaseUrl();
			if (baseUrl.endsWith("/v1")) {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 3);
			}

			return new MistralProvider(config.getApiKey(), baseUrl);
		}

		throw new UnsupportedOperationException(
			"No native LLMProvider for factory type: " + factory.getClass().getSimpleName()
				+ ". Supported: Anthropic, OpenAI, Mistral.");
	}

	/**
	 * Builds a {@link ToolRegistry} containing the tools assigned to the agent.
	 *
	 * <p>
	 * Resolves tool names from the agent's {@code tools} attribute against the
	 * tl-ai-service {@link com.top_logic.ai.service.tools.registry.ToolRegistry} and wraps them as
	 * tl-ai-core {@link Tool} objects.
	 * </p>
	 */
	ToolRegistry buildToolRegistry(TLObject agent) {
		ToolRegistry registry = new ToolRegistry();

		@SuppressWarnings("unchecked")
		Collection<String> toolNames = (Collection<String>) agent.tValueByName("tools");
		if (toolNames == null || toolNames.isEmpty()) {
			return registry.freeze();
		}

		com.top_logic.ai.service.tools.registry.ToolRegistry serviceRegistry =
			com.top_logic.ai.service.tools.registry.ToolRegistry.getInstance();

		for (String toolName : toolNames) {
			ToolImplementation impl = serviceRegistry.getTool(toolName);
			if (impl == null) {
				Logger.warn("Tool not found in registry: " + toolName, AgentFactory.class);
				continue;
			}
			registry.register(wrapTool(toolName, impl));
		}

		return registry.freeze();
	}

	/**
	 * Wraps a tl-ai-service {@link ToolImplementation} as a tl-ai-core {@link Tool}.
	 */
	private Tool wrapTool(String name, ToolImplementation impl) {
		String description = impl.getSpecification().description();

		// Serialize JsonObjectSchema to JSON string for tl-ai-core ToolSchema
		dev.langchain4j.model.chat.request.json.JsonObjectSchema params =
			impl.getSpecification().parameters();
		String inputSchema = params != null ? params.toString() : "{}";

		ToolSchema schema = new ToolSchema(name, description, inputSchema);

		return new Tool() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return description;
			}

			@Override
			public ToolSchema schema() {
				return schema;
			}

			@Override
			public String execute(Map<String, Object> args,
					com.top_logic.tl.ai.core.agent.ToolContext context) {
				try {
					// Convert args map back to JSON for LangChain4j executor
					String argsJson = argsToJson(args);
					dev.langchain4j.agent.tool.ToolExecutionRequest request =
						dev.langchain4j.agent.tool.ToolExecutionRequest.builder()
							.name(name)
							.arguments(argsJson)
							.build();
					return impl.getExecutor().execute(request, null);
				} catch (Exception e) {
					Logger.error("Tool execution failed: " + name, e, AgentFactory.class);
					return "Error: " + e.getMessage();
				}
			}
		};
	}

	private static String argsToJson(Map<String, Object> args) {
		if (args == null || args.isEmpty()) {
			return "{}";
		}
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			for (Map.Entry<String, Object> entry : args.entrySet()) {
				json.name(entry.getKey());
				writeValue(json, entry.getValue());
			}
			json.endObject();
		} catch (IOException ex) {
			Logger.error("Failed to serialize tool arguments.", ex, AgentFactory.class);
			return "{}";
		}
		return buffer.toString();
	}

	private static void writeValue(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof String s) {
			json.value(s);
		} else if (value instanceof Boolean b) {
			json.value(b);
		} else if (value instanceof Long l) {
			json.value(l);
		} else if (value instanceof Number n) {
			json.value(n.doubleValue());
		} else {
			json.value(String.valueOf(value));
		}
	}

	/**
	 * Reads skill references from the agent and injects their content into the system prompt.
	 */
	private String injectSkills(TLObject agent, String systemPrompt) {
		@SuppressWarnings("unchecked")
		Set<? extends TLObject> skills = (Set<? extends TLObject>) agent.tValueByName("skills");
		if (skills == null || skills.isEmpty()) {
			return systemPrompt != null ? systemPrompt : "";
		}

		StringBuilder skillSection = new StringBuilder();
		skillSection.append("\n\n### Available Skills\n\n");

		for (TLObject skill : skills) {
			String skillName = (String) skill.tValueByName("name");
			String skillDescription = (String) skill.tValueByName("description");
			String skillContent = (String) skill.tValueByName("content");

			if (skillName != null) {
				skillSection.append("#### ").append(skillName);
				if (skillDescription != null) {
					skillSection.append(" — ").append(skillDescription);
				}
				skillSection.append("\n\n");
				if (skillContent != null) {
					skillSection.append(skillContent).append("\n\n");
				}
			}
		}

		String base = systemPrompt != null ? systemPrompt : "";
		return base + skillSection;
	}
}
