/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import com.top_logic.layout.react.control.IReactControl;

/**
 * Tracks a single programmatically opened React window.
 */
public class WindowEntry {

	private final String _windowId;

	private final String _openerWindowId;

	private final IReactControl _rootControl;

	private final WindowOptions _options;

	private boolean _connected;

	/**
	 * Creates a new {@link WindowEntry}.
	 *
	 * @param windowId
	 *        The unique ID for this window.
	 * @param openerWindowId
	 *        The window ID of the opener.
	 * @param rootControl
	 *        The pre-built control tree for this window, or null if not yet created.
	 * @param options
	 *        The window display options.
	 */
	public WindowEntry(String windowId, String openerWindowId, IReactControl rootControl,
			WindowOptions options) {
		_windowId = windowId;
		_openerWindowId = openerWindowId;
		_rootControl = rootControl;
		_options = options;
	}

	/** The unique ID for this window. */
	public String getWindowId() {
		return _windowId;
	}

	/** The window ID of the opener. */
	public String getOpenerWindowId() {
		return _openerWindowId;
	}

	/** The pre-built control tree for this window. */
	public IReactControl getRootControl() {
		return _rootControl;
	}

	/** The window display options. */
	public WindowOptions getOptions() {
		return _options;
	}

	/** Whether the browser window has connected via SSE. */
	public boolean isConnected() {
		return _connected;
	}

	/** Marks this window as connected. */
	public void markConnected() {
		_connected = true;
	}
}
