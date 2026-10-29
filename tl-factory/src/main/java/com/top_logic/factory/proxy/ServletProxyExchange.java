/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Flow;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;

/**
 * {@link ProxyExchange} of a servlet request.
 *
 * <p>
 * The {@link #path() path} is the path of the request within the web application. A header added
 * to the answer replaces a header of the same name set before, e.g. by a filter of the web
 * application, so that an answer passed back from an upstream carries the headers of the upstream;
 * further values of the same name are added.
 * </p>
 *
 * <p>
 * An {@link #startAsync() asynchronous answer} uses the asynchronous processing of the servlet
 * container without timeout and writes a {@link AsyncAnswer#stream(Flow.Publisher) streamed body}
 * with non-blocking output: no thread of the container waits for the upstream or for the client.
 * The servlet and its filters must support asynchronous processing.
 * </p>
 */
public class ServletProxyExchange implements ProxyExchange {

	private final HttpServletRequest _req;

	private final HttpServletResponse _resp;

	private final Set<String> _added = new HashSet<>();

	private ServletAsyncAnswer _async;

	/**
	 * Creates a {@link ServletProxyExchange}.
	 *
	 * @param req
	 *        The request.
	 * @param resp
	 *        Its answer.
	 */
	public ServletProxyExchange(HttpServletRequest req, HttpServletResponse resp) {
		_req = req;
		_resp = resp;
	}

	/**
	 * The servlet request.
	 */
	public HttpServletRequest request() {
		return _req;
	}

	@Override
	public String method() {
		return _req.getMethod();
	}

	@Override
	public String path() {
		String pathInfo = _req.getPathInfo();
		return _req.getServletPath() + (pathInfo == null ? "" : pathInfo);
	}

	@Override
	public String query() {
		return _req.getQueryString();
	}

	@Override
	public String remoteAddress() {
		return _req.getRemoteAddr();
	}

	@Override
	public List<String> headerNames() {
		return Collections.list(_req.getHeaderNames());
	}

	@Override
	public List<String> headers(String name) {
		return Collections.list(_req.getHeaders(name));
	}

	@Override
	public InputStream body() throws IOException {
		return _req.getInputStream();
	}

	@Override
	public void addHeader(String name, String value) {
		if (_added.add(name.toLowerCase(Locale.ROOT))) {
			_resp.setHeader(name, value);
		} else {
			_resp.addHeader(name, value);
		}
	}

	@Override
	public void sendHeaders(int status, long length) throws IOException {
		_resp.setStatus(status);
		if (length >= 0) {
			_resp.setContentLengthLong(length);
		} else if (_async == null) {
			// Commits status and headers, so that a streamed answer starts at once.
			_resp.flushBuffer();
		}
		// An asynchronous stream commits them with its first non-blocking flush.
	}

	@Override
	public OutputStream responseBody() throws IOException {
		return _resp.getOutputStream();
	}

	@Override
	public AsyncAnswer startAsync() {
		if (_async == null) {
			AsyncContext context = _req.startAsync(_req, _resp);
			context.setTimeout(0);
			_async = new ServletAsyncAnswer(context);
			context.addListener(_async);
		}
		return _async;
	}

