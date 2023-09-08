/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.top_logic.basic.xml.DOMUtil;

/**
 * Event source producing SAX events for a {@link ContentHandler} from an in-memory {@link Document}.
 * 
 * <p>
 * This class does the following conversion:
 * </p>
 * 
 * <ul>
 * <li>White space is stripped from the beginning and the end of text nodes.</li>
 * <li>Comment nodes are ignored.</li>
 * </ul>
 * 
 * <p>
 * Special nodes are not supported:
 * </p>
 * 
 * <ul>
 * <li>{@link Node#DOCUMENT_TYPE_NODE}</li>
 * <li>{@link Node#ENTITY_NODE}</li>
 * <li>{@link Node#ENTITY_REFERENCE_NODE}</li>
 * <li>{@link Node#NOTATION_NODE}</li>
 * <li>{@link Node#PROCESSING_INSTRUCTION_NODE}</li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DOM2SAX {

	private static final class AttributesAdapter implements Attributes {
		private Element _element;

		private NamedNodeMap _attributes;

		public AttributesAdapter() {
			// Default constructor.
		}

		public void setElement(Element element) {
			_element = element;
			_attributes = null;
		}

		@Override
		public String getValue(String uri, String localName) {
			String result = _element.getAttributeNS(uri, localName);
			if (result.length() == 0) {
				if (!_element.hasAttributeNS(uri, localName)) {
					// Compatibility with the SAX API that defines to return null instead of the
					// empty string.
					return null;
				}
			}
			return result;
		}

		@Override
		public String getValue(String qName) {
			// TODO: Not correct for qualified names that have a prefix. Unclear how to handle
			// correctly in that case.
			return getValue(null, qName);
		}

		@Override
		public String getValue(int index) {
			return attribute(index).getNodeValue();
		}

		@Override
		public String getURI(int index) {
			return attribute(index).getNamespaceURI();
		}

		@Override
		public String getType(String uri, String localName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getType(String qName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getType(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getQName(int index) {
			Node attribute = attribute(index);
			String prefix = attribute.getPrefix();
			if (prefix == null) {
				return attribute.getLocalName();
			} else {
				return prefix + ":" + attribute.getLocalName();
			}
		}

		@Override
		public String getLocalName(int index) {
			return attribute(index).getLocalName();
		}

		@Override
		public int getLength() {
			return getAttributes().getLength();
		}

		@Override
		public int getIndex(String uri, String localName) {
			NamedNodeMap attributes = getAttributes();
			for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
				Node attribute = attributes.item(n);
				if (DOMUtil.hasNamespace(uri, attribute) && DOMUtil.hasLocalName(localName, attribute)) {
					return n;
				}
			}
			return -1;
		}

		@Override
		public int getIndex(String qName) {
			throw new UnsupportedOperationException();
		}

		private Node attribute(int index) {
			return getAttributes().item(index);
		}

		private NamedNodeMap getAttributes() {
			if (_attributes == null) {
				_attributes = _element.getAttributes();
			}
			return _attributes;
		}
	}

	private static final int BUFFER_LENGTH = 4096;

	/**
	 * Re-used buffer for sending text events.
	 */
	private char[] _buffer = new char[BUFFER_LENGTH];

	/**
	 * Re-used {@link Attributes} wrapper for sending element events.
	 */
	private AttributesAdapter _attributes = new AttributesAdapter();

	private final ContentHandler _handler;

	/**
	 * Creates a {@link DOM2SAX}.
	 * 
	 * @param handler
	 *        The destination of the events.
	 */
	public DOM2SAX(ContentHandler handler) {
		_handler = handler;
	}

	/**
	 * Produces events and sends them to the handler.
	 * 
	 * @param root
	 *        The root node from which events should be generated.
	 */
	public void produceEvents(Node root) throws SAXException {
		visit(root);
	}

	private void visit(Node node) throws SAXException {
		try {
			switch (node.getNodeType()) {
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
					handleText(node);
					break;
				case Node.COMMENT_NODE:
					// Ignored node.
					break;
				case Node.DOCUMENT_NODE:
					_handler.startDocument();
					descend(node);
					_handler.endDocument();
					break;
				case Node.ELEMENT_NODE:
					_attributes.setElement((Element) node);
					
					String namespaceURI = node.getNamespaceURI();
					String localName = node.getLocalName();
					_handler.startElement(namespaceURI, localName, localName, _attributes);
					descend(node);
					_handler.endElement(namespaceURI, localName, localName);
					break;
				case Node.DOCUMENT_FRAGMENT_NODE:
					descend(node);
					break;
				case Node.ATTRIBUTE_NODE:
					throw new AssertionError("Attribute nodes are not visited.");
				case Node.DOCUMENT_TYPE_NODE:
				case Node.ENTITY_NODE:
				case Node.ENTITY_REFERENCE_NODE:
				case Node.NOTATION_NODE:
				case Node.PROCESSING_INSTRUCTION_NODE:
				default:
					throw new UnsupportedOperationException("Unsupported node type: " + node.getNodeType());
			}
		} catch (SAXException ex) {
			throw enhanceError(node, ex);
		}
	}

	/**
	 * Enrich the given error message with context information.
	 * 
	 * @param node The node that caused the error.
	 * @param ex The original problem.
	 * @return The exception to throw instead.
	 */
	protected SAXException enhanceError(Node node, SAXException ex) {
		return ex;
	}

	private void descend(Node node) throws SAXException {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			visit(child);
		}
	}

	private void handleText(Node node) throws SAXException {
		String value = node.getNodeValue();
		int length = value.length();

		int start = 0;
		while (start < length && Character.isWhitespace(value.charAt(start))) {
			start++;
		}

		int last = length - 1;
		while (last >= start && Character.isWhitespace(value.charAt(last))) {
			last--;
		}

		if (start <= last) {
			int trimLength = last - start + 1;

			char[] buffer;
			if (trimLength <= BUFFER_LENGTH) {
				buffer = _buffer;
			} else {
				buffer = new char[trimLength];
			}

			value.getChars(start, last, buffer, 0);
			_handler.characters(buffer, 0, trimLength);
		}
	}

}
