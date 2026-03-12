/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.top_logic.ai.service.agents.AgentConfig;
import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.input.AutonomousInput;
import com.top_logic.ai.service.agents.background.input.AutonomousInputAdapter;
import com.top_logic.ai.service.agents.background.input.InputAdapter;
import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.input.ProviderInput;
import com.top_logic.ai.service.agents.background.input.ProviderInputAdapter;
import com.top_logic.ai.service.agents.background.input.ScriptInput;
import com.top_logic.ai.service.agents.background.input.ScriptInputAdapter;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.ai.service.agents.background.output.OutputHandlerImplementation;
import com.top_logic.ai.service.agents.background.output.ScriptOutputHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agentic.UntypedAgent;

/**
 * Central execution pipeline for background agents.
 *
 * <p>
 * Orchestrates the full lifecycle: resolve config, gather input via {@link InputAdapter}, invoke the
 * LLM agent, and run output handlers.
 * </p>
 *
 * <p>
 * External modules (e.g. {@code tl-chat}) can register additional input adapter factories via
 * {@link #registerInputAdapterFactory} to avoid circular dependencies.
 * </p>
 */
public class BackgroundAgentExecutor {

	private final AgentRegistry _registry;

	private static final Map<Class<?>, Function<Object, InputAdapter>> EXTERNAL_ADAPTERS =
		new ConcurrentHashMap<>();

	/**
	 * Creates a new {@link BackgroundAgentExecutor}.
	 *
	 * @param registry
	 *        The agent registry for resolving agent configurations.
	 */
	public BackgroundAgentExecutor(AgentRegistry registry) {
		_registry = registry;
	}

	/**
	 * Executes the background agent pipeline synchronously.
	 *
	 * <p>
	 * The pipeline consists of three steps:
	 * </p>
	 * <ol>
	 * <li>Resolve the {@link InputAdapter} and gather input</li>
	 * <li>Build and invoke the LLM agent</li>
	 * <li>Run output handlers</li>
	 * </ol>
	 *
	 * @param agentName
	 *        The name of the background agent to execute.
	 * @param context
	 *        The execution context providing access to chat history and session data.
	 * @return The raw LLM response string; may be null.
	 * @throws IllegalArgumentException
	 *         if the agent has no background configuration.
	 */
	public String execute(String agentName, BackgroundContext context) {
		AgentConfig agentConfig = _registry.getAgentConfig(agentName);
		BackgroundConfig bgConfig = agentConfig.getBackground();
		if (bgConfig == null) {
			throw new IllegalArgumentException("Agent '" + agentName + "' has no background config");
		}

		// 1. Resolve input adapter and gather input
		InputAdapter adapter = resolveInputAdapter(bgConfig.getInput());
		String input = adapter.gatherInput(context);

		// 2. Build and invoke the agent
		String response = invokeAgent(agentName, input);

		// 3. Run output handlers
		runOutputHandlers(bgConfig, response, context);

		return response;
	}

	/**
	 * Registers an input adapter factory for a config type defined in another module.
	 *
	 * <p>
	 * This allows modules like {@code tl-chat} to register adapters for their own
	 * {@link InputConfig} sub-types without creating a compile-time dependency from
	 * {@code tl-ai-service} to {@code tl-chat}.
	 * </p>
	 *
	 * @param <T>
	 *        The config type.
	 * @param configType
	 *        The class of the config type.
	 * @param factory
	 *        The factory that creates an {@link InputAdapter} from the config.
	 */
	@SuppressWarnings("unchecked")
	public static <T> void registerInputAdapterFactory(
			Class<T> configType, Function<T, InputAdapter> factory) {
		EXTERNAL_ADAPTERS.put(configType, cfg -> factory.apply((T) cfg));
	}

	/**
	 * Resolves the appropriate {@link InputAdapter} for the given {@link InputConfig}.
	 *
	 * <p>
	 * Built-in config types ({@link AutonomousInput}, {@link ScriptInput}, {@link ProviderInput})
	 * are handled directly. Additional types registered via
	 * {@link #registerInputAdapterFactory(Class, Function)} are checked as a fallback.
	 * </p>
	 *
	 * @param inputConfig
	 *        The input configuration.
	 * @return The resolved adapter.
	 * @throws IllegalArgumentException
	 *         if no adapter can be found for the config type.
	 */
	InputAdapter resolveInputAdapter(InputConfig inputConfig) {
		if (inputConfig instanceof AutonomousInput cfg) {
			return new AutonomousInputAdapter(cfg);
		} else if (inputConfig instanceof ScriptInput cfg) {
			return new ScriptInputAdapter(cfg);
		} else if (inputConfig instanceof ProviderInput cfg) {
			return new ProviderInputAdapter(cfg);
		}
		// Check external adapters (e.g. ChatContextInput -> ChatContextAdapter from tl-chat)
		for (Map.Entry<Class<?>, Function<Object, InputAdapter>> entry : EXTERNAL_ADAPTERS.entrySet()) {
			if (entry.getKey().isInstance(inputConfig)) {
				return entry.getValue().apply(inputConfig);
			}
		}
		throw new IllegalArgumentException("No adapter for input config: " + inputConfig.getClass().getName());
	}

	private void runOutputHandlers(BackgroundConfig bgConfig, String response,
			BackgroundContext context) {
		for (OutputHandler handler : bgConfig.getOutputHandlers()) {
			try {
				dispatchOutputHandler(handler, response, context);
			} catch (Exception ex) {
				Logger.error("Output handler failed for agent '" + context.getAgentName() + "'",
					ex, BackgroundAgentExecutor.class);
			}
		}
	}

	private void dispatchOutputHandler(OutputHandler handler, String response,
			BackgroundContext context) {
		if (handler instanceof OutputHandlerImplementation.Config<?> javaConfig) {
			DefaultInstantiationContext instCtx = new DefaultInstantiationContext(BackgroundAgentExecutor.class);
			OutputHandlerImplementation impl = instCtx.getInstance(javaConfig);
			impl.handleOutput(response, context);
			return;
		}
		if (handler instanceof ScriptOutputHandler scriptHandler) {
			// TL-Script evaluation will be implemented later
			Logger.info("ScriptOutputHandler not yet implemented for agent '" + context.getAgentName() + "'",
				BackgroundAgentExecutor.class);
			return;
		}
		Logger.warn("No dispatcher for output handler: " + handler.getClass().getName(),
			BackgroundAgentExecutor.class);
	}

	private String invokeAgent(String agentName, String input) {
		try {
			Object response = ThreadContextManager.inSystemInteraction(
				BackgroundAgentExecutor.class,
				() -> {
					UntypedAgent agent = _registry.buildUntypedAgent(agentName);
					return agent.invoke(Map.of("message", input));
				});
			return response != null ? response.toString() : null;
		} catch (Exception ex) {
			Logger.error("Agent invocation failed for '" + agentName + "'",
				ex, BackgroundAgentExecutor.class);
			return null;
		}
	}
}
