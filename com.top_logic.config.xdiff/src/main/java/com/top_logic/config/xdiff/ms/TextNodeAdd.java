/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import com.top_logic.basic.StringServices;

/**
 * Implementation of the {@link MSXDiffSchema#NODE_ADD_ELEMENT} if the text content is given as
 * attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextNodeAdd extends NodeAdd {

	private final String _nodeName;

	private final String _nodeText;

	/**
	 * Creates a {@link TextNodeAdd}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param belowXpath
	 *        See {@link NodeAdd#NodeAdd(String, String, String)}.
	 * @param beforeXpath
	 *        See {@link NodeAdd#NodeAdd(String, String, String)}.
	 * @param nodeName
	 *        See {@link #getNodeName()}.
	 * @param nodeText
	 *        See {@link #getNodeText()}.
	 */
	public TextNodeAdd(String component, String belowXpath, String beforeXpath, String nodeName, String nodeText) {
		super(component, belowXpath, beforeXpath);
		_nodeName = nodeName;
		_nodeText = StringServices.nonEmpty(nodeText);
	}

	/**
	 * The element name of the element to add.
	 */
	public String getNodeName() {
		return _nodeName;
	}

	/**
	 * The text to add to the new element as single child node.
	 */
	public String getNodeText() {
		return _nodeText;
	}

	@Override
	public <R, A> R visit(MSXDiffVisitor<R, A> v, A arg) {
		return v.visitTextNodeAdd(this, arg);
	}

}
