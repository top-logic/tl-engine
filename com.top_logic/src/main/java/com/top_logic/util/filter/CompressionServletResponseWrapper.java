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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * A HttpServletResponse wrapping a Zip-Stream.
 * 
 * Original Code from the Apache Tomcat examples 
 * (%CATALINA_HOME%\webapps\jsp-examples\WEB-INF\classes\compressionFilters\)
 * Authors Amy Roh, Dmitri Valdin.
 * 
 * @author  <a href="mailto:tma@top-logic.com>tma</a>
 * @author  <a href="mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class CompressionServletResponseWrapper extends HttpServletResponseWrapper {

	/** Descriptive information about this Response implementation. */
	protected static final String info = "CompressionServletResponseWrapper";

	/**
	 * The CompressionResponseStream that has been returned by
	 * <code>getOutputStream()</code>, if any.
	 */
	protected CompressionResponseStream stream;

	/**
	 * The PrintWriter that has been returned by <code>getWriter()</code>, if
	 * any.
	 */
	protected PrintWriter writer;

	/**
	 * The threshold number to compress
	 */
	protected int threshold;


	/**
	 * Calls the parent constructor which creates a ServletResponse adaptor
	 * wrapping the given response object.
	 * 
	 * @param response the wrapped response
	 */
	public CompressionServletResponseWrapper(HttpServletResponse response) {
		super(response);
	}


	/**
	 * Set threshold number
	 */
	public void setCompressionThreshold(int aThreshold) {
		this.threshold = aThreshold;
	}

	/**
	 * Create and return a CompressionResponseStream to write the content associated
	 * with this Response.
	 * 
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	private CompressionResponseStream createOutputStream() throws IOException {
		return new CompressionResponseStream((HttpServletResponse) originalResponse(), threshold);
	}

	private ServletResponse originalResponse() {
		ServletResponse result = super.getResponse();
		while (result instanceof ServletResponseWrapper) {
			result = ((ServletResponseWrapper) result).getResponse();
		}
		return result;
	}
	
	/**
	 * Finish a response.
	 * 
	 * @see CompressionResponseStream#finish()
	 */
	public void finishResponse() throws IOException {
		if (writer != null) {
			writer.flush();
			writer = null;
		}
		if (stream != null) {
			stream.finish();
			stream = null;
		}
	}

	/**
	 * Flush the buffer and commit this response.
	 * 
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) {
			writer.flush();
		} else {
			stream.flush();
		}
	}

	/**
	 * Return the servlet output stream associated with this Response.
	 * 
	 * @exception IllegalStateException
	 *                if <code>getWriter</code> has already been called for
	 *                this response
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IllegalStateException("getWriter() has already been called for this response, " +
					"you may only call either getWriter() or getOutputStream()");

		if (stream == null)
			stream = createOutputStream();

		return stream;
	}

	/**
	 * Return the writer associated with this Response.
	 * 
	 * @exception IllegalStateException
	 *                if <code>getOutputStream</code> has already been called
	 *                for this response
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer != null)
			return (writer);

		if (stream != null)
			throw new IllegalStateException("getOutputStream() has already been called for this response" +
					"you may only call either getWriter() or getOutputStream()");

		stream = createOutputStream();
		// String charset = getCharsetFromContentType(contentType);
		String charEnc = super.getResponse().getCharacterEncoding();
		// HttpServletResponse.getCharacterEncoding() shouldn't return null
		// according the specification, so feel free to remove that "if"
		if (charEnc != null) {
			writer = new PrintWriter(new OutputStreamWriter(stream, charEnc));
		} else {
			writer = new PrintWriter(stream);
		}

		return writer;
	}
	
    /**
     * Overridden to suppress it.
     * 
     * Cannot set content length due to compression
     */
	@Override
	public void setContentLength(int length) {
        // Cannot set content length due to compression
	}

	@Override
	public void setContentLengthLong(long len) {
		// Cannot set content length due to compression
	}

}
