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
import com.top_logic.layout.react.protocol.PageNavigationEvent;
import com.top_logic.layout.react.protocol.RouteChangeEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that the events of a window reach the page they are meant for when the browser loads the
 * window's page again: nothing the previous page was sent reaches the new one, and nothing enqueued
 * for the new one is lost to the previous one.
 *
 * @see SSEUpdateQueue#beginPage()
 */
public class TestNewPageDelivery extends TestCase {

	private SSEUpdateQueue _queue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		ReactControl control = new ReactControl(context, null, "TLPanel");
		control.write(new TagWriter());
	}

	@Override
	protected void tearDown() throws Exception {
		_queue.shutdown();
		super.tearDown();
	}

	/**
	 * The navigation a polling page read before it left is not read again by the first poll of the
	 * page it loaded, which would leave that page as well.
	 */
	public void testNewPageDoesNotReadWhatThePreviousPageWasSent() throws Exception {
		FakeAsyncRequest previousPoll = new FakeAsyncRequest();
		_queue.poll(previousPoll.context(), 0, false);
		_queue.enqueue(PageNavigationEvent.create());
		assertTrue(previousPoll.done());
		assertTrue(previousPoll.body(), previousPoll.body().contains(PageNavigationEvent.PAGE_NAVIGATION_EVENT__TYPE));
		// The previous page leaves without a further poll that would acknowledge the navigation.

		_queue.beginPage();
		FakeAsyncRequest firstPoll = new FakeAsyncRequest();
		_queue.poll(firstPoll.context(), 0, true);

		assertTrue(firstPoll.done());
		assertFalse(firstPoll.body(), firstPoll.body().contains(PageNavigationEvent.PAGE_NAVIGATION_EVENT__TYPE));
	}

	/**
	 * Without a new page, a full poll still reads the events that address no control: the client
	 * asks for it after a gap, and a navigation it has not read yet must reach it.
	 */
	public void testFullPollOfTheSamePageReadsEventsWithoutControl() throws Exception {
		_queue.enqueue(PageNavigationEvent.create());

		FakeAsyncRequest poll = new FakeAsyncRequest();
		_queue.poll(poll.context(), 0, true);

		assertTrue(poll.done());
		assertTrue(poll.body(), poll.body().contains(PageNavigationEvent.PAGE_NAVIGATION_EVENT__TYPE));
	}

	/**
	 * What the rendering of the new page enqueues waits for that page's stream instead of going to
	 * the stream of the previous page, which the server still holds while the browser gave it up.
	 */
	public void testEventsOfTheRenderingReachTheNextStream() throws Exception {
		FakeAsyncRequest previousStream = new FakeAsyncRequest();
		_queue.setConnection(previousStream.context());

		_queue.beginPage();
		assertTrue("the previous page's stream is ended", previousStream.done());
		_queue.enqueue(RouteChangeEvent.create().setUrl("events").setTitle("Events"));

		assertFalse("not written to the previous page's stream", previousStream.body().contains("\"Events\""));

		FakeAsyncRequest nextStream = new FakeAsyncRequest();
		_queue.setConnection(nextStream.context());

		String body = nextStream.body();
		assertTrue(body, body.contains("\"title\":\"Events\""));
		// The page starts with a number of its own (1), above every number of the previous page.
		assertTrue("numbered, so the client knows it arrived: " + body, body.contains("id: 2\ndata: "));
	}

	/**
	 * A reconnect of the same page keeps receiving what is enqueued after it, and what was enqueued
	 * before the new page is dropped with the previous page.
	 */
	public void testOnlyEventsOfTheNewPageReachIt() throws Exception {
		_queue.enqueue(RouteChangeEvent.create().setUrl("old").setTitle("Old"));

		_queue.beginPage();
		_queue.enqueue(RouteChangeEvent.create().setUrl("new").setTitle("New"));

		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());
		FakeAsyncRequest reconnect = new FakeAsyncRequest();
		_queue.setConnection(reconnect.context());
		_queue.enqueue(RouteChangeEvent.create().setUrl("later").setTitle("Later"));

		assertFalse(stream.body(), stream.body().contains("\"Old\""));
		assertTrue(stream.body(), stream.body().contains("\"New\""));
		assertTrue(reconnect.body(), reconnect.body().contains("\"Later\""));
	}

	/**
	 * The new page reads the full state once, with its first poll; its next polls read events.
	 */
	public void testNewPageReadsTheFullStateOnce() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(RouteChangeEvent.create().setUrl("old").setTitle("Old"));
		_queue.enqueue(RouteChangeEvent.create().setUrl("old2").setTitle("Old2"));

		_queue.beginPage();
		FakeAsyncRequest firstPoll = new FakeAsyncRequest();
		_queue.poll(firstPoll.context(), 0, true);
		assertTrue(firstPoll.done());
		long last = _queue.lastSeq();
		assertEquals("the events of the previous page and the number the new page starts with", 3, last);
		assertTrue(firstPoll.body(), firstPoll.body().startsWith("{\"last\":" + last + ","));

		assertFalse(_queue.needsFullState(last));
		FakeAsyncRequest nextPoll = new FakeAsyncRequest();
		_queue.poll(nextPoll.context(), last, false);
		assertFalse("nothing to read yet", nextPoll.done(200));

		_queue.enqueue(RouteChangeEvent.create().setUrl("new").setTitle("New"));

		assertTrue(nextPoll.done());
		String body = nextPoll.body();
		assertTrue(body, body.contains("\"New\""));
		assertFalse("no full state: " + body, body.contains("StateEvent"));
	}

	/**
	 * A page that polls after a number from before the new page - a poll of the previous page - gets
	 * the full state, not the events of the new page as a continuation.
	 */
	public void testPollFromBeforeTheNewPageGetsFullState() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(RouteChangeEvent.create().setUrl("old").setTitle("Old"));
		_queue.enqueue(RouteChangeEvent.create().setUrl("old2").setTitle("Old2"));

		_queue.beginPage();

		assertTrue(_queue.needsFullState(1));
		assertTrue("the last number of the previous page", _queue.needsFullState(2));
		assertFalse("the number the new page starts with", _queue.needsFullState(3));
	}

	/**
	 * The stream of the new page tells its client the number the state it sends stands for: the
	 * client starts without a number, while the numbering of the window continues across pages.
	 */
	public void testStreamOfTheNewPageCarriesTheCurrentNumber() throws Exception {
		_queue.setConnection(new FakeAsyncRequest().context());
		_queue.enqueue(RouteChangeEvent.create().setUrl("old").setTitle("Old"));
		_queue.enqueue(RouteChangeEvent.create().setUrl("old2").setTitle("Old2"));
		assertEquals(2, _queue.lastSeq());

		_queue.beginPage();
		FakeAsyncRequest stream = new FakeAsyncRequest();
		_queue.setConnection(stream.context());

		String body = stream.body();
		assertTrue("the number the new page starts with: " + body, body.contains("id: 3\ndata: [\"Heartbeat\""));
		assertFalse("no event of the previous page: " + body, body.contains("\"Old"));
	}

	/** The poll the previous page left waiting ends without events. */
	public void testWaitingPollOfThePreviousPageEndsEmpty() throws Exception {
		FakeAsyncRequest waiting = new FakeAsyncRequest();
		_queue.poll(waiting.context(), 0, false);
		assertFalse(waiting.done(100));

		_queue.beginPage();

		assertTrue(waiting.done());
		assertTrue(waiting.body(), waiting.body().endsWith("\"events\":[]}"));
	}
}
