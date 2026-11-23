/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.mcp;

import java.net.http.HttpRequest;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.credential.CredentialProvider;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.spec.McpClientTransport;

/**
 * {@link McpTransportConfig} creating an MCP HTTP transport (Streamable HTTP).
 *
 * <p>
 * The MCP server runs as an external HTTP service. Each call of the client is authenticated by the
 * configured {@link Config#getCredentials() credentials} at the moment it is made, e.g. with a bearer
 * token read from an environment variable:
 * </p>
 *
 * <pre>
 * &lt;transport class="com.top_logic.ai.core.mcp.HttpMcpTransport" url="https://mcp.example.com/mcp"&gt;
 *   &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer"&gt;
 *     &lt;secret name="MY_MCP_TOKEN"/&gt;
 *   &lt;/credentials&gt;
 * &lt;/transport&gt;
 * </pre>
 *
 * <p>
 * When the server rejects the credentials (HTTP 401), they are invalidated and the call is sent
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

	/**
	 * A transport whose requests carry only the credentials of the {@link McpTransportContext} of
	 * their call. A request that the library sends by itself gets none.
	 *
	 * <p>
	 * The {@link McpClientConnection} uses {@link #createTransport(Supplier)}, which gives such a
	 * request the headers of the last call that succeeded.
	 * </p>
	 */
	@Override
	public McpClientTransport createTransport() {
		return createTransport(() -> CredentialHeaders.NONE);
	}

	/**
	 * Creates the transport of one connection.
	 *
	 * @param lastSucceeded
	 *        The headers of the last call of the connection that succeeded.
	 */
	McpClientTransport createTransport(Supplier<CredentialHeaders> lastSucceeded) {
		return HttpClientStreamableHttpTransport.builder(_url)
			.addHttpRequestCustomizer((request, method, uri, body, context) -> {
				if (_headers != null) {
					for (NamedValueConfig entry : _headers.values()) {
						request.header(entry.getName(), entry.getValue());
					}
				}
				CredentialHeaders credentials;
				if (context.get(CredentialHeaders.KEY) instanceof CredentialHeaders resolved) {
					credentials = resolved;
				} else {
					// A request that the library sends by itself has no caller, e.g. the new
					// initialize after the server forgot the session. It gets the headers of the
					// last call that succeeded. They belong to the one connection: a client for each
					// person needs a connection for each person.
					credentials = lastSucceeded.get();
				}
				// Limit: the stream of server messages (GET), which the library opens after the
				// first answer of a session, keeps the headers of that call. After a rotation of the
				// secret these headers can be stale.
				credentials.addTo(request);
			})
			.build();
	}

	/**
	 * The {@link Config#getCredentials() credentials}, which the {@link McpClientConnection} calls on
	 * the thread of its caller.
	 */
	CredentialProvider credentials() {
		return _credentials;
	}

	/**
	 * The headers that the {@link Config#getCredentials() credentials} set for one call, resolved on
	 * the thread of the caller.
	 *
	 * <p>
	 * The transport itself never calls the credential provider: it runs inside the wait that a stop
	 * of the run ends, and on threads of the MCP library. A credential provider may read the
	 * knowledge base, which such a wait must not touch, and it may take the person from the thread,
	 * which a thread of the library does not have. The {@link McpClientConnection} calls the
	 * provider on the thread of its caller and gives these headers to the transport in the
	 * {@link McpTransportContext} of the call.
	 * </p>
	 *
	 * @param values
	 *        The header values by header name.
	 */
	record CredentialHeaders(Map<String, String> values) {

		/** The key of the headers in the {@link McpTransportContext} of a call. */
		static final String KEY = CredentialHeaders.class.getName();

		/** No headers, before a call succeeded. */
		static final CredentialHeaders NONE = new CredentialHeaders(Map.of());

		/** Copies the given values, which the caller may change later. */
		CredentialHeaders {
			values = Map.copyOf(values);
		}

		/**
		 * The {@link McpTransportContext} that gives these headers to the transport.
		 */
		McpTransportContext context() {
			return McpTransportContext.create(Map.of(KEY, this));
		}

		void addTo(HttpRequest.Builder request) {
			values.forEach(request::setHeader);
		}

		/**
		 * The header names only: the values are secrets.
		 */
		@Override
		public String toString() {
			return "CredentialHeaders" + values.keySet();
		}
	}

}
