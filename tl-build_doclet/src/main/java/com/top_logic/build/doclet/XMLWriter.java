/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Base class for creating XML through a {@link XMLStreamWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLWriter implements Appendable {

	XMLStreamWriter _out;

	/**
	 * Creates a {@link XMLWriter}.
	 *
	 */
	public XMLWriter() {
		_out = null;
	}

	/**
	 * Creates a {@link XMLWriter}.
	 */
	public XMLWriter(XMLStreamWriter out) {
		_out = out;
	}

	void attribute(String localName, String value) throws XMLStreamException {
		if (value != null) {
			_out.writeAttribute(localName, value);
		}
	}

	void endElement() throws XMLStreamException {
		_out.writeEndElement();
	}

	void startElement(String name) throws XMLStreamException {
		_out.writeStartElement(name);
	}

	void emptyElement(String name) throws XMLStreamException {
		_out.writeEmptyElement(name);
	}

	void nl() throws XMLStreamException {
		_out.writeCharacters("\n");
	}

	@Override
	public Appendable append(char c) throws IOException {
		return append(Character.toString(c));
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {
		return append(csq.subSequence(start, end));
	}

	@Override
	public Appendable append(CharSequence csq) throws IOException {
		try {
			_out.writeCharacters(csq.toString());
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		}
		return this;
	}

}
