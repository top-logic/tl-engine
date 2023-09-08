/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.io.ProtocolOutputStream;

/**
 * Tests {@link ProtocolOutputStream}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestProtocolOutputStream extends TestCase {

	public void testLogging() throws IOException {
		BufferingProtocol protocol = new BufferingProtocol();
		protocol.setVerbosity(Protocol.INFO);
		String content = "logMsg1\nlogMsg2";
		ProtocolOutputStream stream = new ProtocolOutputStream(protocol);
		try {
			stream.write(content.getBytes());
		} finally {
			stream.close();
		}
		assertLoggedStrings(protocol, "logMsg1", "logMsg2");
	}

	private void assertLoggedStrings(BufferingProtocol protocol, String... msg) {
		ArrayList<String> expectedProtocolInfos = new ArrayList<>();
		for (String message : msg) {
			String expectedLogInfo;
			// '.' at end of message is added by BufferingProtocol.
			if (!message.endsWith(".")) {
				expectedLogInfo = message + '.';
			} else {
				expectedLogInfo = message;
			}
			expectedProtocolInfos.add(expectedLogInfo);
		}
		assertEquals(expectedProtocolInfos, protocol.getInfos());
	}

	public void testLoggingWithCarriageReturn() throws IOException {
		BufferingProtocol protocol = new BufferingProtocol();
		protocol.setVerbosity(Protocol.INFO);
		String content = "logMsg1\r\nlog\rMsg2";
		ProtocolOutputStream stream = new ProtocolOutputStream(protocol);
		try {
			stream.write(content.getBytes());
		} finally {
			stream.close();
		}
		assertLoggedStrings(protocol, "logMsg1", "log\rMsg2");
	}

	public void testEmptyFlushing() throws IOException {
		BufferingProtocol protocol = new BufferingProtocol();
		protocol.setVerbosity(Protocol.INFO);
		ProtocolOutputStream stream = new ProtocolOutputStream(protocol);
		try {
			stream.write("logMsg1".getBytes());
			stream.flush();
			assertLoggedStrings(protocol, "logMsg1");

			stream.flush();
			assertSame("Flush without content must not log anything", 1, protocol.getInfos().size());
		} finally {
			stream.close();
		}
		assertLoggedStrings(protocol, "logMsg1");
	}

	public void testLogLevel() throws IOException {
		BufferingProtocol protocol = new BufferingProtocol();
		protocol.setVerbosity(Protocol.INFO);
		ProtocolOutputStream stream = new ProtocolOutputStream(protocol, Protocol.VERBOSE);
		try {
			stream.write("logMsg1".getBytes());
		} finally {
			stream.close();
		}
		assertEquals("Verbose logging does not write anything to info protocol.", BasicTestCase.list(),
			protocol.getInfos());
	}

}

