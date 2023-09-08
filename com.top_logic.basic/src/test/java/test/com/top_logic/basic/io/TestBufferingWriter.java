/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

import com.top_logic.basic.io.BufferingWriter;

/**
 * Test case for {@link BufferingWriter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBufferingWriter extends TestCase {

	private StringWriter _result;

	private BufferingWriter _writer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_result = new StringWriter();
		_writer = new BufferingWriter(_result, 10, 10);
	}

	public void testWrite() throws IOException {
		_writer.write("xxxHelloxxx".toCharArray(), 3, 5);
		_writer.write(' ');
		_writer.write("xxxworldxxx", 3, 5);
		_writer.write('!');
		_writer.flush();

		assertEquals(_result.toString(), "Hello world!");

		_writer.close();

		assertEquals(_result.toString(), "Hello world!");

		_writer.close();

		assertEquals(_result.toString(), "Hello world!");

		try {
			_writer.write("after close");
			fail("Expecting failure.");
		} catch (IOException ex) {
			// Expected.
		}
	}

	public void testFlushChar() throws IOException {
		_writer.write("1234567890");
		_writer.write('!');
		_writer.flush();

		assertEquals(_result.toString(), "1234567890!");
	}

	public void testZeroLength() throws IOException {
		_writer.write("");
		_writer.write(new char[0]);
		_writer.flush();

		assertEquals(_result.toString(), "");
	}

	public void testLargeStringWrite() throws IOException {
		_writer.write("12345678901234567890");
		_writer.flush();

		assertEquals(_result.toString(), "12345678901234567890");
	}

	public void testLargeCharArrayWrite() throws IOException {
		_writer.write("12345678901234567890".toCharArray());
		_writer.flush();

		assertEquals(_result.toString(), "12345678901234567890");
	}

	public void testStringFlush() throws IOException {
		_writer.write("12345");
		_writer.write("1234567");
		_writer.flush();

		assertEquals(_result.toString(), "123451234567");
	}

	public void testCharArrayFlush() throws IOException {
		_writer.write("12345".toCharArray());
		_writer.write("1234567".toCharArray());
		_writer.flush();

		assertEquals(_result.toString(), "123451234567");
	}

}
