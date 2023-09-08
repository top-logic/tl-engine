/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Random;

import test.com.top_logic.basic.BasicTestCase;

/**
 * Stream of dynamically created random data to test streaming interfaces.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingDataReader extends Reader {

	private boolean emptyChunkStartTested = false;
	private boolean emptyChunkMiddleTested = false;
	private boolean emptyChunkEndTested = false;
	private int pos = 0;
	private final int chunk;
	private final int size;
	
	private final Random source;

	/**
	 * Creates a {@link TestingDataReader}.
	 * 
	 * @param size
	 *        The number of <code>char</code>s that can be read from this
	 *        stream.
	 */
	public TestingDataReader(int size) {
		this(size, 1, 42);
	}

	/**
	 * Creates a {@link TestingDataReader}.
	 * 
	 * @param size
	 *        The number of <code>char</code>s that can be read from this
	 *        stream.
	 * @param seed
	 *        The random seed that determines the data content.
	 */
	public TestingDataReader(int size, int chunk, int seed) {
		this.size = size;
		this.chunk = chunk;
		this.source = new Random(seed);
	}

	@Override
	public int read() throws IOException {
		if (pos < size) {
			return nextChar();
		} else {
			return -1;
		}
	}

	@Override
	public int read(char[] b, int off, int len) throws IOException {
		// Simulate read in multiple chunks.
		if (! emptyChunkStartTested) {
			emptyChunkStartTested = true;
			return 0;
		}
		
		if (pos > 0) {
			if (! emptyChunkMiddleTested) {
				emptyChunkMiddleTested = true;
				return 0;
			}
		}
		
		int charsLeft = size - pos;
		if (charsLeft > 0) {
			int direct = Math.min(Math.min(len, charsLeft), chunk);
			BasicTestCase.randomString(source, direct, true, true, true, false).getChars(0, direct, b, off);
			pos += direct;
			return direct;
		}

		if (! emptyChunkEndTested) {
			emptyChunkEndTested = true;
			return 0;
		}
		
		return -1;
	}
	
	@Override
	public void close() throws IOException {
		// Ignore to be able to assert in test cases that all data has been
		// consumed by reading after close().
	}
	
	private int nextChar() {
		return BasicTestCase.randomString(source, 1, true, true, true, false).charAt(0);
	}

}