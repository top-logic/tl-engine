/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;

/**
 * {@link McpTransportConfig} creating an MCP HTTP transport (Streamable HTTP).
 *
 * <p>
 * The MCP server runs as an external HTTP service. Authentication is handled via static headers
 * or a bearer token resolved from an environment variable at connect time.
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

		/** Configuration property name for {@link #getBearerTokenEnvVar()}. */
		String BEARER_TOKEN_ENV_VAR = "bearer-token-env-var";

		/**
		 * The URL of the MCP server endpoint (e.g., {@code "https://mcp.example.com/mcp"}).
		 */
		@Name(URL)
		String getUrl();

		/**
		 * Static HTTP headers sent with every request.
		 *
		 * <p>
		 * In XML: {@code <entry name="X-Api-Key" value="secret"/>}
		 * </p>
		 */
		@Name(HEADERS)
		@Key(NamedValueConfig.NAME_ATTRIBUTE)
		@EntryTag("entry")
		Map<String, NamedValueConfig> getHeaders();

		/**
		 * Name of an environment variable holding a bearer token.
		 *
		 * <p>
		 * Resolved via {@code System.getenv()} at connect time. The token is sent as
		 * {@code Authorization: Bearer <token>} header. This avoids putting secrets in config files.
		 * </p>
		 */
		@Name(BEARER_TOKEN_ENV_VAR)
		String getBearerTokenEnvVar();

	}

	private final String _url;

	private final Map<String, NamedValueConfig> _headers;

	private final String _bearerTokenEnvVar;

	/**
	 * Creates a new {@link HttpMcpTransport} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public HttpMcpTransport(InstantiationContext context, Config config) {
		_url = config.getUrl();
		_headers = config.getHeaders();
		_bearerTokenEnvVar = config.getBearerTokenEnvVar();
	}

	@Override
	public McpClientTransport createTransport() {
		HttpClientStreamableHttpTransport.Builder builder =
			HttpClientStreamableHttpTransport.builder(_url);

		String bearerToken = null;
		if (_bearerTokenEnvVar != null && !_bearerTokenEnvVar.isEmpty()) {
			bearerToken = System.getenv(_bearerTokenEnvVar);
			if (bearerToken == null || bearerToken.isEmpty()) {
				Logger.warn("Bearer token env var '" + _bearerTokenEnvVar
					+ "' is not set for HTTP MCP transport.",
					HttpMcpTransport.class);
				bearerToken = null;
			}
		}

		if (bearerToken != null || (_headers != null && !_headers.isEmpty())) {
			String token = bearerToken;
			builder.httpRequestCustomizer((req, method, uri, body, ctx) -> {
				if (token != null) {
					req.header("Authorization", "Bearer " + token);
				}
				if (_headers != null) {
					for (NamedValueConfig entry : _headers.values()) {
						req.header(entry.getName(), entry.getValue());
					}
				}
			});
		}

		return builder.build();
	}

}
