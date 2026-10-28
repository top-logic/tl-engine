/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;

/**
 * A provider whose answers a test writes, so that a chat run goes the whole way a real one goes -
 * {@code ChatProcessor}, {@code AgentFactory}, the tool catalog, the skills - with no key and no
 * request leaving the test.
 *
 * <p>
 * The same shape as the eval's scripted provider (tl-ai-eval), which this module cannot depend on.
 * The script is installed statically, because the provider is created once at service startup.
 * </p>
 */
public class ScriptedModel implements LLMProviderConfig {

	/** What the scripted model answers. */
	@FunctionalInterface
	public interface Script {

		/**
		 * The content of one assistant message.
		 *
		 * @param turn
		 *        0 for the first call of a run, 1 for the call after the first tool result, and so
		 *        on.
		 * @return The parts of the answer; a {@link ToolCall} among them makes the loop call that
		 *         tool and ask again, text alone ends the run.
		 */
		List<ContentPart> reply(ProviderRequest request, int turn);
	}

	/** Configuration of a {@link ScriptedModel}. */
	public interface Config extends LLMProviderConfig.Config {
		// What it answers is the installed script, not configuration.
	}

	private static final TokenUsage USAGE = new TokenUsage(100, 20, 0, 0, 0);

	/** Refuses, so that a test which installed no script says so instead of passing on nothing. */
	private static volatile Script _script = (request, turn) -> {
		throw new IllegalStateException("No script installed: call ScriptedModel.install(...).");
	};

	private final Config _config;

	/** Creates a {@link ScriptedModel} from configuration. */
	public ScriptedModel(InstantiationContext context, Config config) {
		_config = config;
	}

	/** Installs the script every following call answers from. */
	public static void install(Script script) {
		_script = script;
	}

	@Override
	public LLMProvider createProvider() {
		return new LLMProvider() {
			@Override
			public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
				ScriptedModel.stream(request, handler);
			}

			@Override
			public boolean isAvailable() {
				// What a run asks before each request, as a shipped provider follows its key.
				return ScriptedModel.this.isAvailable();
			}
		};
	}

	@Override
	public List<String> discoverModels() {
		return List.of();
	}

	@Override
	public List<String> getConfiguredModels() {
		return _config.getModels();
	}

	@Override
	public String getCatalogProviderId() {
		// No catalog knows this model; its metadata is in <model-overrides>.
		return null;
	}

	private static void stream(ProviderRequest request, StreamHandler handler) {
		int turn = assistantTurns(request);
		List<ContentPart> content = _script.reply(request, turn);
		handler.onEvent(new StreamEvent.StreamStart());
		boolean calledATool = false;
		for (int i = 0; i < content.size(); i++) {
			ContentPart part = content.get(i);
			if (part instanceof TextContent text) {
				handler.onEvent(new StreamEvent.TextStart(i));
				handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				handler.onEvent(new StreamEvent.TextEnd(i));
			} else if (part instanceof ToolCall call) {
				calledATool = true;
				handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				handler.onEvent(new StreamEvent.ToolCallEnd(i));
			}
		}
		handler.onEvent(new StreamEvent.MessageComplete("scripted-" + turn, request.model(),
			calledATool ? "tool_use" : "end_turn", USAGE, content));
	}

	/** How many turns the model has taken in this conversation, counted off the request. */
	private static int assistantTurns(ProviderRequest request) {
		int result = 0;
		for (Message message : request.messages()) {
			if (message instanceof AssistantMessage) {
				result++;
			}
		}
		return result;
	}
}
