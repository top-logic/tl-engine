/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.protocol.DownloadEvent;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.protocol.StateEvent;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Queue for delivering SSE events to a connected React client in a single browser window.
 *
 * <p>
 * Each browser window has one {@link SSEUpdateQueue} managed by the
 * {@link com.top_logic.layout.react.window.ReactWindowRegistry}. A single SSE connection
 * registers with the queue and receives events as they are enqueued.
 * </p>
 *
 * <p>
 * A periodic heartbeat is sent to the connection to keep it alive and to detect dead connections
 * early. Without the heartbeat, intermediaries (proxies, load balancers) may silently drop idle
 * connections, leaving half-open connections that neither the server nor the client can detect.
 * </p>
 *
 * <p>
 * <b>Polling where the stream does not arrive.</b> A corporate proxy that inspects content
 * (Zscaler, Netskope and others) may hold a {@code text/event-stream} response back until it ends,
 * forward it only in large blocks, or refuse it. Every event a window receives arrives on this
 * queue - each command's result, the login dialog, a navigation - so behind such a proxy the page
 * does not react at all. The queue therefore numbers every event it writes, sends the number as
 * the event's {@code id}, and keeps what it wrote until the client has read it (at most
 * {@link #MAX_RETAINED_CHARS} characters, since the last {@link #setConnection(AsyncContext)
 * connection} or {@link #discardPendingEvents() rendering}). A client whose stream brings nothing
 * changes to {@link #poll(AsyncContext, long, boolean) polling} and continues after the last number
 * it saw. Its first poll asks for the full state of the controls, because the state the connection
 * sent may not have arrived either; the events written since the connection that address no
 * displayed control - a reload, a frame of a listener that is not a control - follow it. A poll
 * waits up to {@link #POLL_HOLD_MILLIS} for an event, so an event is delivered as soon as it
 * exists, and every poll ends before a proxy's idle timeout.
 * </p>
 *
 * <p>
 * <b>A client that went away.</b> The heartbeat keeps running while the queue has no client, see
 * {@link #clientGoneSince(long)}. A browser tab killed without an unload report, a closed laptop, a
 * lost network leave such a queue behind. The heartbeat reports it to its
 * {@link ReactWindowRegistry}, which tears the window down once the client has been gone for
 * {@link ReactWindowRegistry#DISCONNECT_GRACE_MILLIS}, see {@link #checkAbandoned(long)}.
 * </p>
 */
public class SSEUpdateQueue {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.sseQueue";

	/**
	 * Heartbeat message sent as a regular SSE data event so the client can track connection
	 * liveness.
	 */
	private static final String HEARTBEAT_MESSAGE = "data: [\"Heartbeat\",{}]\n\n";

	/**
	 * Interval between heartbeat messages in seconds.
	 *
	 * <p>
	 * Below the 30 seconds after which many proxies close a connection without traffic.
	 * </p>
	 */
	private static final long HEARTBEAT_INTERVAL_SECONDS = 15;

	/** How long a poll waits for an event before it answers with none. */
	public static final long POLL_HOLD_MILLIS = 10_000;

	/**
	 * How long the queue keeps serving a polling client after its last poll: one that polls asks
	 * again at once, so a longer silence means the page is gone.
	 */
	public static final long POLL_ORPHAN_MILLIS = 60_000;

	/** How much the queue keeps of what it wrote, in characters of serialized events. */
	static final int MAX_RETAINED_CHARS = 1_000_000;

	private static final SecureRandom RANDOM = new SecureRandom();

	/** The queues by the token their polls carry instead of the session cookie. */
	private static final Map<String, SSEUpdateQueue> QUEUES_BY_POLL_TOKEN = new ConcurrentHashMap<>();

	/** Ends a poll that found no event within {@link #POLL_HOLD_MILLIS}. */
	private static final ScheduledExecutorService POLL_TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r, "SSE-Poll-Timer");
		thread.setDaemon(true);
		return thread;
	});

	/** An event as it was written, with its number. */
	private record Written(long seq, SSEEvent event, String json) {
		// Pure data.
	}

	/** A poll held open until an event arrives or its time is up. */
	private record WaitingPoll(AsyncContext context, long after, ScheduledFuture<?> timeout) {
		// Pure data.
	}

	/** What the queue wrote since the last connection or rendering, oldest first. Guarded by {@code this}. */
	private final Deque<Written> _written = new ArrayDeque<>();

	/** The characters {@link #_written} holds. Guarded by {@code this}. */
	private int _writtenChars;

	/** The number of the last event written. Guarded by {@code this}. */
	private long _lastSeq;

	/** The highest number that was dropped from {@link #_written} before a poll read it. Guarded by {@code this}. */
	private long _droppedUnread;

	/** Whether the client polls instead of reading a stream. Guarded by {@code this}. */
	private boolean _polling;

	/** When the client last polled. Guarded by {@code this}. */
	private long _lastPoll;

	/**
	 * When the event stream of a client that does not poll was lost, {@code 0} while it is
	 * connected or if the queue never had a stream. Guarded by {@code this}.
	 *
	 * @see #clientGoneSince(long)
	 */
	private long _streamLostAt;

	/** The poll waiting for the next event, {@code null} if none. Guarded by {@code this}. */
	private WaitingPoll _waitingPoll;

	/** The token polls carry instead of the session cookie, created with the first poll. */
	private volatile String _pollToken;

	private final ConcurrentLinkedQueue<SSEEvent> _pendingEvents = new ConcurrentLinkedQueue<>();

	private final Map<String, ReactCommandTarget> _controls = new ConcurrentHashMap<>();

	/**
	 * How long a delivered file waits for the client to fetch it, in milliseconds.
	 *
	 * <p>
	 * A client that does not fetch it - the browser refused a further download, the event was lost
	 * with the connection, the page was reloaded - never will, so its file is given up afterwards.
	 * </p>
	 */
	public static final long DOWNLOAD_TIMEOUT = 5 * 60 * 1000;

	/**
	 * The number of delivered files a window keeps waiting for the client at most; delivering one
	 * more gives up the oldest.
	 */
	public static final int MAX_PENDING_DOWNLOADS = 10;

	/**
	 * The files handed to the user of this window that the client has not fetched yet, by the key
	 * it fetches them under, oldest first.
	 *
	 * @see #deliverDownload(BinaryData, Runnable)
	 */
	private final Map<String, PendingDownload> _downloads = new LinkedHashMap<>();

	private final AtomicInteger _nextId = new AtomicInteger(1);

	private volatile SSEConnection _connection;

	private volatile boolean _shutdown;

	private volatile ScheduledFuture<?> _heartbeatTask;

	private volatile String _windowName;

	private volatile ReactControl _rootControl;

	private volatile TLSessionContext _sessionContext;

	private volatile ReactWindowRegistry _windowRegistry;

	private DialogManager _dialogManager;

	private RouteManager _routeManager;

	private final ScriptRecorder _recorder = new ScriptRecorder();

	/**
	 * Retrieves or creates the {@link SSEUpdateQueue} for the given session.
	 *
	 * <p>
	 * Synchronizes on the session to prevent a race where two concurrent requests both see
	 * {@code null}, create independent queues, and the second {@code setAttribute} triggers
	 * {@code valueUnbound} on the first — clearing its registered controls.
	 * </p>
	 */
	public static SSEUpdateQueue forSession(HttpSession session) {
		SSEUpdateQueue queue = (SSEUpdateQueue) session.getAttribute(SESSION_ATTRIBUTE_KEY);
		if (queue == null) {
			synchronized (session) {
				queue = (SSEUpdateQueue) session.getAttribute(SESSION_ATTRIBUTE_KEY);
				if (queue == null) {
					queue = new SSEUpdateQueue();
					session.setAttribute(SESSION_ATTRIBUTE_KEY, queue);
				}
			}
		}
		return queue;
	}

	/**
	 * Allocates a unique control ID within this session.
	 *
	 * <p>
	 * IDs are prefixed with "v" to distinguish them from old-world control IDs.
	 * </p>
	 */
	public String allocateId() {
		return "v" + _nextId.getAndIncrement();
	}

	/**
	 * The {@link DialogManager} for the current session, or {@code null} if none is installed or the
	 * queue is {@link #shutdown() shut down}.
	 */
	public DialogManager getDialogManager() {
		return _dialogManager;
	}

	/**
	 * Sets the {@link DialogManager} for the current session.
	 *
	 * @param dialogManager
	 *        The dialog manager to install.
	 */
	public void setDialogManager(DialogManager dialogManager) {
		_dialogManager = dialogManager;
	}

	/**
	 * The {@link RouteManager} for the current window, or {@code null} if none is installed or the
	 * queue is {@link #shutdown() shut down}.
	 */
	public RouteManager getRouteManager() {
		return _routeManager;
	}

	/**
	 * Sets the {@link RouteManager} for the current window.
	 *
	 * @param routeManager
	 *        The route manager to install.
	 */
	public void setRouteManager(RouteManager routeManager) {
		_routeManager = routeManager;
		if (routeManager != null) {
			routeManager.setDisplayedParticipants(this::displayedParticipants);
			routeManager.setTitlingParticipants(this::titlingParticipants);
		}
	}

	/**
	 * The {@link RoutingParticipant}s the displayed control tree contains, in display order.
	 *
	 * <p>
	 * The walk follows the {@link ReactControl#visibleChildren() visible children}, so a control that
	 * is rendered but hidden - a covered frame of a tile stack - contributes nothing. Neither does
	 * the content of a participant showing an item without a route (see
	 * {@link RoutingParticipant#routesContent()}): that part of the display has no URL.
	 * </p>
	 *
	 * @see RouteManager#setDisplayedParticipants(java.util.function.Supplier)
	 */
	private List<RoutingParticipant> displayedParticipants() {
		return collectParticipants(false);
	}

	/**
	 * The {@link #displayedParticipants() displayed participants} that title the page, in display
	 * order.
	 *
	 * <p>
	 * The content of a participant that keeps it out of the title (see
	 * {@link RoutingParticipant#titlesContent()}) contributes none of its participants.
	 * </p>
	 *
	 * @see RouteManager#setTitlingParticipants(java.util.function.Supplier)
	 */
	private List<RoutingParticipant> titlingParticipants() {
		return collectParticipants(true);
	}

	private List<RoutingParticipant> collectParticipants(boolean titling) {
		List<RoutingParticipant> result = new ArrayList<>();
		ReactControl root = _rootControl;
		if (root != null) {
			collectParticipants(root, titling, result);
		}
		return result;
	}

	/**
	 * Collects the participants of the given control and, where they let it, of the content it
	 * displays.
	 *
	 * <p>
	 * The participants of a control - the control itself and those
	 * {@link ReactControl#addRouteParticipant(RoutingParticipant) anchored} at it - stand at the
	 * level of the control: what they display is the content of the control, its
	 * {@link ReactControl#visibleChildren() visible children}. A tile stack anchors the participant
	 * of its drill-down path, a view the participants of its bindings, and the frames of the stack
	 * respectively the elements of the view lie inside them.
	 * </p>
	 *
	 * @param titling
	 *        Whether to stop as well at a participant that keeps its content out of the title of the
	 *        page.
	 */
	private static void collectParticipants(ReactControl control, boolean titling, List<RoutingParticipant> result) {
		boolean descend = true;
		if (control instanceof RoutingParticipant participant) {
			result.add(participant);
			descend = includesContent(participant, titling);
		}
		for (RoutingParticipant participant : control.routeParticipants()) {
			result.add(participant);
			descend &= includesContent(participant, titling);
		}
		if (!descend) {
			return;
		}
		for (ReactControl child : control.visibleChildren()) {
			collectParticipants(child, titling, result);
		}
	}

	private static boolean includesContent(RoutingParticipant participant, boolean titling) {
		return participant.routesContent() && (!titling || participant.titlesContent());
	}

	/**
	 * Registers a {@link ReactCommandTarget} so that it can be looked up by ID for command dispatch.
	 *
	 * <p>
	 * A {@link ReactControl} is registered exactly while it is {@link ReactControl#isAttached()
	 * attached}: {@link ReactControl#attach()} registers it and {@link ReactControl#detach()}
	 * unregisters it. The queue therefore holds the controls the window displays, and no control that
	 * left the display is kept alive by it.
	 * </p>
	 */
	public void registerControl(ReactCommandTarget control) {
		_controls.put(control.getID(), control);
	}

	/**
	 * Unregisters a previously registered control.
	 *
	 * @see #registerControl(ReactCommandTarget)
	 */
	public void unregisterControl(ReactCommandTarget control) {
		_controls.remove(control.getID(), control);
	}

	/**
	 * Whether any control is registered with this queue.
	 *
	 * <p>
	 * Rendering a page attaches its root control, which stays registered as long as the page is
	 * displayed. A queue without any controls therefore did not render a page in this session - it
	 * was typically created empty by an SSE reconnect after the session was replaced underneath an
	 * open page.
	 * Commands arriving for such a window target the control tree of a discarded session.
	 * </p>
	 */
	public boolean hasControls() {
		return !_controls.isEmpty();
	}

	/**
	 * Looks up a previously registered control by its ID.
	 *
	 * <p>
	 * Only {@link #registerControl(ReactCommandTarget) registered}, i.e. displayed, controls are
	 * found. A request that was sent for a control before the client unmounted it may arrive after
	 * the server has detached it; missing such a control is expected and therefore logged at debug
	 * level only.
	 * </p>
	 *
	 * @return The control, or {@code null} if not found.
	 */
	public ReactCommandTarget getControl(String controlId) {
		ReactCommandTarget control = _controls.get(controlId);
		if (control == null) {
			Logger.debug("Control '" + controlId + "' is not displayed in window '" + _windowName
				+ "' (queue@" + System.identityHashCode(this) + ", " + _controls.size()
				+ " controls registered).", SSEUpdateQueue.class);
		}
		return control;
	}

	/**
	 * Records the authoritative root control of this window's displayed tree.
	 *
	 * <p>
	 * Set when the window's page is rendered. This is the single root the headless interface projects
	 * from, following the displayed tree in display order.
	 * </p>
	 *
	 * @param rootControl
	 *        The window's root control.
	 */
	public void setRootControl(ReactControl rootControl) {
		_rootControl = rootControl;
	}

	/**
	 * Records the window name this queue serves, so diagnostics can identify the window.
	 *
	 * @param windowName
	 *        The window name.
	 */
	public void setWindowName(String windowName) {
		_windowName = windowName;
	}

	/**
	 * The authoritative root control of this window's displayed tree, or {@code null} if not set or
	 * the queue is {@link #shutdown() shut down}.
	 *
	 * @see #setRootControl(ReactControl)
	 */
	public ReactControl getRootControl() {
		return _rootControl;
	}

	/**
	 * The per-window {@link ScriptRecorder} that captures dispatched commands as replayable steps.
	 */
	public ScriptRecorder getRecorder() {
		return _recorder;
	}

	/**
	 * Hands the given file to the user of this window as a download.
	 *
	 * @see #deliverDownload(BinaryData, Runnable)
	 */
	public String deliverDownload(BinaryData data) {
		return deliverDownload(data, null);
	}

	/**
	 * Hands the given file to the user of this window as a download.
	 *
	 * <p>
	 * The file is kept until the client fetches it - once - from the {@code react-api/download}
	 * endpoint, which a {@link DownloadEvent} tells the client to do. A command that produces a
	 * file for the user, an export say, delivers it this way; the browser saves it under its
	 * {@link BinaryData#getName() name}. A file the client does not fetch within
	 * {@link #DOWNLOAD_TIMEOUT} is given up, and so is the oldest one when more than
	 * {@link #MAX_PENDING_DOWNLOADS} are waiting.
	 * </p>
	 *
	 * @param data
	 *        The file to deliver.
	 * @param discard
	 *        What releases the file - the deletion of a temporary file, say - once it is sent or
	 *        given up; {@code null} for a file nothing has to be released for.
	 * @return The key the client fetches the file under, see {@link #takeDownload(String)}.
	 */
	public String deliverDownload(BinaryData data, Runnable discard) {
		String key = UUID.randomUUID().toString();
		List<PendingDownload> givenUp = new ArrayList<>();
		synchronized (_downloads) {
			collectExpired(System.currentTimeMillis(), givenUp);
			_downloads.put(key, new PendingDownload(data, discard, System.currentTimeMillis()));
			while (_downloads.size() > MAX_PENDING_DOWNLOADS) {
				Iterator<PendingDownload> oldest = _downloads.values().iterator();
				givenUp.add(oldest.next());
				oldest.remove();
			}
		}
		discardAll(givenUp);
		String windowName = _windowName == null ? "" : _windowName;
		enqueue(DownloadEvent.create()
			.setUrl("react-api/download?windowName=" + URLEncoder.encode(windowName, StandardCharsets.UTF_8)
				+ "&key=" + key)
			.setFileName(data.getName()));
		return key;
	}

	/**
	 * Removes and returns the file {@link #deliverDownload(BinaryData, Runnable) delivered} under
	 * the given key, {@code null} if there is none - never delivered, already fetched, or given up.
	 *
	 * <p>
	 * The caller sends the file and then {@link PendingDownload#discard() discards} it.
	 * </p>
	 */
	public PendingDownload takeDownload(String key) {
		if (key == null) {
			return null;
		}
		List<PendingDownload> givenUp = new ArrayList<>();
		PendingDownload result;
		synchronized (_downloads) {
			collectExpired(System.currentTimeMillis(), givenUp);
			result = _downloads.remove(key);
		}
		discardAll(givenUp);
		return result;
	}

	/**
	 * Gives up every delivered file that has waited for the client longer than
	 * {@link #DOWNLOAD_TIMEOUT} at the given time.
	 *
	 * <p>
	 * Done whenever a file is delivered or fetched, and with every heartbeat of the window.
	 * </p>
	 *
	 * @param now
	 *        The current time in milliseconds.
	 */
	public void expireDownloads(long now) {
		List<PendingDownload> givenUp = new ArrayList<>();
		synchronized (_downloads) {
			collectExpired(now, givenUp);
		}
		discardAll(givenUp);
	}

	/** Called while holding the {@link #_downloads} lock. */
	private void collectExpired(long now, List<PendingDownload> out) {
		for (Iterator<PendingDownload> it = _downloads.values().iterator(); it.hasNext();) {
			PendingDownload download = it.next();
			if (now - download.deliveredAt() > DOWNLOAD_TIMEOUT) {
				out.add(download);
				it.remove();
			}
		}
	}

	private static void discardAll(List<PendingDownload> downloads) {
		for (PendingDownload download : downloads) {
			download.discard();
		}
	}

	/**
	 * A file {@link SSEUpdateQueue#deliverDownload(BinaryData, Runnable) delivered} to the window,
	 * waiting for the client to fetch it.
	 *
	 * @param data
	 *        The file.
	 * @param release
	 *        What releases the file once it is sent or given up, {@code null} for nothing.
	 * @param deliveredAt
	 *        When the file was delivered, in milliseconds.
	 */
	public record PendingDownload(BinaryData data, Runnable release, long deliveredAt) {

		/**
		 * Releases the file: it has been sent, or will never be.
		 */
		public void discard() {
			if (release != null) {
				try {
					release.run();
				} catch (RuntimeException ex) {
					Logger.error("Releasing the download '" + data.getName() + "' failed.", ex, SSEUpdateQueue.class);
				}
			}
		}

	}

	/**
	 * Sets the SSE connection for this queue, replacing any existing one.
	 *
	 * <p>
	 * A queue that is {@link #shutdown() shut down} stays dead: the connection is closed at once, so
	 * that a client reconnecting while its window is torn down does not revive the window.
	 * </p>
	 *
	 * @return Whether the connection was accepted, {@code false} if the queue is shut down and the
	 *         connection was closed.
	 */
	public boolean setConnection(AsyncContext asyncContext) {
		SSEConnection old;
		SSEConnection newConn = new SSEConnection(asyncContext);
		synchronized (this) {
			if (_shutdown) {
				Logger.info("SSE connection REFUSED for window '" + _windowName + "' (queue@"
					+ System.identityHashCode(this) + "): the window is torn down.", SSEUpdateQueue.class);
				try {
					asyncContext.complete();
				} catch (RuntimeException ex) {
					// Already closed.
				}
				return false;
			}
			old = _connection;
			_connection = newConn;
			_streamLostAt = 0;
			// The client starts again from the state this connection sends; what was written before
			// is not to be replayed to it.
			forgetWritten();
			_polling = false;
			answerWaitingPoll();
		}
		if (old != null) {
			// Diagnostic for "controls don't react": a second event stream for the SAME window
			// replaces the first, so the previously connected browser tab stops receiving state
			// patches (its clicks still POST, but updates never arrive). Expected on reconnect of a
			// single tab; suspicious if two live tabs share a window.
			Logger.warn("SSE connection REPLACED for window '" + _windowName + "' (queue@"
				+ System.identityHashCode(this) + "); the previous browser event stream is now closed.",
				SSEUpdateQueue.class);
			try {
				old.getContext().complete();
			} catch (Exception ex) {
				// Old connection already closed, ignore.
			}
		} else {
			Logger.info("SSE connection set for window '" + _windowName + "' (queue@"
				+ System.identityHashCode(this) + ").", SSEUpdateQueue.class);
		}
		sendFullState(newConn);
		// A first event at once, also for a window without controls: what tells the client that its
		// stream arrives.
		writeOrDisconnect(newConn, HEARTBEAT_MESSAGE);
		// Flush any events that were enqueued before the SSE connection was
		// established (e.g. RouteChangeEvent from initial RoutingParticipant
		// registration during control tree construction).
		flush();
		ensureHeartbeat();
		return true;
	}

	/**
	 * Clears the SSE connection if it matches the given async context.
	 *
	 * <p>
	 * The client counts as {@link #clientGoneSince(long) gone} from now on, until it connects again
	 * or polls. The heartbeat keeps running, so that it notices when the client stays away.
	 * </p>
	 */
	public void clearConnection(AsyncContext asyncContext) {
		SSEConnection current = _connection;
		if (current != null && current.getContext() == asyncContext) {
			if (dropConnection(current, System.currentTimeMillis())) {
				Logger.info("SSE connection cleared for window '" + _windowName + "' (queue@"
					+ System.identityHashCode(this) + ").", SSEUpdateQueue.class);
			}
		}
	}

	/**
	 * Drops the given connection if it is still the current one, recording when the client was lost.
	 *
	 * @return Whether the given connection was the current one.
	 */
	private synchronized boolean dropConnection(SSEConnection connection, long now) {
		if (_connection != connection) {
			return false;
		}
		_connection = null;
		if (!_polling) {
			_streamLostAt = now;
		}
		return true;
	}

	/**
	 * Sends the full state of all registered {@link ReactControl}s to the given connection.
	 *
	 * <p>
	 * This is called when a new SSE connection is established (including reconnects) to ensure the
	 * client has the current state of all controls, recovering any state that may have been lost
	 * while the connection was down.
	 * </p>
	 *
	 * <p>
	 * Since a control is registered exactly while it is displayed, this sends the state of the
	 * controls the window displays.
	 * </p>
	 */
	private void sendFullState(SSEConnection connection) {
		for (ReactCommandTarget control : _controls.values()) {
			if (control instanceof ReactControl rc) {
				StateEvent event = StateEvent.create()
					.setControlId(rc.getID())
					.setState(rc.stateAsJSON());
				String message = toDataMessage(toJson(event));
				if (message != null) {
					writeOrDisconnect(connection, message);
				}
			}
		}
	}

	/**
	 * Drops all events that are still waiting for a client.
	 *
	 * <p>
	 * Called when a page is (re)rendered from scratch: the rendered output carries the full state of
	 * every control, so a queued update from before that rendering is at best redundant and at worst
	 * describes a state the fresh page has already passed. Unlike a
	 * {@link #setConnection(jakarta.servlet.AsyncContext) reconnect} - which deliberately keeps
	 * pending events, because one-off events such as the logout reload are not part of any control
	 * state - a full page render replaces what the events would have delivered.
	 * </p>
	 */
	public void discardPendingEvents() {
		_pendingEvents.clear();
		synchronized (this) {
			forgetWritten();
		}
	}

	/**
	 * The number of events waiting for a client: enqueued, but not yet written to a connection.
	 *
	 * <p>
	 * An {@link #enqueue(SSEEvent) enqueued} event is removed only once it has been written, so
	 * without a {@link #setConnection(AsyncContext) connection} this counts everything the queue has
	 * been handed. That makes it the seam for observing what a control sends: a count that does not
	 * move across an interaction is the proof that no event was produced.
	 * </p>
	 */
	public int pendingEventCount() {
		return _pendingEvents.size();
	}

	/**
	 * Enqueues an event for delivery to the connected SSE client.
	 *
	 * <p>
	 * Outside an interaction the event is flushed immediately: an SSE (re)connection and background
	 * activity deliver state that nothing is about to revise. Within an open {@link DeliveryScope}
	 * the event only queues and this queue is noted as touched, so that delivery is
	 * {@link #settle() settled} once the interaction has completed. This includes the model events
	 * the heartbeat synthesizes, since it synthesizes them within an interaction.
	 * </p>
	 */
	public void enqueue(SSEEvent event) {
		if (_shutdown) {
			Logger.info("enqueue REJECTED (shutdown) on queue@" + System.identityHashCode(this),
				SSEUpdateQueue.class);
			return;
		}
		_pendingEvents.add(event);
		DeliveryScope scope = DeliveryScope.current();
		if (scope == null) {
			flush();
		} else {
			scope.collect(this);
		}
	}

	/**
	 * Delivers what an interaction produced for this window, dropping the updates that address
	 * controls the interaction stopped displaying.
	 *
	 * <p>
	 * An interaction both updates controls and decides which of them stay displayed: a control is
	 * patched by the listener chain of the channel a command wrote, while the container that
	 * replaces it is notified later in the same chain. An update addressed to a control the client
	 * is about to unmount would send the browser looking for data the server no longer serves, so it
	 * is dropped here, where it is known which controls the interaction leaves displayed. Nothing is
	 * lost: a control that becomes displayed again is serialized with its full state.
	 * </p>
	 *
	 * <p>
	 * Only the events that address a control - a {@link StateEvent} or a {@link PatchEvent} - can be
	 * dropped. Everything else is delivered as enqueued.
	 * </p>
	 *
	 * @see DeliveryScope
	 */
	void settle() {
		_pendingEvents.removeIf(this::addressesRetiredControl);
		flush();
	}

	/**
	 * Whether the given event addresses a control that this window no longer displays, i.e. one
	 * that is not {@link #registerControl(ReactCommandTarget) registered}: a container detached or
	 * disposed it.
	 */
	private boolean addressesRetiredControl(SSEEvent event) {
		String controlId;
		if (event instanceof StateEvent state) {
			controlId = state.getControlId();
		} else if (event instanceof PatchEvent patch) {
			controlId = patch.getControlId();
		} else {
			return false;
		}
		return !_controls.containsKey(controlId);
	}

	/**
	 * Flushes all pending events to the connected SSE client.
	 *
	 * <p>
	 * An event is removed from the queue only after it has been written successfully. If the write
	 * fails (a dead or half-open connection), the event stays queued and is retried on the next
	 * {@link #setConnection(jakarta.servlet.AsyncContext) (re)connection}. This matters for one-off
	 * events such as the login/logout {@code window.location.reload()}: dropping such an event on a
	 * silently-broken connection would leave the action half-applied (e.g. a logout that never
	 * reloads the page), since reconnects only replay control state via {@link #sendFullState}, not
	 * one-off events.
	 * </p>
	 *
	 * <p>
	 * Synchronized so that concurrent callers (enqueue, heartbeat, (re)connect) cannot interleave
	 * writes to the same connection or poll the same event twice.
	 * </p>
	 */
	public synchronized void flush() {
		SSEConnection conn = _connection;
		if (conn == null && !_polling) {
			return;
		}
		SSEEvent event;
		while ((event = _pendingEvents.peek()) != null) {
			String json = toJson(event);
			if (json == null) {
				// Unserializable event: discard it, keeping it would block the queue forever.
				_pendingEvents.poll();
				continue;
			}
			long seq = _lastSeq + 1;
			if (conn != null && !writeOrDisconnect(conn, "id: " + seq + "\n" + toDataMessage(json))) {
				// Keep the event queued for retry on the next (re)connection.
				return;
			}
			// Written successfully - or, while the client polls, ready to be read: now remove it.
			_pendingEvents.poll();
			_lastSeq = seq;
			remember(new Written(seq, event, json));
		}
		answerWaitingPoll();
	}

	/**
	 * Answers a poll of the client: the events after {@code after}, or - for the first poll, and
	 * whenever events after {@code after} were dropped unread - the full state of the displayed
	 * controls, followed by the events written since the connection that address none of them.
	 *
	 * <p>
	 * A poll means the client has given up its stream: the connection is closed, and from now on
	 * events wait here until a poll reads them. With nothing to read, the poll is held open for up
	 * to {@link #POLL_HOLD_MILLIS} and answered by the next event.
	 * </p>
	 *
	 * <p>
	 * The answer is <code>{"last":n,"token":"...","events":[...]}</code>: the number to send as
	 * {@code after} next time, the {@link #getPollToken() token} to send instead of the session
	 * cookie, and the events in the format of the stream.
	 * </p>
	 *
	 * <p>
	 * A full answer reads the state of every displayed control, so the caller holds the window's
	 * interaction and its sub-session context for it, as for a {@link #setConnection(AsyncContext)
	 * connection}.
	 * </p>
	 *
	 * @param context
	 *        The poll request, in asynchronous mode.
	 * @param after
	 *        The number of the last event the client has.
	 * @param full
	 *        Whether the client asks for the full state.
	 */
	public void poll(AsyncContext context, long after, boolean full) {
		SSEConnection abandoned;
		synchronized (this) {
			abandoned = _connection;
			_connection = null;
			_polling = true;
			_streamLostAt = 0;
			_lastPoll = System.currentTimeMillis();
			// Only one poll per window waits: a second one means the client gave up the first.
			answerWaitingPoll(true);
			// What waited for a connection - a stream a proxy refuses never reaches the server - is
			// read by the poll.
			flush();
			acknowledge(after);
			if (full || _droppedUnread > after) {
				answer(context, fullAnswer(after));
			} else if (_lastSeq > after || _shutdown) {
				answer(context, answerAfter(after));
			} else {
				ScheduledFuture<?> timeout =
					POLL_TIMER.schedule(() -> expire(context), POLL_HOLD_MILLIS, TimeUnit.MILLISECONDS);
				_waitingPoll = new WaitingPoll(context, after, timeout);
			}
		}
		if (abandoned != null) {
			Logger.info("Window '" + _windowName + "' polls; its event stream is closed.", SSEUpdateQueue.class);
			try {
				abandoned.getContext().complete();
			} catch (Exception ex) {
				// Already closed.
			}
		}
		ensureHeartbeat();
	}

	/**
	 * The number of the last event written to the client, 0 if none yet.
	 */
	public synchronized long lastSeq() {
		return _lastSeq;
	}

	/**
	 * Whether a poll after the given number can only be answered with the full state: events after
	 * it were dropped before the client read them.
	 */
	public synchronized boolean needsFullState(long after) {
		return _droppedUnread > after;
	}

	/**
	 * Whether the client of this window polls instead of reading a stream.
	 */
	public synchronized boolean isPolling() {
		return _polling;
	}

	/**
	 * The token a poll of this window carries instead of the session cookie.
	 *
	 * <p>
	 * A poll that sends the cookie restarts the session's inactivity timeout, like every request: a
	 * page that polls would keep its session alive for as long as it is open. Polls therefore carry
	 * this token, which names the window, and leave the cookie out.
	 * </p>
	 */
	public String getPollToken() {
		String token = _pollToken;
		if (token == null) {
			synchronized (this) {
				token = _pollToken;
				if (token == null && !_shutdown) {
					byte[] bytes = new byte[16];
					RANDOM.nextBytes(bytes);
					token = HexFormat.of().formatHex(bytes);
					QUEUES_BY_POLL_TOKEN.put(token, this);
					_pollToken = token;
				}
			}
		}
		return token;
	}

	/**
	 * The queue the given poll token belongs to, {@code null} if none: the window or its session has
	 * ended.
	 *
	 * @see #getPollToken()
	 */
	public static SSEUpdateQueue forPollToken(String token) {
		return token == null ? null : QUEUES_BY_POLL_TOKEN.get(token);
	}

	/** Answers the waiting poll when its time is up and no event came. */
	private synchronized void expire(AsyncContext context) {
		if (_waitingPoll != null && _waitingPoll.context() == context) {
			answerWaitingPoll(true);
		}
	}

	/** Answers the waiting poll if there is something for it. Called while holding the lock. */
	private void answerWaitingPoll() {
		answerWaitingPoll(!_polling || _shutdown);
	}

	/** Called while holding the lock. */
	private void answerWaitingPoll(boolean evenIfEmpty) {
		WaitingPoll poll = _waitingPoll;
		if (poll == null || (!evenIfEmpty && _lastSeq <= poll.after())) {
			return;
		}
		_waitingPoll = null;
		poll.timeout().cancel(false);
		answer(poll.context(), answerAfter(poll.after()));
	}

	/** Called while holding the lock. */
	private String answerAfter(long after) {
		List<String> events = new ArrayList<>();
		for (Written written : _written) {
			if (written.seq() > after) {
				events.add(written.json());
			}
		}
		return pollAnswer(events);
	}

	/** Called while holding the lock. */
	private String fullAnswer(long after) {
		List<String> events = new ArrayList<>();
		for (ReactCommandTarget control : _controls.values()) {
			if (control instanceof ReactControl rc) {
				String json = toJson(StateEvent.create().setControlId(rc.getID()).setState(rc.stateAsJSON()));
				if (json != null) {
					events.add(json);
				}
			}
		}
		for (Written written : _written) {
			// The full state is newer than every update written before it.
			if (written.seq() > after && !addressesDisplayedControl(written.event())) {
				events.add(written.json());
			}
		}
		return pollAnswer(events);
	}

	/** Called while holding the lock. */
	private String pollAnswer(List<String> events) {
		String token = _pollToken;
		return "{\"last\":" + _lastSeq + ",\"token\":\"" + (token == null ? "" : token) + "\",\"events\":["
			+ String.join(",", events) + "]}";
	}

	private void answer(AsyncContext context, String body) {
		try {
			PrintWriter writer = context.getResponse().getWriter();
			writer.write(body);
			writer.flush();
		} catch (IOException | RuntimeException ex) {
			Logger.info("A poll answer of window '" + _windowName + "' was not delivered: " + ex.getMessage(),
				SSEUpdateQueue.class);
		}
		try {
			context.complete();
		} catch (RuntimeException ex) {
			// Already completed.
		}
	}

	/** Called while holding the lock. */
	private void remember(Written written) {
		_written.addLast(written);
		_writtenChars += written.json().length();
		while (_writtenChars > MAX_RETAINED_CHARS && _written.size() > 1) {
			Written dropped = _written.removeFirst();
			_writtenChars -= dropped.json().length();
			_droppedUnread = dropped.seq();
		}
	}

	/** Called while holding the lock. */
	private void acknowledge(long upTo) {
		while (!_written.isEmpty() && _written.peekFirst().seq() <= upTo) {
			_writtenChars -= _written.removeFirst().json().length();
		}
	}

	/**
	 * Forgets what was written: the client starts from a state that contains it. Called while
	 * holding the lock.
	 */
	private void forgetWritten() {
		_written.clear();
		_writtenChars = 0;
		_droppedUnread = 0;
	}

	/**
	 * Whether the given event updates a control this window displays, whose full state therefore
	 * supersedes it.
	 */
	private boolean addressesDisplayedControl(SSEEvent event) {
		if (event instanceof StateEvent state) {
			return _controls.containsKey(state.getControlId());
		}
		if (event instanceof PatchEvent patch) {
			return _controls.containsKey(patch.getControlId());
		}
		return false;
	}
	/**
	 * Shuts down this queue, cancelling the heartbeat, closing the connection, and clearing all
	 * state.
	 *
	 * <p>
	 * The queue lets go of everything of its window: the controls, the
	 * {@link #getRootControl() root control}, the {@link #getDialogManager() dialog manager}, the
	 * {@link #getRouteManager() route manager} and the window context the heartbeat synthesizes
	 * model events in. Whoever still references a shut down queue - the container may keep a
	 * listener of an ended stream - does not keep the window's tree and the model scope its
	 * controls observe with it.
	 * </p>
	 */
	public void shutdown() {
		_shutdown = true;
		synchronized (this) {
			if (_heartbeatTask != null) {
				_heartbeatTask.cancel(false);
				_heartbeatTask = null;
			}
			// A waiting poll ends now; the next one finds the token gone and reloads the page.
			answerWaitingPoll(true);
			String token = _pollToken;
			if (token != null) {
				QUEUES_BY_POLL_TOKEN.remove(token, this);
			}
			_written.clear();
			_writtenChars = 0;
		}
		SSEConnection conn = _connection;
		_connection = null;
		if (conn != null) {
			try {
				conn.getContext().complete();
			} catch (Exception ex) {
				// Connection already closed, ignore.
			}
		}
		_pendingEvents.clear();
		_controls.clear();
		_rootControl = null;
		_dialogManager = null;
		_routeManager = null;
		_sessionContext = null;
		_windowRegistry = null;
		List<PendingDownload> givenUp;
		synchronized (_downloads) {
			givenUp = new ArrayList<>(_downloads.values());
			_downloads.clear();
		}
		discardAll(givenUp);
	}

	/**
	 * Sets the window context for model event synthesis during heartbeat.
	 *
	 * @param windowName
	 *        The window name this queue serves.
	 * @param sessionContext
	 *        The session context for subsession lookup.
	 * @param registry
	 *        The window registry for event synthesis.
	 */
	public void setWindowContext(String windowName, TLSessionContext sessionContext,
			ReactWindowRegistry registry) {
		_windowName = windowName;
		_sessionContext = sessionContext;
		_windowRegistry = registry;
	}

	private synchronized void ensureHeartbeat() {
		if (_shutdown) {
			// A queue that is torn down is never revived, not by a client that reconnects late.
			return;
		}
		if (!SchedulerService.Module.INSTANCE.isActive()) {
			// No scheduler outside a running application, e.g. in a test of the queue alone.
			return;
		}
		if (_heartbeatTask == null || _heartbeatTask.isDone()) {
			_heartbeatTask = SchedulerService.getInstance().scheduleAtFixedRate(
				this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
		}
	}

	/** Stops the heartbeat. Done exactly once by whoever finds it still running. */
	private synchronized void stopHeartbeat() {
		if (_heartbeatTask != null) {
			_heartbeatTask.cancel(false);
			_heartbeatTask = null;
		}
	}

	/**
	 * Since when this queue has had no client, {@code 0} if it has one at the given time.
	 *
	 * <p>
	 * The client is present while its event stream is connected, or while it polls: a poll waits,
	 * or the last poll arrived less than {@link #POLL_ORPHAN_MILLIS} ago. It is gone otherwise, and
	 * gone since
	 * </p>
	 * <ul>
	 * <li>the moment its event stream was lost (it ended, or a write to it failed), for a client
	 * that read a stream,</li>
	 * <li>the moment it was orphaned, the last poll plus {@link #POLL_ORPHAN_MILLIS}, for a client
	 * that polled.</li>
	 * </ul>
	 * <p>
	 * A stream connecting or a poll arriving makes the client present again. A rendered page whose
	 * client never arrives - no stream connects and no poll comes - counts as gone since the page was
	 * rendered, see {@link #expectClient(long)}. A queue that neither had a client nor rendered a page
	 * answers {@code 0}.
	 * </p>
	 *
	 * @param now
	 *        The current time in milliseconds.
	 * @return The time in milliseconds since which the client is gone, or {@code 0} if it is
	 *         present.
	 */
	public synchronized long clientGoneSince(long now) {
		if (_connection != null) {
			return 0;
		}
		if (_polling) {
			if (_waitingPoll != null) {
				return 0;
			}
			long orphanedAt = _lastPoll + POLL_ORPHAN_MILLIS;
			return now < orphanedAt ? 0 : orphanedAt;
		}
		return _streamLostAt;
	}

	/**
	 * Announces that a client is to arrive: the window's page was just rendered, and its client
	 * connects a stream or polls within seconds.
	 *
	 * <p>
	 * Unless a client is present, the client counts as {@link #clientGoneSince(long) gone} from the
	 * given time on, and the heartbeat is started, so that a page whose client never arrives - the
	 * tab was closed while it loaded, its script did not run - is torn down like a window whose
	 * client went away. The client arriving makes it present. A present client is left as it is.
	 * </p>
	 *
	 * @param now
	 *        The current time in milliseconds, the time the page was rendered.
	 */
	public void expectClient(long now) {
		synchronized (this) {
			if (_shutdown || _connection != null || isPollingClientAlive(now)) {
				return;
			}
			// The rendered page starts a client of its own, which streams or polls afresh.
			_polling = false;
			_streamLostAt = now;
		}
		ensureHeartbeat();
	}

	/** Whether a client polls and was heard of recently. Called while holding the lock. */
	private boolean isPollingClientAlive(long now) {
		return _polling && (_waitingPoll != null || now - _lastPoll < POLL_ORPHAN_MILLIS);
	}

	/**
	 * Sends one heartbeat, as the scheduler does every {@link #HEARTBEAT_INTERVAL_SECONDS} seconds.
	 *
	 * <p>
	 * Expires the downloads that were not fetched in time. While a client is present, synthesizes
	 * the model events of the window, see {@link #synthesizeModelEventsIfPossible()}, writes the
	 * heartbeat message to its stream and flushes the queue. While the client is gone, reports its
	 * absence, see {@link #checkAbandoned(long)}.
	 * </p>
	 */
	public void sendHeartbeat() {
		long now = System.currentTimeMillis();
		expireDownloads(now);
		SSEConnection conn = _connection;
		boolean polled;
		synchronized (this) {
			polled = isPollingClientAlive(now);
		}
		if (conn != null) {
			synthesizeModelEventsIfPossible();
			writeOrDisconnect(conn, HEARTBEAT_MESSAGE);
			flush();
		} else if (polled) {
			// No heartbeat for a polling client - a poll ends by itself - but the model events of
			// the window are synthesized as for a stream.
			synthesizeModelEventsIfPossible();
			flush();
		}
		if (clientGoneSince(now) != 0) {
			checkAbandoned(now);
		}
	}

	/**
	 * Reports the absence of this queue's client to its window's {@link ReactWindowRegistry}, which
	 * tears the window down once the client has been {@link #clientGoneSince(long) gone} for
	 * {@link ReactWindowRegistry#DISCONNECT_GRACE_MILLIS}.
	 *
	 * <p>
	 * Called by the heartbeat, which runs on a thread without a session context. The registry is
	 * therefore asked in the window's own sub-session, which is what the teardown of the window
	 * disposes its controls in; a window without a sub-session - its page was never rendered - is
	 * torn down without context. Tearing the window down {@link #shutdown() shuts} this queue down,
	 * which stops the heartbeat.
	 * </p>
	 *
	 * <p>
	 * A queue without window context - not served to a window of a registry, see
	 * {@link #setWindowContext(String, TLSessionContext, ReactWindowRegistry)} - has nothing to be
	 * torn down through: once its client has been gone for the grace period, only its heartbeat
	 * stops. So does the heartbeat of a queue whose window the registry no longer knows.
	 * </p>
	 *
	 * @param now
	 *        The current time in milliseconds.
	 */
	public void checkAbandoned(long now) {
		ReactWindowRegistry registry = _windowRegistry;
		TLSessionContext sessionCtx = _sessionContext;
		String windowName = _windowName;
		if (registry == null || sessionCtx == null || windowName == null) {
			long goneSince = clientGoneSince(now);
			if (goneSince != 0 && now - goneSince >= ReactWindowRegistry.DISCONNECT_GRACE_MILLIS) {
				stopHeartbeat();
			}
			return;
		}
		boolean gone;
		TLSubSessionContext subSession = sessionCtx.getSubSession(windowName);
		if (subSession == null) {
			gone = registry.tearDownIfAbandoned(windowName, now);
		} else {
			gone = ThreadContextManager.inContext(subSession, () -> registry.tearDownIfAbandoned(windowName, now));
		}
		if (gone) {
			// Done by the shutdown of a torn-down window already; a window the registry no longer
			// knows has no one else to stop it.
			stopHeartbeat();
		}
	}

	/**
	 * Synthesizes the model events of this queue's window in its sub-session.
	 *
	 * <p>
	 * The events are synthesized within an {@link Interaction}, like every other change of the
	 * window's controls, so that a commit is never delivered to a control tree that a request is
	 * building or changing. The heartbeat does not wait for that request: while another thread holds
	 * the session's interaction, nothing is synthesized. The commit is delivered at the end of that
	 * interaction, or by the next heartbeat.
	 * </p>
	 *
	 * <p>
	 * A queue without window context, or whose window has no sub-session, synthesizes nothing.
	 * </p>
	 */
	private void synthesizeModelEventsIfPossible() {
		ReactWindowRegistry registry = _windowRegistry;
		TLSessionContext sessionCtx = _sessionContext;
		String windowName = _windowName;
		if (registry == null || sessionCtx == null || windowName == null) {
			return;
		}
		TLSubSessionContext subSession = sessionCtx.getSubSession(windowName);
		if (subSession == null) {
			return;
		}
		ThreadContextManager.inContext(subSession, () -> {
			try (Interaction interaction = registry.tryBeginInteraction(0)) {
				if (interaction == null) {
					// The window is busy: the interaction holding it synthesizes at its end, or the
					// next heartbeat does.
					return;
				}
				registry.synthesizeModelEvents(windowName);
			}
		});
	}

	/**
	 * Writes a message to the given connection, clearing it if the write fails.
	 */
	private boolean writeOrDisconnect(SSEConnection connection, String message) {
		try {
			writeToConnection(connection.getContext(), message);
			return true;
		} catch (IOException ex) {
			Logger.info("SSE connection lost for window '" + _windowName + "' (queue@"
				+ System.identityHashCode(this) + ").", SSEUpdateQueue.class);
			dropConnection(connection, System.currentTimeMillis());
			return false;
		}
	}

	/**
	 * Writes a complete SSE message to a connection.
	 *
	 * <p>
	 * Synchronizes on the {@link AsyncContext} to prevent interleaving between heartbeat writes and
	 * event writes from different threads.
	 * </p>
	 *
	 * @throws IOException
	 *         If the write fails, indicating a dead connection.
	 */
	private static void writeToConnection(AsyncContext ctx, String message) throws IOException {
		synchronized (ctx) {
			PrintWriter writer = ctx.getResponse().getWriter();
			writer.write(message);
			writer.flush();
			if (writer.checkError()) {
				throw new IOException("SSE write failed (PrintWriter error flag set).");
			}
		}
	}

	private static String toDataMessage(String json) {
		if (json == null) {
			return null;
		}
		return "data: " + json + "\n\n";
	}

	private static String toJson(SSEEvent event) {
		try {
			StringW sw = new StringW();
			try (JsonWriter jsonWriter = new JsonWriter(sw)) {
				event.writeTo(jsonWriter);
			}
			return sw.toString();
		} catch (IOException ex) {
			Logger.error("Failed to serialize SSE event to JSON.", ex, SSEUpdateQueue.class);
			return null;
		}
	}

	/**
	 * Wraps an {@link AsyncContext} for an SSE connection.
	 */
	static final class SSEConnection {

		private final AsyncContext _context;

		SSEConnection(AsyncContext context) {
			_context = context;
		}

		AsyncContext getContext() {
			return _context;
		}
	}

}
