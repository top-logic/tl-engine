/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.discovery;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.layout.servlet.CacheControlFilter;

import de.haumacher.msgbuf.json.JsonWriter;
import de.haumacher.msgbuf.server.io.WriterAdapter;

/**
 * The HTTP side of the {@link AgentDiscoveryService}.
 *
 * <ul>
 * <li><code>GET</code> {@link #LLMS_TXT_PATH}: the <code>llms.txt</code> document
 * ({@link #MARKDOWN_MEDIA_TYPE}).</li>
 * <li><code>GET</code> {@link #AI_CATALOG_PATH}: the AI Catalog
 * ({@link #AI_CATALOG_MEDIA_TYPE}).</li>
 * </ul>
 *
 * <p>
 * Both documents are public: they need no login, may be read from any origin and may be cached.
 * If the {@link AgentDiscoveryService} is not running, both answer
 * {@link HttpServletResponse#SC_NOT_FOUND}.
 * </p>
 */
public class AgentDiscoveryServlet extends HttpServlet {

	/**
	 * The context-relative path of the <code>llms.txt</code> document.
	 */
	public static final String LLMS_TXT_PATH = "/llms.txt";

	/**
	 * The context-relative path of the AI Catalog.
	 */
	public static final String AI_CATALOG_PATH = "/.well-known/ai-catalog.json";

	/**
	 * The media type of the AI Catalog.
	 */
	public static final String AI_CATALOG_MEDIA_TYPE = "application/ai-catalog+json";

	/**
	 * The media type of the <code>llms.txt</code> document.
	 */
	public static final String MARKDOWN_MEDIA_TYPE = "text/markdown";

	/**
	 * The link relation that points from a page of the application to its AI Catalog.
	 */
	public static final String AI_CATALOG_LINK_RELATION = "ai-catalog";

	/**
	 * The HTTP response header carrying typed links of a resource, e.g. to the AI Catalog with
	 * relation {@link #AI_CATALOG_LINK_RELATION}.
	 */
	public static final String LINK_HEADER = "Link";

	/**
	 * The HTTP response header that allows reading a response from other origins.
	 */
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

	/**
	 * Value of {@link #ACCESS_CONTROL_ALLOW_ORIGIN_HEADER} allowing every origin.
	 */
	public static final String ANY_ORIGIN = "*";

	/**
	 * Value of the {@link CacheControlFilter#CACHE_CONTROL_HEADER} of both documents.
	 */
	public static final String CACHE_CONTROL = "public, max-age=3600";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		AgentDiscoveryService service = AgentDiscoveryService.getInstanceOrNull();
		String path = path(req);
		boolean llmsTxt = LLMS_TXT_PATH.equals(path);
		boolean catalog = AI_CATALOG_PATH.equals(path);
		if (service == null || !(llmsTxt || catalog)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		DiscoveryContext context = DiscoveryContext.fromRequest(req);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ANY_ORIGIN);
		resp.setHeader(CacheControlFilter.CACHE_CONTROL_HEADER, CACHE_CONTROL);
		if (llmsTxt) {
			resp.setContentType(MARKDOWN_MEDIA_TYPE);
			PrintWriter out = resp.getWriter();
			service.writeLlmsTxt(context, out);
			out.flush();
		} else {
			resp.setContentType(AI_CATALOG_MEDIA_TYPE);
			try (JsonWriter json = new JsonWriter(new WriterAdapter(resp.getWriter()))) {
				service.catalog(context).writeTo(json);
			}
		}
	}

	private static String path(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		String pathInfo = req.getPathInfo();
		return (servletPath == null ? "" : servletPath) + (pathInfo == null ? "" : pathInfo);
	}

}
