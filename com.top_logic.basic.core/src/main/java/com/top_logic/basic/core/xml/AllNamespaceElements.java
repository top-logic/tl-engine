/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.xml;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * All direct element child nodes that have a given name space.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class AllNamespaceElements extends DOMIterable<Element> {

	final String _namespace;

	/*package protected*/ AllNamespaceElements(Node parent, String namespace, boolean reverse) {
		super(parent, reverse);
		
		_namespace = namespace;
	}

	@Override
	public Iterator<Element> iterator() {
		return new AllNamespaceElementsIterator();
	}

	final class AllNamespaceElementsIterator extends ChildIterator<Element> {

		@Override
		protected boolean matches(Node node) {
			return DOMUtil.isElement(_namespace, node);
		}

		@Override
		protected Element cast(Node node) {
			return (Element) node;
		}
	}

}