/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.protocol.WindowOpenEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Per-session registry that manages programmatically opened React windows and per-window SSE
 * queues.
 *
 * <p>
 * Stored as an HTTP session attribute. Tracks open windows, their control trees, and owns
 * per-window {@link SSEUpdateQueue} instances.
 * </p>
 */
public class ReactWindowRegistry implements HttpSessionBindingListener {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.windowRegistry";

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ConcurrentHashMap<String, WindowEntry> _windows = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, SSEUpdateQueue> _windowQueues = new ConcurrentHashMap<>();

	private final ReentrantLock _requestLock = new ReentrantLock();

	/**
	 * Creates a new {@link ReactWindowRegistry}.
	 */
	public ReactWindowRegistry() {
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
					registry = new ReactWindowRegistry();
					session.setAttribute(SESSION_ATTRIBUTE_KEY, registry);
				}
			}
		}
		return registry;
	}

	/**
	 * Gets or creates the {@link SSEUpdateQueue} for the given window.
	 */
	public SSEUpdateQueue getOrCreateQueue(String windowId) {
		return _windowQueues.computeIfAbsent(windowId, id -> new SSEUpdateQueue());
	}

	/**
	 * Gets the {@link SSEUpdateQueue} for the given window, or {@code null} if none exists.
	 */
	public SSEUpdateQueue getQueue(String windowId) {
		return _windowQueues.get(windowId);
	}

	/**
	 * The session-wide request lock for serializing command execution.
	 */
	public ReentrantLock getRequestLock() {
		return _requestLock;
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
		String windowId = generateWindowId();
		String openerWindowId = openerContext.getWindowName();

		WindowEntry entry = new WindowEntry(windowId, openerWindowId, options, null);
		_windows.put(windowId, entry);

		WindowOpenEvent event = WindowOpenEvent.create()
			.setTargetWindowId(openerWindowId)
			.setWindowId(windowId)
			.setWidth(options.getWidth())
			.setHeight(options.getHeight())
			.setTitle(options.getTitle())
			.setResizable(options.isResizable());
		SSEUpdateQueue openerQueue = getQueue(openerWindowId);
		if (openerQueue != null) {
			openerQueue.enqueue(event);
		}

		return windowId;
	}

	/**
	 * Opens a new window with a control provider and model.
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param controlProvider
	 *        The factory that creates the control tree for the new window.
	 * @param model
	 *        The model passed to the factory.
	 * @param options
	 *        Window display options.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, ReactControlProvider controlProvider,
			Object model, WindowOptions options) {
		return openWindow(openerContext, controlProvider, model, options, null);
	}

	/**
	 * Opens a new window with a control provider, model, and close callback.
	 *
	 * @param openerContext
	 *        The opener window's {@link ReactContext}.
	 * @param controlProvider
	 *        The factory that creates the control tree for the new window.
	 * @param model
	 *        The model passed to the factory.
	 * @param options
	 *        Window display options.
	 * @param closeCallback
	 *        Optional callback invoked when the window is closed, or null.
	 * @return The generated window ID.
	 */
	public String openWindow(ReactContext openerContext, ReactControlProvider controlProvider,
			Object model, WindowOptions options, Runnable closeCallback) {
		String windowId = generateWindowId();
		String openerWindowId = openerContext.getWindowName();

		WindowEntry entry =
			new WindowEntry(windowId, openerWindowId, controlProvider, model, options, closeCallback);
		_windows.put(windowId, entry);

		WindowOpenEvent event = WindowOpenEvent.create()
			.setTargetWindowId(openerWindowId)
			.setWindowId(windowId)
			.setWidth(options.getWidth())
			.setHeight(options.getHeight())
			.setTitle(options.getTitle())
			.setResizable(options.isResizable());
		SSEUpdateQueue openerQueue = getQueue(openerWindowId);
		if (openerQueue != null) {
			openerQueue.enqueue(event);
		}

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
		Logger.info("windowClosed(" + windowId + "), known windows: " + _windows.keySet(),
			ReactWindowRegistry.class);
		WindowEntry entry = _windows.remove(windowId);
		if (entry != null) {
			Runnable closeCallback = entry.getCloseCallback();
			if (closeCallback != null) {
				Logger.info("Running close callback for '" + windowId + "'.",
					ReactWindowRegistry.class);
				try {
					closeCallback.run();
					Logger.info("Close callback completed for '" + windowId + "'.",
						ReactWindowRegistry.class);
				} catch (Exception ex) {
					Logger.error("Error in window close callback for window '" + windowId + "'.",
						ex, ReactWindowRegistry.class);
				}
			} else {
				Logger.info("No close callback for '" + windowId + "'.",
					ReactWindowRegistry.class);
			}
			ReactControl rootControl = entry.getRootControl();
			if (rootControl != null) {
				rootControl.cleanupTree();
			}
		}
		SSEUpdateQueue queue = _windowQueues.remove(windowId);
		if (queue != null) {
			queue.shutdown();
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// Nothing to do.
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		for (SSEUpdateQueue queue : _windowQueues.values()) {
			queue.shutdown();
		}
		_windowQueues.clear();
		for (WindowEntry entry : _windows.values()) {
			ReactControl rootControl = entry.getRootControl();
			if (rootControl != null) {
				rootControl.cleanupTree();
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
