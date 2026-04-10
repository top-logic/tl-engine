/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link AgentEventListener} that fans each {@link AgentEvent} out to a fixed list of
 * delegate listeners in registration order.
 *
 * <p>Error isolation: a delegate that throws does not prevent the remaining delegates
 * from receiving the event. The throwable is logged at {@link Level#WARNING} and
 * swallowed. This matches the framework-wide contract that a buggy listener cannot
 * abort an agent run.</p>
 *
 * <p>The delegate list is captured defensively at construction time; later mutations
 * of the caller's list do not affect dispatch. Instances are immutable after
 * construction and safe to share across threads; concurrent dispatch serialization is
 * provided by the framework wrapper applied inside the agent loop.</p>
 */
public final class CompositeAgentEventListener implements AgentEventListener {

	private static final Logger LOG = Logger.getLogger(CompositeAgentEventListener.class.getName());

	private final List<AgentEventListener> _delegates;

	/** */
	public CompositeAgentEventListener(List<AgentEventListener> delegates) {
		_delegates = List.copyOf(delegates);
	}

	/** */
	public CompositeAgentEventListener(AgentEventListener... delegates) {
		_delegates = List.of(delegates);
	}

	/** */
	@Override
	public void onEvent(AgentEvent event) {
		for (AgentEventListener delegate : _delegates) {
			try {
				delegate.onEvent(event);
			} catch (Throwable t) {
				LOG.log(Level.WARNING,
					"Agent event listener " + delegate.getClass().getName()
						+ " threw for event " + event.getClass().getSimpleName(),
					t);
			}
		}
	}
}
