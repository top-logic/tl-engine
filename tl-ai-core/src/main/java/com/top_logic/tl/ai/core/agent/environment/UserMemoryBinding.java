/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;
import com.top_logic.tl.ai.core.agent.environment.UserMemories.Entry;

/**
 * Thread-local binding for the memories one conversation was opened with.
 *
 * <p>
 * The environment block must be byte-identical across the turns of a conversation or the providers'
 * prompt caches miss on every one of them. A memory the user had the agent write in turn 3 must
 * therefore not appear in turn 4's block - it appears in the next conversation, which is what the
 * ticket asks for and what the agent needs anyway, having just written it.
 * </p>
 *
 * <p>
 * So the entry point of a conversation reads the list once, at the start, and binds it here for
 * every run of that conversation. A run that binds nothing - a headless run, a scheduled one, a
 * TL-Script one, each of which is a conversation of its own - has {@link UserMemories} read the
 * store instead.
 * </p>
 *
 * <p>
 * Prefer {@link #bind(List)} with try-with-resources. The previous value is captured at bind time
 * and restored on close, which is what makes the binding safe on the cached thread pool the chat
 * runs on.
 * </p>
 */
public class UserMemoryBinding {

	private static final ThreadLocalBinding<List<Entry>> MEMORIES = new ThreadLocalBinding<>();

	private UserMemoryBinding() {
		// Static utility.
	}

	/**
	 * Binds the given snapshot on the current thread and returns a handle whose
	 * {@link AutoCloseable#close() close()} restores the previously bound value (or absence).
	 *
	 * @param memories
	 *        The conversation's memories; {@code null} binds nothing, an empty list binds "this
	 *        conversation was opened with none" and is not the same thing.
	 */
	public static Binding bind(List<Entry> memories) {
		return new Binding(MEMORIES.bind(memories == null ? null : List.copyOf(memories)));
	}

	/**
	 * The snapshot bound to the current thread, or {@code null} when none is - which is the answer
	 * that tells {@link UserMemories#forRun} to read the store.
	 */
	public static List<Entry> snapshot() {
		return MEMORIES.get();
	}

	/** Handle returned by {@link UserMemoryBinding#bind(List)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<List<Entry>>.Handle _handle;

		Binding(ThreadLocalBinding<List<Entry>>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
