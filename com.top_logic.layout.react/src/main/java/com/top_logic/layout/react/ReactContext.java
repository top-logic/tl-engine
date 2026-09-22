/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.model.listen.ModelScope;

/**
 * Lean rendering context for the view system.
 *
 * <p>
 * Replaces the combination of {@link com.top_logic.layout.DisplayContext},
 * {@link com.top_logic.layout.ControlScope}, {@link com.top_logic.layout.FrameScope}, and
 * {@link com.top_logic.layout.LayoutContext} for view-system rendering with a minimal contract.
 * </p>
 *
 * @see DefaultReactContext
 */
public interface ReactContext {

	/**
	 * Allocates a unique ID for a control's DOM element.
	 */
	String allocateId();

	/**
	 * The window name sent to the client for command routing.
	 */
	String getWindowName();

	/**
	 * The webapp context path for constructing URLs.
	 */
	String getContextPath();

	/**
	 * The SSE queue for pushing state updates and registering controls.
	 */
	SSEUpdateQueue getSSEQueue();

	/**
	 * The {@link ReactWindowRegistry} for managing programmatically opened windows.
	 */
	ReactWindowRegistry getWindowRegistry();

	/**
	 * The name of the window that opened this context's window, or {@code null} if there is none
	 * (the main window, or a window whose opener is gone).
	 *
	 * <p>
	 * A tool side-window (the script recorder, the UI inspector) works on the window it was opened
	 * from: this is the handle to that window, from which its
	 * {@link ReactWindowRegistry#getQueue(String) update queue} and everything hanging off it is
	 * reached.
	 * </p>
	 */
	default String getOpenerWindowName() {
		ReactWindowRegistry registry = getWindowRegistry();
		if (registry == null) {
			return null;
		}
		WindowEntry entry = registry.getWindow(getWindowName());
		return entry == null ? null : entry.getOpenerWindowId();
	}

	/**
	 * The {@link ErrorSink} for reporting user-visible errors in the current scope, or {@code null}
	 * if no error sink is installed (legacy mode).
	 */
	default ErrorSink getErrorSink() {
		return null;
	}

	/**
	 * The {@link DialogManager} for opening and managing modal dialogs, or {@code null} if no
	 * dialog manager is installed.
	 */
	default DialogManager getDialogManager() {
		SSEUpdateQueue queue = getSSEQueue();
		return queue != null ? queue.getDialogManager() : null;
	}

	/**
	 * The {@link ModelScope} for observing persistent object changes in this window.
	 *
	 * <p>
	 * {@code null} for a context that belongs to no window - a display built outside a browser
	 * session - which observes no object changes.
	 * </p>
	 */
	ModelScope getModelScope();

	/**
	 * The {@link com.top_logic.layout.react.control.overlay.ContextMenuOpener} rendering into the
	 * enclosing context-menu overlay, or {@code null} if none.
	 *
	 * <p>
	 * A view embedding an app shell is served by that shell's overlay; any other view is served by
	 * the window-level overlay of the enclosing browser window.
	 * </p>
	 */
	default com.top_logic.layout.react.control.overlay.ContextMenuOpener getContextMenuOpener() {
		return null;
	}

	/**
	 * The {@link RouteManager} for URL routing coordination in the view system, or {@code null} if
	 * routing is not available.
	 */
	default RouteManager getRouteManager() {
		return null;
	}

	/**
	 * The {@link ObjectNavigator} leading the user to the place a business object is displayed at,
	 * or {@code null} if nothing here displays business objects.
	 */
	default ObjectNavigator getObjectNavigator() {
		return null;
	}
}
