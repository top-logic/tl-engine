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
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that the loss of an event stream is noticed by one write: nothing more is written to it,
 * and the queue drops it, so that its client counts as gone.
 */
public class TestSSEConnectionLoss extends TestCase {

	private static final int CONTROLS = 5;

	private SSEUpdateQueue _queue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		for (int n = 0; n < CONTROLS; n++) {
			new ReactControl(context, null, "TLPanel").write(new TagWriter());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_queue.shutdown();
		super.tearDown();
	}

	/**
	 * A stream lost while it receives the full state of the window gets no further control state,
	 * no heartbeat and no event, and its client counts as gone.
	 */
	public void testLossDuringFullStateDropsStream() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		stream.failWritesAfter(1);

		_queue.setConnection(stream.context());
		_queue.enqueue(JSSnipplet.create().setCode("later"));
		_queue.sendHeartbeat();

		assertEquals("no write after the one that failed", 2, stream.writes());
		assertClientGone();
	}

	/**
	 * A stream lost after it was connected is written to once more, however many events and
	 * heartbeats follow, and its client counts as gone.
	 */
	public void testLossOfConnectedStreamDropsStream() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());
		int connected = stream.writes();
		stream.failWritesAfter(connected);

		for (int n = 0; n < 3; n++) {
			_queue.enqueue(JSSnipplet.create().setCode("event" + n));
			_queue.sendHeartbeat();
		}

		assertEquals("no write after the one that failed", connected + 1, stream.writes());
		assertClientGone();
	}

	/**
	 * A stream that keeps working receives the state of every control, and its client stays
	 * present.
	 */
	public void testWorkingStreamKeepsClient() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();

		_queue.setConnection(stream.context());
		_queue.enqueue(JSSnipplet.create().setCode("later"));
		_queue.sendHeartbeat();

		String body = stream.body();
		assertEquals(body, CONTROLS, body.split("\\[\"StateEvent\"", -1).length - 1);
		assertTrue(body, body.contains("later"));
		assertClientPresent();
	}

	/**
	 * A stream the queue gave up for a poll is not written to, and its end is no loss of the
	 * client.
	 */
	public void testAbandonedStreamIsNotWrittenTo() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());
		int connected = stream.writes();
		stream.failWritesAfter(connected);

		_queue.poll(new FakeAsyncRequest().context(), 0, true);
		_queue.enqueue(JSSnipplet.create().setCode("later"));
		_queue.sendHeartbeat();

		assertEquals(connected, stream.writes());
		assertClientPresent();
	}

	private void assertClientGone() {
		long now = System.currentTimeMillis();
		assertFalse("client present after the loss of its stream", _queue.hasClient(now));
		assertTrue("client not gone after the loss of its stream", _queue.clientGoneSince(now) > 0);
	}

	private void assertClientPresent() {
		long now = System.currentTimeMillis();
		assertTrue("client lost", _queue.hasClient(now));
		assertEquals("client lost", 0, _queue.clientGoneSince(now));
	}

}
