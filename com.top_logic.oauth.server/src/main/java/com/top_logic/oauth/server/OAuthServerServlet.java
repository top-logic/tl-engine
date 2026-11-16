/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationRequest;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationResponse;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.http.JakartaServletUtils;

import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;

/**
 * The public HTTP endpoints of the {@link OAuthAuthorizationServer}.
 *
 * <ul>
 * <li><code>GET</code> {@link OAuthAuthorizationServer#METADATA_PATH} and
 * {@link OAuthAuthorizationServer#OPENID_CONFIGURATION_PATH}: the authorization server metadata
 * (RFC 8414).</li>
 * <li><code>POST</code> {@link OAuthAuthorizationServer#TOKEN_PATH}: the token endpoint.</li>
 * <li><code>POST</code> {@link OAuthAuthorizationServer#REGISTRATION_PATH}: dynamic client
 * registration (RFC 7591), <code>404</code> when it is switched off.</li>
 * </ul>
 *
 * <p>
 * Requests and answers are read and written with the Nimbus OAuth SDK. Nothing here needs a
 * session: the token endpoint authenticates by the code or refresh token it is given, and runs in a
 * system interaction to look them up. All answers may be read by scripts of any origin, because no
 * answer depends on a cookie.
 * </p>
 */
public class OAuthServerServlet extends HttpServlet {

	/** The HTTP response header that allows reading a response from other origins. */
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

	/** The HTTP response header listing the methods a cross-origin request may use. */
	public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";

	/** The HTTP response header listing the headers a cross-origin request may send. */
	public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";

	/** Value of {@link #ACCESS_CONTROL_ALLOW_ORIGIN_HEADER} allowing every origin. */
	public static final String ANY_ORIGIN = "*";

	/** Value of {@link #ACCESS_CONTROL_ALLOW_METHODS_HEADER}. */
	public static final String ALLOWED_METHODS = "GET, POST, OPTIONS";

	/** Value of {@link #ACCESS_CONTROL_ALLOW_HEADERS_HEADER}. */
	public static final String ALLOWED_HEADERS = "Content-Type, Authorization";

	/** The HTTP response header controlling caching. */
	public static final String CACHE_CONTROL_HEADER = "Cache-Control";

	/** Value of {@link #CACHE_CONTROL_HEADER} for the metadata. */
	public static final String METADATA_CACHE_CONTROL = "public, max-age=3600";

	/** The media type of the metadata and of every answer of the endpoints. */
	public static final String JSON_MEDIA_TYPE = "application/json";

	/**
	 * The largest request body in bytes that is read; a token request or a client registration is
	 * a few hundred bytes.
	 */
	public static final long MAX_REQUEST_SIZE = 64 * 1024;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		String path = path(req);
		if (server == null || !isMetadataPath(path)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		allowAnyOrigin(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(JSON_MEDIA_TYPE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setHeader(CACHE_CONTROL_HEADER, METADATA_CACHE_CONTROL);
		resp.getWriter().write(server.metadata(DiscoveryContext.fromRequest(req)).toJSONObject().toJSONString());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		String path = path(req);
		if (server == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (OAuthAuthorizationServer.TOKEN_PATH.equals(path)) {
			allowAnyOrigin(resp);
			JakartaServletUtils.applyHTTPResponse(token(server, req).toHTTPResponse(), resp);
		} else if (OAuthAuthorizationServer.REGISTRATION_PATH.equals(path)
			&& server.getConfig().getDynamicClientRegistration()) {
			allowAnyOrigin(resp);
			JakartaServletUtils.applyHTTPResponse(register(server, req).toHTTPResponse(), resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
		// The preflight of a cross-origin request from a client running in a browser.
		allowAnyOrigin(resp);
		resp.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, ALLOWED_METHODS);
		resp.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, ALLOWED_HEADERS);
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	private static TokenResponse token(OAuthAuthorizationServer server, HttpServletRequest req) throws IOException {
		TokenRequest request;
		try {
			request = TokenRequest.parse(httpRequest(req));
		} catch (ParseException ex) {
			Logger.info("Refused a malformed token request: " + ex.getMessage(), OAuthServerServlet.class);
			return new TokenErrorResponse(errorObject(ex));
		}
		return ThreadContext.inSystemContext(OAuthServerServlet.class, () -> server.token(request));
	}

	private static ClientRegistrationResponse register(OAuthAuthorizationServer server, HttpServletRequest req)
			throws IOException {
		ClientRegistrationRequest request;
		try {
			request = ClientRegistrationRequest.parse(httpRequest(req));
		} catch (ParseException ex) {
			Logger.info("Refused a malformed client registration: " + ex.getMessage(), OAuthServerServlet.class);
			return new ClientRegistrationErrorResponse(errorObject(ex));
		}
		return ThreadContext.inSystemContext(OAuthServerServlet.class, () -> server.register(request));
	}

	private static HTTPRequest httpRequest(HttpServletRequest req) throws IOException {
		return JakartaServletUtils.createHTTPRequest(req, MAX_REQUEST_SIZE);
	}

	private static ErrorObject errorObject(ParseException ex) {
		ErrorObject error = ex.getErrorObject();
		if (error == null) {
			error = OAuth2Error.INVALID_REQUEST.setDescription(ex.getMessage());
		}
		return error.setHTTPStatusCode(HTTPResponse.SC_BAD_REQUEST);
	}

	private static boolean isMetadataPath(String path) {
		return OAuthAuthorizationServer.METADATA_PATH.equals(path)
			|| OAuthAuthorizationServer.OPENID_CONFIGURATION_PATH.equals(path);
	}

	private static void allowAnyOrigin(HttpServletResponse resp) {
		resp.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, ANY_ORIGIN);
	}

	private static String path(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		String pathInfo = req.getPathInfo();
		return (servletPath == null ? "" : servletPath) + (pathInfo == null ? "" : pathInfo);
	}

}
