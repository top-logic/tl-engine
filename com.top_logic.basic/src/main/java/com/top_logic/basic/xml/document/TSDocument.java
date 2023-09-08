/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.top_logic.basic.StringServices;

/**
 * Immutable {@link Document} implementation.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TSDocument extends NonElementNode implements Document {

	private final Element root;
	private final String documentURI;
	private final boolean strictErrorChecking;
	private final String xmlVersion;
	private final boolean xmlStandalone;
	private final String xmlEncoding;
	private final String inputEncoding;

	protected TSDocument(Document sourceDocument) {
		super(sourceDocument, null);

		this.root = new TSElement(sourceDocument.getDocumentElement(), null) {

			@Override
			public Document getOwnerDocument() {
				return TSDocument.this;
			}
		};
		this.documentURI = sourceDocument.getDocumentURI();
		this.strictErrorChecking = sourceDocument.getStrictErrorChecking();
		this.xmlEncoding = sourceDocument.getXmlEncoding();
		this.xmlStandalone = sourceDocument.getXmlStandalone();
		this.xmlVersion = sourceDocument.getXmlVersion();
		this.inputEncoding = sourceDocument.getInputEncoding();
	}

	@Override
	public Element getDocumentElement() {
		return this.root;
	}

	@Override
	public Document getOwnerDocument() {
		return this;
	}

	@Override
	public DocumentType getDoctype() {
		throw unsupportedOperation();
	}

	@Override
	public DOMImplementation getImplementation() {
		throw unsupportedOperation();
	}

	@Override
	public Element createElement(String tagName) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public DocumentFragment createDocumentFragment() {
		throw unsupportedOperation();
	}

	@Override
	public Text createTextNode(String data) {
		throw unsupportedOperation();
	}

	@Override
	public Comment createComment(String data) {
		throw unsupportedOperation();
	}

	@Override
	public CDATASection createCDATASection(String data) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public Attr createAttribute(String name) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public EntityReference createEntityReference(String name) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public NodeList getElementsByTagName(String tagname) {
		return getDocumentElement().getElementsByTagName(tagname);
	}

	@Override
	public Node importNode(Node importedNode, boolean deep) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public Element createElementNS(String aNamespaceURI, String qualifiedName) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public Attr createAttributeNS(String aNamespaceURI, String qualifiedName) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public NodeList getElementsByTagNameNS(String aNamespaceURI, String aLocalName) {
		return getDocumentElement().getElementsByTagNameNS(aNamespaceURI, aLocalName);
	}

	@Override
	public Element getElementById(String elementId) {
		return searchElement(getDocumentElement(), elementId);
	}

	private Element searchElement(Element rootElement, String elementId) {
		NamedNodeMap attributes = rootElement.getAttributes();
		for (int i = 0, size = attributes.getLength(); i < size; i++) {
			Attr attribute = (Attr) attributes.item(i);
			if (attribute.isId() && StringServices.equals(attribute.getValue(), elementId)) {
				return rootElement;
			}
		}
		NodeList childNodes = rootElement.getChildNodes();
		for (int i = 0, size = childNodes.getLength(); i < size; i++) {
			Element subTreeResult = searchElement((Element) childNodes.item(i), elementId);
			if (subTreeResult != null) {
				return subTreeResult;
			}
		}
		return null;
	}

	@Override
	public String getInputEncoding() {
		return inputEncoding;
	}

	@Override
	public String getXmlEncoding() {
		return xmlEncoding;
	}

	@Override
	public boolean getXmlStandalone() {
		return xmlStandalone;
	}

	@Override
	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
		throw notSupportedErr();
	}

	@Override
	public String getXmlVersion() {
		return xmlVersion;
	}

	@Override
	public void setXmlVersion(String xmlVersion) throws DOMException {
		throw notSupportedErr();
	}

	@Override
	public boolean getStrictErrorChecking() {
		return strictErrorChecking;
	}

	@Override
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		throw unsupportedOperation();
	}

	@Override
	public String getDocumentURI() {
		return documentURI;
	}

	@Override
	public void setDocumentURI(String documentURI) {
		throw unsupportedOperation();
	}

	@Override
	public Node adoptNode(Node source) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public DOMConfiguration getDomConfig() {
		throw unsupportedOperation();
	}

	@Override
	public void normalizeDocument() {
		throw unsupportedOperation();
	}

	@Override
	public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public String getNodeName() {
		return "#document";
	}

	@Override
	public String getNodeValue() throws DOMException {
		return null;
	}

	@Override
	public short getNodeType() {
		return Node.DOCUMENT_NODE;
	}
	
	@Override
	public void setTextContent(String aTextContent) throws DOMException {
		// no effect for Document
	}
}