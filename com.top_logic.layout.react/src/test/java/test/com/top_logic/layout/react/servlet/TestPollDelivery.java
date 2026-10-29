/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.servlet.DeliveryScope;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a client whose event stream does not arrive - a proxy holds it back or refuses it -
 * receives every event of its window by polling, exactly once and in order.
 */
public class TestPollDelivery extends TestCase {

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
	 * Events that waited for a connection - a stream a proxy refused never reaches the server - are
	 * read by the first poll.
	 */
	public void testEventsWithoutConnectionReachThePoll() throws Exception {
		_queue.enqueue(script("one"));
		_queue.enqueue(script("two"));

		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, false);

		assertTrue(poll.done());
		assertEquals(answer(2, "one", "two"), poll.body());
	}

	/** Events written to a stream the client never saw are read by the first poll, which closes the stream. */
	public void testPollTakesOverFromHeldBackStream() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());
		_queue.enqueue(script("held back"));
		assertTrue("numbered on the stream", stream.body().contains("id: 1\ndata: "));

		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, false);

		assertTrue("the stream is given up", stream.done());
		assertTrue(poll.done());
		assertEquals(answer(1, "held back"), poll.body());

		_queue.enqueue(script("later"));
		assertFalse("nothing more goes to the abandoned stream", stream.body().contains("later"));
	}

	/** A poll with nothing to read waits, and the next event answers it; what the client has is not sent again. */
	public void testWaitingPollIsAnsweredByTheNextEvent() throws Exception {
		_queue.enqueue(script("one"));
		FakeAsyncRequest first = new FakeAsyncRequest();
		_queue.poll(first.context(), 0, false);
		assertTrue(first.done());

		FakeAsyncRequest second = new FakeAsyncRequest();
		_queue.poll(second.context(), 1, false);
		assertFalse("nothing to read yet", second.done(200));

		_queue.enqueue(script("two"));

		assertTrue(second.done());
		assertEquals(answer(2, "two"), second.body());
	}

	/**
	 * The first poll gets the full state of the displayed controls; an older update of such a
	 * control is not replayed after it, an event addressing no control is.
	 */
	public void testFullPollReplacesControlUpdatesByState() throws Exception {
		_queue.enqueue(PatchEvent.create().setControlId(_control.getID()).setPatch("{\"hidden\":true}"));
		_queue.enqueue(PatchEvent.create().setControlId("tl-chat-stream:abc").setPatch("{\"frames\":[]}"));

		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, true);

		assertTrue(poll.done());
		String body = poll.body();
		assertTrue(body, body.contains("[\"StateEvent\",{\"controlId\":\"" + _control.getID() + "\""));
		assertFalse("superseded by the state: " + body, body.contains("{\\\"hidden\\\":true}"));
		assertTrue("not a control's update: " + body, body.contains("tl-chat-stream:abc"));
	}

	/**
	 * A poll after events that a reconnection of the stream forgot - written to a stream the client
	 * did not read, replaced by a stream that never reached the client - gets the full state, not
	 * the events after the gap.
	 */
	public void testPollAfterForgottenEventsGetsFullState() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());
		_queue.enqueue(script("read"));
		// The client read event 1; 2 and 3 are held back on the way.
		_queue.enqueue(script("held back"));
		_queue.enqueue(script("held back too"));

		// The stream reconnects; what the reconnection sends never reaches the client either.
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("after reconnect"));

		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 1, false);

		assertTrue(poll.done());
		String body = poll.body();
		assertTrue("the full state: " + body,
			body.contains("[\"StateEvent\",{\"controlId\":\"" + _control.getID() + "\""));
		assertTrue("followed by the event addressing no control: " + body, body.contains("after reconnect"));
		assertTrue("a token poll is sent to ask for the full state", _queue.needsFullState(1));
	}

	/**
	 * A client whose reconnected stream arrives continues with the events of that stream: its polls
	 * read them as events, without a full state.
	 */
	public void testReconnectedStreamNeedsNoFullState() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(script("one"));
		FakeAsyncRequest reconnected = new FakeAsyncRequest();
		_queue.setConnection(reconnected.context());
		_queue.enqueue(script("two"));
		assertTrue(reconnected.body(), reconnected.body().contains("id: 2\ndata: "));

		// The client read the full state and event 2 from the reconnected stream, and polls then.
		assertFalse(_queue.needsFullState(2));
		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 2, false);
		assertFalse("nothing to read yet", poll.done(200));

		_queue.enqueue(script("three"));

		assertTrue(poll.done());
		assertEquals(answer(3, "three"), poll.body());
	}

	/**
	 * A waiting poll whose events exceed what the queue retains is not answered with the events
	 * that are left: it is answered without events and without advancing, so that the next poll
	 * asks for the full state.
	 */
	public void testWaitingPollIsNotAnsweredWithAGap() throws Exception {
		FakeAsyncRequest waiting = new FakeAsyncRequest();
		_queue.poll(waiting.context(), 0, false);
		assertFalse(waiting.done(100));

		String large = "x".repeat(600_000);
		// Delivered together, as the events of one interaction.
		DeliveryScope.enter();
		try {
			_queue.enqueue(script(large + "1"));
			_queue.enqueue(script(large + "2"));
		} finally {
			DeliveryScope.exit();
		}

		assertTrue(waiting.done());
		assertEquals(answer(0), waiting.body());
		assertTrue(_queue.needsFullState(0));
	}

	/** Polls without the session cookie find the window by its token until the window ends. */
	public void testTokenNamesTheWindowUntilItEnds() throws Exception {
		String token = _queue.getPollToken();
		assertSame(_queue, SSEUpdateQueue.forPollToken(token));

		FakeAsyncRequest waiting = new FakeAsyncRequest();
		_queue.poll(waiting.context(), 0, false);
		_queue.shutdown();

		assertTrue("a waiting poll ends with its window", waiting.done());
		assertNull(SSEUpdateQueue.forPollToken(token));
	}

	private static JSSnipplet script(String code) {
		return JSSnipplet.create().setCode(code);
	}

	private String answer(long last, String... codes) {
		StringBuilder events = new StringBuilder();
		for (String code : codes) {
			if (events.length() > 0) {
				events.append(',');
			}
			events.append("[\"JSSnipplet\",{\"code\":\"").append(code).append("\"}]");
		}
		// No poll of these tests asked for a token, so the answers name none.
		return "{\"last\":" + last + ",\"token\":\"\",\"events\":[" + events + "]}";
	}

}
