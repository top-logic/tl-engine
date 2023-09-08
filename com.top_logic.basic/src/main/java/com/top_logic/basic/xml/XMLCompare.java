/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;

/**
 * Utility to semantically compare two XML documents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLCompare {
	private final Protocol _log;
	private final Filter<? super String> _namespaceFilter;
	private boolean _strict;

	/**
	 * Creates a {@link XMLCompare}.
	 * 
	 * @param log
	 *        The {@link Protocol} to report errors to.
	 * @param strict
	 *        Whether to compare strict or white-space and syntax ignorant.
	 * @param namespaceFilter
	 *        {@link Filter} of compared namespaces. If the filter does not accept a namespace,
	 *        elements of that namespace are completely ignored.
	 */
	public XMLCompare(Protocol log, boolean strict, Filter<? super String> namespaceFilter) {
		_log = log;
		_strict = strict;
		_namespaceFilter = namespaceFilter;
	}

	/**
	 * Compares the given nodes for equality with respect to the parameters of this
	 * {@link XMLCompare}.
	 * 
	 * @param expectedChild
	 *        The one node.
	 * @param actualChild
	 *        The other node.
	 */
	public void assertEqualsNode(Node expectedChild, Node actualChild) {
		switch (expectedChild.getNodeType()) {
			case Node.DOCUMENT_NODE: {
				assertSameNodeType(expectedChild, actualChild);
				assertEqualsDocument(((Document) expectedChild), ((Document) actualChild));
				break;
			}

			case Node.ELEMENT_NODE: {
				assertSameNodeType(expectedChild, actualChild);
				assertEqualsElement((Element) expectedChild, (Element) actualChild);
				break;
			}

			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE: {
				if (actualChild instanceof Text) {
					assertEqualsText((Text) expectedChild, (Text) actualChild);
					if (_strict) {
						assertSameNodeType(expectedChild, actualChild);
					}
				} else {
					throw errorIncompatibleNodes(expectedChild, actualChild);
				}
				break;
			}
				
			case Node.DOCUMENT_TYPE_NODE:
			case Node.COMMENT_NODE:
			case Node.ENTITY_NODE:
			case Node.ENTITY_REFERENCE_NODE:
			case Node.PROCESSING_INSTRUCTION_NODE: 
			case Node.NOTATION_NODE: {
				assertSameNodeType(expectedChild, actualChild);
				assertSameNodeName(expectedChild, actualChild);
				assertSameNodeValue(expectedChild, actualChild);
				break;
			}
			
			default:
				error("Unknown node type '" + getPath(expectedChild) + "'.");
				break;
		}
	}

	
	private void assertEqualsDocument(Document expectedDocument, Document actualDocument) {
		assertEqualContent(expectedDocument, actualDocument);
	}

	private void assertEqualsElement(Element expectedElement, Element actualElement) {
		if (!sameNameSpace(expectedElement, actualElement)) {
			throw errorIncompatibleNodes(expectedElement, actualElement);
		}

		if (!sameLocalName(expectedElement, actualElement)) {
			throw errorIncompatibleNodes(expectedElement, actualElement);
		}

		NamedNodeMap expectedAttributes = expectedElement.getAttributes();
		for (int n = 0, cnt = expectedAttributes.getLength(); n < cnt; n++) {
			Attr expectedAttr = (Attr) expectedAttributes.item(n);

			String attributeNS = expectedAttr.getNamespaceURI();
			if (_namespaceFilter.accept(attributeNS)) {
				if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attributeNS)) {
					// The definition of a namespace and its prefix has no relevance for comparision.
					continue;
				}

				Attr resultAttr = actualElement.getAttributeNodeNS(attributeNS,
					expectedAttr.getLocalName());
				if (resultAttr == null) {
					throw error("Missing attribute '" + expectedAttr + "' in '"
						+ getPath(actualElement) + "'.");
				} else {
					String expectedValue = expectedAttr.getValue();
					String resultValue = resultAttr.getValue();
					if (!expectedValue.equals(resultValue)) {
						throw error("Expected value '" + expectedValue + "' in '" + resultAttr
							+ "' of '" + getPath(actualElement) + "', got '" + resultValue + "'.");
					}
				}
			}
		}

		NamedNodeMap resultAttributes = actualElement.getAttributes();
		for (int n = 0, cnt = resultAttributes.getLength(); n < cnt; n++) {
			Attr resultAttr = (Attr) resultAttributes.item(n);

			String attributeNS = resultAttr.getNamespaceURI();
			if (_namespaceFilter.accept(attributeNS)) {
				if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attributeNS)) {
					// The definition of a namespace and its prefix has no relevance for comparision.
					continue;
				}

				Attr expectedAttr = expectedElement.getAttributeNodeNS(attributeNS,
					resultAttr.getLocalName());
				if (expectedAttr == null) {
					throw error("Unexpected attribute '" + resultAttr + "' in '"
						+ getPath(actualElement) + "'.");
				}
			}
		}

		assertEqualContent(expectedElement, actualElement);

	}

	private void assertEqualContent(Node expected, Node actual) {
		Node expectedChild = acceptedSibling(expected.getFirstChild());
		Node actualChild = acceptedSibling(actual.getFirstChild());
		for (; expectedChild != null && actualChild != null; 
				expectedChild = acceptedSibling(expectedChild.getNextSibling()), 
				actualChild = acceptedSibling(actualChild.getNextSibling())) {

			assertEqualsNode(expectedChild, actualChild);
		}

		if (expectedChild != null) {
			throw error("Missing element '" + getPath(expectedChild) + "'.");
		} else if (actualChild != null) {
			throw error("Unexpected element '" + getPath(actualChild) + "'.");
		}
	}

	private void assertEqualsText(Text expected, Text actual) {
		if (getTextForCompare(expected).equals(getTextForCompare(actual))) {
			return;
		}

		throw errorIncompatibleNodes(expected, actual);
	}

	private String getTextForCompare(Text expected) {
		String textContent = expected.getNodeValue();
		if (_strict || expected.getNodeType() == Node.CDATA_SECTION_NODE) {
			return textContent;
		} else {
			return textContent.trim();
		}
	}

	private Node acceptedSibling(Node node) {
		while (node != null) {
			if (isAccepted(node)) {
				return node;
			}
			
			node = node.getNextSibling();
		}
		return node;
	}

	private boolean isAccepted(Node node) {
		if (_strict) return true;
		
		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				return _namespaceFilter.accept(node.getNamespaceURI());
			case Node.CDATA_SECTION_NODE:
				return true;
			case Node.TEXT_NODE:
				String textValue = node.getNodeValue();
				if (textValue.length() > 0) {
					return textValue.trim().length() > 0;
				} else {
					return false;
				}
			default:
				return false;
		}
	}
	
	private static boolean sameLocalName(Node expectedElement, Node resultElement) {
		return expectedElement.getLocalName().equals(resultElement.getLocalName());
	}

	private static boolean sameNameSpace(Node expectedElement, Node resultElement) {
		return StringServices.equals(expectedElement.getNamespaceURI(), resultElement.getNamespaceURI());
	}

	private void assertSameNodeName(Node expectedChild, Node resultChild) {
		if (!sameNodeName(expectedChild, resultChild)) {
			throw errorIncompatibleNodes(expectedChild, resultChild);
		}
	}

	private static boolean sameNodeName(Node expectedChild, Node resultChild) {
		return StringServices.equals(expectedChild.getNodeName(), resultChild.getNodeName());
	}
	
	private void assertSameNodeValue(Node expectedChild, Node resultChild) {
		if (!sameNodeValue(expectedChild, resultChild)) {
			throw errorIncompatibleNodes(expectedChild, resultChild);
		}
	}

	private static boolean sameNodeValue(Node expectedChild, Node resultChild) {
		return StringServices.equals(expectedChild.getNodeValue(), resultChild.getNodeValue());
	}

	private void assertSameNodeType(Node expectedChild, Node resultChild) {
		if (! sameNodeType(expectedChild, resultChild)) {
			throw errorIncompatibleNodes(expectedChild, resultChild);
		}
	}

	private static boolean sameNodeType(Node expectedChild, Node resultChild) {
		return expectedChild.getNodeType() == resultChild.getNodeType();
	}

	private String getPath(Node node) {
		Node parent = node.getParentNode();
		if (node instanceof Document) {
			return "";
		} else {
			int index = getIndexOfEquivalent(parent, node);
			return getPath(parent) + "/" + typeCheck(node) + "[" + index + "]" + valueCheck(node);
		}
	}

	private String typeCheck(Node node) {
		switch (node.getNodeType()) {
			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE:
				return "text()";
			case Node.COMMENT_NODE:
				return "comment()";
			case Node.ELEMENT_NODE:
				String namespace = node.getNamespaceURI();
				String namespaceCheck;
				if (StringServices.isEmpty(namespace)) {
					namespaceCheck = "";
				} else {
					namespaceCheck = "[namespace-uri()='" + namespace + "']";
				}
				return node.getLocalName() + namespaceCheck;
			case Node.PROCESSING_INSTRUCTION_NODE:
				return "processing-instruction()";
			default:
				return "node()";
		}
	}

	private String valueCheck(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			return "";
		} else {
			return "[string()='" + node.getNodeValue() + "']";
		}
	}

	private int getIndexOfEquivalent(Node parent, Node node) {
		int index = 1;
		for (Node child = parent.getFirstChild(); child != node; child = child.getNextSibling()) {
			short nodeType = node.getNodeType();
			if (child.getNodeType() == nodeType) {
				if (nodeType != Node.ELEMENT_NODE || (sameNameSpace(child, node) && sameLocalName(child, node))) {
					index++;
				}
			}
		}
		return index;
	}
	
	private RuntimeException errorIncompatibleNodes(Node expected, Node actual) {
		return error("Expected '" + getPath(expected) + "', got '"
				+ getPath(actual) + "'.");
	}

	protected RuntimeException error(String message) {
		return _log.fatal(message);
	}
	
}