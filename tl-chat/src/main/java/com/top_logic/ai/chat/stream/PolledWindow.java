/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.stream;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Stands in for the event stream of a React window whose stream a proxy holds back, and lets the
 * browser poll what the window's {@link SSEUpdateQueue} writes to it.
 *
 * <p>
 * <b>Temporary workaround (#940)</b> for an engine without its own fallback: every result of a
 * React command arrives on the window's event stream ({@code /react-api/events}), so behind a proxy
 * that holds a {@code text/event-stream} response back (Zscaler, Netskope) the page does not react
 * at all. The queue accepts any {@link AsyncContext} as its connection and writes the stream's text
 * to the writer of its response. This class is such a connection: it keeps what the queue writes,
 * one numbered event per {@code data:} block, until a poll has read it, and holds a poll open until
 * the next event arrives or {@link #POLL_HOLD_MILLIS} pass. Connecting it makes the queue send the
 * full state of the window's controls first, as for a reconnecting stream.
 * </p>
 *
 * <p>
 * Remove it, with {@link WindowPollServlet} and {@code script/tl-window-stream.js}, when the engine
 * polls by itself. It relies on what {@link SSEUpdateQueue} does with its connection today: it
 * writes {@code data: <json>} blocks to {@code getResponse().getWriter()}, synchronized on the
 * connection, and calls {@link #complete()} when another connection replaces it.
 * </p>
 */
final class PolledWindow implements AsyncContext {

	/** How long a poll waits for an event before it answers with none. */
	static final long POLL_HOLD_MILLIS = 10_000;

	/** A window nobody polled for this long has no page any more. */
	private static final long ORPHAN_MILLIS = 60_000;

	/** How much the window keeps of what was written, in characters. */
	static final int MAX_CHARS = 1_000_000;

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final Map<String, PolledWindow> BY_TOKEN = new ConcurrentHashMap<>();

	private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r, "Window-Poll-Timer");
		thread.setDaemon(true);
		return thread;
	});

	/** An event as the queue wrote it, with its number. */
	private record Event(long seq, String json) {
		// Pure data.
	}

	/** A poll held open until an event arrives or its time is up. */
	private record WaitingPoll(AsyncContext context, long after, ScheduledFuture<?> timeout) {
		// Pure data.
	}

	private final SSEUpdateQueue _queue;

	private final String _token;

	/** Text written since the last complete block. Guarded by {@code this}. */
	private final StringBuilder _partial = new StringBuilder();

	/** The events not yet read, oldest first. Guarded by {@code this}. */
	private final Deque<Event> _events = new ArrayDeque<>();

	private int _chars;

	private long _lastSeq;

	/** The highest number dropped before it was read; the browser then reloads. Guarded by {@code this}. */
	private long _droppedUnread;

	private WaitingPoll _waiting;

	private long _lastPoll = System.currentTimeMillis();

	private boolean _ended;

	private final ScheduledFuture<?> _orphanCheck;

	private final PrintWriter _writer = new PrintWriter(new Writer() {
		@Override
		public void write(char[] buffer, int offset, int length) {
			synchronized (PolledWindow.this) {
				_partial.append(buffer, offset, length);
			}
		}

		@Override
		public void flush() {
			takeBlocks();
		}

		@Override
		public void close() {
			// Nothing to release.
		}
	});

	private final ServletResponse _response = (ServletResponse) Proxy.newProxyInstance(
		PolledWindow.class.getClassLoader(), new Class<?>[] { ServletResponse.class },
		(proxy, method, args) -> {
			switch (method.getName()) {
				case "getWriter":
					return _writer;
				case "isCommitted":
					return Boolean.TRUE;
				case "getCharacterEncoding":
					return "UTF-8";
				case "getContentType":
					return "text/event-stream";
				default:
					Class<?> type = method.getReturnType();
					if (type == boolean.class) {
						return Boolean.FALSE;
					}
					if (type == int.class) {
						return 0;
					}
					if (type == long.class) {
						return 0L;
					}
					return null;
			}
		});

	private PolledWindow(SSEUpdateQueue queue) {
		_queue = queue;
		byte[] bytes = new byte[16];
		RANDOM.nextBytes(bytes);
		_token = HexFormat.of().formatHex(bytes);
		_orphanCheck = TIMER.scheduleWithFixedDelay(this::dropIfOrphaned, 15, 15, TimeUnit.SECONDS);
	}

	/**
	 * A new stand-in for the given window's stream. The caller connects it with
	 * {@link SSEUpdateQueue#setConnection(AsyncContext)}.
	 */
	static PolledWindow create(SSEUpdateQueue queue) {
		PolledWindow window = new PolledWindow(queue);
		BY_TOKEN.put(window._token, window);
		return window;
	}

	/**
	 * The window a poll names by its token, {@code null} if it has ended or never was.
	 *
	 * <p>
	 * Polls carry the token instead of the session cookie: a request with the cookie restarts the
	 * session's inactivity timeout, and a page that polls would keep its session alive for as long
	 * as it is open.
	 * </p>
	 */
	static PolledWindow forToken(String token) {
		return token == null ? null : BY_TOKEN.get(token);
	}

	/** The token polls of this window carry. */
	String token() {
		return _token;
	}

	/**
	 * Answers a poll with the events after {@code after}, or holds it open until one arrives.
	 *
	 * <p>
	 * The answer is <code>{"last":n,"gap":false,"ended":false,"events":[...]}</code>: the number to
	 * send as {@code after} next time, whether events were dropped before they were read, whether
	 * another connection replaced this one, and the events in the format of the stream.
	 * </p>
	 */
	synchronized void poll(AsyncContext context, long after) {
		_lastPoll = System.currentTimeMillis();
		answerWaiting();
		acknowledge(after);
		if (_lastSeq > after || _ended || _droppedUnread > after) {
			answer(context, after);
		} else {
			ScheduledFuture<?> timeout = TIMER.schedule(() -> expire(context), POLL_HOLD_MILLIS, TimeUnit.MILLISECONDS);
			_waiting = new WaitingPoll(context, after, timeout);
		}
	}

	/** Moves the complete blocks the queue wrote into the numbered events. */
	private synchronized void takeBlocks() {
		int end;
		boolean added = false;
		while ((end = _partial.indexOf("\n\n")) >= 0) {
			String block = _partial.substring(0, end);
			_partial.delete(0, end + 2);
			StringBuilder data = new StringBuilder();
			for (String line : block.split("\n")) {
				if (line.startsWith("data:")) {
					if (data.length() > 0) {
						data.append('\n');
					}
					data.append(line.substring(line.startsWith("data: ") ? 6 : 5));
				}
			}
			if (data.length() == 0) {
				// A comment, such as the padding.
				continue;
			}
			Event event = new Event(++_lastSeq, data.toString());
			_events.addLast(event);
			_chars += event.json().length();
			while (_chars > MAX_CHARS && _events.size() > 1) {
				Event dropped = _events.removeFirst();
				_chars -= dropped.json().length();
				_droppedUnread = dropped.seq();
			}
			added = true;
		}
		if (added && _waiting != null) {
			answerWaiting();
		}
	}

	private synchronized void expire(AsyncContext context) {
		if (_waiting != null && _waiting.context() == context) {
			answerWaiting();
		}
	}

	/** Called while holding the lock. */
	private void answerWaiting() {
		WaitingPoll poll = _waiting;
		if (poll == null) {
			return;
		}
		_waiting = null;
		poll.timeout().cancel(false);
		answer(poll.context(), poll.after());
	}

	/** Called while holding the lock. */
	private void answer(AsyncContext context, long after) {
		List<String> events = new ArrayList<>();
		for (Event event : _events) {
			if (event.seq() > after) {
				events.add(event.json());
			}
		}
		String body = "{\"last\":" + _lastSeq + ",\"gap\":" + (_droppedUnread > after) + ",\"ended\":" + _ended
			+ ",\"events\":[" + String.join(",", events) + "]}";
		try {
			PrintWriter writer = context.getResponse().getWriter();
			writer.write(body);
			writer.flush();
		} catch (IOException | RuntimeException ex) {
			Logger.info("A window poll answer was not delivered: " + ex.getMessage(), PolledWindow.class);
		}
		try {
			context.complete();
		} catch (RuntimeException ex) {
			// Already completed.
		}
	}

	/** Called while holding the lock. */
	private void acknowledge(long upTo) {
		while (!_events.isEmpty() && _events.peekFirst().seq() <= upTo) {
			_chars -= _events.removeFirst().json().length();
		}
	}

	/** Gives the window up when nobody polled it for {@link #ORPHAN_MILLIS}: its page is gone. */
	private void dropIfOrphaned() {
		synchronized (this) {
			if (_ended || _waiting != null || System.currentTimeMillis() - _lastPoll < ORPHAN_MILLIS) {
				return;
			}
		}
		_queue.clearConnection(this);
		complete();
	}

	// --- AsyncContext: what the queue asks of its connection ---

	@Override
	public ServletResponse getResponse() {
		return _response;
	}

	/**
	 * Called by the queue when another connection replaces this one - the page was loaded again -
	 * and when the window is given up.
	 */
	@Override
	public void complete() {
		synchronized (this) {
			if (_ended) {
				return;
			}
			_ended = true;
			answerWaiting();
		}
		BY_TOKEN.remove(_token, this);
		_orphanCheck.cancel(false);
	}

	@Override
	public ServletRequest getRequest() {
		return null;
	}

	@Override
	public boolean hasOriginalRequestAndResponse() {
		return true;
	}

	@Override
	public void dispatch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dispatch(String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dispatch(ServletContext context, String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void start(Runnable run) {
		TIMER.execute(run);
	}

	@Override
	public void addListener(AsyncListener listener) {
		// The queue registers none; nothing else knows this connection.
	}

	@Override
	public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
		// See addListener(AsyncListener).
	}

	@Override
	public <T extends AsyncListener> T createListener(Class<T> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTimeout(long timeout) {
		// Never times out; dropIfOrphaned() ends it.
	}

	@Override
	public long getTimeout() {
		return 0;
	}

}
