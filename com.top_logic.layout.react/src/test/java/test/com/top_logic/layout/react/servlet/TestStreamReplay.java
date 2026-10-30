/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.lang.reflect.Proxy;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.servlet.SSEServlet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a stream reconnecting after the last event its client received gets the events that
 * address no control and were written after it - one-off events the full state does not contain -
 * and nothing its client already has.
 *
 * @see SSEUpdateQueue#setConnection(jakarta.servlet.AsyncContext, long)
 */
public class TestStreamReplay extends TestCase {

	private SSEUpdateQueue _queue;

	private ReactControl _control;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		_control = new ReactControl(context, null, "TLPanel");
		_control.write(new TagWriter());
	}

	@Override
	protected void tearDown() throws Exception {
		_queue.shutdown();
		super.tearDown();
	}

	/**
	 * The events written after the client's last one follow the full state with their numbers,
	 * before the heartbeat that names the number the stream stands for.
	 */
	public void testReconnectReplaysEventsAfterTheLastReceived() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("received"));
		_queue.enqueue(script("lost"));
		_queue.enqueue(script("lost too"));

		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context(), 1);

		String body = stream.body();
		int state = body.indexOf("[\"StateEvent\"");
		int lost = body.indexOf("id: 2\ndata: [\"JSSnipplet\",{\"code\":\"lost\"}]");
		int lostToo = body.indexOf("id: 3\ndata: [\"JSSnipplet\",{\"code\":\"lost too\"}]");
		int heartbeat = body.indexOf("id: 3\ndata: [\"Heartbeat\"");
		assertTrue(body, state >= 0);
		assertTrue("replayed after the full state: " + body, lost > state);
		assertTrue("in order: " + body, lostToo > lost);
		assertTrue("before the numbered heartbeat: " + body, heartbeat > lostToo);
		assertFalse("not what the client has: " + body, body.contains("\"received\""));
	}

	/** A client that received everything gets nothing again. */
	public void testReconnectAfterTheLastEventReplaysNothing() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("one"));
		_queue.enqueue(script("two"));

		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context(), 2);

		assertFalse(stream.body(), stream.body().contains("JSSnipplet"));
	}

	/** A stream that does not name the client's last event replays nothing. */
	public void testReconnectWithoutNumberReplaysNothing() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("one"));

		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());

		assertFalse(stream.body(), stream.body().contains("JSSnipplet"));
	}

	/** An update of a displayed control is not replayed: the full state supersedes it. */
	public void testControlUpdatesAreNotReplayed() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(PatchEvent.create().setControlId(_control.getID()).setPatch("{\"hidden\":true}"));
		_queue.enqueue(script("lost"));

		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context(), 0);

		String body = stream.body();
		assertFalse("superseded by the state: " + body, body.contains("PatchEvent"));
		assertTrue(body, body.contains("id: 2\ndata: [\"JSSnipplet\",{\"code\":\"lost\"}]"));
	}

	/**
	 * The replayed events stay readable: a client whose reconnected stream does not arrive either
	 * reads them with the full state of its first poll.
	 */
	public void testReplayedEventsReachAPoll() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("received"));
		_queue.enqueue(script("lost"));
		_queue.setConnection(new FakeAsyncRequest().context(), 1);

		assertTrue(_queue.needsFullState(1));
		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 1, true);

		assertTrue(poll.done());
		String body = poll.body();
		assertTrue(body, body.contains("\"lost\""));
		assertFalse(body, body.contains("\"received\""));
	}

	/**
	 * A stream of the page the window displayed before replays nothing of the page it displays now:
	 * the numbers of a page never continue those of the page before it.
	 */
	public void testStreamOfThePreviousPageReplaysNothingOfTheNewPage() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("old page"));
		long oldLast = _queue.lastSeq();

		_queue.beginPage();
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("new page"));

		FakeAsyncRequest oldStream = new FakeAsyncRequest();
		_queue.setConnection(oldStream.context(), oldLast);
		assertFalse(oldStream.body(), oldStream.body().contains("\"new page\""));
	}

	/**
	 * A stream of the new page that reconnects after the number its first stream started with gets
	 * the new page's events.
	 */
	public void testStreamOfTheNewPageReplaysItsEvents() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("old page"));

		_queue.beginPage();
		FakeAsyncRequest newStream = new FakeAsyncRequest();
		_queue.setConnection(newStream.context());
		long newStart = _queue.lastSeq();
		assertTrue(newStream.body(), newStream.body().contains(
			"id: " + newStart + "\ndata: [\"Heartbeat\""));
		_queue.enqueue(script("new page"));

		FakeAsyncRequest reconnect = new FakeAsyncRequest();
		_queue.setConnection(reconnect.context(), newStart);
		assertTrue(reconnect.body(), reconnect.body().contains("\"new page\""));
		assertFalse(reconnect.body(), reconnect.body().contains("\"old page\""));
	}

	/**
	 * Events after the client's last one that the queue no longer holds - more than it retains was
	 * written - leave no gap: the stream replays what is held, and a poll after the client's number
	 * reads the full state.
	 */
	public void testReconnectBeyondTheRetainedEventsLeavesNoGap() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("received"));
		String large = "x".repeat(600_000);
		_queue.enqueue(script(large + "dropped"));
		_queue.enqueue(script(large + "held"));

		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context(), 1);

		String body = stream.body();
		assertFalse("no longer held", body.contains("dropped\"}"));
		assertTrue("held", body.contains("held\"}"));
		assertTrue(_queue.needsFullState(1));
	}

	/**
	 * The browser's header names the client's last event; without it, the parameter the client
	 * sends for a stream it opens again; neither, or a parameter of 0, names none.
	 */
	public void testLastEventIdOfTheRequest() throws Exception {
		assertEquals(7, SSEServlet.lastEventId(request("7", "3")));
		assertEquals(3, SSEServlet.lastEventId(request(null, "3")));
		assertEquals(-1, SSEServlet.lastEventId(request(null, "0")));
		assertEquals(-1, SSEServlet.lastEventId(request(null, null)));
		assertEquals(-1, SSEServlet.lastEventId(request("x", "y")));
	}

	private static HttpServletRequest request(String header, String after) {
		return (HttpServletRequest) Proxy.newProxyInstance(TestStreamReplay.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "getHeader":
						return SSEServlet.LAST_EVENT_ID_HEADER.equals(args[0]) ? header : null;
					case "getParameter":
						return SSEServlet.AFTER_PARAM.equals(args[0]) ? after : null;
					default:
						return null;
				}
			});
	}

	private static JSSnipplet script(String code) {
		return JSSnipplet.create().setCode(code);
	}

}
