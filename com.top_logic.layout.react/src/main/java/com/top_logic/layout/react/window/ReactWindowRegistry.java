/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.WindowFocusEvent;
import com.top_logic.layout.react.protocol.WindowOpenEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.listen.ModelScope;

/**
 * Per-session registry of the browser windows of that session.
 *
 * <p>
 * Stored as an HTTP session attribute, so it dies with the session - a login or logout therefore
 * discards every window it knows. Each window is represented by one {@link WindowEntry} that owns its
 * per-window state; the registry only maps window IDs to entries, resolves singleton windows, and
 * decides when a window whose page was unloaded has stayed away long enough to be torn down.
 * </p>
 */
public class ReactWindowRegistry implements HttpSessionBindingListener {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.windowRegistry";

	/**
	 * The registries of all live sessions, by session ID.
	 *
	 * <p>
	 * A registry is reachable through its HTTP session, but a session that ends is announced by its
	 * ID alone - {@link com.top_logic.base.bus.UserEvent} carries no session object, and the
	 * container hands out none for a foreign ID. This index closes that gap.
	 * </p>
	 *
	 * @implNote Entries are removed in {@link #valueUnbound(HttpSessionBindingEvent)}, which the
	 *           container calls for every attribute of an ending session, so the index cannot
	 *           outlive the sessions it points to.
	 */
	private static final ConcurrentHashMap<String, ReactWindowRegistry> REGISTRIES_BY_SESSION_ID =
		new ConcurrentHashMap<>();

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Listener on the requests of a session, which are what keeps the session from timing out.
	 *
	 * @see ReactWindowRegistry#addActivityListener(ActivityListener)
	 */
	public interface ActivityListener {

		/**
		 * Called while a request of the session is being handled, which is when the container
		 * restarts the session's inactivity timeout.
		 *
		 * @param accessTime
		 *        When the request arrived, as epoch milliseconds. This is the reference point of the
		 *        renewed timeout - deliberately not
		 *        {@link jakarta.servlet.http.HttpSession#getLastAccessedTime()}, which reports the
		 *        request before this one while the container measures inactivity from this one.
		 * @param maxInactiveSeconds
		 *        How long the session survives without a request, see
		 *        {@link jakarta.servlet.http.HttpSession#getMaxInactiveInterval()}. A value
		 *        {@code <= 0} means that it never times out.
		 */
		void handleActivity(long accessTime, int maxInactiveSeconds);

	}

	private final ConcurrentHashMap<String, WindowEntry> _windows = new ConcurrentHashMap<>();

	/** Maps singleton keys to window IDs for singleton window reuse. */
	private final ConcurrentHashMap<String, String> _singletonKeys = new ConcurrentHashMap<>();

	/**
	 * How long a window whose page was unloaded is kept before it is torn down.
	 *
	 * <p>
	 * A reload is back within a fraction of a second, so any value well above that separates a reload
	 * from a window that really went away.
	 * </p>
	 */
	private static final long UNLOAD_GRACE_MILLIS = 30_000;

	private final java.util.Map<String, PendingViewPick> _pendingPicks = new java.util.concurrent.ConcurrentHashMap<>();

	private final ReentrantLock _requestLock = new ReentrantLock();

	/** The ID of the session this registry belongs to, remembered for the index. */
	private final String _sessionId;

