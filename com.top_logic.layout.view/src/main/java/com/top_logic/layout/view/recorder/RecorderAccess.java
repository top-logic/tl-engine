/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.recorder;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Resolves the {@link ScriptRecorder} a recorder side-window controls: the recorder of the window
 * that opened it.
 *
 * <p>
 * The recorder lives on the recorded (main) window's {@link SSEUpdateQueue}, not on the side-window's
 * own, so the side-window's own commands (start/stop) are never captured into the script. The
 * side-window reaches the main window through its {@link ReactContext#getOpenerWindowName() opener}.
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
		String openerWindowId = context.getOpenerWindowName();
		if (openerWindowId == null) {
			return null;
		}
		SSEUpdateQueue openerQueue = context.getWindowRegistry().getQueue(openerWindowId);
		return openerQueue == null ? null : openerQueue.getRecorder();
	}
}
