/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import com.top_logic.tl.ai.core.binding.ThreadLocalBinding;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;

/**
 * Thread-local binding of the reading list of the run a tool call belongs to.
 *
 * <p>
 * A tool itself registers through {@link ToolExecutionContext#readSource(ReadSource)}. This exists
 * for what a tool <em>calls</em>: the read filter that renders every object a tool shows the model,
 * the page renderer of the browser - code with no context parameter, which is exactly where a read
 * becomes visible.
 * </p>
 *
 * <p>
 * {@code ToolExecutions#execute} binds it around every tool call, on the thread the tool runs on,
 * and restores the previous binding when the call returns. Outside a tool call nothing is bound and
 * {@link #read(ReadSource, String...)} does nothing.
 * </p>
 */
public final class ReadingListBinding {

	private static final ThreadLocalBinding<ReadingList> LIST = new ThreadLocalBinding<>();

	private ReadingListBinding() {
		// Static utility.
	}

	/**
	 * Binds {@code list} on the current thread.
	 *
	 * @param list
	 *        The list of the run; <code>null</code> binds nothing.
	 * @return The handle whose {@link AutoCloseable#close()} restores the previous binding.
	 */
	public static Binding bind(ReadingList list) {
		return new Binding(LIST.bind(list));
	}

	/** The list bound to the current thread, or <code>null</code> outside a tool call. */
	public static ReadingList current() {
		return LIST.get();
	}

	/**
	 * Registers a source on the bound list, if there is one.
	 *
	 * @see ReadingList#add(ReadSource, String...)
	 */
	public static void read(ReadSource source, String... aliases) {
		ReadingList list = LIST.get();
		if (list != null) {
			list.add(source, aliases);
		}
	}

	/**
	 * Records the web addresses in a text that the run's user or lead wrote to it, on the bound list,
	 * if there is one: the answer to a question the run asked.
	 *
	 * <p>
	 * Such a text gives addresses the way a message of the user or the lead does (see
	 * {@link SeenUrls}). Never pass a text the model wrote, also not when a person sent it back - an
	 * option the model offered and the user picked.
	 * </p>
	 *
	 * @param text
	 *        The text; may be {@code null}.
	 */
	public static void givenAddressesIn(String text) {
		ReadingList list = LIST.get();
		if (list != null) {
			list.seenUrls().addAllIn(text);
		}
	}

	/** Handle returned by {@link ReadingListBinding#bind(ReadingList)}. */
	public static final class Binding implements AutoCloseable {

		private final ThreadLocalBinding<ReadingList>.Handle _handle;

		Binding(ThreadLocalBinding<ReadingList>.Handle handle) {
			_handle = handle;
		}

		@Override
		public void close() {
			_handle.close();
		}
	}
}
