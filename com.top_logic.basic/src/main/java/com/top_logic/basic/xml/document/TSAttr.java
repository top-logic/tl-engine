/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * Thread safe {@link Attr} implementation.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TSAttr extends AbstractTSNode implements Attr {
	
	private boolean isID;
	private boolean specified;
	private String name;
	private String value;

	protected TSAttr(Attr anAttr) {
		super(anAttr);
		this.isID = anAttr.isId();
		this.name = anAttr.getName();
		this.value = anAttr.getValue();
		this.specified = anAttr.getSpecified();
	}
	
	@Override
	public String getNodeName() {
		return getName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean getSpecified() {
		return specified;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Element getOwnerElement() {
		return (Element) this.getParentNode();
	}

	@Override
	public TypeInfo getSchemaTypeInfo() { 
		throw unsupportedOperation();
	}

	@Override
	public boolean isId() {
		return isID;
	}

	@Override
	public NodeList getChildNodes() {
		return EmptyNodeList.INSTANCE;
	}

	@Override
	public Node getFirstChild() {
		return null;
	}

	@Override
	public Node getLastChild() {
		return null;
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public boolean hasChildNodes() {
		return false;
	}

	@Override
	public NamedNodeMap getAttributes() {
		return null;
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}
	
	@Override
	public Node getParentNode() {
		// Attr's have no parent
		return null;
	}

	@Override
	public String getNodeValue() throws DOMException {
		return getValue();
	}

	@Override
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}

}
