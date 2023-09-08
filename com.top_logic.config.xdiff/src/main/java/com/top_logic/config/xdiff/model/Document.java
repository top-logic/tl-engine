/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

/**
 * A root node in the {@link Node} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Document extends FragmentBase {

	/* package protected */Document() {
		// Default constructor.
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.DOCUMENT;
	}

	@Override
	public <R, A> R visit(NodeVisitor<R, A> v, A arg) {
		return v.visitDocument(this, arg);
	}

}
