/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import junit.framework.TestCase;

import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests {@link SSEUpdateQueue#clientGoneSince(long)}: when the queue counts its client as gone, and
 * since when.
 */
public class TestSSEUpdateQueueClientPresence extends TestCase {

	private SSEUpdateQueue _queue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new SSEUpdateQueue();
	}

	@Override
	protected void tearDown() throws Exception {
		_queue.shutdown();
		super.tearDown();
	}

	/**
	 * A queue that never had a client is not reported as abandoned.
	 */
	public void testNoClientYet() {
		assertEquals(0, _queue.clientGoneSince(System.currentTimeMillis() + 3_600_000));
	}

	/**
	 * A connected stream is a present client, however long it is connected.
	 */
	public void testConnectedStream() {
		_queue.setConnection(new FakeAsyncRequest().context());

		assertEquals(0, _queue.clientGoneSince(System.currentTimeMillis() + 3_600_000));
	}

	/**
	 * The client is gone from the moment its stream ends, until a new stream connects.
	 */
	public void testStreamLostAndReconnected() {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());

		long before = System.currentTimeMillis();
		_queue.clearConnection(stream.context());
		long after = System.currentTimeMillis();

		long goneSince = _queue.clientGoneSince(after + 3_600_000);
		assertTrue("Gone since the stream ended: " + goneSince, before <= goneSince && goneSince <= after);

		_queue.setConnection(new FakeAsyncRequest().context());
		assertEquals("A reconnected stream is a present client.", 0,
			_queue.clientGoneSince(after + 3_600_000));
	}

	/**
	 * The end of a stream that was already replaced does not make the client gone.
	 */
	public void testEndOfReplacedStream() {
		FakeAsyncRequest first = new FakeAsyncRequest();
		_queue.setConnection(first.context());
		_queue.setConnection(new FakeAsyncRequest().context());

		_queue.clearConnection(first.context());

		assertEquals(0, _queue.clientGoneSince(System.currentTimeMillis() + 3_600_000));
	}

	/**
	 * A poll makes a client whose stream was lost present again.
	 */
	public void testPollAfterLostStream() throws Exception {
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());
		_queue.clearConnection(stream.context());

		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, true);
		assertTrue(poll.done());

		assertEquals(0, _queue.clientGoneSince(System.currentTimeMillis()));
	}

	/**
	 * A polling client is present while its last poll is recent, and gone from the moment it was
	 * orphaned: {@link SSEUpdateQueue#POLL_ORPHAN_MILLIS} after the last poll.
	 */
	public void testPollingClientOrphaned() throws Exception {
		long before = System.currentTimeMillis();
		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, true);
		long after = System.currentTimeMillis();
		assertTrue("Answered with the full state.", poll.done());

		assertEquals("A recent poll is a present client.", 0,
			_queue.clientGoneSince(before + SSEUpdateQueue.POLL_ORPHAN_MILLIS - 1));

		long goneSince = _queue.clientGoneSince(after + SSEUpdateQueue.POLL_ORPHAN_MILLIS + 1_000);
		assertTrue("Gone since orphaned: " + goneSince,
			before + SSEUpdateQueue.POLL_ORPHAN_MILLIS <= goneSince
				&& goneSince <= after + SSEUpdateQueue.POLL_ORPHAN_MILLIS);

		FakeAsyncRequest next = new FakeAsyncRequest();
		_queue.poll(next.context(), 0, true);
		assertTrue(next.done());
		assertEquals("A new poll makes the client present again.", 0,
			_queue.clientGoneSince(System.currentTimeMillis() + SSEUpdateQueue.POLL_ORPHAN_MILLIS - 1_000));
	}

	/**
	 * A poll that waits for an event is a present client, however old it is.
	 */
	public void testWaitingPoll() throws Exception {
		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, false);
		assertFalse("Nothing to answer, the poll waits.", poll.done(100));

		assertEquals(0, _queue.clientGoneSince(System.currentTimeMillis() + 3_600_000));
	}

	/**
	 * A rendered page whose client does not arrive counts as gone since the rendering, until its
	 * stream connects.
	 */
	public void testExpectedClientNotArrived() {
		long rendered = System.currentTimeMillis();
		_queue.expectClient(rendered);

		assertEquals("Gone since the page was rendered.", rendered,
			_queue.clientGoneSince(rendered + 3_600_000));

		_queue.setConnection(new FakeAsyncRequest().context());
		assertEquals("The arrived client is present.", 0, _queue.clientGoneSince(rendered + 3_600_000));
	}

	/**
	 * Expecting a client while one is connected leaves it present.
	 */
	public void testExpectedClientAlreadyConnected() {
		_queue.setConnection(new FakeAsyncRequest().context());

		long rendered = System.currentTimeMillis();
		_queue.expectClient(rendered);

		assertEquals(0, _queue.clientGoneSince(rendered + 3_600_000));
	}

	/**
	 * A stream connecting to a queue that is shut down is closed, and the queue stays dead.
	 */
	public void testConnectToShutDownQueue() throws Exception {
		_queue.shutdown();

		FakeAsyncRequest stream = new FakeAsyncRequest();
		assertFalse("The connection must be refused.", _queue.setConnection(stream.context()));
		assertTrue("The refused stream must be closed.", stream.done());
		assertEquals("Nothing must be written to a refused stream.", "", stream.body());
	}

}
