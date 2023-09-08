/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link CombinedWriter} has some internal {@link Writer} and dispatches the
 * abstract methods to each internal {@link Writer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CombinedWriter extends Writer {

	private final Writer[] writer;

	/**
	 * Creates a new {@link CombinedWriter} with the {@link Writer} in the given
	 * array as inner {@link Writer}.
	 * 
	 * @param writer
	 *        the writer to dispatch to. must not be <code>null</code>. each
	 *        {@link Writer} in the array must not be <code>null</code>
	 */
	public CombinedWriter(Writer[] writer) {
		this.writer = new Writer[writer.length];
		System.arraycopy(writer, 0, this.writer, 0, writer.length);
	}

	/**
	 * Closes all inner {@link Writer}.
	 * 
	 * @see Writer#close()
	 * @see CombinedIOException
	 */
	@Override
	public void close() throws CombinedIOException {
		CombinedIOException exep = null;
		for (int i = 0; i < writer.length; i++) {
			try {
				writer[i].close();
			} catch (IOException ex) {
				if (exep == null) {
					exep = new CombinedIOException();
				}
				exep.addException(ex);
			}
		}
		if (exep != null) {
			throw exep;
		}
	}

	/**
	 * Flushes all inner {@link Writer}.
	 * 
	 * @see Writer#flush()
	 * @see CombinedIOException
	 */
	@Override
	public void flush() throws CombinedIOException {
		CombinedIOException exep = null;
		for (int i = 0; i < writer.length; i++) {
			try {
				writer[i].flush();
			} catch (IOException ex) {
				if (exep == null) {
					exep = new CombinedIOException();
				}
				exep.addException(ex);
			}
		}
		if (exep != null) {
			throw exep;
		}
	}

	/**
	 * Writes the given char's to all inner {@link Writer}.
	 * 
	 * @see Writer#write(char[], int, int)
	 * @see CombinedIOException
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws CombinedIOException {
		CombinedIOException exep = null;
		for (int i = 0; i < writer.length; i++) {
			try {
				writer[i].write(cbuf, off, len);
			} catch (IOException ex) {
				if (exep == null) {
					exep = new CombinedIOException();
				}
				exep.addException(ex);
			}
		}
		if (exep != null) {
			throw exep;
		}
	}

	/**
	 * A {@link CombinedWriter.CombinedIOException} is an {@link IOException}
	 * thrown by a {@link CombinedWriter}. It contains a {@link Collection} of
	 * {@link IOException} which were thrown by the inner {@link Writer} of the
	 * {@link CombinedWriter}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class CombinedIOException extends IOException {

		private final List/* <IOException> */l = new ArrayList/* <IOException> */();

		public CombinedIOException() {
		}

		void addException(IOException ex) {
			l.add(ex);
		}

		@Override
		public String getMessage() {
			StringBuffer result = new StringBuffer("CombinedIOException: ");
			for (int i = 0; i < l.size(); i++) {
				result.append(((IOException) l.get(i)).getMessage());
			}
			return result.toString();
		}

		@Override
		public String getLocalizedMessage() {
			StringBuffer result = new StringBuffer("CombinedIOException: ");
			for (int i = 0; i < l.size(); i++) {
				result.append(((IOException) l.get(i)).getLocalizedMessage());
			}
			return result.toString();
		}

		public Collection/* <IOException> */getExceptions() {
			return Collections.unmodifiableCollection(l);
		}

	}

}