	/**
	 * The listeners to inform about the requests of this session.
	 *
	 * @see #noteActivity(HttpSession)
	 */
	private final List<ActivityListener> _activityListeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a new {@link ReactWindowRegistry}.
	 *
	 * @param sessionId
	 *        The ID of the session this registry belongs to.
	 */
	public ReactWindowRegistry(String sessionId) {
		_sessionId = sessionId;
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
					registry = new ReactWindowRegistry(session.getId());
					session.setAttribute(SESSION_ATTRIBUTE_KEY, registry);
					REGISTRIES_BY_SESSION_ID.put(session.getId(), registry);
				}
			}
		}
		return registry;
	}

	/**
	 * Registers a listener on the requests of this session.
	 *
	 * <p>
	 * Nothing else tells a control that the session's inactivity timeout was restarted: a request
	 * renews it as a side effect, and the moment it would expire passes without any request at all.
	 * A control counting down to that moment therefore has to be told about every request, see
	 * {@code SessionTimeoutNoticeElement}.
	 * </p>
	 *
	 * @param listener
	 *        Informed about every request of this session until it is
	 *        {@link #removeActivityListener(ActivityListener) removed}. A control registering one
	 *        must remove it when it is disposed, since the registry outlives the windows it holds.
	 */
	public void addActivityListener(ActivityListener listener) {
		_activityListeners.add(listener);
	}

	/**
	 * Removes a listener registered with {@link #addActivityListener(ActivityListener)}.
	 */
	public void removeActivityListener(ActivityListener listener) {
		_activityListeners.remove(listener);
	}

	/**
	 * Reports that a request of this registry's session is being handled, restarting the session's
	 * inactivity timeout.
	 *
	 * @param session
	 *        The session the request belongs to, read for its inactivity interval.
	 *
	 * @implNote The reference point passed on is the current time rather than
	 *           {@link HttpSession#getLastAccessedTime()}: the latter reports the request before
	 *           this one, while the container measures the inactivity that ends the session from
	 *           this one.
	 */
	public void noteActivity(HttpSession session) {
		if (_activityListeners.isEmpty()) {
			return;
		}
		long accessTime = System.currentTimeMillis();
		int maxInactiveSeconds = session.getMaxInactiveInterval();
		for (ActivityListener listener : _activityListeners) {
			try {
				listener.handleActivity(accessTime, maxInactiveSeconds);
			} catch (RuntimeException ex) {
				// A listener that fails must not keep the request from being answered, nor the
				// remaining listeners from being informed.
				Logger.error("Failed to announce the activity of session '" + _sessionId + "'.", ex,
					ReactWindowRegistry.class);
			}
		}
	}

	/**
	 * Gets or creates the {@link SSEUpdateQueue} for the given window.
	 */
	public SSEUpdateQueue getOrCreateQueue(String windowId) {
		WindowEntry entry = getOrCreateWindow(windowId);
		// The window is being displayed again - a reload rather than a close.
		entry.markDisplayed();
		return entry.getQueue();
	}

	/**
	 * Gets the {@link SSEUpdateQueue} for the given window, or {@code null} if none exists.
	 */
	public SSEUpdateQueue getQueue(String windowId) {
		WindowEntry entry = _windows.get(windowId);
		return entry == null ? null : entry.getQueue();
	}

	/**
	 * The IDs of all windows that currently have an {@link SSEUpdateQueue} in this session.
	 *
	 * <p>
	 * Used by the headless interface to let a non-browser consumer discover the windows it can
	 * observe and drive.
	 * </p>
	 */
	public java.util.Set<String> windowNames() {
		return java.util.Collections.unmodifiableSet(_windows.keySet());
	}

	/**
	 * Gets or creates the {@link ModelScope} for the given window.
	 *
	 * <p>
	 * Each window gets its own {@link GlobalModelEventForwarder} with an independent
	 * {@link com.top_logic.knowledge.service.UpdateChain} cursor. Events synthesized on one
	 * scope are dispatched only to listeners registered on that scope.
	 * </p>
	 */
	public ModelScope getOrCreateModelScope(String windowId) {
		return getOrCreateWindow(windowId).getOrCreateModelScope();
	}

	/**
	 * Synthesizes pending model events for the given window.
	 *
	 * @param windowId
	 *        The window to synthesize events for.
	 */
	public void synthesizeModelEvents(String windowId) {
		WindowEntry entry = _windows.get(windowId);
		if (entry != null) {
			entry.synthesizeModelEvents();
		}
	}

	/**
	 * The session-wide request lock for serializing command execution.
	 */
	public ReentrantLock getRequestLock() {
		return _requestLock;
	}

	/**
	 * Registers a pending "select view" pick under the given token.
	 */
	public void registerPick(String token, PendingViewPick pending) {
		_pendingPicks.put(token, pending);
	}

	/**
	 * Looks up and removes the pending pick for the given token.
	 *
	 * @return The pending pick, or {@code null} if the token is unknown or already consumed.
	 */
	public PendingViewPick consumePick(String token) {
		return _pendingPicks.remove(token);
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
		String existingWindowId = focusSingletonIfOpen(openerContext, options);
		if (existingWindowId != null) {
			return existingWindowId;
		}

		String windowId = generateWindowId();
		String openerWindowId = openerContext.getWindowName();

		WindowEntry entry = new WindowEntry(windowId, openerWindowId, options, null);
		_windows.put(windowId, entry);
		registerSingletonKey(options, windowId);

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
		String existingWindowId = focusSingletonIfOpen(openerContext, options);
		if (existingWindowId != null) {
			return existingWindowId;
		}

		String windowId = generateWindowId();
		String openerWindowId = openerContext.getWindowName();

		WindowEntry entry =
			new WindowEntry(windowId, openerWindowId, controlProvider, model, options, closeCallback);
		_windows.put(windowId, entry);
		registerSingletonKey(options, windowId);

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
	 * The entry representing the given browser window, created on first sight.
	 *
	 * <p>
	 * Unlike {@link #openWindow(ReactContext, WindowOptions)}, this is for a window the browser opened
	 * by itself - a tab the user navigated to. Such a window has no opener, no display options and no
	 * control provider, but it holds the same per-window state as any other: the tree it currently
	 * displays, which {@link #windowUnloaded(String)} detaches and {@link #windowClosed(String)}
	 * disposes.
	 * </p>
	 */
	public WindowEntry getOrCreateWindow(String windowId) {
		return _windows.computeIfAbsent(windowId,
			id -> new WindowEntry(id, null, new WindowOptions(), null));
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
			String singletonKey = entry.getOptions().getSingletonKey();
			if (singletonKey != null) {
				_singletonKeys.remove(singletonKey, windowId);
			}
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

			// After the tree: disposing it unregisters its controls from the queue.
			entry.getQueue().shutdown();
		}
	}

	/**
	 * Reports that the page of the given window was unloaded, without saying whether it will come
	 * back.
	 *
	 * <p>
	 * Sent on {@code beforeunload}, which fires for a reload just as it does for a close. The window
	 * therefore keeps its queue, its control tree and its model scope for
	 * {@link #UNLOAD_GRACE_MILLIS}: a reload arriving within that period finds all of it and can
	 * render the tree it already has, while a window that really went away is collected by
	 * {@link #sweepUnloadedWindows()}.
	 * </p>
	 */
	public void windowUnloaded(String windowId) {
		if (windowId == null) {
			return;
		}
		WindowEntry entry = _windows.get(windowId);
		if (entry == null) {
			return;
		}
		entry.markUnloaded();

		// Nothing is displayed until the page comes back, so the tree stops observing the model. It
		// re-attaches by being rendered again; a page that does not come back is collected by
		// sweepUnloadedWindows().
		ReactControl rootControl = entry.getRootControl();
		if (rootControl != null) {
			rootControl.detach();
		}
	}

	/**
	 * Tears down the windows whose page was unloaded and did not come back within
	 * {@link #UNLOAD_GRACE_MILLIS}.
	 *
	 * <p>
	 * Called from request handling rather than from a timer, so that the teardown runs in a thread
	 * that has a session context - {@link #windowClosed(String)} runs close callbacks and disposes
	 * control trees. A window whose grace period expires while its session makes no further requests
	 * is released when the session ends ({@link #valueUnbound(HttpSessionBindingEvent)}).
	 * </p>
	 */
	public void sweepUnloadedWindows() {
		long now = System.currentTimeMillis();
		for (WindowEntry entry : _windows.values()) {
			long unloadedAt = entry.getUnloadedAt();
			if (unloadedAt == 0 || now - unloadedAt < UNLOAD_GRACE_MILLIS) {
				continue;
			}
			String windowId = entry.getWindowId();
			// Mirrors ReactServlet's window-close handling: the callbacks run by windowClosed patch
			// the opener's state and flush SSE events, and must not race with concurrent commands.
			_requestLock.lock();
			try {
				windowClosed(windowId);
			} finally {
				_requestLock.unlock();
			}
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// Nothing to do.
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		REGISTRIES_BY_SESSION_ID.remove(_sessionId, this);
		for (WindowEntry entry : _windows.values()) {
			requestReload(entry.getQueue());
			ReactControl rootControl = entry.getRootControl();
			if (rootControl != null) {
				rootControl.cleanupTree();
			}
			entry.getQueue().shutdown();
		}
		_windows.clear();
		_singletonKeys.clear();
		_activityListeners.clear();
	}

	/**
	 * Tells every window of the identified session to reload.
	 *
	 * <p>
	 * Called when a session was logged out without being invalidated, which is how the maintenance
	 * mode and an administrator terminating a session end one: the HTTP session stays alive, so
	 * {@link #valueUnbound(HttpSessionBindingEvent)} never runs and the browser would keep showing
	 * a user who is no longer logged in.
	 * </p>
	 *
	 * @param sessionId
	 *        The ID of the session whose windows are to reload; an unknown ID is ignored.
	 */
	public static void reloadWindowsOfSession(String sessionId) {
		ReactWindowRegistry registry = REGISTRIES_BY_SESSION_ID.get(sessionId);
		if (registry == null) {
			return;
		}
		for (WindowEntry entry : registry._windows.values()) {
			requestReload(entry.getQueue());
		}
	}

	/**
	 * Tells the browser of the given window to reload, so that it does not keep displaying a page
	 * belonging to a session that no longer exists.
	 *
	 * <p>
	 * A session can end without the browser doing anything: an administrator terminates it, or the
	 * maintenance mode starts and logs out everybody who may not stay. The page then still shows the
	 * previous user and their content, while the first interaction merely establishes a fresh
	 * anonymous session behind the scenes and appears to do nothing at all. Reloading brings the
	 * browser back as the anonymous user, showing the login and whatever the application announces
	 * to it.
	 * </p>
	 *
	 * @implNote Enqueued before the queue is shut down, because {@link SSEUpdateQueue#enqueue} writes
	 *           through immediately while {@link SSEUpdateQueue#shutdown()} closes the connection and
	 *           discards whatever is still pending. A window whose browser is already gone simply has
	 *           no connection to write to.
	 */
	private static void requestReload(SSEUpdateQueue queue) {
		try {
			queue.enqueue(JSSnipplet.create().setCode("window.location.reload();"));
		} catch (RuntimeException ex) {
			// The session is ending either way: a window that cannot be reached must not keep the
			// remaining ones from being told.
			Logger.error("Failed to request a reload of a window whose session ended.", ex,
				ReactWindowRegistry.class);
		}
	}

	/**
	 * If the given options specify a singleton key and a window with that key is already open,
	 * sends a {@link WindowFocusEvent} to the opener and returns the existing window ID.
	 *
	 * @return The existing window ID if focused, or {@code null} if no singleton match was found.
	 */
	private String focusSingletonIfOpen(ReactContext openerContext, WindowOptions options) {
		String singletonKey = options.getSingletonKey();
		if (singletonKey != null) {
			String existingWindowId = _singletonKeys.get(singletonKey);
			if (existingWindowId != null && focusWindow(existingWindowId)) {
				return existingWindowId;
			}
		}
		return null;
	}

	/**
	 * Brings the given window to the front of the user's desktop.
	 *
	 * <p>
	 * A window is focused through its opener, which holds the handle to it, so the request is sent to
	 * the opener's update queue.
	 * </p>
	 *
	 * @param windowId
	 *        The window to bring to the front.
	 * @return Whether the request was sent. {@code false} if the window is unknown or its opener is
	 *         gone, in which case there is nothing to focus.
	 */
	public boolean focusWindow(String windowId) {
		WindowEntry entry = _windows.get(windowId);
		if (entry == null) {
			return false;
		}
		SSEUpdateQueue openerQueue = getQueue(entry.getOpenerWindowId());
		if (openerQueue == null) {
			return false;
		}
		openerQueue.enqueue(WindowFocusEvent.create()
			.setTargetWindowId(entry.getOpenerWindowId())
			.setWindowId(windowId));
		return true;
	}

	private void registerSingletonKey(WindowOptions options, String windowId) {
		String singletonKey = options.getSingletonKey();
		if (singletonKey != null) {
			_singletonKeys.put(singletonKey, windowId);
		}
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
