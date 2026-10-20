/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.io.StreamUtilities;

/**
 * Delivers the static PDF.js viewer resources below {@code /html/pdfjs/*} straight out of the web
 * application.
 *
 * <p>
 * {@link GeneratedHtmlPageServlet} claims {@code /html/*} for the generated document route. A
 * servlet mapping wins over the container's handling of static web resources, so without this
 * servlet every request below {@code /html/pdfjs/} - the viewer shipped by
 * {@code ext-org-mozilla-pdfjs} and embedded in an iframe by the React PDF viewer control - would
 * reach the document servlet, which does not know the path and answers with the application's
 * "Requested resource not found" page. This servlet re-establishes static delivery for that
 * sub-tree by claiming the more specific prefix {@code /html/pdfjs/*}, which the container prefers
 * over {@code /html/*}, and reading the resources back through
 * {@link ServletContext#getResource(String)}.
 * </p>
 *
 * <p>
 * The viewer resources are not shipped by this module and the lookup goes through the servlet
 * container only, so no dependency on the React stack is created. In an application that does not
 * deploy them, nothing is found below the prefix and every request is answered with a plain
 * {@link HttpServletResponse#SC_NOT_FOUND} - the same answer the container would have given if this
 * module had never claimed {@code /html/*}.
 * </p>
 *
 * <p>
 * This is a workaround, not the final shape. Where the viewer lives is an implementation detail of
 * the React PDF viewer control, and moving it out of the {@code /html/} name space is a change to
 * the engine. Until that happens, the module that took {@code /html/*} yields back the sub-path it
 * does not own.
 * </p>
 */
public class PdfJsResourceServlet extends HttpServlet {

	/**
	 * Prefix this servlet is mapped to in {@code web-fragment.xml}, and at the same time the root
	 * of the web application resources it delivers.
	 *
	 * <p>
	 * Hard-coded rather than derived from {@link HttpServletRequest#getServletPath()} so that a
	 * mapping mistake can never turn this servlet into a reader for arbitrary web application
	 * resources such as {@code /WEB-INF/conf/}.
	 * </p>
	 */
	static final String RESOURCE_PREFIX = "/html/pdfjs";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String resourcePath = resourcePath(req.getPathInfo());
		if (resourcePath == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		ServletContext context = getServletContext();
		URL resource = context.getResource(resourcePath);
		if (resource == null || isDirectory(context, resourcePath)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		URLConnection connection = resource.openConnection();
		// Prevent the JVM from keeping the underlying JAR open when the resource is delivered from
		// a packed web fragment.
		connection.setUseCaches(false);
		try (InputStream in = connection.getInputStream()) {
			long lastModified = connection.getLastModified();
			if (isUnmodified(req, lastModified)) {
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			resp.setContentType(contentType(context, resourcePath));
			long length = connection.getContentLengthLong();
			if (length >= 0) {
				resp.setContentLengthLong(length);
			}
			if (lastModified > 0) {
				resp.setDateHeader("Last-Modified", lastModified);
			}
			StreamUtilities.copyStreamContents(in, resp.getOutputStream());
		}
	}

	/**
	 * The content type to announce for the given resource.
	 *
	 * <p>
	 * The viewer ships file types the container has no mapping for ({@code .bcmap} character maps,
	 * {@code .ftl} translations, the license text). They are all fetched by the viewer's own code
	 * rather than loaded as a script or a style sheet, so the generic default is enough - and
	 * delivering a body without saying what it is would be worse.
	 * </p>
	 */
	private static String contentType(ServletContext context, String resourcePath) {
		String mimeType = context.getMimeType(resourcePath);
		return mimeType != null ? mimeType : "application/octet-stream";
	}

	/**
	 * Resolves the path info of a request to the web application resource it addresses.
	 *
	 * @param pathInfo
	 *        The {@link HttpServletRequest#getPathInfo()} of the request, may be <code>null</code>.
	 * @return The absolute resource path within the web application, or <code>null</code> if the
	 *         request does not address a file below {@link #RESOURCE_PREFIX}.
	 */
	static String resourcePath(String pathInfo) {
		if (pathInfo == null || !pathInfo.startsWith("/")) {
			return null;
		}
		// The first segment is empty by construction, since the path info starts with a slash.
		String[] segments = pathInfo.split("/", -1);
		for (int n = 1; n < segments.length; n++) {
			if (!isSafeSegment(segments[n])) {
				return null;
			}
		}
		return RESOURCE_PREFIX + pathInfo;
	}

	/**
	 * Whether the given path segment may be resolved against the web application.
	 *
	 * <p>
	 * The container normalizes and rejects escaping requests before dispatching, but a resource
	 * lookup below {@code /} could reach configuration and credentials, so the check is repeated
	 * here. An empty segment is rejected as well: it means a doubled or a trailing slash, and a
	 * trailing slash addresses a directory, which is nothing this servlet delivers.
	 * </p>
	 */
	private static boolean isSafeSegment(String segment) {
		if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
			return false;
		}
		return segment.indexOf('\\') < 0 && segment.indexOf('\0') < 0;
	}

	/**
	 * Whether the given resource path addresses a directory rather than a file.
	 */
	private static boolean isDirectory(ServletContext context, String resourcePath) {
		Set<String> children = context.getResourcePaths(resourcePath + "/");
		return children != null && !children.isEmpty();
	}

	/**
	 * Whether the client already holds the current version of the resource.
	 *
	 * @param lastModified
	 *        Modification time of the resource, or {@code 0} if the source cannot tell.
	 */
	private static boolean isUnmodified(HttpServletRequest req, long lastModified) {
		if (lastModified <= 0) {
			return false;
		}
		long ifModifiedSince;
		try {
			ifModifiedSince = req.getDateHeader("If-Modified-Since");
		} catch (IllegalArgumentException ex) {
			// Unparsable date: deliver the resource instead of failing the request.
			return false;
		}
		// HTTP dates have second granularity, the resource time stamp has not.
		return ifModifiedSince >= 0 && lastModified / 1000 * 1000 <= ifModifiedSince;
	}
}
