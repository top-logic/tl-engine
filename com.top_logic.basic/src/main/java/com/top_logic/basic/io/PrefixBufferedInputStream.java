/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} that buffers a number og bytes form the start of the Stream.
 * 
 * <p>
 * In contrast to {@link BufferedInputStream}, the buffered bytes can still be
 * read after this stream has been closed. This can be quite useful in case you
 * must post mortem analyze why some StreamContent caused an error. 
 * </p>
 * 
 * This class is used by the 
 * <code>com.top_logic.base.services.simpleajax.AJAXServlet</code>
 * to log a (broken) request.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrefixBufferedInputStream extends FilterInputStream {

    /** Default prefixSize of 4096 bytes */
    public static final int DEFAULT_PREFIX = 4096;

    /** Position in {@link #buffer} */
	private int    pos;

	/** Buffer this amount of initial bytes */
	private byte[] buffer;
	
	public PrefixBufferedInputStream(InputStream in, int prefixSize) {
		super(in);
		this.pos    = 0;
		this.buffer = new byte[prefixSize];
	}
	
	/**
	 * Use a default prefixSize {@link #DEFAULT_PREFIX}.
	 */
    public PrefixBufferedInputStream(InputStream in) {
        this(in, DEFAULT_PREFIX);
    }


	@Override
	public int read() throws IOException {
		int result = super.read();
		
		if (result >= 0 && pos < buffer.length) {
			buffer[pos++] = (byte) result;
		}
		
		return result;
	}
		
	@Override
	public int read(byte[] readResult, int offset, int length) throws IOException {
        int bufferLeft = buffer.length - pos;
		int bytesRead  = super.read(readResult, offset, length);
		if (bufferLeft > 0 && bytesRead >= 0) {
            int bufferedLength = Math.min(bufferLeft, bytesRead);
            System.arraycopy(readResult, offset, buffer, pos, bufferedLength);
            pos += bufferedLength;
		}
				
		return bytesRead;
	}

	/**
	 * Number of valid bytes in {@link #getBuffer()}.
	 */
	public int getSize() {
		return pos;
	}

	/**
	 * Buffer with {@link #getSize()} buffered bytes.
	 */
	public byte[] getBuffer() {
		return buffer;
	}
	
}
