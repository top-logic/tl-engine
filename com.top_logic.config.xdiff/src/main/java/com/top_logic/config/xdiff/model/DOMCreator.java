/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;

import com.top_logic.basic.xml.DefaultNamespaceContext;

/**
 * {@link NodeVisitor} converting from W3C nodes to {@link Node}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class DOMCreator implements NodeVisitor<Void, org.w3c.dom.Node> {

	private static final Void none = null;

	private org.w3c.dom.Node _before;

	private NamespaceContext _namespaces;

	public DOMCreator() {
		this(null);
	}

	public DOMCreator(org.w3c.dom.Node before) {
		_before = before;
		_namespaces = new DefaultNamespaceContext();
	}

	@Override
	public Void visitText(Text node, org.w3c.dom.Node parent) {
		org.w3c.dom.Text result = getOwnerDocument(parent).createTextNode(node.getContents());
		append(parent, result);
		return none;
	}

	@Override
	public Void visitCData(CData node, org.w3c.dom.Node parent) {
		org.w3c.dom.Text result = getOwnerDocument(parent).createCDATASection(node.getContents());
		append(parent, result);
		return none;
	}

	@Override
	public Void visitComment(Comment node, org.w3c.dom.Node parent) {
		org.w3c.dom.Comment result = getOwnerDocument(parent).createComment(node.getContents());
		append(parent, result);
		return none;
	}

	@Override
	public Void visitDocument(Document node, org.w3c.dom.Node parent) {
		descent(node, parent);
		return none;
	}

	@Override
	public Void visitFragment(Fragment node, org.w3c.dom.Node parent) {
		descent(node, parent);
		return none;
	}

	@Override
	public Void visitElement(Element node, org.w3c.dom.Node parent) {
		org.w3c.dom.Element result = getOwnerDocument(parent).createElementNS(node.getNamespace(), node.getLocalName());
		copyPrefix(node, result);
		copyAttributes(node, result);
		appendContents(node, result);
		if (node.isEmpty()) {
			result.setUserData(DocumentBuilder.DOM_EMPTY_PROPERTY, Boolean.TRUE, null);
		}
		append(parent, result);
		return none;
	}

	@Override
	public Void visitEntityReference(EntityReference node, org.w3c.dom.Node parent) {
		org.w3c.dom.EntityReference result = getOwnerDocument(parent).createEntityReference(node.getName());
		append(parent, result);
		return none;
	}

	private void copyAttributes(Element orig, org.w3c.dom.Element copy) {
		for (Attribute origAttr : orig.getOrderedAttributes()) {
			Attr copiedAttr = 
				copy.getOwnerDocument().createAttributeNS(origAttr.getNamespace(), origAttr.getLocalName());
			copiedAttr.setValue(origAttr.getValue());
			copyPrefix(origAttr, copiedAttr);
			copy.setAttributeNodeNS(copiedAttr);
		}
	}

	private void copyPrefix(Named orig, org.w3c.dom.Node copied) {
		String namespace = orig.getNamespace();
		if (namespace != null) {
			String prefix = _namespaces.getPrefix(namespace);
			if (prefix != null) {
				copied.setPrefix(prefix);
			}
		}
	}

	private void appendContents(FragmentBase sourceFragment, org.w3c.dom.Node targetFragment) {
		org.w3c.dom.Node oldBefore = _before;
		_before = null;
		descent(sourceFragment, targetFragment);
		_before = oldBefore;
	}

	private void descent(FragmentBase sourceFragment, org.w3c.dom.Node targetFragment) {
		for (Node child : sourceFragment.getChildren()) {
			child.visit(this, targetFragment);
		}
	}


	private static org.w3c.dom.Document getOwnerDocument(org.w3c.dom.Node node) {
		if (node instanceof org.w3c.dom.Document) {
			return (org.w3c.dom.Document) node;
		} else {
			return node.getOwnerDocument();
		}
	}

	private void append(org.w3c.dom.Node parent, org.w3c.dom.Node result) {
		parent.insertBefore(result, _before);
	}

}
