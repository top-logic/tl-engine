/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

/**
 * Base class for instances building up an XPath.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XPathNode {

	/**
	 * Visits this {@link XPathNode} with the given {@link XPathVisitor}.
	 */
	public abstract <R, A> R visit(XPathVisitor<R, A> v, A arg);
	
	@Override
	public String toString() {
		return XPathSerializer.toString(this);
	}

}
