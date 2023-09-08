/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A SAX content handler that forwards all events to a delegate handler.
 * 
 * The delegate handler can be changed during processing with
 * {@link #setDelegate(ContentHandler)}. In addition to forwarding all events
 * to its delegate, this handler also keeps track of the current nesting depth
 * of elements (see {@link #getElementDepth()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DelegatingContentHandler implements ContentHandler {

	/**
	 * The current delegate handler.
	 * 
	 * @see #setDelegate(ContentHandler)
	 */
	private ContentHandler delegate;

	/**
	 * @see #getElementDepth()
	 */
	private int elementDepth;

	/**
	 * Create a new {@link DelegatingContentHandler} with a given delegate.
	 * 
	 * @see #setDelegate(ContentHandler)
	 */
	public DelegatingContentHandler(ContentHandler aDelegate) {
		this.delegate = aDelegate;
	}

	/**
	 * Sets a new handler to delegate events to.
	 * 
	 * The delegate handler may be <code>null</code>, in which case all
	 * events are dropped.
	 * 
	 * @return The old delegate that was installed before.
	 */
	public ContentHandler setDelegate(ContentHandler newDelegate) {
		ContentHandler result = this.delegate;
		this.delegate = newDelegate;
		return result;
	}

	/**
	 * The currently installed delegate handler.
	 */
	public ContentHandler getDelegate() {
		return this.delegate;
	}

	/**
	 * Return the current nesting depth of elements. The nesting depth is the
	 * numner of calls to
	 * {@link #startElement(String, String, String, Attributes)}, that did not
	 * yet receive a corresponding {@link #endElement(String, String, String)}
	 * event.
	 */
	public int getElementDepth() {
		return this.elementDepth;
	}

	@Override
	public void startDocument() throws SAXException {
		if (delegate != null) {
			delegate.startDocument();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		if (delegate != null)
			delegate.endDocument();
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (delegate != null)
			delegate.startElement(namespaceURI, localName, qName, atts);
		elementDepth++;
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		elementDepth--;
		if (delegate != null)
			delegate.endElement(namespaceURI, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (delegate != null)
			delegate.characters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if (delegate != null)
			delegate.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		if (delegate != null)
			delegate.endPrefixMapping(prefix);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		if (delegate != null)
			delegate.skippedEntity(name);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		if (delegate != null)
			delegate.setDocumentLocator(locator);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		if (delegate != null)
			delegate.processingInstruction(target, data);
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		if (delegate != null)
			delegate.startPrefixMapping(prefix, uri);
	}

}
