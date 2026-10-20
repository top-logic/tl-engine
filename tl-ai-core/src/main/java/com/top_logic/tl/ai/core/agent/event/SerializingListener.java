/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Package-private wrapper that serializes {@link AgentEventListener#onEvent(AgentEvent)}
 * dispatch on an internal monitor and swallows any {@link Throwable} thrown by the
 * delegate.
 *
 * <p>Used by the framework to wrap user-supplied listeners before handing them to
 * {@code AgentLoop}. Guarantees:</p>
 * <ul>
 *   <li><b>Serialized dispatch:</b> only one thread may be inside
 *       {@code delegate.onEvent(...)} at a time, even when the agent loop emits events
 *       from concurrent virtual threads during parallel tool execution.</li>
 *   <li><b>Crash isolation:</b> any exception (including {@link Error}) thrown by the
 *       delegate is logged at {@link Level#WARNING} and otherwise swallowed, so a
 *       misbehaving listener cannot abort the agent run.</li>
 * </ul>
 *
 * <p>This class is not public: call sites construct it via framework entry points
 * rather than wrapping listeners themselves.</p>
 */
final class SerializingListener implements AgentEventListener {

	private static final Logger LOG = Logger.getLogger(SerializingListener.class.getName());

	private final AgentEventListener _delegate;

	private final Object _lock = new Object();

	/** */
	SerializingListener(AgentEventListener delegate) {
		_delegate = delegate;
	}

	/** */
	@Override
	public void onEvent(AgentEvent event) {
		synchronized (_lock) {
			try {
				_delegate.onEvent(event);
			} catch (Throwable t) {
				LOG.log(Level.WARNING,
					"Agent event listener threw for event " + event.getClass().getSimpleName(), t);
			}
		}
	}
}
