/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.WindowOpenEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Per-session registry that manages programmatically opened React windows.
 *
 * <p>
 * Stored as an HTTP session attribute alongside {@link SSEUpdateQueue}. Tracks open windows, their
 * control trees, and enqueues lifecycle SSE events.
 * </p>
 */
public class ReactWindowRegistry implements HttpSessionBindingListener {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.windowRegistry";

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ConcurrentHashMap<String, WindowEntry> _windows = new ConcurrentHashMap<>();

	private final SSEUpdateQueue _sseQueue;

	/**
	 * Creates a new {@link ReactWindowRegistry}.
	 *
	 * @param sseQueue
	 *        The session's SSE queue for enqueuing window events.
	 */
	public ReactWindowRegistry(SSEUpdateQueue sseQueue) {
		_sseQueue = sseQueue;
	}

	/**
	 * Gets the registry for the given session, creating it if necessary.
	 */
	public static ReactWindowRegistry forSession(HttpSession session) {
		ReactWindowRegistry registry =
			(ReactWindowRegistry) session.getAttribute(SESSION_ATTRIBUTE_KEY);
		if (registry == null) {
			synchronized (session) {
				registry = (ReactWindowRegistry) session.getAttribute(SESSION_ATTRIBUTE_KEY);
				if (registry == null) {
					SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
					registry = new ReactWindowRegistry(queue);
					session.setAttribute(SESSION_ATTRIBUTE_KEY, registry);
				}
			}
		}
		return registry;
	}

	/**
	 * Opens a new window without a pre-built control tree.
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param options
	 *        Window display options.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, WindowOptions options) {
		return openWindow(openerContext, null, options, null);
	}

	/**
	 * Opens a new window with a pre-built control tree.
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param rootControl
	 *        The pre-built control tree for the new window, or null.
	 * @param options
	 *        Window display options.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, IReactControl rootControl,
			WindowOptions options) {
		return openWindow(openerContext, rootControl, options, null);
	}

	/**
	 * Opens a new window with a pre-built control tree and a close callback.
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param rootControl
	 *        The pre-built control tree for the new window, or null.
	 * @param options
	 *        Window display options.
	 * @param closeCallback
	 *        Optional callback invoked when the window is closed, or null.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, IReactControl rootControl,
			WindowOptions options, Runnable closeCallback) {
		String windowId = generateWindowId();
		String openerWindowId = openerContext.getWindowName();

		WindowEntry entry = new WindowEntry(windowId, openerWindowId, rootControl, options, closeCallback);
		_windows.put(windowId, entry);

		WindowOpenEvent event = WindowOpenEvent.create()
			.setTargetWindowId(openerWindowId)
			.setWindowId(windowId)
			.setWidth(options.getWidth())
			.setHeight(options.getHeight())
			.setTitle(options.getTitle())
			.setResizable(options.isResizable());
		_sseQueue.enqueue(event);

		return windowId;
	}

	/**
	 * Looks up a window by its ID.
	 *
	 * @return The window entry, or null if not found.
	 */
	public WindowEntry getWindow(String windowId) {
		return _windows.get(windowId);
	}

	/**
	 * Called when a window is closed (either by the user or programmatically).
	 * Invokes the close callback (if any), then cleans up the control tree and removes the entry.
	 */
	public void windowClosed(String windowId) {
		if (windowId == null) {
			return;
		}
		WindowEntry entry = _windows.remove(windowId);
		if (entry != null) {
			Runnable closeCallback = entry.getCloseCallback();
			if (closeCallback != null) {
				try {
					closeCallback.run();
				} catch (Exception ex) {
					Logger.error("Error in window close callback for window '" + windowId + "'.",
						ex, ReactWindowRegistry.class);
				}
			}
			IReactControl rootControl = entry.getRootControl();
			if (rootControl instanceof ReactControl) {
				((ReactControl) rootControl).cleanupTree();
			}
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// Nothing to do.
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		for (WindowEntry entry : _windows.values()) {
			IReactControl rootControl = entry.getRootControl();
			if (rootControl instanceof ReactControl) {
				((ReactControl) rootControl).cleanupTree();
			}
		}
		_windows.clear();
	}

	private String generateWindowId() {
		byte[] bytes = new byte[8];
		RANDOM.nextBytes(bytes);
		StringBuilder sb = new StringBuilder("v");
		for (byte b : bytes) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
}
