/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;

/**
 * Simple implementation of the {@link TemplateWriter} interface that uses a
 * {@link ByteArrayOutputStream} to write to.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TemplateOutputStreamWriter extends TemplateWriter {
	
	private ByteArrayOutputStream stream;

	/**
	 * Creates a new {@link TemplateOutputStreamWriter}.
	 */
	public TemplateOutputStreamWriter() {
		this.stream  = new ByteArrayOutputStream();
	}

	@Override
	public void writeLiteral(String literal) {
		try {
			this.stream.write(quote(literal).getBytes());
		}
		catch (IOException e) {
			Logger.error("Writing to output stream for " + literal + " failed.", e, TemplateOutputStreamWriter.class);
		}
	}

	@Override
	public void writeValue(Object value) {
		String theStringValue = StringServices.toString(value);
		try {
			this.stream.write(quote(theStringValue).getBytes());
		}
		catch (IOException e) {
			Logger.error("Writing to output stream for " + theStringValue + " failed.", e, TemplateOutputStreamWriter.class);
		}
	}
	
	/**
	 * Returns the internally used {@link ByteArrayOutputStream}.
	 */
	public ByteArrayOutputStream getStream() {
		return stream;
	}
	
}
