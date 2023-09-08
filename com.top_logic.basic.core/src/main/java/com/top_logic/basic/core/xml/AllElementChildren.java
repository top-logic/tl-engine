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
 * All {@link Element} children of a {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class AllElementChildren extends DOMIterable<Element> {

	/*package protected*/ AllElementChildren(Node parent, boolean reverse) {
		super(parent, reverse);
	}

	@Override
	public Iterator<Element> iterator() {
		return new AllElementChildrenIterator();
	}

	final class AllElementChildrenIterator extends ChildIterator<Element> {

		@Override
		protected boolean matches(Node node) {
			return node.getNodeType() == Node.ELEMENT_NODE;
		}

		@Override
		protected Element cast(Node node) {
			return (Element) node;
		}
	}

}
