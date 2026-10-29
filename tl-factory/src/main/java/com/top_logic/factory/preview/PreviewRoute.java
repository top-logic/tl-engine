/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The decisions of the {@link PreviewServlet preview route}: which workspace a request names,
 * where it is forwarded to, and which cookies pass in either direction.
 */
public final class PreviewRoute {

	private static final String PATH_SEPARATOR = "/";

	private static final char QUERY_SEPARATOR = '?';

	private static final String COOKIE_SEPARATOR = ";";

	private static final String COOKIE_JOIN = "; ";

	private static final char COOKIE_VALUE_SEPARATOR = '=';

	private static final String QUOTE = "\"";

	private static final String PATH_PARAMETER_SEPARATOR = ";";

	private static final String CURRENT_SEGMENT = ".";

	private static final String PARENT_SEGMENT = "..";

	private static final String BACKSLASH = "\\";

	/**
	 * Prefix that makes {@link HttpCookie#parse(String)} read a header value as the value of a
	 * {@code Set-Cookie} header, one cookie with its attributes.
	 */
	private static final String SET_COOKIE_PREFIX = "set-cookie:";

	private PreviewRoute() {
		// Static decisions only.
	}

	/**
	 * The identifier of the workspace a request names: the first segment of its path below the
	 * preview route.
	 *
	 * @param pathInfo
	 *        The path of the request below the route, e.g. {@code /my-app-1a2b3c4d/my-app/}.
	 * @return The identifier, {@code null} if the path names none.
	 */
	public static String workspaceId(String pathInfo) {
		if (pathInfo == null || !pathInfo.startsWith(PATH_SEPARATOR)) {
			return null;
		}
		int end = pathInfo.indexOf(PATH_SEPARATOR, PATH_SEPARATOR.length());
		String result = end < 0 ? pathInfo.substring(PATH_SEPARATOR.length())
			: pathInfo.substring(PATH_SEPARATOR.length(), end);
		return result.isEmpty() ? null : result;
	}

	/**
	 * The URL a request is forwarded to: the path and query of the request unchanged, at the
	 * origin of the application.
	 *
	 * <p>
	 * A request is forwarded only if its path - as it is sent and decoded - is the
	 * {@link PreviewTarget#contextPath() context path} of the application or below it, and has no
	 * segment {@code .} or {@code ..} (also with path parameters) and no backslash, so that no
	 * server resolves it to a path outside the application.
	 * </p>
	 *
	 * @param target
	 *        The application of the workspace the request names.
	 * @param requestUri
	 *        The path of the request as it is sent, not decoded, with the context path of the
	 *        control application.
	 * @param query
	 *        The query of the request, {@code null} if it has none.
	 * @return The URL to forward to, {@code null} if the request is not forwarded.
	 */
	public static URI target(PreviewTarget target, String requestUri, String query) {
		if (requestUri == null || requestUri.contains(BACKSLASH)) {
			return null;
		}
		String contextPath = target.contextPath();
		if (!isBelow(requestUri, contextPath)) {
			return null;
		}
		String decoded;
		URI result;
		try {
			decoded = URI.create(requestUri).getPath();
			result = URI.create(target.origin() + requestUri + (query == null ? "" : QUERY_SEPARATOR + query));
		} catch (IllegalArgumentException ex) {
			return null;
		}
		if (decoded == null || decoded.contains(BACKSLASH) || !isBelow(decoded, contextPath) || hasDotSegment(decoded)) {
			return null;
		}
		return result;
	}

	private static boolean hasDotSegment(String path) {
		for (String segment : path.split(PATH_SEPARATOR, -1)) {
			int parameters = segment.indexOf(PATH_PARAMETER_SEPARATOR);
			String name = parameters < 0 ? segment : segment.substring(0, parameters);
			if (name.equals(CURRENT_SEGMENT) || name.equals(PARENT_SEGMENT)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given path is the given context path or below it.
	 */
	public static boolean isBelow(String path, String contextPath) {
		return path.equals(contextPath) || path.startsWith(contextPath + PATH_SEPARATOR);
	}

	/**
	 * The cookies forwarded to the application of a workspace: all cookies of the request except
	 * the session cookie of the control application.
	 *
	 * <p>
	 * The browser sends the session cookie of the control application with every request below
	 * its context path, so also with the requests of the preview route; the application of the
	 * workspace never gets it. Its own session cookie may have the same name: the cookie of the
	 * control application is the one whose value is the session of the request in the control
	 * application.
	 * </p>
	 *
	 * @param cookieHeaders
	 *        The values of the {@code Cookie} headers of the request.
	 * @param sessionCookie
	 *        The name of the session cookie of the control application.
	 * @param sessionId
	 *        The session of the request in the control application as the client sent it,
	 *        {@code null} if it has none.
	 * @return The value of the forwarded {@code Cookie} header, {@code null} if no cookie is
	 *         forwarded.
	 */
	public static String forwardedCookies(List<String> cookieHeaders, String sessionCookie, String sessionId) {
		List<String> kept = new ArrayList<>();
		for (String header : cookieHeaders) {
			for (String part : header.split(COOKIE_SEPARATOR)) {
				String cookie = part.strip();
				if (cookie.isEmpty()) {
					continue;
				}
				int separator = cookie.indexOf(COOKIE_VALUE_SEPARATOR);
				String name = separator < 0 ? cookie : cookie.substring(0, separator).strip();
				String value = separator < 0 ? "" : unquote(cookie.substring(separator + 1).strip());
				if (sessionId != null && name.equalsIgnoreCase(sessionCookie) && value.equals(sessionId)) {
					continue;
				}
				kept.add(cookie);
			}
		}
		return kept.isEmpty() ? null : String.join(COOKIE_JOIN, kept);
	}

	private static String unquote(String value) {
		if (value.length() >= 2 * QUOTE.length() && value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
			return value.substring(QUOTE.length(), value.length() - QUOTE.length());
		}
		return value;
	}

	/**
	 * Whether a cookie the application of a workspace sets is passed to the browser: only a
	 * cookie confined to the context path of the application.
	 *
	 * <p>
	 * The cookie must not name a domain, and its path must be the context path of the application
	 * or below it; without a path, the browser takes the path of the request, which is below it.
	 * So the application can neither replace the cookies of the control application nor set
	 * cookies of the applications of other workspaces.
	 * </p>
	 *
	 * @param setCookie
	 *        The value of a {@code Set-Cookie} header of the answer of the application.
	 * @param contextPath
	 *        The context path of the application.
	 */
	public static boolean isConfinedCookie(String setCookie, String contextPath) {
		List<HttpCookie> cookies;
		try {
			cookies = HttpCookie.parse(SET_COOKIE_PREFIX + setCookie);
		} catch (IllegalArgumentException ex) {
			return false;
		}
		if (cookies.isEmpty()) {
			return false;
		}
		for (HttpCookie cookie : cookies) {
			if (cookie.getDomain() != null) {
				return false;
			}
			String path = cookie.getPath();
			if (path != null && !isBelow(stripSeparator(path), contextPath)) {
				return false;
			}
		}
		return true;
	}

	private static String stripSeparator(String path) {
		return path.length() > PATH_SEPARATOR.length() && path.endsWith(PATH_SEPARATOR)
			? path.substring(0, path.length() - PATH_SEPARATOR.length())
			: path;
	}

}
