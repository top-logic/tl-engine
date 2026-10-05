/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;

/**
 * A chat UI that observes a running agent, bound to one {@link TaskProgressTracker}.
 *
 * <p>
 * Beyond the raw {@link AgentEvent} stream inherited from {@link AgentEventListener}, a chat UI
 * needs two notifications that {@link ChatProcessor} produces itself rather than the agent loop:
 * the rendered final assistant message and an unrecoverable provider error. Those are
 * {@link #sendMessageComplete(MessageCompleteEvent)} and
 * {@link #sendProviderError(ProviderErrorEvent)}.
 * </p>
 *
 * <p>
 * {@link ChatSSEHandler} is the implementation used by this module's SSE servlet. An application
 * that renders its chat differently implements this interface and registers the implementation
 * with {@link ChatEventHandlerRegistry}; {@link ChatProcessor} routes a run's events through the
 * registry and therefore reaches it in exactly the same places.
 * </p>
 *
 * @see ChatEventHandlerRegistry
 */
public interface ChatEventHandler extends AgentEventListener {

	/**
	 * The tracker this handler observes; the key it is registered and looked up under.
	 *
	 * @return Never {@code null} for a registered handler.
	 */
	TaskProgressTracker getTracker();

	/**
	 * Whether this handler can still deliver events.
	 *
	 * <p>
	 * A handler whose underlying connection is gone returns {@code false} and is skipped by
	 * {@link ChatEventHandlerRegistry#find(TaskProgressTracker)}.
	 * </p>
	 */
	boolean isActive();

	/**
	 * Delivers a piece of the answer, together with where in the run's text it ends.
	 *
	 * <p>
	 * A window that opens the conversation while the run is writing is first shown what has been
	 * streamed so far, and then receives the deltas - on another connection, so some of the first
	 * ones may already be in what it was shown. The position tells it which: <code>end</code> counts
	 * every character of text the run has streamed up to and including this delta, and the
	 * snapshot says how far it reaches in the same count (issue #544).
	 * </p>
	 *
	 * <p>
	 * Default: the delta as an ordinary event, for a chat UI that never shows a snapshot.
	 * </p>
	 *
	 * @param delta
	 *        The piece of text.
	 * @param end
	 *        How many characters of text the run has streamed, this delta included.
	 */
	default void onTextDelta(AgentEvent.TextDelta delta, long end) {
		onEvent(delta);
	}

	/**
	 * Delivers the final assistant message of a completed run, already rendered to HTML.
	 *
	 * <p>
	 * Not part of the {@link AgentEvent} stream: the HTML is produced by {@link ChatProcessor}
	 * after the run finishes.
	 * </p>
	 */
	void sendMessageComplete(MessageCompleteEvent event);

	/**
	 * Delivers an error that aborted the run before the agent loop could report it as an
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ProviderError}.
	 */
	void sendProviderError(ProviderErrorEvent event);

	/**
	 * The account this chat UI belongs to - whose windows share what a run asks its user.
	 *
	 * <p>
	 * A pending approval or question is addressed to an account, and every window of that account
	 * may list it and answer it. This is what makes a window one of them, and it is asked only to
	 * decide who to tell when one of them answers.
	 * </p>
	 *
	 * <p>
	 * Default: unknown. A chat UI that does not say whose it is is never told about another
	 * window's answer, which is what every chat UI got before that frame existed.
	 * </p>
	 */
	default Person getAccount() {
		return null;
	}

	/**
	 * Tells this chat UI that an approval or question it may be showing has been answered
	 * elsewhere.
	 *
	 * <p>
	 * A pending request belongs to the account, so every window of that account may list it and
	 * answer it. The window whose run is parked on it learns the outcome from the run's own
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ApprovalResolved}; a window that only
	 * listed the request has no run to hear that from, and this is what stops it from showing a
	 * card nobody is waiting on any more.
	 * </p>
	 *
	 * <p>
	 * Default: nothing. A chat UI that never lists another window's requests has nothing to take
	 * back.
	 * </p>
	 */
	default void sendInteractionResolved(InteractionResolvedEvent event) {
		// Nothing to take back.
	}

}
