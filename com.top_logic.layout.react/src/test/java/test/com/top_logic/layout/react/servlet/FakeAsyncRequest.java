/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletResponse;

/**
 * An asynchronous request - an event stream or a poll - whose response body and completion a test
 * reads.
 */
public final class FakeAsyncRequest {

	private final StringWriter _out = new StringWriter();

	private final CountDownLatch _completed = new CountDownLatch(1);

	private final AsyncContext _context;

	private int _writes;

	private int _failAfter = -1;

	/**
	 * Creates a {@link FakeAsyncRequest}.
	 */
	public FakeAsyncRequest() {
		PrintWriter writer = new BreakableWriter(_out);
		ServletResponse response = (ServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
			new Class<?>[] { ServletResponse.class },
			(proxy, method, args) -> method.getName().equals("getWriter") ? writer : null);
		_context = (AsyncContext) Proxy.newProxyInstance(getClass().getClassLoader(),
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

	/**
	 * The asynchronous context of the request, to hand to the queue.
	 */
	public AsyncContext context() {
		return _context;
	}

	/**
	 * Whether the request was completed within a second.
	 */
	public boolean done() throws InterruptedException {
		return done(1000);
	}

	/**
	 * Whether the request was completed within the given time.
	 */
	public boolean done(long millis) throws InterruptedException {
		return _completed.await(millis, TimeUnit.MILLISECONDS);
	}

	/**
	 * What was written to the response so far.
	 */
	public String body() {
		return _out.toString();
	}

	/**
	 * Lets every write after the given number of writes fail, as the writes to a connection the
	 * client has dropped do.
	 */
	public void failWritesAfter(int writes) {
		_failAfter = writes;
	}

	/**
	 * The number of writes to the response, the failed ones included.
	 */
	public int writes() {
		return _writes;
	}

	private boolean failing() {
		return _failAfter >= 0 && _writes > _failAfter;
	}

	/**
	 * The writer of the response, which reports an error once its writes fail.
	 */
	private final class BreakableWriter extends PrintWriter {

		BreakableWriter(Writer out) {
			super(out);
		}

		@Override
		public void write(String s) {
			_writes++;
			if (!failing()) {
				super.write(s);
			}
		}

		@Override
		public boolean checkError() {
			return failing() || super.checkError();
		}
	}
}
