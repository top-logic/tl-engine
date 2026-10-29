/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import com.top_logic.base.context.TLSubSessionContext;

/**
 * What became of an action that was to run in a browser window.
 *
 * @see ReactWindowReplay#inWindow(TLSubSessionContext, long, java.util.function.Consumer)
 */
public enum WindowAccess {

	/** The window was reached and the action ran. */
	DONE,

	/**
	 * No live browser window displays the given sub-session: it belongs to no window, as for a
	 * headless or scheduled computation, or its window was closed. The action did not run.
	 */
	NO_WINDOW,

	/**
	 * The window exists, but another thread kept its session's interaction for longer than the
	 * given time, or the calling thread was interrupted while it waited. The action did not run and
	 * may be tried again.
	 */
	BUSY;

}
