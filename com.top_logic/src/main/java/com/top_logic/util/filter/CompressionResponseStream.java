/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package com.top_logic.util.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

/**
 * Original Code from the Apache Tomcat examples
 * (%CATALINA_HOME%\webapps\jsp-examples\WEB-INF\classes\compressionFilters\)
 * Authors Amy Roh, Dmitri Valdin.
 * 
 * @author <a href="mailto:tma@top-logic.com>tma</a>
 */
class CompressionResponseStream extends ServletOutputStream {

	/**
	 * The threshold number which decides to compress or not. Users can
	 * configure in web.xml to set it to fit their needs.
	 */
	protected int compressionThreshold = 0;

	/**
	 * The buffer through which all of our output bytes are passed.
	 */
	protected byte[] buffer = null;

	/**
	 * The number of data bytes currently in the buffer.
	 */
	protected int bufferCount = 0;

	/**
	 * The underlying gzip output stream to which we should write data.
	 */
	protected OutputStream gzipstream = null;

	/**
	 * Has this stream been closed?
	 */
	protected boolean closed = false;

	/**
	 * The content length past which we will not write, or -1 if there is no
	 * defined content length.
	 */
	protected int length = -1;

	/**
	 * The response with which this servlet output stream is associated.
	 */
	protected HttpServletResponse response = null;

	/**
	 * The underlying servket output stream to which we should write data.
	 */
	protected ServletOutputStream output = null;

	/**
	 * Construct a servlet output stream associated with the specified Response.
	 * 
	 * @param aResponse
	 *        The associated response
	 * @param threshold
	 *        number of bytes after which compression will start.
	 */
	public CompressionResponseStream(HttpServletResponse aResponse, int threshold) throws IOException {
		super();
		this.response = aResponse;
		this.output = response.getOutputStream();
		this.compressionThreshold = threshold;
		this.buffer = new byte[compressionThreshold];
	}

	/**
	 * Close this output stream, causing any buffered data to be flushed and any
	 * further output data to throw an IOException.
	 */
	@Override
	public void close() throws IOException {
		if (closed) {
			return;
		}

		// flushes anything to streams
		finish();

		if (gzipstream != null) {
			gzipstream.close();
		} else {
			output.close();
		}

		closed = true;
	}

	/**
	 * Flush any buffered data for this output stream, which also causes the
	 * response to be committed.
	 */
	@Override
	public void flush() throws IOException {
		if (closed) {
			return;
		}

		if (gzipstream != null) {
			gzipstream.flush();
		}
	}

	/**
	 * Finish the response.
	 * 
	 * @see java.util.zip.DeflaterOutputStream#finish()
	 */
	public void finish() throws IOException {
		if (closed) {
			return;
		}

		if (gzipstream != null) {
			flushToGZip();
			if (gzipstream instanceof GZIPOutputStream) {
				((GZIPOutputStream) gzipstream).finish();
			}
		} else {
			/*
			 * Not enough data to zip response, flush anything direct into
			 * output stream of response.
			 */
			flushToOutput();
		}
	}

	/**
	 * Write the specified byte to our output stream.
	 * 
	 * @param b
	 *        The byte to be written
	 * 
	 * @exception IOException
	 *            if an input/output error occurs
	 */
	@Override
	public void write(int b) throws IOException {
		ensureOpen("Cannot write to a closed output stream");

		if (bufferCount >= compressionThreshold) {
			flushToGZip();
		}

		buffer[bufferCount++] = (byte) b;
	}

	/**
	 * Write <code>b.length</code> bytes from the specified byte array to our
	 * output stream.
	 * 
	 * @param b
	 *        The byte array to be written
	 * 
	 * @exception IOException
	 *            if an input/output error occurs
	 */
	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	/**
	 * Write <code>len</code> bytes from the specified byte array, starting at
	 * the specified offset, to our output stream.
	 * 
	 * @param b
	 *        The byte array containing the bytes to be written
	 * @param off
	 *        Zero-relative starting offset of the bytes to be written
	 * @param len
	 *        The number of bytes to be written
	 * 
	 * @exception IOException
	 *            if an input/output error occurs
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		ensureOpen("Cannot write to a closed output stream");

		if (len == 0)
			return;

		// Can we write into buffer ?
		if (len <= (compressionThreshold - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			return;
		}

		// There is not enough space in buffer. Flush it ...
		flushToGZip();

		// ... and try again. Note, that bufferCount = 0 here !
		if (len <= (compressionThreshold - bufferCount)) {
			System.arraycopy(b, off, buffer, bufferCount, len);
			bufferCount += len;
			return;
		}

		// write direct to gzip
		writeToGZip(b, off, len);
	}

	/**
	 * Copies the buffer to the zipped stream and resets the
	 * {@link #bufferCount}.
	 * 
	 * @see #writeToGZip(byte[], int, int)
	 */
	private void flushToGZip() throws IOException {
		if (bufferCount > 0) {
			writeToGZip(buffer, 0, bufferCount);
			bufferCount = 0;
		}
	}

	/**
	 * Copies the buffer to the output stream and resets the
	 * {@link #bufferCount}.
	 * 
	 * @see OutputStream#write(byte[], int, int)
	 */
	private void flushToOutput() throws IOException {
		if (bufferCount > 0) {
			output.write(buffer, 0, bufferCount);
			bufferCount = 0;
		}
	}

	/**
	 * Installs a {@link GZIPOutputStream} (if not already present) and writes
	 * <code>len</code> bytes from the given array starting with the
	 * <code>off</code>'th to the zipped stream.
	 * 
	 * @see GZIPOutputStream#write(byte[], int, int)
	 */
	private void writeToGZip(byte b[], int off, int len) throws IOException {
		if (gzipstream == null) {
			response.addHeader("Content-Encoding", "gzip");
			String actualValue = response.getHeader("Content-Encoding");
			if (actualValue != null && actualValue.contains("gzip")) {
				// Setting the header succeeded.
				gzipstream = new GZIPOutputStream(output);
			} else {
				// The header could not be set. The container may reject setting a header under
				// certain conditions. Actually the Jetty server disallows setting a response
				// header, if the response is currently including another resource.
				gzipstream = output;
			}
		}
		gzipstream.write(b, off, len);
	}

	/**
	 * Checks whether this stream is still open
	 * 
	 * @param failureMessage
	 *        detailed message when the stream is already closed
	 * 
	 * @throws IOException
	 *         iff this stream is already closed
	 */
	private void ensureOpen(String failureMessage) throws IOException {
		if (closed)
			throw new IOException(failureMessage);
	}

	/**
	 * Has this response stream been closed?
	 * 
	 * @return true if the stream is closed, otherwise false
	 */
	public boolean isClosed() {
		return (this.closed);
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// Ignore.
	}

}
