/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import static com.top_logic.config.xdiff.model.NodeFactory.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.NamedNodeMap;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.config.xdiff.util.Utils;

/**
 * Parser creating {@link Node} hierachies.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentBuilder {
	
	static final String DOM_EMPTY_PROPERTY = "EMPTY";

	private final boolean _collapseWhiteSpace;

	private List<Attribute> attributeBuffer = new ArrayList<>();

	private DocumentBuilder(boolean collapseWhiteSpace) {
		_collapseWhiteSpace = collapseWhiteSpace;
	}

	/**
	 * Creates a {@link DocumentBuilder} that does not strip white space.
	 * 
	 * @see #newDocumentBuilder(boolean)
	 */
	public static DocumentBuilder newDocumentBuilder() {
		return newDocumentBuilder(false);
	}

	/**
	 * Creates a {@link DocumentBuilder}.
	 * 
	 * @param collapseWhiteSpace
	 *        Whether to collapse white space in element content.
	 * @return The new {@link DocumentBuilder}.
	 */
	public static DocumentBuilder newDocumentBuilder(boolean collapseWhiteSpace) {
		return new DocumentBuilder(collapseWhiteSpace);
	}

	/**
	 * Parses the given stream literally into a new {@link Document}.
	 * 
	 * @see #parseDocument(boolean, XMLStreamReader)
	 */
	public static Document parseDocument(XMLStreamReader in) throws XMLStreamException {
		return parseDocument(false, in);
	}

	/**
	 * Parses events from the given stream into a new {@link Document}.
	 * 
	 * @param collapseWhiteSpace
	 *        Whether to collapse white space in element content.
	 * @param in
	 *        The stream to parse.
	 */
	public static Document parseDocument(boolean collapseWhiteSpace, XMLStreamReader in) throws XMLStreamException {
		Document document = document();
		newDocumentBuilder(collapseWhiteSpace).parse(in, document);
		document.init();
		return document;
	}

	private void parse(XMLStreamReader in, FragmentBase context) throws XMLStreamException {
		readChildren:
		while (true) {
			int event = in.next();
			switch (event) {
				case XMLStreamConstants.CDATA:
					context.addChild(cdata(in.getText()));
					break;
				case XMLStreamConstants.SPACE:
					if (!_collapseWhiteSpace) {
						context.addChild(text(in.getText()));
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					String contents = in.getText();
					if ((!_collapseWhiteSpace) || (!Utils.isWhiteSpace(contents))) {
						context.addChild(text(contents));
					}
					break;
				case XMLStreamConstants.COMMENT:
					if (!_collapseWhiteSpace) {
						context.addChild(comment(in.getText()));
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					Element element = element(in.getNamespaceURI(), in.getLocalName());
					context.addChild(element);

					List<Attribute> attributes = attributeBuffer;
					for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
						Attribute attribute =
							new Attribute(
								XMLStreamUtil.getAttributeNamespace(in, n), in.getAttributeLocalName(n), in.getAttributeValue(n));
						attributes.add(attribute);
					}
					element.setAttributes(attributes);
					attributes.clear();

					int startOffset = in.getLocation().getCharacterOffset();
					parse(in, element);
					int endOffset = in.getLocation().getCharacterOffset();

					element.setEmpty(endOffset == startOffset);
					break;
				case XMLStreamConstants.END_ELEMENT:
				case XMLStreamConstants.END_DOCUMENT:
					// Continue with outer fragment.
					break readChildren;
				case XMLStreamConstants.START_DOCUMENT:
					// Ignore.
					break;
				case XMLStreamConstants.ENTITY_REFERENCE:
					context.addChild(entityRef(in.getLocalName()));
					break;
				case XMLStreamConstants.DTD:
				case XMLStreamConstants.ENTITY_DECLARATION:
				case XMLStreamConstants.NOTATION_DECLARATION:
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					break;
				case XMLStreamConstants.ATTRIBUTE:
				case XMLStreamConstants.NAMESPACE:
					throw new UnsupportedOperationException("Encountered unsupported event type: " + event);
			}
		}
	}

	/**
	 * Converts from a {@link Document} to a W3C document.
	 */
	public static org.w3c.dom.Document convertToDOM(com.top_logic.config.xdiff.model.Document document) {
		org.w3c.dom.Document result = DOMUtil.newDocument();
		convertToDOM(result, null, document);
		return result;
	}

	/**
	 * Converts from a {@link Node} to a W3C node.
	 * 
	 * @param into
	 *        The parent into which the conversion is inserted.
	 * @param before
	 *        A reference child of the given parent, before which the conversion is inserted.
	 *        <code>null</code> means appending to the children of the parent.
	 * @param source
	 *        The node to convert.
	 */
	public static void convertToDOM(org.w3c.dom.Node into, org.w3c.dom.Node before, Node source) {
		source.visit(new DOMCreator(before), into);
	}

	/**
	 * Converts the given W3C node to a {@link Node}.
	 */
	public static Node convertFromDOM(org.w3c.dom.Node node) {
		switch (node.getNodeType()) {
			case org.w3c.dom.Node.DOCUMENT_NODE:
				return document(convertContentsFromDOM(node));
			case org.w3c.dom.Node.ELEMENT_NODE:
				org.w3c.dom.Element element = (org.w3c.dom.Element) node;
				return convertElementShallow(element).setChildren(convertContentsList(element));
			case org.w3c.dom.Node.CDATA_SECTION_NODE:
				return cdata(node.getNodeValue());
			case org.w3c.dom.Node.TEXT_NODE:
				return text(node.getNodeValue());
			case org.w3c.dom.Node.COMMENT_NODE:
				return comment(node.getNodeValue());
			case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
				return entityRef(node.getLocalName());
			default:
				throw new UnsupportedOperationException("Unsupported node " + node.getNodeName());
		}
	}

	/**
	 * Converts the contents of the given W3C node to a {@link Fragment}.
	 */
	public static Fragment convertContentsFromDOM(org.w3c.dom.Node node) {
		return fragment(convertContentsList(node));
	}

	private static List<Node> convertContentsList(org.w3c.dom.Node node) {
		List<Node> nodes = new ArrayList<>();

		for (org.w3c.dom.Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			nodes.add(convertFromDOM(child));
		}
		return nodes;
	}

	private static Element convertElementShallow(org.w3c.dom.Element element) {
		Element result = element(element.getNamespaceURI(), element.getLocalName());
		result.setAttributes(convertAttributes(element));
		result.setEmpty(element.getUserData(DOM_EMPTY_PROPERTY) == Boolean.TRUE);
		return result;
	}

	private static List<Attribute> convertAttributes(org.w3c.dom.Element from) {
		NamedNodeMap attributes = from.getAttributes();
		List<Attribute> newAttributes = new ArrayList<>(attributes.getLength());
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			org.w3c.dom.Node attribute = attributes.item(n);
			newAttributes.add(attr(attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getNodeValue()));
		}
		return newAttributes;
	}

}
