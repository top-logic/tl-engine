/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Proxy for an {@link InputStream}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ProxyInputStream extends InputStream {

	/**
	 * {@link InputStream} implementation to delegate all methods to.
	 * 
	 * @return The {@link InputStream} to read.
	 * 
	 * @throws IOException
	 *         When creating {@link InputStream} fails.
	 */
	protected abstract InputStream getImpl() throws IOException;

	@Override
	public int read() throws IOException {
		return getImpl().read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return getImpl().read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return getImpl().read(b, off, len);
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		return getImpl().readAllBytes();
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		return getImpl().readNBytes(b, off, len);
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		return getImpl().readNBytes(len);
	}

	@Override
	public synchronized void mark(int readlimit) {
		getImplNoDeclaredException().mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return getImplNoDeclaredException().markSupported();
	}

	InputStream getImplNoDeclaredException() {
		try {
			return getImpl();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	@Override
	public int available() throws IOException {
		return getImpl().available();
	}

	@Override
	public long skip(long n) throws IOException {
		return getImpl().skip(n);
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return getImpl().transferTo(out);
	}

}

