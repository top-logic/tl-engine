/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A minimal stand-in for the servlet container, enough to drive the servlets of this package
 * directly from a unit test.
 *
 * <p>
 * Only the handful of methods the servlets actually call is implemented. Everything else answers
 * <code>null</code>, which keeps the stubs from having to cover the full servlet API. A call a
 * servlet starts to make and that is not listed here therefore fails loudly rather than silently
 * doing something plausible.
 * </p>
 */
class ServletStubs {

	private ServletStubs() {
		// Static utility.
	}

	/**
	 * A {@link ServletContext} that resolves web application resources against a directory in the
	 * file system.
	 */
	static class Webapp {

		private static final Map<String, String> MIME_TYPES = new HashMap<>();
		static {
			MIME_TYPES.put("html", "text/html");
			MIME_TYPES.put("js", "text/javascript");
			MIME_TYPES.put("mjs", "text/javascript");
		}

		private final File _root;

		/**
		 * Creates a {@link Webapp} serving the given directory as the web application root.
		 */
		Webapp(File root) {
			_root = root;
		}

		/**
		 * Adds a web application resource with the given content.
		 */
		void add(String resourcePath, String content) throws IOException {
			File file = resolve(resourcePath);
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
		}

		/**
		 * The {@link ServletContext} view of this web application.
		 */
		ServletContext asContext() {
			return proxy(ServletContext.class, (method, args) -> {
				switch (method.getName()) {
					case "getResource":
						return getResource((String) args[0]);
					case "getResourcePaths":
						return getResourcePaths((String) args[0]);
					case "getMimeType":
						return getMimeType((String) args[0]);
					default:
						return null;
				}
			});
		}

		private URL getResource(String path) throws IOException {
			File file = resolve(path);
			return file.exists() ? file.toURI().toURL() : null;
		}

		private Set<String> getResourcePaths(String path) {
			File[] children = resolve(path).listFiles();
			if (children == null) {
				return null;
			}
			Set<String> result = new LinkedHashSet<>();
			for (File child : children) {
				result.add(path + child.getName() + (child.isDirectory() ? "/" : ""));
			}
			return result;
		}

		private static String getMimeType(String path) {
			int dot = path.lastIndexOf('.');
			return dot < 0 ? null : MIME_TYPES.get(path.substring(dot + 1));
		}

		/**
		 * Resolves like a servlet container: strictly relative to the web application root, never
		 * outside it.
		 */
		private File resolve(String path) {
			return new File(_root, path.startsWith("/") ? path.substring(1) : path);
		}
	}

	/**
	 * Records what a servlet wrote to its {@link HttpServletResponse}.
	 */
	static class Response {

		/** Headers set through {@link HttpServletResponse#setDateHeader(String, long)}. */
		final Map<String, Long> _dateHeaders = new HashMap<>();

		/** Headers set through {@link HttpServletResponse#setHeader(String, String)}. */
		final Map<String, String> _headers = new HashMap<>();

		/** The content type, or <code>null</code> if the servlet did not set one. */
		String _contentType;

		/** The content length, or {@code -1} if the servlet did not set one. */
		long _contentLength = -1;

		private int _status = HttpServletResponse.SC_OK;

		private final Buffer _out = new Buffer();

		/**
		 * The status the servlet answered with; {@link HttpServletResponse#SC_OK} unless it set
		 * one.
		 */
		int status() {
			return _status;
		}

		/**
		 * The delivered content, decoded as UTF-8.
		 */
		String content() {
			return new String(_out._buffer.toByteArray(), StandardCharsets.UTF_8);
		}

		/**
		 * The {@link HttpServletResponse} view to hand to a servlet.
		 */
		HttpServletResponse asResponse() {
			return proxy(HttpServletResponse.class, (method, args) -> {
				switch (method.getName()) {
					case "sendError":
					case "setStatus":
						_status = ((Integer) args[0]).intValue();
						return null;
					case "setContentType":
						_contentType = (String) args[0];
						return null;
					case "setContentLengthLong":
						_contentLength = ((Long) args[0]).longValue();
						return null;
					case "setDateHeader":
						_dateHeaders.put((String) args[0], (Long) args[1]);
						return null;
					case "setHeader":
						_headers.put((String) args[0], (String) args[1]);
						return null;
					case "getOutputStream":
						return _out;
					default:
						return null;
				}
			});
		}
	}

	/**
	 * A {@code GET} request with the given {@link HttpServletRequest#getPathInfo() path info}.
	 */
	static HttpServletRequest request(String pathInfo) {
		return request(pathInfo, -1);
	}

	/**
	 * A {@code GET} request that carries the given {@code If-Modified-Since} header.
	 *
	 * @param ifModifiedSince
	 *        The header value, or {@code -1} for a request without the header.
	 */
	static HttpServletRequest request(String pathInfo, long ifModifiedSince) {
		return proxy(HttpServletRequest.class, (method, args) -> {
			switch (method.getName()) {
				case "getPathInfo":
					return pathInfo;
				case "getDateHeader":
					return Long.valueOf(ifModifiedSince);
				default:
					return null;
			}
		});
	}

	/**
	 * A {@link ServletConfig} handing out the given {@link ServletContext}.
	 */
	static ServletConfig config(ServletContext context, String servletName) {
		return proxy(ServletConfig.class, (method, args) -> {
			switch (method.getName()) {
				case "getServletContext":
					return context;
				case "getServletName":
					return servletName;
				case "getInitParameterNames":
					return Collections.enumeration(Collections.<String> emptyList());
				default:
					return null;
			}
		});
	}

	/**
	 * Deletes a directory tree created for a test.
	 */
	static void deleteRecursively(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteRecursively(child);
			}
		}
		file.delete();
	}

	private static final class Buffer extends ServletOutputStream {

		final ByteArrayOutputStream _buffer = new ByteArrayOutputStream();

		@Override
		public void write(int b) {
			_buffer.write(b);
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener listener) {
			throw new UnsupportedOperationException("Delivery is synchronous.");
		}
	}

	/**
	 * Dispatches the calls a stub cares about by method name.
	 */
	private interface Dispatch {

		Object invoke(Method method, Object[] args) throws Exception;
	}

	private static <T> T proxy(Class<T> type, Dispatch dispatch) {
		InvocationHandler handler = (instance, method, args) -> dispatch.invoke(method, args);
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}
}
