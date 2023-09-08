/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link XMLStreamIndentationWriter} proxy that applies basic indentation to
 * the output.
 * 
 * <p>
 * The indentation strategy does not consider elements with mixed content
 * models like most elements in XHTML.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLStreamIndentationWriter extends XMLStreamWriterProxy {
	
	private final String newline = "\n";
	private final String tab = "\t";
	
	private StringBuilder indent = new StringBuilder();
	
	private int depth;
	
	private boolean hasElementContent = true;

	/**
	 * Creates a {@link XMLStreamIndentationWriter}.
	 *
	 * @param impl See {@link #getImpl()}.
	 */
	public XMLStreamIndentationWriter(XMLStreamWriter impl) {
		super(impl);
	}
	
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		beforeStart();
		
		super.writeStartElement(localName);
		
		afterStart();
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		beforeStart();
		
		super.writeStartElement(namespaceURI, localName);
		
		afterStart();
	}
	
	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		beforeStart();
		
		super.writeStartElement(prefix, localName, namespaceURI);
		
		afterStart();
	}
	
	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		beforeEmpty();

		super.writeEmptyElement(localName);
		
		afterEmpty();
	}
	
	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		beforeEmpty();

		super.writeEmptyElement(namespaceURI, localName);
		
		afterEmpty();
	}
	
	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		beforeEmpty();

		super.writeEmptyElement(prefix, localName, namespaceURI);
		
		afterEmpty();
	}
	
	@Override
	public void writeEndElement() throws XMLStreamException {
		beforeEnd();
		
		super.writeEndElement();
		
		afterEnd();
	}
	
	/**
	 * Hook called before writing an empty element.
	 */
	private void beforeEmpty() throws XMLStreamException {
		hasElementContent = true;
		writeIndent();
	}
	
	/**
	 * Hook called after writing an empty element.
	 */
	private void afterEmpty() throws XMLStreamException {
		// Nothing to do.
	}

	/**
	 * Hook called before writing a start element.
	 */
	private void beforeStart() throws XMLStreamException {
		hasElementContent = true;
		writeIndent();
		indent();
	}

	/**
	 * Hook called after writing a start element.
	 */
	private void afterStart() throws XMLStreamException {
		hasElementContent = false;
	}
	
	/**
	 * Hook called before writing an end element.
	 */
	private void beforeEnd() throws XMLStreamException {
		unindent();
		writeIndent();
	}

	/**
	 * Hook called after writing an end element.
	 */
	private void afterEnd() throws XMLStreamException {
		hasElementContent = true;
	}

	/**
	 * Writes white space for indenting the next output.
	 */
	private void writeIndent() throws XMLStreamException {
		if (hasElementContent) {
			super.writeCharacters(newline);
			super.writeCharacters(indent.toString());
		}
	}
	
	/**
	 * Increments the indentation.
	 */
	private void indent() {
		depth++;
		indent.append(tab);
	}
	
	/**
	 * Decrements the indentation.
	 */
	private void unindent() {
		depth--;
		indent.setLength(indent.length() - tab.length());
	}
	
}
