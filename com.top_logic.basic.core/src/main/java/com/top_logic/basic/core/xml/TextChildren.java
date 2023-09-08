/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.xml;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.Text;


/**
 * All direct {@link Text} child nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class TextChildren extends DOMIterable<Text> {
	
	/*package protected*/ TextChildren(Node parent, boolean reverse) {
		super(parent, reverse);
	}

	@Override
	public Iterator<Text> iterator() {
		return new TextChildrenIterator();
	}

	class TextChildrenIterator extends ChildIterator<Text> {

		@Override
		protected boolean matches(Node node) {
			short nodeType = node.getNodeType();
			return nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE;
		}

		@Override
		protected Text cast(Node node) {
			return (Text) node;
		}
	}
}