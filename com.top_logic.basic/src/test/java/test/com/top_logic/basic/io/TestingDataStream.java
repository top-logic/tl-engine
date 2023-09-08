/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Stream of dynamically created random data to test streaming interfaces.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingDataStream extends InputStream {

	private boolean emptyChunkStartTested;
	private boolean emptyChunkMiddleTested;
	private boolean emptyChunkEndTested;
	private int pos = 0;
	private final int chunk;
	private final long size;

	private final Random source;

	private boolean isClosed = false;

	/**
	 * Creates a {@link TestingDataStream}.
	 * 
	 * @param size
	 *        The size of the data that can be read from this stream.
	 */
	public TestingDataStream(long size) {
		this(size, 1, 42);
	}

	@Override
	public void close() throws IOException {
		isClosed = true;
		super.close();
	}	

	/**
	 * Creates a {@link TestingDataStream}.
	 * 
	 * @param size
	 *        The size of the data that can be read from this stream.
	 * @param seed
	 *        The random seed that determines the data content.
	 */
	public TestingDataStream(long size, int chunk, long seed) {
		this(size, chunk, seed, true);
	}

	/**
	 * Creates a {@link TestingDataStream}.
	 * 
	 * @param size
	 *        The size of the data that can be read from this stream.
	 * @param seed
	 *        The random seed that determines the data content.
	 * @param testEmptyChunks
	 *        Whether to produce empty chunks.
	 */
	public TestingDataStream(long size, int chunk, long seed, boolean testEmptyChunks) {
		this.size = size;
		this.chunk = chunk;
		this.source = new Random(seed);

		this.emptyChunkStartTested = ! testEmptyChunks;
		this.emptyChunkMiddleTested = ! testEmptyChunks;
		this.emptyChunkEndTested = ! testEmptyChunks;
	}

	@Override
	public int read() throws IOException {
		if(isClosed) throw new IOException("Stream is closed");
		if (pos < size) {
			int result = nextByte();
			pos++;
			return result;
		} else {
			return -1;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(isClosed) throw new IOException("Stream is closed");
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

		long bytesLeft = size - pos;
		if (bytesLeft > 0) {
			int direct = Math.min((int) Math.min(len, bytesLeft), chunk);
			for (int n = 0; n < direct; n++) {
				b[off++] = (byte) nextByte();
			}
			pos += direct;
			return direct;
		}

		if (! emptyChunkEndTested) {
			emptyChunkEndTested = true;
			return 0;
		}

		return -1;
	}

	private int nextByte() {
		return source.nextInt() & 0xFF;
	}

}