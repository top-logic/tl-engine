/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletResponse;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.PatchEvent;
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

		Request poll = new Request();
		_queue.poll(poll.context, 0, false);

		assertTrue(poll.done());
		assertEquals(answer(2, "one", "two"), poll.body());
	}

	/** Events written to a stream the client never saw are read by the first poll, which closes the stream. */
	public void testPollTakesOverFromHeldBackStream() throws Exception {
		Request stream = new Request();
		_queue.setConnection(stream.context);
		_queue.enqueue(script("held back"));
		assertTrue("numbered on the stream", stream.body().contains("id: 1\ndata: "));

		Request poll = new Request();
		_queue.poll(poll.context, 0, false);

		assertTrue("the stream is given up", stream.done());
		assertTrue(poll.done());
		assertEquals(answer(1, "held back"), poll.body());

		_queue.enqueue(script("later"));
		assertFalse("nothing more goes to the abandoned stream", stream.body().contains("later"));
	}

	/** A poll with nothing to read waits, and the next event answers it; what the client has is not sent again. */
	public void testWaitingPollIsAnsweredByTheNextEvent() throws Exception {
		_queue.enqueue(script("one"));
		Request first = new Request();
		_queue.poll(first.context, 0, false);
		assertTrue(first.done());

		Request second = new Request();
		_queue.poll(second.context, 1, false);
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

		Request poll = new Request();
		_queue.poll(poll.context, 0, true);

		assertTrue(poll.done());
		String body = poll.body();
		assertTrue(body, body.contains("[\"StateEvent\",{\"controlId\":\"" + _control.getID() + "\""));
		assertFalse("superseded by the state: " + body, body.contains("{\\\"hidden\\\":true}"));
		assertTrue("not a control's update: " + body, body.contains("tl-chat-stream:abc"));
	}

	/** Polls without the session cookie find the window by its token until the window ends. */
	public void testTokenNamesTheWindowUntilItEnds() throws Exception {
		String token = _queue.getPollToken();
		assertSame(_queue, SSEUpdateQueue.forPollToken(token));

		Request waiting = new Request();
		_queue.poll(waiting.context, 0, false);
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

	/** An asynchronous request whose response body and completion the test reads. */
	private static final class Request {

		private final StringWriter _out = new StringWriter();

		private final CountDownLatch _completed = new CountDownLatch(1);

		final AsyncContext context;

		Request() {
			PrintWriter writer = new PrintWriter(_out);
			ServletResponse response = (ServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { ServletResponse.class },
				(proxy, method, args) -> method.getName().equals("getWriter") ? writer : null);
			context = (AsyncContext) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { AsyncContext.class }, (proxy, method, args) -> {
					switch (method.getName()) {
						case "getResponse":
							return response;
						case "complete":
							_completed.countDown();
							return null;
						default:
							return null;
					}
				});
		}

		boolean done() throws InterruptedException {
			return done(1000);
		}

		boolean done(long millis) throws InterruptedException {
			return _completed.await(millis, TimeUnit.MILLISECONDS);
		}

		String body() {
			return _out.toString();
		}
	}

}
