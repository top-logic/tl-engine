/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.llm;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.top_logic.factory.llm.LLMProxy;
import com.top_logic.factory.llm.LLMProxyHandler;
import com.top_logic.factory.llm.LLMProxyListener;

/**
 * The tests of {@link TestLLMProxyServlet} against the proxy on a port of its own, the
 * {@link LLMProxyListener}.
 */
public class TestLLMProxyListener extends TestLLMProxyServlet {

	private LLMProxyListener _listener;

	@Override
	protected String startProxy(LLMProxy proxy) throws Exception {
		_listener = new LLMProxyListener(new InetSocketAddress(LOOPBACK, 0), () -> proxy);
		return "http://" + LOOPBACK + ":" + _listener.getAddress().getPort() + LLMProxyHandler.PATH;
	}

	@Override
	protected void stopProxy() {
		_listener.close();
	}

	/**
	 * The listener serves the proxy only: other paths do not exist.
	 */
	public void testOtherPathsOfListenerNotFound() throws Exception {
		try (HttpClient client = HttpClient.newHttpClient()) {
			for (String path : new String[] { "/", "/tl-factory-app/", LLMProxyHandler.PATH + "x/v1/messages" }) {
				HttpResponse<String> response = client.send(
					HttpRequest.newBuilder(URI.create("http://" + LOOPBACK + ":" + _listener.getAddress().getPort() + path))
						.timeout(Duration.ofSeconds(10))
						.POST(HttpRequest.BodyPublishers.ofString("{}"))
						.build(),
					HttpResponse.BodyHandlers.ofString());
				assertEquals(path, 404, response.statusCode());
			}
		}
	}

}
