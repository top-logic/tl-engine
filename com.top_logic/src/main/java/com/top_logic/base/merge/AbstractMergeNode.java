/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.layout.tree.model.AbstractTLTreeNode;

/**
 * Common base class for merge tree nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMergeNode extends AbstractTLTreeNode<AbstractMergeNode> {

	/**
	 * Creates a {@link AbstractMergeNode}.
	 */
	public AbstractMergeNode(AbstractMergeNode parent, Object businessObject) {
		super(parent, businessObject);
	}

	/**
	 * Checks whether the children of this node are already initialized.
	 */
	public abstract boolean isInitialized();

}
