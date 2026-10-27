/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.docker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardProtocolFamily;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.SSLConfig;

/**
 * Connection to the Docker daemon that starts an exec instance and is then taken over by the
 * streams of the command: its standard input in one direction, its multiplexed output in the
 * other.
 *
 * <p>
 * The Docker client library offers this as well, but cannot half-close the connection, and the
 * daemon closes the standard input of the command only when the client closes its sending side.
 * This connection does that in {@link #shutdownOutput()}. It supports the {@value #UNIX_SCHEME}
 * socket of a local daemon and {@value #TCP_SCHEME} connections, with TLS if the client
 * configuration has an {@link SSLConfig}.
 * </p>
 */
final class HijackedConnection implements AutoCloseable {

	/**
	 * Scheme of the URI of a daemon reached through a Unix domain socket.
	 */
	static final String UNIX_SCHEME = "unix";

	/**
	 * Scheme of the URI of a daemon reached through TCP.
	 */
	static final String TCP_SCHEME = "tcp";

	/**
	 * Status of the answer of the daemon taking the connection over for the streams.
	 */
	private static final int STATUS_SWITCHING_PROTOCOLS = 101;

	private static final String HTTP_LINE_END = "\r\n";

	private static final String UNIX_HOST_HEADER = "docker";

	private static final String START_BODY = "{\"Detach\":false,\"Tty\":false}";

	private static final int MAX_HEADER_SIZE = 64 * 1024;

	private static final int MAX_ERROR_SIZE = 4 * 1024;

	private static final int BUFFER_SIZE = 8 * 1024;

	private final InputStream _in;

	private final OutputStream _out;

	private final Transport _transport;

	private HijackedConnection(Transport transport) throws IOException {
		_transport = transport;
		_in = new BufferedInputStream(transport.input(), BUFFER_SIZE);
		_out = new BufferedOutputStream(transport.output(), BUFFER_SIZE);
	}

	/**
	 * Connects to the daemon and starts an exec instance created with standard input, output and
	 * error attached.
	 *
	 * @param config
	 *        The configuration of the client, telling where the daemon is.
	 * @param connectTimeout
	 *        The timeout for establishing a TCP connection.
	 * @param execId
	 *        The exec instance to start.
	 * @return The connection carrying the streams of the command.
	 */
	static HijackedConnection startExec(DockerClientConfig config, Duration connectTimeout, String execId)
			throws IOException {
		URI host = config.getDockerHost();
		Transport transport;
		String hostHeader;
		if (UNIX_SCHEME.equals(host.getScheme())) {
			transport = ChannelTransport.unix(host.getPath());
			hostHeader = UNIX_HOST_HEADER;
		} else if (TCP_SCHEME.equals(host.getScheme())) {
			transport = SocketTransport.tcp(host.getHost(), host.getPort(), config.getSSLConfig(), connectTimeout);
			hostHeader = host.getHost() + ":" + host.getPort();
		} else {
			throw new IOException("Unsupported Docker host for running commands: " + host);
		}

		HijackedConnection connection;
		try {
			connection = new HijackedConnection(transport);
		} catch (IOException | RuntimeException ex) {
			transport.close();
			throw ex;
		}
		try {
			connection.requestStart(hostHeader, execId);
		} catch (IOException | RuntimeException ex) {
			connection.close();
			throw ex;
		}
		return connection;
	}

	private void requestStart(String hostHeader, String execId) throws IOException {
		byte[] body = START_BODY.getBytes(StandardCharsets.UTF_8);
		String request = "POST /exec/" + execId + "/start HTTP/1.1" + HTTP_LINE_END
			+ "Host: " + hostHeader + HTTP_LINE_END
			+ "Content-Type: application/json" + HTTP_LINE_END
			+ "Connection: Upgrade" + HTTP_LINE_END
			+ "Upgrade: tcp" + HTTP_LINE_END
			+ "Content-Length: " + body.length + HTTP_LINE_END
			+ HTTP_LINE_END;
		_out.write(request.getBytes(StandardCharsets.US_ASCII));
		_out.write(body);
		_out.flush();

		String header = readHeader();
		String statusLine = header.substring(0, header.indexOf(HTTP_LINE_END));
		String[] parts = statusLine.split(" ", 3);
		int status;
		try {
			status = Integer.parseInt(parts[1]);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
			throw new IOException("Invalid answer of the Docker daemon: " + statusLine);
		}
		if (status != STATUS_SWITCHING_PROTOCOLS) {
			throw new IOException(statusLine + ": " + readError());
		}
	}

