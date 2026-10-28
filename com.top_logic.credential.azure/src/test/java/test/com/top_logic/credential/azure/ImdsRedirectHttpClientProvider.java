/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.azure;

import java.net.MalformedURLException;
import java.net.URL;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.jdk.httpclient.JdkHttpClientProvider;
import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;

import reactor.core.publisher.Mono;

/**
 * {@link HttpClientProvider} of the tests, sending the requests to the instance metadata service
 * to a mock server.
 *
 * <p>
 * Registered as service of the test class path and selected by the Azure configuration property
 * <code>AZURE_HTTP_CLIENT_IMPLEMENTATION</code> (see {@link AbstractAzureMockTest}). Other requests
 * are sent unchanged with the JDK HTTP client.
 * </p>
 */
public class ImdsRedirectHttpClientProvider implements HttpClientProvider {

	/**
	 * Host of the instance metadata service.
	 */
	public static final String IMDS_HOST = "169.254.169.254";

	private static volatile String _target;

	/**
	 * Sets the base URL (protocol, host and port) receiving the requests to the instance metadata
	 * service.
	 */
	public static void setTarget(String baseUrl) {
		_target = baseUrl;
	}

	private final JdkHttpClientProvider _jdk = new JdkHttpClientProvider();

	@Override
	public HttpClient createInstance() {
		return new Redirect(_jdk.createInstance());
	}

	@Override
	public HttpClient createInstance(HttpClientOptions clientOptions) {
		return new Redirect(_jdk.createInstance(clientOptions));
	}

	private static final class Redirect implements HttpClient {

		private final HttpClient _inner;

		Redirect(HttpClient inner) {
			_inner = inner;
		}

		@Override
		public Mono<HttpResponse> send(HttpRequest request) {
			return _inner.send(redirect(request));
		}

		@Override
		public Mono<HttpResponse> send(HttpRequest request, Context context) {
			return _inner.send(redirect(request), context);
		}

		@Override
		public HttpResponse sendSync(HttpRequest request, Context context) {
			return _inner.sendSync(redirect(request), context);
		}

		private static HttpRequest redirect(HttpRequest request) {
			URL url = request.getUrl();
			String target = _target;
			if (target == null || !IMDS_HOST.equals(url.getHost())) {
				return request;
			}
			String file = url.getFile();
			try {
				return request.setUrl(new URL(target + file));
			} catch (MalformedURLException ex) {
				throw new IllegalArgumentException(ex);
			}
		}

	}

}
