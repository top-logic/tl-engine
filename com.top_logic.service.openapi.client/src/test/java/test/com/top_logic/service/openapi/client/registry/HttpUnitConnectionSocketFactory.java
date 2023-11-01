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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
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
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.protocol.MessageBody;
import com.meterware.httpunit.protocol.ParameterCollection;
import com.meterware.servletunit.InvocationContext;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BinaryContent;
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

	static final byte CR = '\r';

	static final byte LF = '\n';

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

		private ByteArrayStream _requestContent = new ByteArrayStream();

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
				return new HttpUnitResponse(request, _requestContent);
			} else {
				return super.getInputStream();
			}
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			HttpRequest request = getRequest(_context);
			if (isLocalhost(request)) {
				return _requestContent;
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

		private final BinaryContent _requestContent;

		private InputStream _impl;

		public HttpUnitResponse(HttpRequest request, BinaryContent requestContent) {
			_request = request;
			_requestContent = requestContent;
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
			WebRequest webRequest = newWebRequest(_request, _requestContent);
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
				w.append((char) CR).append((char) LF);
				for (String fieldName : response.getHeaderFieldNames()) {
					for (String fieldValue : response.getHeaderFields(fieldName)) {
						w.append(fieldName).append(": ").append(StringServices.nonNull(fieldValue))
							.append((char) CR).append((char) LF);
					}
				}
				w.append((char) CR).append((char) LF);
			}
			try (InputStream responseContent = response.getInputStream()) {
				StreamUtilities.copyStreamContents(responseContent, byteArrayStream);
			}
			return byteArrayStream.getStream();
		}

		private WebRequest newWebRequest(HttpRequest request, BinaryContent content) {
			HttpMethod httpMethod = HttpMethod.normalizedValueOf(request.getMethod());
			String urlString = urlString(request);
			WebRequest webRequest;
			if (httpMethod.supportsBody()) {
				HttpEntity entity = ((HttpEntityContainer) request).getEntity();
				webRequest =
					new MessageBodyWebRequest(urlString, new HttpEntityMessageBody(entity, content)) {
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

	}

	/**
	 * Adapter to use an {@link HttpEntity} as {@link MessageBody}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class HttpEntityMessageBody extends MessageBody {

		private final HttpEntity _entity;

		private final BinaryContent _requestContent;

		/**
		 * Creates a {@link HttpEntityMessageBody}.
		 */
		public HttpEntityMessageBody(HttpEntity entity, BinaryContent requestContent) {
			super(entity.getContentEncoding());
			_entity = entity;
			_requestContent = requestContent;
		}

		@Override
		public String getContentType() {
			return _entity.getContentType();
		}

		@Override
		public void writeTo(OutputStream outputStream, ParameterCollection parameters) throws IOException {
			try (InputStream in = _requestContent.getStream()) {
				skipStartLine(in);

				Map<String, byte[]> headers = readHeaders(in);
				Charset encoding = encodingFromHeader(headers);
				byte[] transferEncoding = headers.get("Transfer-Encoding");
				boolean chunked;
				if (transferEncoding != null) {
					chunked = Pattern.compile("\\bchunked\\b")
						.matcher(new String(transferEncoding, encoding))
						.find();
				} else {
					chunked = false;
				}
				InputStream stream;
				if (chunked) {
					stream = wrapChunked(in, encoding);
				} else {
					stream = in;
				}
				StreamUtilities.copyStreamContents(stream, outputStream);
			}
		}

		private InputStream wrapChunked(InputStream in, Charset encoding) throws IOException {
			return new InputStream() {
				
				int _remaining;
				{
					readChunk(true);
				}

				@Override
				public int read() throws IOException {
					switch (_remaining) {
						case 0: {
							readChunk(false);
							return read();
						}
						case -1: {
							return -1;
						}
						default: {
							_remaining--;
							return in.read();
						}
					}
				}
				
				private void readChunk(boolean firstChunk) throws IOException {
					if (!firstChunk) {
						// Read CRLF from last chunk
						int cr = in.read();
						if (cr == -1) {
							failUnexpectedEndOfContent();
						}
						byte b = (byte) cr;
						if (b != CR) {
							throw new IOException("Missing \\r.");
						} else {
							ensureLinefeedAfterCarriageReturn(in);
						}
					}
					byte[] line = new byte[16];
					int size = 0;
					readLine:
					while (true) {
						int current = in.read();
						if (current == -1) {
							_remaining = -1;
							return;
						}
						byte b = (byte) current;
						switch (b) {
							case CR: {
								ensureLinefeedAfterCarriageReturn(in);
								break readLine;
							}
							default: {
								line = addToBuffer(line, b, size);
								size++;
							}
						}
					}
					String chunkSize = new String(line, 0, size, encoding);
					int startSize =0;
					while (true) {
						if (startSize == chunkSize.length()) {
							throw new IOException("Empty chunk size.");
						}
						if (chunkSize.charAt(startSize) == ' ') {
							startSize++;
						} else {
							break;
						}
					}
					int sizeSeparator = chunkSize.indexOf(' ', startSize+1); 
					if (sizeSeparator < 0) {
						// Only size
						_remaining = Integer.parseInt(chunkSize, startSize, chunkSize.length(), 16);
					} else {
						_remaining = Integer.parseInt(chunkSize, startSize, sizeSeparator, 16);
					}
					if (_remaining == 0) {
						_remaining = -1;
					}
				}

				@Override
				public int read(byte b[], int off, int len) throws IOException {
			        Objects.checkFromIndexSize(off, len, b.length);
			        if (len == 0) {
			            return 0;
			        }
					switch (_remaining) {
						case 0: {
							readChunk(false);
							return read(b, off, len);
						}
						case -1: {
							return -1;
						}
						default: {
							int numberBytes = in.read(b, off, Math.min(_remaining, len));
							_remaining -= numberBytes;
							return numberBytes;
						}
					}
			    }

				@Override
				public int available() throws IOException {
					switch (_remaining) {
						case 0: {
							readChunk(false);
							return available();
						}
						case -1: {
							return 0;
						}
						default: {
							return Math.min(_remaining, in.available());
						}

					}
				}

				@Override
				public void close() throws IOException {
					super.close();
					in.close();
				}
			};

		}

		private Charset encodingFromHeader(Map<String, byte[]> headers) throws IOException {
			Charset encoding;
			byte[] contentType = headers.get("Content-Type");
			if (contentType == null) {
				encoding = StandardCharsets.ISO_8859_1;
			} else {
				MimeType mimeType;
				try {
					mimeType = new MimeType(new String(contentType, StandardCharsets.ISO_8859_1));
				} catch (MimeTypeParseException ex) {
					throw new IOException(ex);
				}
				String contentEncoding = mimeType.getParameter("charset");
				if (contentEncoding == null) {
					encoding = StandardCharsets.ISO_8859_1;
				} else {
					encoding = Charset.forName(contentEncoding);
				}
			}
			return encoding;
		}

		private void skipStartLine(InputStream in) throws IOException {
			while (true) {
				int current = in.read();
				if (current == -1) {
					// end of stream reached.
					break;
				}
				byte b = (byte) current;
				switch (b) {
					case CR: {
						ensureLinefeedAfterCarriageReturn(in);
						return;
					}
				}
			}
		}

		private Map<String, byte[]> readHeaders(InputStream in) throws IOException {
			Map<String, byte[]> headers = new HashMap<>();

			byte[] nameBuffer = new byte[16];
			byte[] valueBuffer = new byte[16];

			int nameIndex = 0, valueIndex = 0;
				boolean readingName = true;
				while (true) {
					int current = in.read();
					if (current == -1) {
						if (nameIndex > 0 || valueIndex > 0) {
							failUnexpectedEndOfContent();
						}
						return Collections.emptyMap();
					}
					byte b = (byte) current;
					if (readingName) {
						switch (b) {
							case ':': {
								readingName = false;
								break;
							}
							case CR: {
								if (nameIndex == 0) {
									ensureLinefeedAfterCarriageReturn(in);
									return headers;
								} else {
									throw new IOException("Unexpected char \\r in field name.");
								}
							}
							default: {
								nameBuffer = addToBuffer(nameBuffer, b, nameIndex);
								nameIndex++;
							}
						}
					} else {
						switch (b) {
							case CR: {
								ensureLinefeedAfterCarriageReturn(in);
								readingName = true;
								headers.put(
									new String(nameBuffer, 0, nameIndex, StandardCharsets.ISO_8859_1),
									Arrays.copyOf(valueBuffer, valueIndex));
								nameIndex = valueIndex = 0;
								break;
							}
							default: {
								valueBuffer = addToBuffer(valueBuffer, b, valueIndex);
								valueIndex++;
							}
						}

					}
				}

		}

		private void ensureLinefeedAfterCarriageReturn(InputStream in) throws IOException {
			int next = in.read();
			if (next == -1) {
				failUnexpectedEndOfContent();
			}
			if (next != LF) {
				throw new IOException("Missing \\n after \\r.");
			}
		}

		void failUnexpectedEndOfContent() throws IOException {
			throw new IOException("Unexpected end of content.");
		}

		byte[] addToBuffer(byte[] buffer, byte value, int index) {
			if (index == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length * 2);
			}
			buffer[index] = value;
			return buffer;
		}

	}

}