	private String readHeader() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int matched = 0;
		byte[] end = (HTTP_LINE_END + HTTP_LINE_END).getBytes(StandardCharsets.US_ASCII);
		while (matched < end.length) {
			int next = _in.read();
			if (next < 0) {
				throw new IOException("Connection closed by the Docker daemon.");
			}
			if (buffer.size() >= MAX_HEADER_SIZE) {
				throw new IOException("Answer header of the Docker daemon too long.");
			}
			buffer.write(next);
			matched = next == end[matched] ? matched + 1 : (next == end[0] ? 1 : 0);
		}
		return buffer.toString(StandardCharsets.US_ASCII);
	}

	private String readError() throws IOException {
		byte[] message = _in.readNBytes(MAX_ERROR_SIZE);
		return new String(message, StandardCharsets.UTF_8).trim();
	}

	/**
	 * The multiplexed output of the command.
	 */
	InputStream input() {
		return _in;
	}

	/**
	 * The standard input of the command.
	 */
	OutputStream output() {
		return _out;
	}

	/**
	 * Closes the sending side, so that the command reads the end of its input. Output is still
	 * received.
	 */
	void shutdownOutput() throws IOException {
		_out.flush();
		_transport.shutdownOutput();
	}

	@Override
	public void close() {
		_transport.close();
	}

	/**
	 * The connection below the HTTP protocol.
	 */
	private interface Transport {

		InputStream input() throws IOException;

		OutputStream output() throws IOException;

		void shutdownOutput() throws IOException;

		void close();

	}

	/**
	 * {@link Transport} over a {@link SocketChannel}, reading and writing concurrently without a
	 * lock in common.
	 */
	private static final class ChannelTransport implements Transport {

		private final SocketChannel _channel;

		private ChannelTransport(SocketChannel channel) {
			_channel = channel;
		}

		static ChannelTransport unix(String path) throws IOException {
			SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
			try {
				channel.connect(UnixDomainSocketAddress.of(path));
			} catch (IOException | RuntimeException ex) {
				channel.close();
				throw ex;
			}
			return new ChannelTransport(channel);
		}

		@Override
		public InputStream input() {
			return new InputStream() {
				@Override
				public int read() throws IOException {
					byte[] single = new byte[1];
					int read = read(single, 0, 1);
					return read < 0 ? -1 : single[0] & 0xFF;
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					if (len == 0) {
						return 0;
					}
					int read;
					do {
						read = _channel.read(ByteBuffer.wrap(b, off, len));
					} while (read == 0);
					return read;
				}
			};
		}

		@Override
		public OutputStream output() {
			return new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					write(new byte[] { (byte) b }, 0, 1);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
					while (buffer.hasRemaining()) {
						_channel.write(buffer);
					}
				}
			};
		}

		@Override
		public void shutdownOutput() throws IOException {
			_channel.shutdownOutput();
		}

		@Override
		public void close() {
			try {
				_channel.close();
			} catch (IOException ex) {
				// Nothing left to release.
			}
		}
	}

	/**
	 * {@link Transport} over a TCP {@link Socket}, with TLS if configured.
	 */
	private static final class SocketTransport implements Transport {

		private final Socket _socket;

		private SocketTransport(Socket socket) {
			_socket = socket;
		}

		static SocketTransport tcp(String host, int port, SSLConfig sslConfig, Duration connectTimeout)
				throws IOException {
			Socket socket;
			if (sslConfig != null) {
				SSLContext context;
				try {
					context = sslConfig.getSSLContext();
				} catch (GeneralSecurityException ex) {
					throw new IOException("TLS setup for the Docker daemon failed: " + ex.getMessage(), ex);
				}
				socket = context.getSocketFactory().createSocket();
			} else {
				socket = new Socket();
			}
			try {
				socket.connect(new InetSocketAddress(host, port), (int) connectTimeout.toMillis());
				if (socket instanceof SSLSocket) {
					((SSLSocket) socket).startHandshake();
				}
			} catch (IOException | RuntimeException ex) {
				socket.close();
				throw ex;
			}
			return new SocketTransport(socket);
		}

		@Override
		public InputStream input() throws IOException {
			return _socket.getInputStream();
		}

		@Override
		public OutputStream output() throws IOException {
			return _socket.getOutputStream();
		}

		@Override
		public void shutdownOutput() throws IOException {
			_socket.shutdownOutput();
		}

		@Override
		public void close() {
			try {
				_socket.close();
			} catch (IOException ex) {
				// Nothing left to release.
			}
		}
	}

}
