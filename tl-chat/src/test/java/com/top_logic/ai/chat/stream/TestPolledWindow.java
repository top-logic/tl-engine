/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.stream;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletResponse;

import junit.framework.TestCase;

import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * What the window's queue writes to a {@link PolledWindow} reaches the polls exactly once and in
 * order - written the way {@link SSEUpdateQueue} writes to its connection: text to the writer of the
 * connection's response, flushed per message.
 */
public class TestPolledWindow extends TestCase {

	private PolledWindow _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_window = PolledWindow.create(new SSEUpdateQueue());
	}

	@Override
	protected void tearDown() throws Exception {
		_window.complete();
		super.tearDown();
	}

	/** Each data block is one event; a comment block (the stream's padding) is none. */
	public void testEventsFromDataBlocks() throws Exception {
		write(": connected" + " ".repeat(100) + "\n\n");
		write("data: [\"Heartbeat\",{}]\n\n");
		write("data: [\"JSSnipplet\",{\"code\":\"a\"}]\n\n");

		Request poll = new Request();
		_window.poll(poll.context, 0);

		assertTrue(poll.done());
		assertEquals("{\"last\":2,\"gap\":false,\"ended\":false,\"events\":[[\"Heartbeat\",{}],[\"JSSnipplet\",{\"code\":\"a\"}]]}",
			poll.body());
	}

	/** A waiting poll is answered by the next event; what the browser has is not sent again. */
	public void testWaitingPollGetsOnlyNewEvents() throws Exception {
		write("data: [\"JSSnipplet\",{\"code\":\"a\"}]\n\n");
		Request first = new Request();
		_window.poll(first.context, 0);
		assertTrue(first.done());

		Request second = new Request();
		_window.poll(second.context, 1);
		assertFalse("nothing new yet", second.done(200));

		// Written in two pieces, as a writer may hand it on: one event once the block is complete.
		write("data: [\"JSSnipplet\",");
		assertFalse("half a block is no event", second.done(100));
		write("{\"code\":\"b\"}]\n\n");

		assertTrue(second.done());
		assertEquals("{\"last\":2,\"gap\":false,\"ended\":false,\"events\":[[\"JSSnipplet\",{\"code\":\"b\"}]]}",
			second.body());
	}

	/**
	 * When another connection replaces this one - the page was loaded again - a waiting poll learns
	 * it at once, and the token names no window any more.
	 */
	public void testReplacedConnectionEndsThePolls() throws Exception {
		Request poll = new Request();
		_window.poll(poll.context, 0);

		_window.complete();

		assertTrue(poll.done());
		assertTrue(poll.body(), poll.body().contains("\"ended\":true"));
		assertNull(PolledWindow.forToken(_window.token()));
	}

	/** Writes as the queue does: to the connection's response writer, flushed. */
	private void write(String text) throws Exception {
		synchronized (_window) {
			PrintWriter writer = _window.getResponse().getWriter();
			writer.write(text);
			writer.flush();
		}
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