	/**
	 * {@link AsyncAnswer} writing to the non-blocking output of the servlet container.
	 *
	 * <p>
	 * All state is guarded by the answer itself: the pieces of the body arrive on a thread of the
	 * upstream client, the container signals writability and errors on its own threads.
	 * </p>
	 */
	private final class ServletAsyncAnswer
			implements AsyncAnswer, AsyncListener, Flow.Subscriber<List<ByteBuffer>> {

		private final AsyncContext _context;

		private final Deque<ByteBuffer> _pending = new ArrayDeque<>();

		private ServletOutputStream _out;

		private Flow.Subscription _subscription;

		/** Whether the next piece of the body is requested and not yet arrived. */
		private boolean _requested;

		/** Whether the body has arrived completely. */
		private boolean _ended;

		private boolean _completed;

		/** Whether {@link #drain()} runs on the current thread. */
		private boolean _draining;

		/** Whether {@link #drain()} must run once more, see {@link #_draining}. */
		private boolean _drainAgain;

		ServletAsyncAnswer(AsyncContext context) {
			_context = context;
		}

		@Override
		public void stream(Flow.Publisher<List<ByteBuffer>> body) {
			synchronized (this) {
				try {
					_out = _resp.getOutputStream();
				} catch (IOException ex) {
					Logger.info("Proxy: no output for the answer: " + ex.getMessage(), ServletProxyExchange.class);
					complete();
					return;
				}
			}
			// Writing starts when the container calls onWritePossible(), the first flush commits
			// status and headers.
			_out.setWriteListener(new WriteListener() {
				@Override
				public void onWritePossible() {
					drain();
				}

				@Override
				public void onError(Throwable ex) {
					clientGone(ex);
				}
			});
			body.subscribe(this);
		}

		@Override
		public synchronized void complete() {
			if (_completed) {
				return;
			}
			_completed = true;
			if (_subscription != null && !_ended) {
				_subscription.cancel();
			}
			_context.complete();
		}

		@Override
		public synchronized void onSubscribe(Flow.Subscription subscription) {
			_subscription = subscription;
			if (_completed) {
				subscription.cancel();
				return;
			}
			_requested = true;
			subscription.request(1);
		}

		@Override
		public synchronized void onNext(List<ByteBuffer> item) {
			_requested = false;
			_pending.addAll(item);
			drain();
		}

		@Override
		public synchronized void onComplete() {
			_ended = true;
			drain();
		}

		@Override
		public synchronized void onError(Throwable throwable) {
			// The upstream broke off: the answer ends incomplete.
			_ended = true;
			Logger.info("Proxy: the upstream broke off its answer: " + throwable, ServletProxyExchange.class);
			complete();
		}

		/**
		 * Writes what has arrived as long as the output takes it without blocking; requests the
		 * next piece when everything is written, completes the exchange at the end of the body.
		 */
		private synchronized void drain() {
			if (_draining) {
				// A piece arrived synchronously while requesting it: the running drain continues.
				_drainAgain = true;
				return;
			}
			_draining = true;
			try {
				do {
					_drainAgain = false;
					drainOnce();
				} while (_drainAgain);
			} finally {
				_draining = false;
			}
		}

		private void drainOnce() {
			if (_completed || _out == null) {
				return;
			}
			try {
				while (_out.isReady()) {
					ByteBuffer next = _pending.poll();
					if (next == null) {
						break;
					}
					byte[] bytes = new byte[next.remaining()];
					next.get(bytes);
					_out.write(bytes);
				}
				if (!_pending.isEmpty() || !_out.isReady()) {
					// Continued by onWritePossible().
					return;
				}
				if (_ended) {
					complete();
					return;
				}
				// Passes on what was written, e.g. an event of a stream, and commits status and
				// headers before the first piece of the body.
				_out.flush();
				if (!_out.isReady()) {
					return;
				}
				if (!_requested && _subscription != null) {
					_requested = true;
					_subscription.request(1);
				}
			} catch (IOException ex) {
				clientGone(ex);
			}
		}

		@Override
		public void onError(AsyncEvent event) {
			clientGone(event.getThrowable());
		}

		@Override
		public void onTimeout(AsyncEvent event) {
			clientGone(event.getThrowable());
		}

		@Override
		public void onComplete(AsyncEvent event) {
			synchronized (this) {
				if (!_completed) {
					_completed = true;
					if (_subscription != null && !_ended) {
						_subscription.cancel();
					}
				}
			}
		}

		@Override
		public void onStartAsync(AsyncEvent event) {
			// Started once.
		}

		private synchronized void clientGone(Throwable ex) {
			if (!_completed) {
				Logger.debug("Proxy: the client went away: " + ex, ServletProxyExchange.class);
			}
			complete();
		}
	}

}
