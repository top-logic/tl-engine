/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.xml;

import static java.util.Objects.*;

import java.io.IOError;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility methods for processing DOM {@link Document}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DOMUtil {

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = safeFactory();

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_VALIDATING;

	static {
		DOCUMENT_BUILDER_FACTORY_VALIDATING = safeFactory();
		DOCUMENT_BUILDER_FACTORY_VALIDATING.setValidating(true);
	}

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_NAMESPACE_AWARE;

	static {
		DOCUMENT_BUILDER_FACTORY_NAMESPACE_AWARE = safeFactory();
		DOCUMENT_BUILDER_FACTORY_NAMESPACE_AWARE.setNamespaceAware(true);
	}

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_VALIDATING_NAMESPACE_AWARE;

	private static final Logger LOG = Logger.getLogger(DOMUtil.class.getName());

	static {
		DOCUMENT_BUILDER_FACTORY_VALIDATING_NAMESPACE_AWARE = safeFactory();
		DOCUMENT_BUILDER_FACTORY_VALIDATING_NAMESPACE_AWARE.setValidating(true);
		DOCUMENT_BUILDER_FACTORY_VALIDATING_NAMESPACE_AWARE.setNamespaceAware(true);
	}

	/**
	 * @see #hardenAgainsXXEAttacks(DocumentBuilderFactory)
	 */
	private static DocumentBuilderFactory safeFactory() {
		// IGNORE FindBugs(TL_XML_UNSAFE_DOM_PARSING): Factory is hardened against attacks below.
		DocumentBuilderFactory result = DocumentBuilderFactory.newInstance();
		hardenAgainsXXEAttacks(result);
		return result;
	}

	/**
	 * XML eXternal Entity injection (XXE), which is now part of the OWASP Top 10, is a type of
	 * attack against an application that parses XML input. This attack occurs when untrusted XML
	 * input containing a reference to an external entity is processed by a weakly configured XML
	 * parser. This attack may lead to the disclosure of confidential data, denial of service,
	 * Server Side Request Forgery (SSRF), port scanning from the perspective of the machine where
	 * the parser is located, and other system impacts.
	 * 
	 * @see "https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#Java"
	 */
	private static void hardenAgainsXXEAttacks(DocumentBuilderFactory result) {
		// This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity
		// attacks are prevented
		// Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
		setFeature(result, "http://apache.org/xml/features/disallow-doctype-decl", true);

		// If you can't completely disable DTDs, then at least do the following:
		// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
		// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
		// JDK7+ - http://xml.org/sax/features/external-general-entities
		setFeature(result, "http://xml.org/sax/features/external-general-entities", false);

		// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
		// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
		// JDK7+ - http://xml.org/sax/features/external-parameter-entities
		setFeature(result, "http://xml.org/sax/features/external-parameter-entities", false);

		// Disable external DTDs as well
		setFeature(result, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
		result.setXIncludeAware(false);
		// result.setExpandEntityReferences(false);

		// And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a
		// requirement, then
		// ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
		// (http://cwe.mitre.org/data/definitions/918.html) and denial
		// of service attacks (such as billion laughs or decompression bombs via "jar:") are a
		// risk."
	}

	private static void setFeature(DocumentBuilderFactory factory, String feature, boolean value) {
		try {
			factory.setFeature(feature, value);
		} catch (ParserConfigurationException ex) {
			LOG.warning("Setting security-relevant feature '" + feature
				+ "' not supported, no protection agains XML external entity injection is provided.");
		}
	}

	/**
	 * Creates a {@link DocumentBuilder} from a {@link DocumentBuilderFactory} with default
	 * settings.
	 * <p>
	 * The default settings are documented on the corresponding setter in the
	 * {@link DocumentBuilderFactory}.
	 * </p>
	 * 
	 * @return Never null.
	 */
	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return requireNonNull(DOCUMENT_BUILDER_FACTORY.newDocumentBuilder());
	}

	/**
	 * Variant of {@link #newDocumentBuilder()} that
	 * {@link DocumentBuilderFactory#setValidating(boolean) sets validating} to true.
	 * 
	 * @return Never null.
	 */
	public static DocumentBuilder newDocumentBuilderValidating() throws ParserConfigurationException {
		return requireNonNull(DOCUMENT_BUILDER_FACTORY_VALIDATING.newDocumentBuilder());
	}

	/**
	 * Variant of {@link #newDocumentBuilder()} that
	 * {@link DocumentBuilderFactory#setValidating(boolean) sets namespace aware} to true.
	 * 
	 * @return Never null.
	 */
	public static DocumentBuilder newDocumentBuilderNamespaceAware() throws ParserConfigurationException {
		return requireNonNull(DOCUMENT_BUILDER_FACTORY_NAMESPACE_AWARE.newDocumentBuilder());
	}

	/**
	 * Variant of {@link #newDocumentBuilder()} that sets
	 * {@link DocumentBuilderFactory#setValidating(boolean) validating} and
	 * {@link DocumentBuilderFactory#setValidating(boolean) namespace aware} to true.
	 * 
	 * @return Never null.
	 */
	public static DocumentBuilder newDocumentBuilderValidatingNamespaceAware() throws ParserConfigurationException {
		return requireNonNull(DOCUMENT_BUILDER_FACTORY_VALIDATING_NAMESPACE_AWARE.newDocumentBuilder());
	}

	/**
	 * Checks, whether the given element is from the given name space and has the given local name.
	 */
	public static boolean isElement(String nameSpaceUri, String localName, Node element) {
		return isElement(nameSpaceUri, element) && hasLocalName(localName, element);
	}

	/**
	 * Checks, whether the given element is from the given name space.
	 */
	public static boolean isElement(String nameSpaceUri, Node element) {
		return isElement(element) && hasNamespace(nameSpaceUri, element);
	}

	/**
	 * Checks, whether the given node is from the given name space.
	 */
	public static boolean hasNamespace(String nameSpaceUri, Node node) {
		return equals(nameSpaceUri, node.getNamespaceURI());
	}

	private static boolean equals(Object s1, Object s2) {
		return ((s1 == null) && (s2 == null)) || ((s1 != null) && (s1.equals(s2)));
	}

	/**
	 * Determines whether the given {@link Node} has some inner content to
	 * write, i.e. whether the node has children to render.
	 */
	public static boolean hasContent(Node node) {
		return node.getFirstChild() != null;
	}

	/**
	 * Checks, whether the given node has the given local name.
	 */
	public static boolean hasLocalName(String localName, Node node) {
		return localName.equals(node.getLocalName());
	}

	/**
	 * Checks, whether the given node is an {@link Element} node.
	 */
	public static boolean isElement(Node node) {
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * Checks, whether the given node is an {@link Text} node.
	 */
	public static boolean isText(Node node) {
		return node.getNodeType() == Node.TEXT_NODE;
	}

	/**
	 * Finds the following sibling node of the given node that
	 * {@link #isElement(Node) is an element node}.
	 * 
	 * @param element
	 *        The node from which to start the search.
	 * @return The next element sibling or <code>null</code>, if none exists.
	 */
	public static Element getNextElementSibling(Node element) {
		return getNextSiblingOrSelfElement(element.getNextSibling());
	}

	/**
	 * Finds the first child node of the given node that
	 * {@link #isElement(Node) is an element node}.
	 * 
	 * @param element
	 *        The node from which to start the search.
	 * @return The first element child or <code>null</code>, if none exists.
	 */
	public static Element getFirstElementChild(Node element) {
		return getNextSiblingOrSelfElement(element.getFirstChild());
	}

	/**
	 * Finds the next sibling or self of the given node that
	 * {@link #isElement(Node) is an element node}.
	 * 
	 * @param node
	 *        The node from which to start the search.
	 * @return The given, not, if it is an element node, or the next following
	 *         sibling that is an element node, or <code>null</code>, if none
	 *         exists.
	 */
	public static Element getNextSiblingOrSelfElement(Node node) {
		while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
			node = node.getNextSibling();
		}
		return (Element) node;
	}

	/**
	 * This method returns a new {@link DocumentBuilder} with namespace aware <code>true</code> and
	 * validating <code>false</code>.
	 * 
	 * @see #newDocumentBuilderNamespaceAware()
	 */
	public static DocumentBuilder getDocumentBuilder() {
		try {
			return newDocumentBuilderNamespaceAware();
		} catch (ParserConfigurationException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * This method returns a new {@link Document} created by the builder
	 * given by {@link #getDocumentBuilder()}.
	 */
	public static Document newDocument() {
		return getDocumentBuilder().newDocument();
	}

	/**
	 * Parses the given string as XML document and return the result.
	 * 
	 * <p>
	 * <b>Note</b>: The input is required to be a well-formed XML document.
	 * </p>
	 * 
	 * @param input
	 *        The input string to parse.
	 * @return The parsed DOM document.
	 * @throws IllegalArgumentException
	 *         If the given string is not a well-formed XML document.
	 */
	public static Document parse(String input) {
		try {
			InputSource source = new InputSource(new StringReader(input));
			return getDocumentBuilder().parse(source);
		} catch (SAXException ex) {
			throw new IllegalArgumentException("Given input is not well-formed XML: " + ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Assuming, the given {@link XMLStreamReader} points to a
	 * {@link XMLStreamConstants#START_ELEMENT} event, this element is created in the given
	 * {@link Document} as new child of the given parent {@link Element}.
	 *
	 * @param in
	 *        The {@link XMLStreamReader} to interpret the current event from.
	 * @param document
	 *        The {@link Document} to create the contents in.
	 * @param parent
	 *        The parent {@link Element} to add the new child element to.
	 * @return The newly created {@link Element} node.
	 */
	public static Element createCurrentElement(XMLStreamReader in, Document document, Node parent) {
		assert in.getEventType() == XMLStreamConstants.START_ELEMENT;
		String namespace = in.getNamespaceURI();
		String qName;
		if (namespace != null) {
			qName = getName(in);
		} else {
			qName = in.getLocalName();
		}
		Element newElement = document.createElementNS(namespace, qName);
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			String attrNamespace = getAttributeNamespace(in, n);
			String attrQName;
			if (attrNamespace != null) {
				attrQName = getAttributeName(in, n);
			} else {
				attrQName = in.getAttributeLocalName(n);
			}
			String value = in.getAttributeValue(n);
			newElement.setAttributeNS(attrNamespace, attrQName, value);
		}
		parent.appendChild(newElement);
		return newElement;
	}

	private static String getAttributeNamespace(XMLStreamReader xmlStreamReader, int index) {
		return nonEmpty(xmlStreamReader.getAttributeNamespace(index));
	}

	private static String nonEmpty(String input) {
		return isEmpty(input) ? null : input;
	}

	private static String getAttributeName(XMLStreamReader in, int index) {
		String attrQName;
		String attrPrefix = in.getAttributePrefix(index);
		if (!isEmpty(attrPrefix)) {
			attrQName = attrPrefix + ':' + in.getAttributeLocalName(index);
		} else {
			attrQName = in.getAttributeLocalName(index);
		}
		return attrQName;
	}

	private static String getName(XMLStreamReader in) {
		String qName;
		String prefix = in.getPrefix();
		if (!isEmpty(prefix)) {
			qName = prefix + ':' + in.getLocalName();
		} else {
			qName = in.getLocalName();
		}
		return qName;
	}

	private static final boolean isEmpty(String aObject) {
		return aObject == null || (aObject.length() == 0);
	}

	/**
	 * Indentation for <b>older</b> versions of Xalan: Ticket #10678
	 * 
	 * @return If setting the indent fails and an {@link IllegalArgumentException} is thrown, it is
	 *         returned.
	 */
	public static IllegalArgumentException setIndentOlderXalan(TransformerFactory factory, int indent) {
		try {
			// indent-number according to TransformerFactoryImpl.INDENT_NUMBER (JDK 1.5)
			factory.setAttribute("indent-number", indent);
			return null;
		} catch (IllegalArgumentException ex) {
			return ex;
		}
	}

	/**
	 * Indentation for <b>newer</b> versions of Xalan.
	 * 
	 * @return If setting the indent fails and an {@link IllegalArgumentException} is thrown, it is
	 *         returned.
	 */
	public static IllegalArgumentException setIndentNewerXalan(Transformer transformer, int indent) {
		try {
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
			return null;
		} catch (IllegalArgumentException ex) {
			return ex;
		}
	}

	/**
	 * {@link Iterable} of all direct children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 */
	public static Iterable<Node> children(Node parent) {
		return children(parent, false);
	}

	/**
	 * {@link Iterable} of all direct children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param reverse
	 *        Whether to report children in reverse order.
	 */
	public static Iterable<Node> children(Node parent, boolean reverse) {
		return new AllChildren(parent, reverse);
	}

	/**
	 * {@link Iterable} of all direct {@link Element} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 */
	public static Iterable<Element> elements(Node parent) {
		return elements(parent, false);
	}

	/**
	 * {@link Iterable} of all direct {@link Element} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param reverse
	 *        Whether to report children in reverse order.
	 */
	public static Iterable<Element> elements(Node parent, boolean reverse) {
		return new AllElementChildren(parent, reverse);
	}

	/**
	 * {@link Iterable} of all direct {@link Element} children with a given namespace.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param namespace
	 *        Only children of that namespace are reported. <code>null</code> means all namespaces.
	 */
	public static Iterable<Element> elementsNS(Node parent, String namespace) {
		return elementsNS(parent, namespace, false);
	}

	/**
	 * {@link Iterable} of all direct {@link Element} children with a given namespace.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param namespace
	 *        Only children of that namespace are reported. <code>null</code> means all namespaces.
	 * @param reverse
	 *        Whether to report children in reverse order.
	 */
	public static Iterable<Element> elementsNS(Node parent, String namespace, boolean reverse) {
		return new AllNamespaceElements(parent, namespace, reverse);
	}

	/**
	 * {@link Iterable} of all direct {@link Element} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param namespace
	 *        Only children of that namespace are reported. <code>null</code> means all namespaces.
	 * @param localName
	 *        Only children with that local name are reported. <code>null</code> means all local
	 *        names.
	 */
	public static Iterable<Element> elementsNS(Node parent, String namespace, String localName) {
		return elementsNS(parent, namespace, localName, false);
	}

	/**
	 * {@link Iterable} of all direct {@link Element} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param namespace
	 *        Only children of that namespace are reported. <code>null</code> means only elements
	 *        without namespace.
	 * @param localName
	 *        Only children with that local name are reported. <code>null</code> means all local
	 *        names.
	 * @param reverse
	 *        Whether to report children in reverse order.
	 */
	public static Iterable<Element> elementsNS(Node parent, String namespace, String localName, boolean reverse) {
		if (localName == null) {
			return elementsNS(parent, namespace, reverse);
		} else {
			return new ElementChildren(parent, namespace, localName, reverse);
		}
	}

	/**
	 * {@link Iterable} of all direct {@link Comment} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 */
	public static Iterable<Comment> comments(Node parent) {
		return comments(parent, false);
	}

	/**
	 * {@link Iterable} of all direct {@link Comment} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param reverse
	 *        Whether to report children in reverse order.
	 */
	public static Iterable<Comment> comments(Node parent, boolean reverse) {
		return new CommentChildren(parent, reverse);
	}

	/**
	 * {@link Iterable} of all direct {@link Text} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 */
	public static Iterable<Text> texts(Node parent) {
		return texts(parent, false);
	}

	/**
	 * {@link Iterable} of all direct {@link Text} children of the given parent node.
	 * 
	 * @param parent
	 *        The node to take children from.
	 * @param reverse
	 *        Whether to report children in reverse order.
	 */
	public static Iterable<Text> texts(Node parent, boolean reverse) {
		return new TextChildren(parent, reverse);
	}

	/**
	 * Evaluate the given XPath expression on the given context node. 
	 * 
	 * @return The result as {@link NodeList}.
	 */
	public static NodeList selectNodeList(Node contextNode, String xpath) throws XPathExpressionException {
		return (NodeList) XPathFactory.newInstance().newXPath().evaluate(xpath, contextNode, XPathConstants.NODESET);
	}

	/**
	 * Evaluate the given XPath expression on the given context node. 
	 * 
	 * @return The result as single {@link Node}.
	 */
	public static Node selectSingleNode(Node contextNode, String xpath) throws XPathExpressionException {
		return (Node) XPathFactory.newInstance().newXPath().evaluate(xpath, contextNode, XPathConstants.NODE);
	}

}
