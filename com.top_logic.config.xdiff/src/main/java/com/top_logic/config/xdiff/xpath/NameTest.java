/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.QName;

/**
 * A XPath {@link NodeTest} that tests the node name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NameTest extends NodeTest {

	private QName _name;

	NameTest(QName name) {
		super();
		setName(name);
	}

	/**
	 * The name to test for.
	 */
	public QName getName() {
		return _name;
	}

	/**
	 * Setter for {@link #getName()}.
	 */
	public void setName(QName name) {
		this._name = name;
	}

	@Override
	public NodeType getType() {
		return NodeType.ELEMENT;
	}

	@Override
	public <R, A> R visit(XPathVisitor<R, A> v, A arg) {
		return v.visitNameTest(this, arg);
	}

}