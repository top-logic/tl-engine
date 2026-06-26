/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.recorder;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.headless.ScriptRecorder;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;

/**
 * Resolves the {@link ScriptRecorder} a recorder side-window controls: the recorder of the window
 * that opened it.
 *
 * <p>
 * The recorder lives on the recorded (main) window's {@link SSEUpdateQueue}, not on the side-window's
 * own, so the side-window's own commands (start/stop) are never captured into the script. The
 * side-window reaches the main window through its {@link WindowEntry#getOpenerWindowId() opener}.
 * </p>
 */
public final class RecorderAccess {

	private RecorderAccess() {
		// Static utility.
	}

	/**
	 * The {@link ScriptRecorder} of the window that opened the given context's window, or {@code null}
	 * if it cannot be resolved (no opener, or the opener window is gone).
	 *
	 * @param context
	 *        The side-window's context.
	 * @return The opener window's recorder, or {@code null}.
	 */
	public static ScriptRecorder openerRecorder(ReactContext context) {
		String openerWindowId = openerWindowId(context);
		if (openerWindowId == null) {
			return null;
		}
		SSEUpdateQueue openerQueue = context.getWindowRegistry().getQueue(openerWindowId);
		return openerQueue == null ? null : openerQueue.getRecorder();
	}

	/**
	 * The root {@link ReactControl} of the window that opened the given context's window, or
	 * {@code null} if it cannot be resolved.
	 *
	 * <p>
	 * The recorder side-window resolves a recorded step's semantic address against this root to reach
	 * the target control (e.g. to render the step for humans).
	 * </p>
	 *
	 * @param context
	 *        The side-window's context.
	 * @return The opener window's root control, or {@code null}.
	 */
	public static ReactControl openerRoot(ReactContext context) {
		String openerWindowId = openerWindowId(context);
		if (openerWindowId == null) {
			return null;
		}
		SSEUpdateQueue openerQueue = context.getWindowRegistry().getQueue(openerWindowId);
		return openerQueue == null ? null : openerQueue.getRootControl();
	}

	/**
	 * The id of the window that opened the given context's window — the recorded (main) window a
	 * recorder side-window drives — or {@code null} if there is no opener.
	 *
	 * @param context
	 *        The side-window's context.
	 * @return The opener window id, or {@code null}.
	 */
	public static String openerWindowId(ReactContext context) {
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (registry == null) {
			return null;
		}
		WindowEntry entry = registry.getWindow(context.getWindowName());
		return entry == null ? null : entry.getOpenerWindowId();
	}
}
