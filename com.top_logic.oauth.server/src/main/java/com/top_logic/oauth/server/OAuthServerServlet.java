/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.AuthorizationErrorResponse;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
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
import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * The public HTTP endpoints of the {@link OAuthAuthorizationServer}.
 *
 * <ul>
 * <li><code>GET</code> {@link OAuthAuthorizationServer#METADATA_PATH} and
 * {@link OAuthAuthorizationServer#OPENID_CONFIGURATION_PATH}: the authorization server metadata
 * (RFC 8414).</li>
 * <li><code>GET</code> {@link ProtectedResources#METADATA_PATH} followed by the path of a resource:
 * the protected resource metadata (RFC 9728) of a resource whose tokens an endpoint accepts, see
 * {@link ProtectedResources}; <code>404</code> for any other resource.</li>
 * <li><code>GET</code> {@link OAuthAuthorizationServer#AUTHORIZATION_PATH}: the authorization
 * endpoint, which sends the browser to the consent page.</li>
 * <li><code>POST</code> {@link OAuthAuthorizationServer#TOKEN_PATH}: the token endpoint.</li>
 * <li><code>POST</code> {@link OAuthAuthorizationServer#REGISTRATION_PATH}: dynamic client
 * registration (RFC 7591), <code>404</code> when it is switched off.</li>
 * </ul>
 *
 * <p>
 * Requests and answers are read and written with the Nimbus OAuth SDK. Nothing here needs a
 * session: the token endpoint authenticates by the code or refresh token it is given, and runs in a
 * system interaction to look them up; the authorization endpoint binds the request to nobody, the
 * person decides on the consent page. The answers of the metadata, token and registration
 * endpoints may be read by scripts of any origin, because no answer depends on a cookie.
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

	/** The media type of the page showing a refused authorization request. */
	public static final String HTML_MEDIA_TYPE = "text/html";

	/** Value of {@link #CACHE_CONTROL_HEADER} for the answers of the authorization endpoint. */
	public static final String NO_STORE = "no-store";

	/**
	 * The largest request body in bytes that is read; a token request or a client registration is
	 * a few hundred bytes.
	 */
	public static final long MAX_REQUEST_SIZE = 64 * 1024;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		String path = path(req);
		if (server != null && OAuthAuthorizationServer.AUTHORIZATION_PATH.equals(path)) {
			authorize(server, req, resp);
			return;
		}
		if (server != null && isProtectedResourceMetadataPath(path)) {
			protectedResourceMetadata(path.substring(ProtectedResources.METADATA_PATH.length()), req, resp);
			return;
		}
		if (server == null || !isMetadataPath(path)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		writeMetadata(resp, server.metadata(DiscoveryContext.fromRequest(req)).toJSONObject().toJSONString());
	}

	/**
	 * Answers the protected resource metadata of the resource whose metadata path ends with the
	 * given suffix, see {@link ProtectedResources#lookup(String)}.
	 */
	private static void protectedResourceMetadata(String suffix, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String resource = ProtectedResources.lookup(suffix);
		if (resource == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		ProtectedResourceMetadata metadata = ProtectedResources.metadata(resource, DiscoveryContext.fromRequest(req));
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			metadata.writeTo(json);
		}
		writeMetadata(resp, buffer.toString());
	}

	private static void writeMetadata(HttpServletResponse resp, String json) throws IOException {
		allowAnyOrigin(resp);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(JSON_MEDIA_TYPE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resp.setHeader(CACHE_CONTROL_HEADER, METADATA_CACHE_CONTROL);
		resp.getWriter().write(json);
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

	/**
	 * The authorization endpoint: checks the request and sends the browser to the consent page.
	 *
	 * <p>
	 * A request whose client or redirect URI is not known to be valid is answered with a page
	 * naming the problem ({@link HttpServletResponse#SC_BAD_REQUEST}), never with a redirect. Any other refusal goes
	 * back to the redirect URI of the client with the error and the state. An accepted request is
	 * kept as pending authorization, and the browser is sent to the
	 * {@link OAuthAuthorizationServer.Config#getConsentPage() consent page} with its identifier in
	 * the parameter {@link OAuthAuthorizationServer#PENDING_PARAMETER}.
	 * </p>
	 */
	private static void authorize(OAuthAuthorizationServer server, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String consentPage = server.getConfig().getConsentPage();
		if (consentPage == null) {
			Logger.warn("An authorization request was refused: the authorization server has no consent page.",
				OAuthServerServlet.class);
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		resp.setHeader(CACHE_CONTROL_HEADER, NO_STORE);

		AuthorizationRequest request;
		try {
			request = AuthorizationRequest.parse(parameters(req));
		} catch (ParseException ex) {
			Logger.info("Refused a malformed authorization request: " + ex.getMessage(), OAuthServerServlet.class);
			refuseMalformed(server, ex, resp);
			return;
		}

		DiscoveryContext context = DiscoveryContext.fromRequest(req);
		String pendingId;
		try {
			pendingId = ThreadContext.inSystemContext(OAuthServerServlet.class,
				() -> server.createPendingAuthorization(server.validateAuthorizationRequest(request, context)));
		} catch (OAuthException ex) {
			Logger.info("Refused an authorization request: " + ex.description(), OAuthServerServlet.class);
			if (ex.isRedirectable()) {
				resp.sendRedirect(new AuthorizationErrorResponse(request.getRedirectionURI(), ex.toErrorObject(),
					request.getState(), request.getResponseMode()).toURI().toString());
			} else {
				writeRefusal(resp, ex.description());
			}
			return;
		}
		resp.sendRedirect(consentUrl(req.getContextPath(), consentPage, pendingId));
	}

	/**
	 * The URL of the consent page for the given pending authorization.
	 *
	 * @param contextPath
	 *        The context path of the application.
	 * @param consentPage
	 *        The {@link OAuthAuthorizationServer.Config#getConsentPage() consent page}, relative to
	 *        the context path.
	 * @param pendingId
	 *        The identifier of the pending authorization.
	 */
	public static String consentUrl(String contextPath, String consentPage, String pendingId) {
		char separator = consentPage.indexOf('?') < 0 ? '?' : '&';
		return contextPath + consentPage + separator + OAuthAuthorizationServer.PENDING_PARAMETER + '='
			+ URLEncoder.encode(pendingId, StandardCharsets.UTF_8);
	}

	/**
	 * Answers an authorization request that cannot be parsed.
	 *
	 * <p>
	 * The error goes back to the client only if the request names a known client and one of its
	 * redirect URIs; otherwise the page shows it.
	 * </p>
	 */
	private static void refuseMalformed(OAuthAuthorizationServer server, ParseException ex,
			HttpServletResponse resp) throws IOException {
		URI redirectUri = ex.getRedirectionURI();
		ClientID clientId = ex.getClientID();
		if (redirectUri != null && clientId != null && isRegisteredRedirect(server, clientId, redirectUri)) {
			resp.sendRedirect(new AuthorizationErrorResponse(redirectUri, errorObject(ex), ex.getState(),
				ex.getResponseMode()).toURI().toString());
			return;
		}
		writeRefusal(resp, OAuthException.wireDescription(String.valueOf(ex.getMessage())));
	}

	private static boolean isRegisteredRedirect(OAuthAuthorizationServer server, ClientID clientId, URI redirectUri) {
		try {
			OAuthClientInfo client = ThreadContext.inSystemContext(OAuthServerServlet.class,
				() -> server.resolveClient(clientId.getValue()));
			return RedirectUris.matches(client.redirectUris(), redirectUri);
		} catch (OAuthException ex) {
			return false;
		}
	}

	/**
	 * Writes the page telling the person that an authorization request is refused, with status
	 * {@link HttpServletResponse#SC_BAD_REQUEST}.
	 */
	private static void writeRefusal(HttpServletResponse resp, String reason) throws IOException {
		String title = Resources.getLogInstance().getString(I18NConstants.AUTHORIZATION_REFUSED_TITLE);
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		resp.setContentType(HTML_MEDIA_TYPE);
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
		TagWriter out = new TagWriter(resp.getWriter());
		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginTag(HTMLConstants.HTML);
		out.beginTag(HTMLConstants.HEAD);
		out.beginTag(HTMLConstants.TITLE);
		out.writeText(title);
		out.endTag(HTMLConstants.TITLE);
		out.endTag(HTMLConstants.HEAD);
		out.beginTag(HTMLConstants.BODY);
		out.beginTag(HTMLConstants.H1);
		out.writeText(title);
		out.endTag(HTMLConstants.H1);
		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText(reason);
		out.endTag(HTMLConstants.PARAGRAPH);
		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);
		out.flushBuffer();
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

	/**
	 * The parameters of a request, each with all its values.
	 */
	private static Map<String, List<String>> parameters(HttpServletRequest req) {
		Map<String, List<String>> result = new LinkedHashMap<>();
		for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
			result.put(entry.getKey(), List.of(entry.getValue()));
		}
		return result;
	}

	private static boolean isMetadataPath(String path) {
		return OAuthAuthorizationServer.METADATA_PATH.equals(path)
			|| OAuthAuthorizationServer.OPENID_CONFIGURATION_PATH.equals(path);
	}

	private static boolean isProtectedResourceMetadataPath(String path) {
		return path.equals(ProtectedResources.METADATA_PATH)
			|| path.startsWith(ProtectedResources.METADATA_PATH + '/');
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
