/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.servlet.ServletException;

import org.apache.hc.client5.http.io.DetachedSocketFactory;

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
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.service.openapi.common.conf.HttpMethod;

/**
 * {@link DetachedSocketFactory} creating {@link Socket}s that answer requests to {@code localhost}
 * using httpunit instead of a real network connection.
 *
 * <p>
 * The produced socket buffers the raw HTTP request written by the client, parses it and dispatches
 * it into the currently running tested application (via {@link TestedApplicationSession}), then
 * serves the application's response back through the socket's input stream. Requests to a
 * non-loopback address are handled by a real socket.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HttpUnitConnectionSocketFactory implements DetachedSocketFactory {

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
	public Socket create(Proxy proxy) throws IOException {
		return new HttpUnitSocket();
	}

	/**
	 * Special {@link Socket} answering an HTTP request through httpunit.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class HttpUnitSocket extends Socket {

		private final ByteArrayStream _requestContent = new ByteArrayStream();

		private boolean _real;

		private boolean _simulatedConnected;

		/**
		 * Creates a {@link HttpUnitSocket}.
		 */
		public HttpUnitSocket() {
			super();
		}

		@Override
		public void connect(SocketAddress endpoint, int timeout) throws IOException {
			if (isLoopback(endpoint)) {
				// Do not open a real connection: the request is answered by httpunit.
				_simulatedConnected = true;
			} else {
				_real = true;
				super.connect(endpoint, timeout);
			}
		}

		@Override
		public void connect(SocketAddress endpoint) throws IOException {
			connect(endpoint, 0);
		}

		@Override
		public void bind(SocketAddress bindpoint) throws IOException {
			// No local bind for simulated connections.
		}

		@Override
		public boolean isConnected() {
			return _simulatedConnected || super.isConnected();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (_real) {
				return super.getInputStream();
			}
			return new HttpUnitResponse(_requestContent);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (_real) {
				return super.getOutputStream();
			}
			return _requestContent;
		}

		private static boolean isLoopback(SocketAddress endpoint) {
			if (endpoint instanceof InetSocketAddress) {
				InetSocketAddress address = (InetSocketAddress) endpoint;
				return address.getAddress() != null && address.getAddress().isLoopbackAddress();
			}
			return false;
		}
	}

	/**
	 * {@link InputStream} containing the response to the HTTP request buffered in the given content,
	 * produced by dispatching the request through httpunit.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class HttpUnitResponse extends ProxyInputStream {

		private final BinaryContent _requestContent;

		private InputStream _impl;

		public HttpUnitResponse(BinaryContent requestContent) {
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
			WebRequest webRequest = newWebRequest();
			TestedApplicationSession applicationSession = TestedApplicationSession.get();
			InvocationContext invocation = applicationSession.newInvocation(webRequest);

			inThread(() -> {
				try {
					invocation.service();
				} catch (ServletException ex) {
					throw new IOException(ex);
				}
				return null;
			});
			return responseAsStream(invocation);
		}

		private InputStream inThread(ComputationEx<InputStream, IOException> supplier) throws IOException {
			Class<HttpUnitResponse> clazz = HttpUnitResponse.class;

			AtomicReference<Throwable> problem = new AtomicReference<>();
			AtomicReference<InputStream> result = new AtomicReference<>();
			Thread other = new Thread() {

				@Override
				public void run() {
					try {
						result.set(ThreadContextManager.inSystemInteraction(clazz, supplier));
					} catch (IOException ex) {
						problem.set(ex);
					} catch (RuntimeException | Error ex) {
						problem.set(ex);
					}
					super.run();
				}

			};
			other.start();
			try {
				other.join();
			} catch (InterruptedException ex) {
				throw new RuntimeException("Unexpected interrupt.", ex);
			}

			if (problem.get() != null) {
				if (problem.get() instanceof IOException) {
					throw (IOException) problem.get();
				} else if (problem.get() instanceof Error) {
					throw (Error) problem.get();
				} else {
					throw (RuntimeException) problem.get();
				}
			}
			return result.get();
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

		private WebRequest newWebRequest() throws IOException {
			RequestHead head;
			try (InputStream in = _requestContent.getStream()) {
				head = readRequestHead(in);
			}

			HttpMethod httpMethod = HttpMethod.normalizedValueOf(head.getMethod());
			String urlString = "http://" + head.getHost() + head.getTarget();
			WebRequest webRequest;
			if (httpMethod.supportsBody()) {
				MessageBody body =
					new HttpEntityMessageBody(head.getContentType(), head.getContentEncoding(), _requestContent);
				webRequest = new MessageBodyWebRequest(urlString, body) {
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
			for (String[] header : head.getHeaders()) {
				String name = header[0];
				if (isFramingHeader(name)) {
					// Content type is delivered through the message body, framing headers are
					// re-computed by httpunit.
					continue;
				}
				webRequest.setHeaderField(name, header[1]);
			}
			return webRequest;
		}

		private static boolean isFramingHeader(String name) {
			return name.equalsIgnoreCase("Host")
				|| name.equalsIgnoreCase("Content-Type")
				|| name.equalsIgnoreCase("Content-Length")
				|| name.equalsIgnoreCase("Transfer-Encoding")
				|| name.equalsIgnoreCase("Connection");
		}

		/**
		 * Parses the request line and headers of the buffered request.
		 */
		private RequestHead readRequestHead(InputStream in) throws IOException {
			String requestLine = readAsciiLine(in);
			if (requestLine == null) {
				throw new IOException("Empty request.");
			}
			int methodEnd = requestLine.indexOf(' ');
			int targetEnd = requestLine.indexOf(' ', methodEnd + 1);
			if (methodEnd < 0 || targetEnd < 0) {
				throw new IOException("Malformed request line: " + requestLine);
			}
			String method = requestLine.substring(0, methodEnd);
			String target = requestLine.substring(methodEnd + 1, targetEnd);

			List<String[]> headers = new ArrayList<>();
			while (true) {
				String line = readAsciiLine(in);
				if (line == null || line.isEmpty()) {
					break;
				}
				int colon = line.indexOf(':');
				if (colon < 0) {
					continue;
				}
				headers.add(new String[] { line.substring(0, colon).trim(), line.substring(colon + 1).trim() });
			}
			return new RequestHead(method, target, headers);
		}

		private String readAsciiLine(InputStream in) throws IOException {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			while (true) {
				int current = in.read();
				if (current == -1) {
					if (buffer.size() == 0) {
						return null;
					}
					break;
				}
				byte b = (byte) current;
				if (b == CR) {
					ensureLinefeedAfterCarriageReturn(in);
					break;
				}
				buffer.write(b);
			}
			return new String(buffer.toByteArray(), StandardCharsets.ISO_8859_1);
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

	}

	/**
	 * Request line and headers parsed from a buffered HTTP request.
	 */
	private static final class RequestHead {

		private final String _method;

		private final String _target;

		private final List<String[]> _headers;

		RequestHead(String method, String target, List<String[]> headers) {
			_method = method;
			_target = target;
			_headers = headers;
		}

		String getMethod() {
			return _method;
		}

		String getTarget() {
			return _target;
		}

		List<String[]> getHeaders() {
			return _headers;
		}

		String getHost() {
			return header("Host");
		}

		String getContentType() {
			return header("Content-Type");
		}

		String getContentEncoding() {
			return header("Content-Encoding");
		}

		private String header(String name) {
			for (String[] header : _headers) {
				if (header[0].equalsIgnoreCase(name)) {
					return header[1];
				}
			}
			return null;
		}

	}

	/**
	 * Adapter to use the body of a buffered HTTP request as httpunit {@link MessageBody}.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class HttpEntityMessageBody extends MessageBody {

		private final String _contentType;

		private final BinaryContent _requestContent;

		/**
		 * Creates a {@link HttpEntityMessageBody}.
		 */
		public HttpEntityMessageBody(String contentType, String contentEncoding, BinaryContent requestContent) {
			super(contentEncoding);
			_contentType = contentType;
			_requestContent = requestContent;
		}

		@Override
		public String getContentType() {
			return _contentType;
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
