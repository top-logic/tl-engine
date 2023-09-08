/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * Abstract immutable thread safe implementation of {@link Node}. 
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTSNode implements Node {

	private final String localName;
	private final String namespaceURI;
	private final String prefix;
	private final String baseURI;
	private final String textContent;
	private volatile Node nextSibl;
	private volatile Node prevSibl;

	public AbstractTSNode(Node node) {
		this.localName = node.getLocalName();
		this.namespaceURI = node.getNamespaceURI();
		this.prefix = node.getPrefix();
		this.baseURI = node.getBaseURI();
		this.textContent = node.getTextContent();
	}

	@Override
	public void setNodeValue(String aNodeValue) throws DOMException {
		if (aNodeValue == null) {
			return;
		}
		throw noModificationAllowedErr();
	}

	@Override
	public Node getPreviousSibling() {
		return prevSibl;
	}

	@Override
	public Node getNextSibling() {
		return nextSibl;
	}

	@Override
	public Document getOwnerDocument() {
		return getParentNode().getOwnerDocument();
	}

	@Override
	public Node insertBefore(Node aNewChild, Node aRefChild) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node cloneNode(boolean aDeep) {
		throw unsupportedOperation();
	}

	@Override
	public void normalize() {
		throw unsupportedOperation();
	}

	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String aPrefix) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getBaseURI() {
		return baseURI;
	}

	@Override
	public short compareDocumentPosition(Node aOther) throws DOMException {
		throw unsupportedOperation();
	}

	@Override
	public String getTextContent() throws DOMException {
		return textContent;
	}

	@Override
	public void setTextContent(String aTextContent) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public boolean isSameNode(Node aOther) {
		throw unsupportedOperation();
	}

	@Override
	public String lookupPrefix(String aNamespaceURI) {
		throw unsupportedOperation();
	}

	@Override
	public boolean isDefaultNamespace(String aNamespaceURI) {
		throw unsupportedOperation();
	}

	@Override
	public String lookupNamespaceURI(String aPrefix) {
		throw unsupportedOperation();
	}

	@Override
	public boolean isEqualNode(Node aArg) {
		throw unsupportedOperation();
	}
	
	@Override
	public boolean isSupported(String aFeature, String aVersion) {
		return false;
	}

	@Override
	public Object getFeature(String aFeature, String aVersion) {
		return null;
	}

	@Override
	public Object setUserData(String aKey, Object aData, UserDataHandler aHandler) {
		throw unsupportedOperation();
	}

	@Override
	public Object getUserData(String aKey) {
		throw unsupportedOperation();
	}

	void setPreviousSibling(Node createdItem) {
		this.prevSibl = createdItem;
	}

	void setNextSibling(Node createdItem) {
		this.nextSibl = createdItem;
	}

	static DOMException noModificationAllowedErr() {
		return new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, null);
	}

	static DOMException notSupportedErr() {
		return new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
	}
	
	static RuntimeException unsupportedOperation() {
		return new UnsupportedOperationException();
	}

	static AbstractTSNode createItem(Node item, Node parent) {
		if (item instanceof Element) {
			return ThreadSafeDOMFactory.importElement((Element) item, parent);
		} else if (item instanceof Attr) {
			return ThreadSafeDOMFactory.importAttr((Attr) item);
		} else if (item instanceof Text) {
			return ThreadSafeDOMFactory.importText((Text) item, parent);
		}

		throw new UnsupportedOperationException("Node " + item + " not supported in AbstractTSNode.createItem()");
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		builder.append(getNodeName());
		builder.append(',');
		if (hasAttributes()) {
			builder.append("attr:");
			builder.append(getAttributes());
		} else {
			builder.append("no attr.");
		}
		builder.append(',');
		if (hasChildNodes()) {
			builder.append("children:");
			builder.append(getChildNodes());
		} else {
			builder.append("no children.");
		}
		builder.append(']');
		return builder.toString();
	}

}
