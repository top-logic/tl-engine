/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;

/**
 * Tracks a single programmatically opened React window.
 */
public class WindowEntry {

	private final String _windowId;

	private final String _openerWindowId;

	private final ReactControlProvider _controlProvider;

	private final Object _model;

	private volatile ReactControl _rootControl;

	private final WindowOptions _options;

	private final Runnable _closeCallback;

	private boolean _connected;

	/**
	 * Creates a new {@link WindowEntry} with a control provider and model.
	 *
	 * @param windowId
	 *        The unique ID for this window.
	 * @param openerWindowId
	 *        The window ID of the opener.
	 * @param controlProvider
	 *        The factory that creates the control tree for this window.
	 * @param model
	 *        The model passed to the factory.
	 * @param options
	 *        The window display options.
	 * @param closeCallback
	 *        Optional callback invoked when the window is closed, or null.
	 */
	public WindowEntry(String windowId, String openerWindowId, ReactControlProvider controlProvider,
			Object model, WindowOptions options, Runnable closeCallback) {
		_windowId = windowId;
		_openerWindowId = openerWindowId;
		_controlProvider = controlProvider;
		_model = model;
		_options = options;
		_closeCallback = closeCallback;
	}

	/**
	 * Creates a new {@link WindowEntry} without a control provider.
	 *
	 * @param windowId
	 *        The unique ID for this window.
	 * @param openerWindowId
	 *        The window ID of the opener.
	 * @param options
	 *        The window display options.
	 * @param closeCallback
	 *        Optional callback invoked when the window is closed, or null.
	 */
	public WindowEntry(String windowId, String openerWindowId, WindowOptions options,
			Runnable closeCallback) {
		this(windowId, openerWindowId, null, null, options, closeCallback);
	}

	/** The unique ID for this window. */
	public String getWindowId() {
		return _windowId;
	}

	/** The window ID of the opener. */
	public String getOpenerWindowId() {
		return _openerWindowId;
	}

	/** The control tree for this window. */
	public ReactControl getRootControl() {
		return _rootControl;
	}

	/** Sets the control tree for this window. */
	public void setRootControl(ReactControl rootControl) {
		_rootControl = rootControl;
	}

	/** The factory that creates the control tree for this window. */
	public ReactControlProvider getControlProvider() {
		return _controlProvider;
	}

	/** The model passed to the factory. */
	public Object getModel() {
		return _model;
	}

	/** The window display options. */
	public WindowOptions getOptions() {
		return _options;
	}

	/** Optional callback invoked when the window is closed, or null. */
	public Runnable getCloseCallback() {
		return _closeCallback;
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
