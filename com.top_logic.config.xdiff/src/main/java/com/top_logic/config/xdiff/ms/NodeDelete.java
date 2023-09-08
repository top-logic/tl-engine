/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import com.top_logic.config.xdiff.util.Utils;



/**
 * Implementation of {@link MSXDiffSchema#NODE_DELETE_ELEMENT}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeDelete extends MSXDiff {

	private final String _nodeXpath;

	/**
	 * Creates a {@link NodeDelete}.
	 * 
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param nodeXpath
	 *        See {@link #getNodeXpath()}.
	 */
	public NodeDelete(String component, String nodeXpath) {
		super(component);

		Utils.checkRequiredAttribute(MSXDiffSchema.NODE_DELETE_ELEMENT, MSXDiffSchema.NODE_DELETE__XPATH_ATTRIBUTE,
			nodeXpath);

		_nodeXpath = nodeXpath;
	}

	@Override
	public ArtifactType getType() {
		return ArtifactType.NODE_DELETE;
	}

	/**
	 * Reference to the node to delete.
	 */
	public String getNodeXpath() {
		return _nodeXpath;
	}

	@Override
	public <R, A> R visit(MSXDiffVisitor<R, A> v, A arg) {
		return v.visitNodeDelete(this, arg);
	}

}
