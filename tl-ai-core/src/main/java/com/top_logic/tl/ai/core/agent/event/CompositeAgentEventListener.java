/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.util.List;

/**
 * {@link AgentEventListener} that fans each {@link AgentEvent} out to a fixed list of
 * delegate listeners in order.
 *
 * <p>This class is a pure dispatcher: it does not serialize dispatch and does not
 * catch exceptions thrown by delegates. Use {@code SerializingListener} to add those
 * guarantees (framework entry points do this automatically for user-supplied
 * listeners).</p>
 *
 * <p>The delegate list is captured defensively at construction time; later mutations
 * of the caller's list do not affect dispatch.</p>
 */
public final class CompositeAgentEventListener implements AgentEventListener {

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
			delegate.onEvent(event);
		}
	}
}
