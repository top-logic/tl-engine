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
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.input.CustomInput;
import com.top_logic.ai.service.agents.background.input.CustomInputAdapter;
import com.top_logic.ai.service.agents.background.input.InputAdapter;
import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.input.ProviderInput;
import com.top_logic.ai.service.agents.background.input.ProviderInputAdapter;
import com.top_logic.ai.service.agents.background.input.ScriptInput;
import com.top_logic.ai.service.agents.background.input.ScriptInputAdapter;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.ai.service.agents.background.output.OutputHandlerImplementation;
import com.top_logic.ai.service.agents.background.output.ScriptOutputHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

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
	 * @return The {@link ExecutionResult} containing the LLM response and any block reason.
	 * @throws IllegalArgumentException
	 *         if the agent has no background configuration.
	 */
	public ExecutionResult execute(String agentName, BackgroundContext context) {
		AgentConfig agentConfig = _registry.getAgentConfig(agentName);
		if (!(agentConfig instanceof BackgroundAgentConfig bgConfig)) {
			throw new IllegalArgumentException("Agent '" + agentName + "' is not a BackgroundAgentConfig");
		}

		// 1. Resolve input: pre-turn always includes the pending user message
		String input;
		if (bgConfig.getTrigger() instanceof PreTurnTrigger && context instanceof ChatContext chatContext) {
			InputConfig inputConfig = bgConfig.getInput();
			input = "<user-message>\n" + chatContext.getPendingUserMessage() + "\n</user-message>";
			if (inputConfig != null) {
				InputAdapter adapter = resolveInputAdapter(inputConfig);
				String additional = adapter.gatherInput(context);
				input += "\n\n<additional-context>\n" + (additional != null ? additional : "") + "\n</additional-context>";
			}
		} else {
			InputAdapter adapter = resolveInputAdapter(bgConfig.getInput());
			input = adapter.gatherInput(context);
		}

		// 2. Build and invoke the agent
		BlockTurnResult.clear();
		String response = invokeAgent(agentName, input);

		// 3. Check if agent called block_turn
		String blockReason = BlockTurnResult.consumeBlockReason();

		// 4. Run output handlers
		runOutputHandlers(bgConfig, response, context);

		return new ExecutionResult(response, blockReason);
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
	 * Built-in config types ({@link CustomInput}, {@link ScriptInput}, {@link ProviderInput})
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
		if (inputConfig instanceof CustomInput cfg) {
			return new CustomInputAdapter(cfg);
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

	private void runOutputHandlers(BackgroundAgentConfig bgConfig, String response,
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
			evaluateScript(scriptHandler, response, context);
			return;
		}
		Logger.warn("No dispatcher for output handler: " + handler.getClass().getName(),
			BackgroundAgentExecutor.class);
	}

	private void evaluateScript(ScriptOutputHandler scriptHandler, String response,
			BackgroundContext context) {
		try {
			ThreadContextManager.inSystemInteraction(BackgroundAgentExecutor.class, () -> {
				TLModel model = ModelService.getApplicationModel();
				SearchExpression searchExpr = SearchBuilder.toSearchExpression(model, scriptHandler.getScript());

				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
				QueryExecutor executor = QueryExecutor.compile(kb, model, searchExpr);

				return kb.withoutModifications(() ->
					executor.executeWith(
						executor.context(true, null, null),
						Args.some(response)));
			});
		} catch (Exception ex) {
			Logger.error("Script output handler failed for agent '" + context.getAgentName() + "'",
				ex, BackgroundAgentExecutor.class);
		}
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
