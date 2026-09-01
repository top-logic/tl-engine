/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
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
	 * {@link AgentEvent.ProviderError}.
	 */
	void sendProviderError(ProviderErrorEvent event);

}
