/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.xml;

import java.util.Iterator;

import org.w3c.dom.Node;

/**
 * All children of a {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/ 
class AllChildren extends DOMIterable<Node> {

	/*package protected*/ AllChildren(Node parent, boolean reverse) {
		super(parent, reverse);
	}

	@Override
	public Iterator<Node> iterator() {
		return new AllChildrenIterator();
	}

	final class AllChildrenIterator extends ChildIterator<Node> {

		@Override
		protected boolean matches(Node node) {
			return true;
		}

		@Override
		protected Node cast(Node node) {
			return node;
		}
	}

}
