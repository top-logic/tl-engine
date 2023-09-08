/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Proxy implementation for the {@link XMLStreamWriter} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLStreamWriterProxy implements XMLStreamWriter {
	
	private final XMLStreamWriter impl;
	
	/**
	 * Creates a {@link XMLStreamWriterProxy}.
	 *
	 * @param impl See {@link #getImpl()}.
	 */
	public XMLStreamWriterProxy(XMLStreamWriter impl) {
		this.impl = impl;
	}
	
	/**
	 * The underlying implementation of this proxy.
	 */
	public XMLStreamWriter getImpl() {
		return impl;
	}

	@Override
	public void close() throws XMLStreamException {
		impl.close();
	}

	@Override
	public void flush() throws XMLStreamException {
		impl.flush();
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return impl.getNamespaceContext();
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return impl.getPrefix(uri);
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return impl.getProperty(name);
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		impl.setDefaultNamespace(uri);
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		impl.setNamespaceContext(context);
	}
	
	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		impl.setPrefix(prefix, uri);
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		impl.writeAttribute(localName, value);
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		impl.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
		impl.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
		impl.writeCData(data);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		impl.writeCharacters(text);
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		impl.writeCharacters(text, start, len);
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		impl.writeComment(data);
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		impl.writeDTD(dtd);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		impl.writeDefaultNamespace(namespaceURI);
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		impl.writeEmptyElement(localName);
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		impl.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		impl.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
		impl.writeEndDocument();
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		impl.writeEndElement();
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		impl.writeEntityRef(name);
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		impl.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		impl.writeProcessingInstruction(target);
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		impl.writeProcessingInstruction(target, data);
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		impl.writeStartDocument();
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		impl.writeStartDocument(version);
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		impl.writeStartDocument(encoding, version);
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		impl.writeStartElement(localName);
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		impl.writeStartElement(namespaceURI, localName);
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		impl.writeStartElement(prefix, localName, namespaceURI);
	}

}
