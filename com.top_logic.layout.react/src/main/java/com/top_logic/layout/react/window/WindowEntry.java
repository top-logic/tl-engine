/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.listen.ModelScope;

/**
 * The server-side state of a single browser window.
 *
 * <p>
 * Holds everything that lives as long as the window does: the {@link #getQueue() update queue} its
 * client is served from, the {@link #getOrCreateModelScope() model scope} its controls observe, and
 * the {@link #getRootControl() control tree} it currently displays. A programmatically opened window
 * additionally brings the {@link #getControlProvider() provider} that builds its content, the
 * {@link #getModel() model} to build it from and its display {@link #getOptions() options}; a window
 * the browser opened by itself has none of those.
 * </p>
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

	private final SSEUpdateQueue _queue;

	private volatile GlobalModelEventForwarder _modelScope;

	/**
	 * When the window's page was reported as unloaded, or {@code 0} while it is displayed.
	 *
	 * @see #markUnloaded()
	 */
	private volatile long _unloadedAt;

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
		_queue = new SSEUpdateQueue();
		_queue.setWindowName(windowId);
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

	/**
	 * The queue delivering events to this window's client.
	 *
	 * <p>
	 * Created with the window and shut down when it is torn down, so a queue never outlives the window
	 * it belongs to.
	 * </p>
	 */
	public SSEUpdateQueue getQueue() {
		return _queue;
	}

	/**
	 * The {@link ModelScope} this window's controls observe, created on first access.
	 *
	 * <p>
	 * Each window gets its own {@link GlobalModelEventForwarder} with an independent update-chain
	 * cursor, so events synthesized for one window are not dispatched to another.
	 * </p>
	 */
	public synchronized ModelScope getOrCreateModelScope() {
		if (_modelScope == null) {
			_modelScope = GlobalModelEventForwarder.createForWindow();
		}
		return _modelScope;
	}

	/**
	 * Synthesizes the model events pending for this window, if it observes the model at all.
	 */
	public void synthesizeModelEvents() {
		GlobalModelEventForwarder forwarder = _modelScope;
		if (forwarder != null) {
			forwarder.synthesizeModelEvents();
		}
	}

	/**
	 * Records that the window's page was unloaded, which is also what a reload does.
	 *
	 * @see #getUnloadedAt()
	 */
	public void markUnloaded() {
		_unloadedAt = System.currentTimeMillis();
	}

	/**
	 * Records that the window is displaying a page again.
	 */
	public void markDisplayed() {
		_unloadedAt = 0;
	}

	/**
	 * When the window's page was reported as unloaded, or {@code 0} while it is displayed.
	 *
	 * <p>
	 * A reload reports an unload as well, so this only says that nothing is displayed right now - not
	 * that the window is gone. How long that is tolerated is the registry's decision.
	 * </p>
	 */
	public long getUnloadedAt() {
		return _unloadedAt;
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
