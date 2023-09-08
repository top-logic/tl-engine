/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.TimeValue;

import test.com.top_logic.layout.scripting.runtime.TestedApplicationSession;

import com.meterware.httpunit.HeaderOnlyWebRequest;
import com.meterware.httpunit.MessageBodyWebRequest;
import com.meterware.httpunit.MessageBodyWebRequest.InputStreamMessageBody;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.ProxyInputStream;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.service.openapi.common.conf.HttpMethod;

/**
 * {@link ConnectionSocketFactory} creating {@link Socket} that connect to server using httpunit.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HttpUnitConnectionSocketFactory implements ConnectionSocketFactory {

	/** Singleton {@link HttpUnitConnectionSocketFactory} instance. */
	public static final HttpUnitConnectionSocketFactory INSTANCE = new HttpUnitConnectionSocketFactory();

	/**
	 * Creates a new {@link HttpUnitConnectionSocketFactory}.
	 */
	protected HttpUnitConnectionSocketFactory() {
		// singleton instance
	}

	/**
	 * Singleton {@link HttpUnitConnectionSocketFactory} instance.
	 */
	public static HttpUnitConnectionSocketFactory getSocketFactory() {
		return INSTANCE;
	}

	@Override
	public Socket createSocket(HttpContext context) throws IOException {
		return new HttpUnitSocket(context);
	}

	static HttpRequest getRequest(HttpContext context) {
		return (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
	}

	@Override
	public Socket connectSocket(TimeValue connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
			InetSocketAddress localAddress, HttpContext context) throws IOException {
		final Socket sock = socket != null ? socket : createSocket(context);
		if (localAddress != null) {
			sock.bind(localAddress);
		}
		return sock;
	}

	/**
	 * Special {@link Socket} returning the response of a {@link HttpRequest} using http unit.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class HttpUnitSocket extends Socket {

		private HttpContext _context;

		/**
		 * Creates a {@link HttpUnitSocket}.
		 */
		public HttpUnitSocket(HttpContext context) {
			_context = context;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			HttpRequest request = getRequest(_context);
			if (isLocalhost(request)) {
				return new HttpUnitResponse(request);
			} else {
				return super.getInputStream();
			}
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			HttpRequest request = getRequest(_context);
			if (isLocalhost(request)) {
				return OutputStream.nullOutputStream();
			} else {
				return super.getOutputStream();
			}
		}

		private static boolean isLocalhost(HttpRequest request) {
			try {
				String host = request.getUri().getHost();
				return "localhost".equalsIgnoreCase(host);
			} catch (URISyntaxException ex) {
				return false;
			}
		}
	}

	/**
	 * {@link InputStream} containing the response of a {@link HttpRequest} using http unit.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class HttpUnitResponse extends ProxyInputStream {

		private final HttpRequest _request;

		private InputStream _impl;

		public HttpUnitResponse(HttpRequest request) {
			_request = request;
		}

		@Override
		protected InputStream getImpl() throws IOException {
			if (_impl != null) {
				return _impl;
			}
			_impl = initStream();
			return _impl;
		}

		private InputStream initStream() throws IOException {
			WebRequest webRequest = newWebRequest(_request);
			InvocationContext invocation;
			try {
				TestedApplicationSession applicationSession = TestedApplicationSession.get();
				invocation = applicationSession.newInvocation(webRequest);
				invocation.service();
			} catch (ServletException ex) {
				throw new IOException(ex);
			}
			return responseAsStream(invocation);
		}

		private InputStream responseAsStream(InvocationContext invocation) throws IOException {
			WebResponse response = invocation.getServletResponse();
			ByteArrayStream byteArrayStream = new ByteArrayStream();
			try (Writer w = new OutputStreamWriter(byteArrayStream, response.getCharacterSet())) {
				w.append(invocation.getRequest().getProtocol());
				w.append(" ");
				w.append(Integer.toString(response.getResponseCode()));
				String responseMessage = response.getResponseMessage();
				if (!StringServices.isEmpty(responseMessage)) {
					w.append(" ").append(responseMessage);
				}
				w.append('\n');
				for (String fieldName : response.getHeaderFieldNames()) {
					for (String fieldValue : response.getHeaderFields(fieldName)) {
						w.append(fieldName).append(": ").append(StringServices.nonNull(fieldValue))
							.append('\n');
					}
				}
				w.append('\n');
			}
			try (InputStream responseContent = response.getInputStream()) {
				StreamUtilities.copyStreamContents(responseContent, byteArrayStream);
			}
			return byteArrayStream.getStream();
		}

		private WebRequest newWebRequest(HttpRequest request) throws IOException {
			HttpMethod httpMethod = HttpMethod.normalizedValueOf(request.getMethod());
			String urlString = urlString(request);
			WebRequest webRequest;
			if (httpMethod.supportsBody()) {
				HttpEntity entity = ((HttpEntityContainer) request).getEntity();
				webRequest =
						new MessageBodyWebRequest(urlString,
							new InputStreamMessageBody(getInputStream(entity),
								entity.getContentType())) {
						{
							method = httpMethod.name().toUpperCase(Locale.ROOT);
						}
					};
			} else {
				webRequest = new HeaderOnlyWebRequest(urlString) {
					{
						method = httpMethod.name().toUpperCase(Locale.ROOT);
					}
				};
			}
			for (Header h : request.getHeaders()) {
				webRequest.setHeaderField(h.getName(), h.getValue());
			}
			return webRequest;
		}

		private String urlString(HttpRequest request) {
			String urlString;
			try {
				URI origURI = request.getUri();
				// Use protocol that is known as valid: httpunit is unknown.
				URI adaptedUri = new URI("http",
					origURI.getUserInfo(),
					origURI.getHost(),
					origURI.getPort(),
					origURI.getPath(),
					origURI.getQuery(),
					origURI.getFragment());
				urlString = adaptedUri.toURL().toExternalForm();
			} catch (MalformedURLException | URISyntaxException ex) {
				throw new IllegalArgumentException(ex);
			}
			return urlString;
		}

		private InputStream getInputStream(HttpEntity entity) throws IOException {
			InputStream in;
			if (entity.isRepeatable()) {
				in = entity.getContent();
			} else {
				in = new LazyHttpEntityContent(entity);
			}
			return in;
		}

	}

	/**
	 * Proxy for {@link HttpEntity#getContent()} but content is fetched lazy.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class LazyHttpEntityContent extends ProxyInputStream {

		private final HttpEntity _entity;

		private InputStream _impl;

		public LazyHttpEntityContent(HttpEntity entity) {
			_entity = entity;
		}

		@Override
		protected InputStream getImpl() throws IOException {
			if (_impl == null) {
				_impl = _entity.getContent();
			}
			return _impl;
		}

	}

}

