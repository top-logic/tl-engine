/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;

/**
 * Thread-local binding of the {@link SubagentInbox} the run on this thread reads its lead's messages
 * from; bound by the spawner on a background subagent's thread, unbound everywhere else.
 */
public final class SubagentInboxBinding {

	private static final ThreadLocalBinding<SubagentInbox> INBOX = new ThreadLocalBinding<>();

	private SubagentInboxBinding() {
		// Static binding.
	}

	/**
	 * Binds the given inbox on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value.
	 */
	public static Binding bind(SubagentInbox inbox) {
		return new Binding(INBOX.bind(inbox));
	}

	/** The bound inbox, or <code>null</code>. */
	public static SubagentInbox getInbox() {
		return INBOX.get();
	}

	/** Handle returned by {@link SubagentInboxBinding#bind(SubagentInbox)}. */
	public static final class Binding implements AutoCloseable {
		private final ThreadLocalBinding<SubagentInbox>.Handle _handle;

		Binding(ThreadLocalBinding<SubagentInbox>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
