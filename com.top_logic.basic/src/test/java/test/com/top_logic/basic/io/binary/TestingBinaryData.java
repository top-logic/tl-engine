/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;

import test.com.top_logic.basic.io.TestingDataStream;

import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link BinaryData} that generates its content on the fly using a random
 * sequence.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingBinaryData extends AbstractBinaryData {

	final int seed;
	final long size;
	final boolean testEmptyChunks;

	private String _contentType = CONTENT_TYPE_OCTET_STREAM;

	/**
	 * Creates a {@link TestingBinaryData}.
	 *
	 * @param seed The random seed.
	 * @param size The size of the contents.
	 * @param testEmptyChunks See {@link TestingDataStream#TestingDataStream(long, int, long, boolean)}.
	 */
	public TestingBinaryData(int seed, long size, boolean testEmptyChunks) {
		this.seed = seed;
		this.size = size;
		this.testEmptyChunks = testEmptyChunks;
	}

	/**
	 * Creates a {@link TestingBinaryData}.
	 * 
	 * @param seed
	 *        The random seed.
	 * @param size
	 *        The size of the contents.
	 */
	public TestingBinaryData(int seed, long size) {
		this(seed, size, false);
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public InputStream getStream() throws IOException {
		return new TestingDataStream(size, 4096, seed, testEmptyChunks);
	}

	@Override
	public String getName() {
		return getClass().getName() + "(seed: " + seed + ", size: " + size + ", emptyChunks: " + testEmptyChunks + ")";
	}
	
	@Override
	public String getContentType() {
		return _contentType;
	}

	/**
	 * Setter for {@link #getContentType()}.
	 */
	public void setContentType(String contentType) {
		_contentType = contentType;
	}

}
