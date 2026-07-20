/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary.scan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.scan.ClamAvScanner;
import com.top_logic.basic.io.binary.scan.ClamAvScanner.UnavailablePolicy;
import com.top_logic.basic.util.ResKey;

/**
 * Test for {@link ClamAvScanner} using a mock <code>clamd</code> daemon that speaks the
 * <code>INSTREAM</code> protocol.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClamAvScannerTest extends BasicTestCase {

	private static final BinaryData CONTENT =
		BinaryDataFactory.createBinaryData("some data".getBytes(StandardCharsets.UTF_8), "text/plain", "test.txt");

	/**
	 * A clean reply accepts the upload.
	 */
	public void testCleanAccepted() throws IOException {
		try (MockClamd clamd = new MockClamd("stream: OK")) {
			assertNull(scanner(clamd.getPort(), UnavailablePolicy.REJECT).check(CONTENT));
		}
	}

	/**
	 * A signature match rejects the upload.
	 */
	public void testInfectedRejected() throws IOException {
		try (MockClamd clamd = new MockClamd("stream: Eicar-Test-Signature FOUND")) {
			ResKey error = scanner(clamd.getPort(), UnavailablePolicy.REJECT).check(CONTENT);
			assertNotNull("Infected content must be rejected.", error);
		}
	}

	/**
	 * An unreachable daemon rejects the upload under the {@link UnavailablePolicy#REJECT} policy.
	 */
	public void testUnreachableRejected() throws IOException {
		int deadPort = freePort();
		ResKey error = scanner(deadPort, UnavailablePolicy.REJECT).check(CONTENT);
		assertNotNull("Unreachable scanner must reject under REJECT policy.", error);
	}

	/**
	 * An unreachable daemon accepts the upload under the {@link UnavailablePolicy#ACCEPT} policy.
	 */
	public void testUnreachableAccepted() throws IOException {
		int deadPort = freePort();
		assertNull("Unreachable scanner must accept under ACCEPT policy.",
			scanner(deadPort, UnavailablePolicy.ACCEPT).check(CONTENT));
	}

	/**
	 * A daemon error reply is treated as unavailable and rejected under the
	 * {@link UnavailablePolicy#REJECT} policy.
	 */
	public void testErrorReplyRejected() throws IOException {
		try (MockClamd clamd = new MockClamd("INSTREAM size limit exceeded. ERROR")) {
			ResKey error = scanner(clamd.getPort(), UnavailablePolicy.REJECT).check(CONTENT);
			assertNotNull("A daemon error must reject under REJECT policy.", error);
		}
	}

	private static ClamAvScanner scanner(int port, UnavailablePolicy policy) {
		ClamAvScanner.Config config = TypedConfiguration.newConfigItem(ClamAvScanner.Config.class);
		set(config, "host", "localhost");
		set(config, "port", port);
		set(config, "connect-timeout", 2000L);
		set(config, "read-timeout", 2000L);
		set(config, "unavailable-policy", policy);
		return new ClamAvScanner(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	private static void set(ClamAvScanner.Config config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	/**
	 * Minimal <code>clamd</code> stand-in: accepts one connection, drains the <code>INSTREAM</code>
	 * chunks and answers with a fixed, null-terminated reply.
	 */
	private static final class MockClamd implements AutoCloseable {

		private final ServerSocket _server;

		private final Thread _thread;

		MockClamd(String reply) throws IOException {
			_server = new ServerSocket(0);
			_thread = new Thread(() -> serve(reply), "mock-clamd");
			_thread.setDaemon(true);
			_thread.start();
		}

		int getPort() {
			return _server.getLocalPort();
		}

		private void serve(String reply) {
			try (Socket connection = _server.accept()) {
				InputStream in = connection.getInputStream();
				drainCommand(in);
				drainChunks(in);

				OutputStream out = connection.getOutputStream();
				out.write(reply.getBytes(StandardCharsets.US_ASCII));
				out.write(0);
				out.flush();
			} catch (IOException ex) {
				// Connection closed by the client or during shutdown - nothing to do.
			}
		}

		private static void drainCommand(InputStream in) throws IOException {
			int b;
			while ((b = in.read()) != -1 && b != 0) {
				// Read the null-terminated "zINSTREAM" command.
			}
		}

		private static void drainChunks(InputStream in) throws IOException {
			while (true) {
				int length = readInt(in);
				if (length <= 0) {
					break;
				}
				for (int i = 0; i < length; i++) {
					if (in.read() == -1) {
						return;
					}
				}
			}
		}

		private static int readInt(InputStream in) throws IOException {
			int b1 = in.read();
			int b2 = in.read();
			int b3 = in.read();
			int b4 = in.read();
			if ((b1 | b2 | b3 | b4) < 0) {
				return 0;
			}
			return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
		}

		@Override
		public void close() throws IOException {
			_server.close();
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(ClamAvScannerTest.class));
	}

}
