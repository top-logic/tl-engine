/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.classic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.services.simpleajax.RequestLock;
import com.top_logic.basic.Logger;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.internal.SubsessionHandler;

/**
 * Serializes the React commands of a classic window with that window's classic AJAX requests.
 *
 * <p>
 * Temporary workaround for the classic-UI bridge (E4): a React command for a window that is a
 * classic window - which is what {@link ReactViewComponent} makes it - runs the classic
 * revalidation of that window's {@code MainLayout} to forward its repaints, but the engine's React
 * servlet does not enter the window's {@link RequestLock}, which the AJAX servlet holds for every
 * classic request. This filter enters it in front of {@code /react-api/command}, as a reader: the
 * lock lets readers and the AJAX servlet's writers exclude each other. Remove when the engine's
 * React servlet takes the classic window's lock itself, or when the classic UI is gone.
 * </p>
 *
 * <p>
 * Uploads ({@code /react-api/upload}) are not covered: their window name is a field of a multipart
 * body, which a filter cannot read without consuming it. A command of a window that is not a classic
 * window, the usual case in a React page, passes unchanged.
 * </p>
 *
 * <p>
 * If the lock cannot be entered in time, the command runs anyway, as it would without this filter:
 * a stalled classic request must not make the React controls unusable.
 * </p>
 */
public class ReactCommandLockFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest httpRequest) || !"POST".equals(httpRequest.getMethod())) {
			chain.doFilter(request, response);
			return;
		}

		byte[] body = httpRequest.getInputStream().readAllBytes();
		HttpServletRequest replay = new BodyReplay(httpRequest, body);

		RequestLock lock = classicLock(httpRequest, windowName(body));
		if (lock == null) {
			chain.doFilter(replay, response);
			return;
		}

		Object resource = new Object();
		Integer key;
		try {
			key = lock.enterReader(resource);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new ServletException("Interrupted while waiting for the classic window's request lock.", ex);
		} catch (Exception ex) {
			Logger.warn("React command runs without the classic window's request lock: " + ex.getMessage(),
				ReactCommandLockFilter.class);
			chain.doFilter(replay, response);
			return;
		}
		try {
			chain.doFilter(replay, response);
		} finally {
			lock.exitReader(key, resource);
		}
	}

	/**
	 * The window a React command addresses, or <code>null</code> if the body does not name one.
	 */
	static String windowName(byte[] body) {
		try {
			Object parsed = JSON.fromString(new String(body, StandardCharsets.UTF_8));
			if (parsed instanceof Map<?, ?> command && command.get("windowName") instanceof String name) {
				return name;
			}
		} catch (JSON.ParseException ex) {
			// Not a command the React servlet will accept either; it answers it.
		}
		return null;
	}

	/**
	 * The request lock of the classic window of the given name in the session of the request, or
	 * <code>null</code> if there is no such classic window.
	 */
	private static RequestLock classicLock(HttpServletRequest request, String windowName) {
		if (windowName == null) {
			return null;
		}
		SessionService sessions = SessionService.getInstance();
		HttpSession httpSession = sessions.getSession(request);
		if (httpSession == null) {
			return null;
		}
		TLSessionContext session = sessions.getSession(httpSession);
		if (session == null) {
			return null;
		}
		SubsessionHandler handler = session.getHandlersRegistry().getContentHandler(windowName);
		return handler == null ? null : handler.getLock();
	}

	/**
	 * The request with its body, which this filter has read, available again to the servlet.
	 */
	private static final class BodyReplay extends HttpServletRequestWrapper {

		private final byte[] _body;

		BodyReplay(HttpServletRequest request, byte[] body) {
			super(request);
			_body = body;
		}

		@Override
		public ServletInputStream getInputStream() {
			ByteArrayInputStream in = new ByteArrayInputStream(_body);
			return new ServletInputStream() {
				@Override
				public int read() {
					return in.read();
				}

				@Override
				public int read(byte[] buffer, int offset, int length) {
					return in.read(buffer, offset, length);
				}

				@Override
				public boolean isFinished() {
					return in.available() == 0;
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setReadListener(ReadListener listener) {
					throw new UnsupportedOperationException("Synchronous replay of a buffered body.");
				}
			};
		}
	}

}
