/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.xml.DOMUtil;

/**
 * {@link DocumentRewrite} visiting all {@link Element} nodes.
 * 
 * @see #rewriteElement(Element)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DescendingDocumentRewrite implements DocumentRewrite {

	@Override
	public boolean rewrite(Document document) {
		return descend(document);
	}

	/**
	 * Recursively visits every node.
	 */
	protected boolean descend(Node node) {
		boolean result = false;
		if (node instanceof Element) {
			result |= rewriteElement((Element) node);
		}
		for (Node child : DOMUtil.children(node)) {
			result |= descend(child);
		}
		return result;
	}

	/**
	 * Called for all {@link Element} nodes in the {@link Document}.
	 * 
	 * @return See {@link #rewrite(Document)}.
	 */
	protected abstract boolean rewriteElement(Element element);

}
