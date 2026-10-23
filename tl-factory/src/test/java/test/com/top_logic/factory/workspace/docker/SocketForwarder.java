/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 * TCP server forwarding every connection it accepts to a target address - the Unix socket of a
 * Docker daemon, for example -, so that a client reaches the daemon over TCP, with TLS if the
 * server is given an {@link SSLContext}.
 *
 * <p>
 * A half-close is passed on in both directions: when one side closes its sending side, the
 * forwarder closes the sending side of the other connection and keeps forwarding the opposite
 * direction, as a daemon expects from a client ending the standard input of a command.
 * </p>
 */
public final class SocketForwarder implements AutoCloseable {

	private static final int BUFFER_SIZE = 8 * 1024;

	private final ServerSocket _server;

	private final SocketAddress _target;

	private final List<AutoCloseable> _open = new CopyOnWriteArrayList<>();

	private final Thread _acceptor;

	private SocketForwarder(ServerSocket server, SocketAddress target) {
		_server = server;
		_target = target;
		_acceptor = new Thread(this::accept, "SocketForwarder-" + server.getLocalPort());
		_acceptor.setDaemon(true);
		_acceptor.start();
	}

	/**
	 * Starts a forwarder listening on an ephemeral port of the loopback interface.
	 *
	 * @param target
	 *        Where the connections are forwarded to.
	 * @param tls
	 *        The context of the TLS server, {@code null} for plain TCP. A TLS server requires a
	 *        client certificate, as a daemon with TLS verification does.
	 */
	public static SocketForwarder start(SocketAddress target, SSLContext tls) throws IOException {
		ServerSocket server;
		if (tls == null) {
			server = new ServerSocket();
		} else {
			SSLServerSocket sslServer = (SSLServerSocket) tls.getServerSocketFactory().createServerSocket();
			sslServer.setNeedClientAuth(true);
			server = sslServer;
		}
		server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
		return new SocketForwarder(server, target);
	}

	/**
	 * The URI under which a Docker client reaches the target through this forwarder.
	 */
	public String dockerHost() {
		return "tcp://" + _server.getInetAddress().getHostAddress() + ":" + _server.getLocalPort();
	}

	private void accept() {
		while (!_server.isClosed()) {
			Socket client;
			try {
				client = _server.accept();
			} catch (IOException ex) {
				return;
			}
			_open.add(client);
			SocketChannel target;
			try {
				target = SocketChannel.open(_target);
			} catch (IOException ex) {
				closeQuietly(client);
				continue;
			}
			_open.add(target);
			Pump up = new Pump(client, target);
			Thread toTarget = new Thread(() -> forward(up, true), "SocketForwarder-up");
			Thread toClient = new Thread(() -> forward(up, false), "SocketForwarder-down");
			toTarget.setDaemon(true);
			toClient.setDaemon(true);
			toTarget.start();
			toClient.start();
		}
	}

	/**
	 * Forwards one direction of a connection until its end, then closes the sending side of the
	 * other connection.
	 *
	 * <p>
	 * The target channel is read and written directly: its streams of {@link java.nio.channels.Channels}
	 * share a lock, which would block one direction while the other waits for data.
	 * </p>
	 */
	private static void forward(Pump pump, boolean toTarget) {
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			if (toTarget) {
				InputStream in = pump.client().getInputStream();
				int read;
				while ((read = in.read(buffer)) >= 0) {
					ByteBuffer data = ByteBuffer.wrap(buffer, 0, read);
					while (data.hasRemaining()) {
						pump.target().write(data);
					}
				}
				pump.target().shutdownOutput();
			} else {
				OutputStream out = pump.client().getOutputStream();
				ByteBuffer data = ByteBuffer.wrap(buffer);
				int read;
				while ((read = pump.target().read(data)) >= 0) {
					out.write(buffer, 0, read);
					out.flush();
					data.clear();
				}
				pump.client().shutdownOutput();
			}
		} catch (IOException ex) {
			// The connection is gone; end both directions.
			closeQuietly(pump.client());
			closeQuietly(pump.target());
			return;
		}
		pump.ended();
	}

	@Override
	public void close() {
		closeQuietly(_server);
		for (AutoCloseable open : _open) {
			closeQuietly(open);
		}
	}

	private static void closeQuietly(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception ex) {
			// Nothing left to release.
		}
	}

	/**
	 * The two connections of a forwarded connection, closed when both directions have ended.
	 */
	private static final class Pump {

		private final Socket _client;

		private final SocketChannel _target;

		private int _ended;

		Pump(Socket client, SocketChannel target) {
			_client = client;
			_target = target;
		}

		Socket client() {
			return _client;
		}

		SocketChannel target() {
			return _target;
		}

		synchronized void ended() {
			if (++_ended == 2) {
				closeQuietly(_client);
				closeQuietly(_target);
			}
		}
	}

}
