/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.xml;

import java.util.Iterator;

import org.w3c.dom.Comment;
import org.w3c.dom.Node;


/**
 * All direct comment child nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class CommentChildren extends DOMIterable<Comment> {
	
	/*package protected*/ CommentChildren(Node parent, boolean reverse) {
		super(parent, reverse);
	}

	@Override
	public Iterator<Comment> iterator() {
		return new CommentChildrenIterator();
	}

	class CommentChildrenIterator extends ChildIterator<Comment> {

		@Override
		protected boolean matches(Node node) {
			return node.getNodeType() == Node.COMMENT_NODE;
		}

		@Override
		protected Comment cast(Node node) {
			return (Comment) node;
		}
	}
}