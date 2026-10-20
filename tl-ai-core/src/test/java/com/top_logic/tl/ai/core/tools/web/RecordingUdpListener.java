/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A minimal loopback <b>UDP</b> listener for the WebRTC-egress test that records whether any datagram
 * arrived at all - the UDP counterpart of {@link RecordingServer}.
 *
 * <p>
 * It stands in for a STUN server: a page's ICE gathering sends STUN binding requests over UDP to the
 * {@code stun:} address it is handed, and those datagrams do <em>not</em> traverse an HTTP proxy. So
 * {@link #packetCount()} is exactly the measurement the test needs - not "did the tool report an
 * error", but "did a packet reach the forbidden destination at all". Nothing is ever answered: a STUN
 * server that never replies still proves the browser reached it, and staying silent keeps the fixture
 * from being drawn into an actual ICE negotiation.
 * </p>
 */
final class RecordingUdpListener implements AutoCloseable {

	/** Bounded so a stray large datagram cannot grow the fixture's memory. */
	private static final int BUFFER_BYTES = 2048;

	private final DatagramSocket _socket;

	private final AtomicInteger _packets = new AtomicInteger();

	private volatile boolean _closed;

	private RecordingUdpListener(DatagramSocket socket) {
		_socket = socket;
	}

	/**
	 * Starts a listener on an ephemeral loopback UDP port.
	 *
	 * @return The started listener, or {@code null} when no UDP socket can be bound at all - a
	 *         sandbox restriction rather than a test failure, so the caller skips cleanly.
	 */
	static RecordingUdpListener startOrNull() {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(0, InetAddress.getByName("127.0.0.1"));
		} catch (IOException ex) {
			return null;
		}
		RecordingUdpListener listener = new RecordingUdpListener(socket);
		Thread receiver = new Thread(listener::receiveLoop, "recording-udp-listener");
		receiver.setDaemon(true);
		receiver.start();
		return listener;
	}

	/** The ephemeral loopback port this listener is bound to. */
	int port() {
		return _socket.getLocalPort();
	}

	/** {@code 127.0.0.1:<port>} - the address a page is pointed at as its STUN server. */
	String address() {
		return "127.0.0.1:" + port();
	}

	/**
	 * How many UDP datagrams reached this listener. {@code 0} is the assertion the WebRTC-egress test
	 * needs: no packet escaped the browser towards a non-proxied address.
	 */
	int packetCount() {
		return _packets.get();
	}

	@Override
	public void close() {
		_closed = true;
		_socket.close();
	}

	private void receiveLoop() {
		byte[] buffer = new byte[BUFFER_BYTES];
		while (!_closed) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				_socket.receive(packet);
			} catch (IOException ex) {
				if (_closed) {
					return;
				}
				continue;
			}
			_packets.incrementAndGet();
		}
	}
}
