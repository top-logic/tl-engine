/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.io.BinaryContent;

/**
 * {@link ByteArrayOutputStream} that provides its content also as {@link InputStream}.
 * 
 * <p>
 * This stream is not <i>synchronized</i>. It is not allowed to access input stream and output
 * stream concurrently.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ByteArrayStream extends ByteArrayOutputStream implements BinaryContent {

	/**
	 * Creates a new {@link ByteArrayStream}.
	 * 
	 * @see ByteArrayOutputStream#ByteArrayOutputStream()
	 */
	public ByteArrayStream() {
		super();
	}

	/**
	 * Creates a new {@link ByteArrayStream}.
	 * 
	 * @see ByteArrayOutputStream#ByteArrayOutputStream(int)
	 */
	public ByteArrayStream(int size) {
		super(size);
	}

	/**
	 * Returns the original buffer. Modification will ruin this stream.
	 * 
	 * <p>
	 * Note: The original buffer may be larger than {@link #size()}, therefore only the values from
	 * <code>0</code> to <code>{@link #size()}</code> (excl.) must be used.
	 * </p>
	 * 
	 * @see ByteArrayOutputStream#toByteArray()
	 */
	public byte[] getOrginalByteBuffer() {
		return buf;
	}

	/**
	 * Delivers the original buffer cache as {@link InputStream}.
	 * 
	 * <p>
	 * Note: This {@link ByteArrayOutputStream} must not be modified after this method is called.
	 * </p>
	 * 
	 * @see com.top_logic.basic.io.BinaryContent#getStream()
	 */
	@Override
	public ByteArrayInputStream getStream() throws IOException {
		return new ByteArrayInputStream(buf, 0, count);
	}

}

