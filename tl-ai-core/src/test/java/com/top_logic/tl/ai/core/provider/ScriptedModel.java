/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;

/**
 * A model whose answers a test writes, and which records everything it was asked.
 *
 * <p>
 * Each request is answered by a {@link Script}, which sees the request and the number of turns the
 * model has already taken in the conversation, and returns the {@link Reply} - text, tool calls,
 * thinking, the reason the model stopped - or throws a {@link ProviderException} to play a provider
 * that refuses. The reply is streamed to the caller the way a real provider streams it, so an
 * {@link com.top_logic.tl.ai.core.agent.AgentLoop} cannot tell the difference.
 * </p>
 *
 * <p>
 * Every request is recorded before it is answered, so a test can assert what reached the model -
 * the system prompt, the tools offered, that no credential was sent - with {@link #requests()}.
 * </p>
 *
 * <p>
 * Hand an instance directly to an {@link com.top_logic.tl.ai.core.agent.AgentLoop} to test the loop
 * on its own; to test a run that goes through the configured route of {@link LLMProviderService},
 * configure a {@link ScriptedProvider}, which answers from one installed {@link ScriptedModel}.
 * </p>
 */
public class ScriptedModel implements LLMProvider {

	/** The finish reason of a reply that calls a tool and expects the loop to call again. */
	public static final String TOOL_USE = "tool_use";

	/** The finish reason of a reply that ends the model's turn. */
	public static final String END_TURN = "end_turn";

	/** The finish reason of a reply cut off at the output limit. */
	public static final String MAX_TOKENS = "max_tokens";

	/** What a {@link Reply} reports as used unless it says otherwise. */
	public static final TokenUsage DEFAULT_USAGE = new TokenUsage(120, 40, 0, 0, 0);

	/**
	 * What the scripted model answers.
	 */
	@FunctionalInterface
	public interface Script {

		/**
		 * The answer to one request.
		 *
		 * @param request
		 *        What the caller is about to send; {@link ProviderRequest#messages()} holds the
		 *        conversation so far.
		 * @param turn
		 *        0 for the first call in a conversation, 1 for the call after the first assistant
		 *        message, and so on: the number of {@link AssistantMessage}s in the request. Derived
		 *        from the conversation, so it is right for interleaved conversations - a delegating
		 *        agent and its sub-agents - and for a script shared by several runs. A conversation
		 *        that was compacted may have lost assistant messages; a script for such a case
		 *        counts its calls itself.
		 * @return The answer. A {@link ToolCall} among its parts makes the loop execute that tool
		 *         and call again; a reply of text alone ends the run.
		 * @throws ProviderException
		 *         To play a provider that refuses - a wrong key, an overloaded model, a request
		 *         larger than the context window.
		 */
		Reply reply(ProviderRequest request, int turn) throws ProviderException;

		/**
		 * A script that answers the turns of a conversation with the given replies, one per turn.
		 *
		 * <p>
		 * A request for a turn after the last reply fails with an {@link IllegalStateException}
		 * naming the turn: a run that asks more often than the test expected is a finding, not
		 * something to paper over with a made-up answer. A script whose run ends on a text answer
		 * therefore ends with that answer.
		 * </p>
		 */
		static Script of(Reply... replies) {
			return of(Arrays.asList(replies));
		}

		/**
		 * A script that answers the turns of a conversation with the given replies, one per turn.
		 *
		 * @see #of(Reply...)
		 */
		static Script of(List<Reply> replies) {
			List<Reply> script = List.copyOf(replies);
			return (request, turn) -> {
				if (turn >= script.size()) {
					throw new IllegalStateException("The script has " + script.size()
						+ " replies, but the model was asked for turn " + (turn + 1) + " of the conversation.");
				}
				return script.get(turn);
			};
		}

		/**
		 * A script that answers the requests it receives with the given replies, one per request, in
		 * the order it receives them.
		 *
		 * <p>
		 * For a run whose conversation does not show every reply - a reply without text or tool call
		 * that the loop does not keep, a compaction that summarizes earlier turns away - so that
		 * {@link #of(Reply...) the turn} does not count the requests. The script counts them itself
		 * and therefore serves one run. A request after the last reply fails with an
		 * {@link IllegalStateException}, as in {@link #of(Reply...)}.
		 * </p>
		 */
		static Script inOrder(Reply... replies) {
			List<Reply> script = List.of(replies);
			AtomicInteger next = new AtomicInteger();
			return (request, turn) -> {
				int index = next.getAndIncrement();
				if (index >= script.size()) {
					throw new IllegalStateException("The script has " + script.size()
						+ " replies, but the model was asked for the " + (index + 1) + ". time.");
				}
				return script.get(index);
			};
		}

