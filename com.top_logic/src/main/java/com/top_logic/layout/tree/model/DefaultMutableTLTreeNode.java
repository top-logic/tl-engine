/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


/**
 * The class {@link DefaultMutableTLTreeNode} is an implementation of {@link DefaultMutableTLTreeNode}
 * which interacts with its {@link AbstractMutableTLTreeModel model}.
 * 
 * @see AbstractMutableTLTreeModel
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultMutableTLTreeNode extends AbstractMutableTLTreeNode<DefaultMutableTLTreeNode> {

	public DefaultMutableTLTreeNode(AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model,
			DefaultMutableTLTreeNode parent,
			Object businessObject) {
		super(model, parent, businessObject);
	}

}
