/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import org.w3c.dom.Node;

/**
 * {@link NodeAdd} that adds a whole fragment.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComplexNodeAdd extends NodeAdd {

	private final Node _fragmentRoot;

	/**
	 * Creates a {@link ComplexNodeAdd}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param belowXpath
	 *        See {@link NodeAdd#NodeAdd(String, String, String)}.
	 * @param beforeXpath
	 *        See {@link NodeAdd#NodeAdd(String, String, String)}.
	 * @param fragmentRoot
	 *        See {@link #getFragment()}.
	 */
	public ComplexNodeAdd(String component, String belowXpath, String beforeXpath, Node fragmentRoot) {
		super(component, belowXpath, beforeXpath);
		_fragmentRoot = fragmentRoot;
	}

	/**
	 * {@link Node} whose whole contents is copied to the target document.
	 */
	public Node getFragment() {
		return _fragmentRoot;
	}

	@Override
	public <R, A> R visit(MSXDiffVisitor<R, A> v, A arg) {
		return v.visitComplexNodeAdd(this, arg);
	}

}