		/**
		 * A script that answers each request with the script of the model it is addressed to.
		 *
		 * <p>
		 * For a configuration with several models - a failover chain, sub-agents on a model of
		 * their own. The map is read on each request, so a test may change it between runs.
		 * </p>
		 *
		 * @param scripts
		 *        The script of each model, by {@link ProviderRequest#model()}. A request to a model
		 *        not in it fails with an {@link IllegalStateException}.
		 */
		static Script byModel(Map<String, ? extends Script> scripts) {
			return (request, turn) -> {
				Script script = scripts.get(request.model());
				if (script == null) {
					throw new IllegalStateException(
						"No script for the model '" + request.model() + "', only for " + scripts.keySet() + ".");
				}
				return script.reply(request, turn);
			};
		}
	}

	/**
	 * One answer of the model.
	 *
	 * @param content
	 *        The parts of the answer: {@link TextContent}, {@link ToolCall}, {@link ThinkingContent}.
	 * @param finishReason
	 *        Why the model stopped. <code>null</code> stands for the usual reason:
	 *        {@link ScriptedModel#TOOL_USE} when the answer calls a tool, else
	 *        {@link ScriptedModel#END_TURN}.
	 * @param usage
	 *        The tokens the answer reports as used. <code>null</code> stands for
	 *        {@link ScriptedModel#DEFAULT_USAGE}.
	 */
	public record Reply(List<ContentPart> content, String finishReason, TokenUsage usage) {

		/** Fills in the defaults. */
		public Reply {
			content = List.copyOf(content);
			if (finishReason == null) {
				finishReason = content.stream().anyMatch(ToolCall.class::isInstance) ? TOOL_USE : END_TURN;
			}
			if (usage == null) {
				usage = DEFAULT_USAGE;
			}
		}

		/** An answer of the given parts, with the usual finish reason. */
		public static Reply of(ContentPart... content) {
			return of(Arrays.asList(content));
		}

		/** An answer of the given parts, with the usual finish reason. */
		public static Reply of(List<? extends ContentPart> content) {
			return new Reply(List.copyOf(content), null, null);
		}

		/** An answer of text alone, which ends the run. */
		public static Reply text(String text) {
			return of(new TextContent(text));
		}

		/** This answer, stopped for the given reason. */
		public Reply withFinishReason(String reason) {
			return new Reply(content, reason, usage);
		}

		/** This answer, reporting the given usage. */
		public Reply withUsage(TokenUsage tokens) {
			return new Reply(content, finishReason, tokens);
		}
	}

	private final Script _script;

	private final List<ProviderRequest> _requests = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link ScriptedModel}.
	 *
	 * @param script
	 *        What it answers.
	 */
	public ScriptedModel(Script script) {
		_script = script;
	}

	/**
	 * Every request this model received, in the order it received them, including those its script
	 * refused.
	 */
	public List<ProviderRequest> requests() {
		return List.copyOf(_requests);
	}

	/** Forgets the recorded {@link #requests()}. */
	public void reset() {
		_requests.clear();
	}

	@Override
	public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
		_requests.add(request);
		int turn = assistantTurns(request);
		Reply reply = _script.reply(request, turn);
		List<ContentPart> content = reply.content();
		handler.onEvent(new StreamEvent.StreamStart());
		for (int i = 0; i < content.size(); i++) {
			ContentPart part = content.get(i);
			if (part instanceof TextContent text) {
				handler.onEvent(new StreamEvent.TextStart(i));
				handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				handler.onEvent(new StreamEvent.TextEnd(i));
			} else if (part instanceof ThinkingContent thinking) {
				handler.onEvent(new StreamEvent.ThinkingStart(i));
				handler.onEvent(new StreamEvent.ThinkingDelta(i, thinking.text()));
				handler.onEvent(new StreamEvent.ThinkingEnd(i));
			} else if (part instanceof ToolCall call) {
				handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				handler.onEvent(new StreamEvent.ToolCallEnd(i));
			}
		}
		handler.onEvent(new StreamEvent.MessageComplete("scripted-" + turn, request.model(),
			reply.finishReason(), reply.usage(), content));
	}

	/**
	 * How many turns the model has already taken in the conversation of the given request.
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
