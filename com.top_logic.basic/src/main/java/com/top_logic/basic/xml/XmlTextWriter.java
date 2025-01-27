/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Adapter to allow writing text content to a {@link XMLStreamWriter} through the {@link Writer}
 * interface.
 */
public class XmlTextWriter extends Writer {

	private XMLStreamWriter _out;

	/**
	 * Creates a {@link XmlTextWriter}.
	 *
	 * @param out
	 *        The XML stream to write text to.
	 */
	public XmlTextWriter(XMLStreamWriter out) {
		_out = out;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		try {
			_out.writeCharacters(cbuf, off, len);
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void flush() throws IOException {
		// Ignore, no buffering.
	}

	@Override
	public void close() throws IOException {
		// Ignore.
	}

}
