/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.ai.mcp.server.authentication.McpAuthenticationFilter;
import com.top_logic.base.discovery.AgentDiscoveryServlet;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.layout.servlet.CacheControlFilter;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * The HTTP side of the {@link McpServerCard server card}: <code>GET</code> {@link McpServerCard#PATH}
 * and the CORS preflight <code>OPTIONS</code> of it.
 *
 * <p>
 * The card is public: the {@link McpAuthenticationFilter} lets exactly these two requests through
 * without a credential ({@link #isPublicRequest(HttpServletRequest)}), any origin may read it, and
 * it may be cached. A <code>GET</code> carrying the {@link #ETAG_HEADER} of the current card in
 * {@link #IF_NONE_MATCH_HEADER} is answered {@link HttpServletResponse#SC_NOT_MODIFIED}.
 * </p>
 */
public final class ServerCardResource {

	/** The HTTP method that reads the card. */
	public static final String GET = "GET";

	/** The HTTP method of a CORS preflight request. */
	public static final String OPTIONS = "OPTIONS";

	/** The response header with the entity tag of the card. */
	public static final String ETAG_HEADER = "ETag";

	/** The request header with the entity tags a client already has. */
	public static final String IF_NONE_MATCH_HEADER = "If-None-Match";

	/** The response header listing the methods another origin may use. */
	public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";

	/** The response header listing the request headers another origin may send. */
	public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";

	/** The response header listing the response headers another origin may read. */
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";

	/** Value of {@link #ACCESS_CONTROL_ALLOW_HEADERS_HEADER}. */
	public static final String ALLOWED_HEADERS = "Content-Type, " + IF_NONE_MATCH_HEADER;

	/** The entity tag that matches every entity in {@link #IF_NONE_MATCH_HEADER}. */
	private static final String ANY_TAG = "*";

	/** The prefix of a weak entity tag. */
	private static final String WEAK_PREFIX = "W/";

	private ServerCardResource() {
		// Static utility.
	}

	/**
	 * Whether the given request is one for the public server card, which needs no credential.
	 *
	 * <p>
	 * Only <code>GET</code> and <code>OPTIONS</code> of exactly {@link McpServerCard#PATH}; any other
	 * method or path below the endpoint is guarded like the endpoint.
	 * </p>
	 */
	public static boolean isPublicRequest(HttpServletRequest req) {
		if (!McpServerCard.PATH_INFO.equals(req.getPathInfo())) {
			return false;
		}
		String method = req.getMethod();
		return GET.equals(method) || OPTIONS.equals(method);
	}

	/**
	 * Answers a {@link #isPublicRequest(HttpServletRequest) public request} for the card of the
	 * given service.
	 */
	public static void serve(MCPServerService service, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		answer(req, resp, () -> service.getServerCard(DiscoveryContext.fromRequest(req)));
	}

	/**
	 * Answers a {@link #isPublicRequest(HttpServletRequest) public request} for the given card.
	 *
	 * @param card
	 *        Builds the card; called for a <code>GET</code> only.
	 */
	static void answer(HttpServletRequest req, HttpServletResponse resp, Supplier<ServerCard> card)
			throws IOException {
		setCorsHeaders(resp);
		if (OPTIONS.equals(req.getMethod())) {
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}

		byte[] body = json(card.get());
		String etag = etag(body);
		resp.setHeader(ETAG_HEADER, etag);
		resp.setHeader(CacheControlFilter.CACHE_CONTROL_HEADER, AgentDiscoveryServlet.CACHE_CONTROL);
		if (matches(req.getHeader(IF_NONE_MATCH_HEADER), etag)) {
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(McpServerCard.MEDIA_TYPE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setContentLength(body.length);
		OutputStream out = resp.getOutputStream();
		out.write(body);
		out.flush();
	}

	private static void setCorsHeaders(HttpServletResponse resp) {
		resp.setHeader(AgentDiscoveryServlet.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, AgentDiscoveryServlet.ANY_ORIGIN);
		resp.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, GET);
		resp.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, ALLOWED_HEADERS);
		resp.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, ETAG_HEADER);
	}

	/**
	 * The card as UTF-8 encoded JSON.
	 */
	static byte[] json(ServerCard card) throws IOException {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			card.writeTo(json);
		}
		return buffer.toString().getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * The strong entity tag of the given body: a hash of it, quoted.
	 */
	static String etag(byte[] body) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(body);
			return '"' + HexFormat.of().formatHex(hash, 0, 16) + '"';
		} catch (NoSuchAlgorithmException ex) {
			// Every Java platform supports SHA-256.
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Whether the given {@link #IF_NONE_MATCH_HEADER} value names the given entity tag.
	 *
	 * <p>
	 * The comparison is weak, as RFC 9110 requires for {@link #IF_NONE_MATCH_HEADER}.
	 * </p>
	 */
	static boolean matches(String ifNoneMatch, String etag) {
		if (ifNoneMatch == null) {
			return false;
		}
		for (String tag : ifNoneMatch.split(",")) {
			String candidate = tag.strip();
			if (ANY_TAG.equals(candidate)) {
				return true;
			}
			if (candidate.startsWith(WEAK_PREFIX)) {
				candidate = candidate.substring(WEAK_PREFIX.length());
			}
			if (candidate.equals(etag)) {
				return true;
			}
		}
		return false;
	}

}
