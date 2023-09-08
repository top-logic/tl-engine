/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import com.top_logic.basic.col.BidiHashMap;

/**
 * Default mutable {@link NamespaceContext} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultNamespaceContext implements NamespaceContext {
	
	private static final String W3C_XML_SCHEMA_INSTANCE_NS_PREFIX = "xsi";
	
	private final BidiHashMap namespaces = new BidiHashMap();

	public DefaultNamespaceContext() {
		setPrefix(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
		setPrefix(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		setPrefix(W3C_XML_SCHEMA_INSTANCE_NS_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
	}
	
	/**
	 * Associates the given prefix with the given namespace.
	 * 
	 * @param prefix
	 *        The new prefix.
	 * @param namespace
	 *        The assigned namespace.
	 * @return The namespace that was formerly assigned to the given prefix, or
	 *         <code>null</code>, if no namespace was assigend to the given prefix.
	 */
	public String setPrefix(String prefix, String namespace) {
		return (String) namespaces.put(prefix, namespace);
	}
	
	@Override
	public Iterator getPrefixes(String namespaceURI) {
		return namespaces.keySet().iterator();
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			throw new IllegalArgumentException("Namespace URI must not be null.");
		}
		return (String) namespaces.getKey(namespaceURI);
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return (String) namespaces.get(prefix);
	}
}