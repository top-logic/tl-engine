/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import static com.top_logic.config.xdiff.util.Utils.*;

import java.util.List;

/**
 * Factory for the {@link Node} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeFactory {

	/**
	 * Creates a {@link Document} with the given children.
	 */
	public static Document document(Node... children) {
		return document(list(children));
	}

	/**
	 * Creates a {@link Document} with the given children.
	 */
	public static Document document(List<Node> childrenList) {
		Document document = new Document();
		document.setChildren(childrenList);
		return document;
	}

	/**
	 * Creates an {@link Element} with the given children.
	 */
	public static Element element(String namespace, String localName, Node... children) {
		return element(namespace, localName, list(children));
	}

	/**
	 * Creates an {@link Element} with the given children.
	 */
	public static Element element(String namespace, String localName, List<Node> childrenList) {
		return element(qname(namespace, localName), childrenList);
	}

	/**
	 * Creates an {@link Element} with the given children.
	 */
	public static Element element(QName name, Node... children) {
		return element(name, list(children));
	}

	/**
	 * Creates an {@link Element} with the given children.
	 */
	public static Element element(QName name, List<Node> childrenList) {
		Element result = new Element(name);
		result.setChildren(childrenList);
		return result;
	}

	/**
	 * Creates an {@link Attribute}.
	 * 
	 * @param namespace
	 *        See {@link Attribute#getNamespace()}.
	 * @param localName
	 *        See {@link Attribute#getLocalName()}.
	 * @param value
	 *        See {@link Attribute#getValue()}.
	 * @return The new {@link Attribute}, or <code>null</code>, if the given value was
	 *         <code>null</code>.
	 */
	public static Attribute attr(String namespace, String localName, String value) {
		if (value == null) {
			return null;
		}
		return new Attribute(namespace, localName, value);
	}

	/**
	 * Creates an {@link Attribute} without namespace.
	 * 
	 * @param localName
	 *        See {@link Attribute#getLocalName()}.
	 * @param value
	 *        See {@link Attribute#getValue()}.
	 * @return The new {@link Attribute}.
	 */
	public static Attribute attr(String localName, String value) {
		return attr(null, localName, value);
	}

	/**
	 * Creates a {@link Text}.
	 */
	public static Text text(String contents) {
		return new Text(contents);
	}

	/**
	 * Creates a {@link Text}.
	 */
	public static CData cdata(String contents) {
		return new CData(contents);
	}

	/**
	 * Creates a {@link Comment}.
	 */
	public static Comment comment(String text) {
		return new Comment(text);
	}

	/**
	 * Creates an {@link EntityReference}.
	 */
	public static EntityReference entityRef(String name) {
		return new EntityReference(name);
	}

	/**
	 * Creates a {@link Fragment} of nodes.
	 */
	public static Fragment fragment(Node... nodes) {
		return fragment(list(nodes));
	}

	/**
	 * Creates a {@link Fragment} of nodes.
	 */
	public static Fragment fragment(List<Node> childrenList) {
		Fragment fragment = new Fragment();
		fragment.setChildren(childrenList);
		return fragment;
	}

	/**
	 * Creates a {@link QName}.
	 */
	public static QName qname(String namespace, String localName) {
		return new QName(namespace, localName);
	}


}
