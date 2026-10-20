/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

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
 * A provider whose answers a test writes: the whole eval pipeline - matrix, runner, graders, CSV,
 * report - exercised in an ordinary build, at no cost and with no key.
 *
 * <p>
 * It is a configured {@link LLMProviderConfig}, not a lambda handed to an {@code AgentLoop}, and
 * that is the point. The eval measures the harness, so its plumbing tests have to go through the
 * same route a real run takes: the agent definition, the tool registry, the approval gate, the
 * transaction scope, {@code AgentFactory}. A fake wired in below all of that would test a loop
 * nobody ships.
 * </p>
 *
 * <p>
 * The script is installed statically, because the matrix runs sequentially on one thread and a
 * provider is created once at service startup. A script therefore dispatches on what it is asked -
 * the prompt in the request and how many assistant turns have already happened - rather than on
 * being re-installed between runs.
 * </p>
 */
public class ScriptedProvider implements LLMProviderConfig {

	/**
	 * What the scripted model answers.
	 */
	@FunctionalInterface
	public interface Script {

		/**
		 * The content of one assistant message.
		 *
		 * @param request
		 *        What the loop is about to send; {@link ProviderRequest#messages()} holds the
		 *        conversation so far, starting with the scenario's prompt.
		 * @param turn
		 *        0 for the first call of a run, 1 for the call after the first tool result, and so
		 *        on. Derived from the conversation, so it is right even though the script is static.
		 * @return The parts of the answer. A {@link ToolCall} among them makes the loop execute that
		 *         tool and call again; a reply of text alone ends the run.
		 * @throws ProviderException
		 *         To play a provider that refuses - a wrong key, an unpaid bill, an outage. The eval
		 *         treats some of those as "could not run" rather than as a model failure, and that
		 *         is a decision a test has to be able to reach.
		 */
		List<ContentPart> reply(ProviderRequest request, int turn) throws ProviderException;
	}

	/** Configuration of a {@link ScriptedProvider}. */
	public interface Config extends LLMProviderConfig.Config {
		// No properties of its own: what it answers is the installed script, not configuration.
	}

	private static final TokenUsage USAGE = new TokenUsage(120, 40, 0, 0, 0);

	/**
	 * Refuses rather than answers, so that a test which forgot to install a script fails saying so
	 * instead of timing out or passing on an empty answer.
	 */
	private static volatile Script _script = (request, turn) -> {
		throw new IllegalStateException("No script installed. Call ScriptedProvider.install(...)"
			+ " before running anything against the model '" + request.model() + "'.");
	};

	private final Config _config;

	/** Creates a {@link ScriptedProvider} from configuration. */
	public ScriptedProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	/** Installs the script every subsequent call answers from. */
	public static void install(Script script) {
		_script = script;
	}

	@Override
	public LLMProvider createProvider() {
		return ScriptedProvider::stream;
	}

	@Override
	public List<String> discoverModels() {
		// Nothing to discover: what this provider serves is whatever the configuration allows it to.
		return List.of();
	}

	@Override
	public List<String> getConfiguredModels() {
		return _config.getModels();
	}

	@Override
	public String getCatalogProviderId() {
		// No remote catalogue knows this model; its metadata comes from <model-overrides>.
		return null;
	}

	private static void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
		List<ContentPart> content = _script.reply(request, assistantTurns(request));
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
		handler.onEvent(new StreamEvent.MessageComplete("scripted-" + assistantTurns(request),
			request.model(), calledATool ? "tool_use" : "end_turn", USAGE, content));
	}

	/**
	 * How many turns the model has already taken in this conversation.
	 *
	 * <p>
	 * Counted off the request rather than kept in a field, so that concurrent or interleaved runs -
	 * a delegating agent, a retried turn - cannot hand a script the wrong index.
	 * </p>
	 */
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
