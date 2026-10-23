/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.net.HttpURLConnection;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer.McpHttpClientAuthorizationErrorHandler;
import io.modelcontextprotocol.spec.McpClientTransport;

/**
 * {@link McpTransportConfig} creating an MCP HTTP transport (Streamable HTTP).
 *
 * <p>
 * The MCP server runs as an external HTTP service. Each request is authenticated by the configured
 * {@link Config#getCredentials() credentials} at the moment it is sent, e.g. with a bearer token
 * read from an environment variable:
 * </p>
 *
 * <pre>
 * &lt;transport class="com.top_logic.tl.ai.core.mcp.HttpMcpTransport" url="https://mcp.example.com/mcp"&gt;
 *   &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer"&gt;
 *     &lt;secret class="com.top_logic.basic.credential.EnvironmentSecretSource" name="MY_MCP_TOKEN"/&gt;
 *   &lt;/credentials&gt;
 * &lt;/transport&gt;
 * </pre>
 *
 * <p>
 * When the server rejects the credentials (HTTP 401), they are invalidated and the request is sent
 * once more with fresh ones.
 * </p>
 */
@InApp
public class HttpMcpTransport implements McpTransportConfig {

	/**
	 * Configuration for {@link HttpMcpTransport}.
	 */
	public interface Config extends PolymorphicConfiguration<HttpMcpTransport> {

		/** Configuration property name for {@link #getUrl()}. */
		String URL = "url";

		/** Configuration property name for {@link #getHeaders()}. */
		String HEADERS = "headers";

		/** Configuration property name for {@link #getCredentials()}. */
		String CREDENTIALS = "credentials";

		/**
		 * The URL of the MCP server endpoint (e.g., {@code "https://mcp.example.com/mcp"}).
		 */
		@Name(URL)
		String getUrl();

		/**
		 * Static HTTP headers sent with every request.
		 *
		 * <p>
		 * Each entry is a header name and the value to send under it, e.g. a routing header the
		 * server expects in addition to the credentials.
		 * </p>
		 */
		@Name(HEADERS)
		@Key(NamedValueConfig.NAME_ATTRIBUTE)
		@EntryTag("entry")
		Map<String, NamedValueConfig> getHeaders();

		/**
		 * How the requests to the MCP server are authenticated.
		 *
		 * <p>
		 * E.g. a bearer token from an environment variable or a file, or an access token from an
		 * OAuth2 authorization server. If not set, the requests are sent without credentials.
		 * </p>
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

	}

	private final String _url;

	private final Map<String, NamedValueConfig> _headers;

	private final CredentialProvider _credentials;

	/**
	 * Creates a new {@link HttpMcpTransport} from configuration.
	 */
	@CalledByReflection
	public HttpMcpTransport(InstantiationContext context, Config config) {
		_url = config.getUrl();
		_headers = config.getHeaders();
		_credentials = CredentialProvider.create(context, _url, config.getCredentials(), null, null, null);
	}

	@Override
	public McpClientTransport createTransport() {
		return HttpClientStreamableHttpTransport.builder(_url)
			.httpRequestCustomizer((request, method, uri, body, context) -> {
				if (_headers != null) {
					for (NamedValueConfig entry : _headers.values()) {
						request.header(entry.getName(), entry.getValue());
					}
				}
				_credentials.authenticate(RequestAuthentication.of(request));
			})
			.authorizationErrorHandler(McpHttpClientAuthorizationErrorHandler.fromSync(
				(response, context) -> {
					if (response.statusCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
						return false;
					}
					// Cached credentials (an access token) may have expired or been revoked: one
					// more attempt with fresh ones.
					_credentials.invalidate();
					return true;
				}))
			.build();
	}

}
