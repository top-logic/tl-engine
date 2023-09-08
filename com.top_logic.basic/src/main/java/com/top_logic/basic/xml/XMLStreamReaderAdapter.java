/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Adapter for {@link XMLStreamReader}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class XMLStreamReaderAdapter implements XMLStreamReader {

	private XMLStreamReader reader;

	public XMLStreamReaderAdapter(XMLStreamReader reader) {
		this.reader = reader;
	}

	public void setDelegate(XMLStreamReader reader) {
		this.reader = reader;
	}

	public XMLStreamReader getDelegate() {
		return reader;
	}

	@Override
	public int next() throws XMLStreamException {
		return reader.next();
	}

	@Override
	public int nextTag() throws XMLStreamException {
		return reader.nextTag();
	}

	@Override
	public String getElementText() throws XMLStreamException {
		return reader.getElementText();
	}

	@Override
	public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
		reader.require(type, namespaceURI, localName);
	}

	@Override
	public boolean hasNext() throws XMLStreamException {
		return reader.hasNext();
	}

	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return reader.getNamespaceURI(prefix);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return reader.getNamespaceContext();
	}

	@Override
	public boolean isStartElement() {
		return reader.isStartElement();
	}

	@Override
	public boolean isEndElement() {
		return reader.isEndElement();
	}

	@Override
	public boolean isCharacters() {
		return reader.isCharacters();
	}

	@Override
	public boolean isWhiteSpace() {
		return reader.isWhiteSpace();
	}

	@Override
	public QName getAttributeName(int index) {
		return reader.getAttributeName(index);
	}

	@Override
	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
		return reader.getTextCharacters(sourceStart, target, targetStart, length);
	}

	@Override
	public String getAttributeValue(String namespaceUri, String localName) {
		return reader.getAttributeValue(namespaceUri, localName);
	}

	@Override
	public int getAttributeCount() {
		return reader.getAttributeCount();
	}

	@Override
	public String getAttributePrefix(int index) {
		return reader.getAttributePrefix(index);
	}

	@Override
	public String getAttributeNamespace(int index) {
		return reader.getAttributeNamespace(index);
	}

	@Override
	public String getAttributeLocalName(int index) {
		return reader.getAttributeLocalName(index);
	}

	@Override
	public String getAttributeType(int index) {
		return reader.getAttributeType(index);
	}

	@Override
	public String getAttributeValue(int index) {
		return reader.getAttributeValue(index);
	}

	@Override
	public boolean isAttributeSpecified(int index) {
		return reader.isAttributeSpecified(index);
	}

	@Override
	public int getNamespaceCount() {
		return reader.getNamespaceCount();
	}

	@Override
	public String getNamespacePrefix(int index) {
		return reader.getNamespacePrefix(index);
	}

	@Override
	public String getNamespaceURI(int index) {
		return reader.getNamespaceURI(index);
	}

	@Override
	public int getEventType() {
		return reader.getEventType();
	}

	@Override
	public String getText() {
		return reader.getText();
	}

	@Override
	public char[] getTextCharacters() {
		return reader.getTextCharacters();
	}

	@Override
	public int getTextStart() {
		return reader.getTextStart();
	}

	@Override
	public int getTextLength() {
		return reader.getTextLength();
	}

	@Override
	public String getEncoding() {
		return reader.getEncoding();
	}

	@Override
	public boolean hasText() {
		return reader.hasText();
	}

	@Override
	public Location getLocation() {
		return reader.getLocation();
	}

	@Override
	public QName getName() {
		return reader.getName();
	}

	@Override
	public String getLocalName() {
		return reader.getLocalName();
	}

	@Override
	public boolean hasName() {
		return reader.hasName();
	}

	@Override
	public String getNamespaceURI() {
		return reader.getNamespaceURI();
	}

	@Override
	public String getPrefix() {
		return reader.getPrefix();
	}

	@Override
	public String getVersion() {
		return reader.getVersion();
	}

	@Override
	public boolean isStandalone() {
		return reader.isStandalone();
	}

	@Override
	public boolean standaloneSet() {
		return reader.standaloneSet();
	}

	@Override
	public String getCharacterEncodingScheme() {
		return reader.getCharacterEncodingScheme();
	}

	@Override
	public String getPITarget() {
		return reader.getPITarget();
	}

	@Override
	public String getPIData() {
		return reader.getPIData();
	}

	@Override
	public Object getProperty(String name) {
		return reader.getProperty(name);
	}
}
