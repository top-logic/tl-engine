/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.top_logic.basic.StringServices;
import com.top_logic.config.xdiff.XApplyException;
import com.top_logic.config.xdiff.util.Utils;

/**
 * Base class for implementations of the {@link MSXDiffSchema#NODE_ADD_ELEMENT}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class NodeAdd extends MSXDiff {

	private String _belowXpath;

	private String _beforeXpath;

	/**
	 * Creates a {@link NodeAdd}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param belowXpath
	 *        See {@link #getBelowXpath()}.
	 * @param beforeXpath
	 *        See {@link #getBeforeXpath()}.
	 */
	public NodeAdd(String component, String belowXpath, String beforeXpath) {
		super(component);

		Utils.checkRequiredAttribute(MSXDiffSchema.NODE_ADD_ELEMENT, MSXDiffSchema.NODE_ADD__XPATH_ATTRIBUTE,
			belowXpath);

		_belowXpath = belowXpath;
		_beforeXpath = StringServices.nonEmpty(beforeXpath);
	}

	@Override
	public ArtifactType getType() {
		return ArtifactType.NODE_ADD;
	}

	/**
	 * Reference of the parent element below which to add new contents.
	 */
	public String getBelowXpath() {
		return _belowXpath;
	}

	/**
	 * Reference to the node before which to add. Reference is relative to the parent node below
	 * which is added. <code>null</code> means to append to the children of the parent node.
	 */
	public String getBeforeXpath() {
		return _beforeXpath;
	}

	/**
	 * Setter for {@link #getBeforeXpath()}.
	 */
	public void setBeforeXpath(String beforeXpath) {
		_beforeXpath = beforeXpath;
	}

	/**
	 * Adds the given new node to the target document.
	 */
	protected void addNode(Document target, Node newNode) throws XApplyException {
		Node parent = getBelowNode(target);
		Node before = getBeforeNode(parent);

		parent.insertBefore(newNode, before);
	}

	/**
	 * The node before which to add.
	 * 
	 * <p>
	 * <code>null</code>, if no node was specified.
	 * </p>
	 */
	protected Node getBeforeNode(Node parent) throws XApplyException {
		Node before;
		if (_beforeXpath == null) {
			before = null;
		} else {
			before = XPathUtil.findNode(_beforeXpath, parent);
		}
		return before;
	}

	/**
	 * The parent node below which contents is added.
	 */
	protected Node getBelowNode(Document target) throws XApplyException {
		Node parent = XPathUtil.findNode(_belowXpath, target);
		return parent;
	}

}
