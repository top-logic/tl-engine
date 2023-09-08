/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

import com.top_logic.config.xdiff.model.NodeType;

/**
 * A XPath {@link NodeTest} that only test the {@link NodeType}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeTest extends NodeTest {
	private NodeType _type;

	TypeTest(NodeType type) {
		setType(type);
	}

	@Override
	public NodeType getType() {
		return _type;
	}

	/**
	 * Setter for {@link #getType()}.
	 */
	public void setType(NodeType type) {
		_type = type;
	}

	@Override
	public <R, A> R visit(XPathVisitor<R, A> v, A arg) {
		return v.visitTypeTest(this, arg);
	}
}