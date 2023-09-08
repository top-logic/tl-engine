/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

/**
 * Abstract super class which implements all but creating the actual child list.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTLTreeNodeBuilder implements TreeBuilder<DefaultMutableTLTreeNode> {

	@Override
	public DefaultMutableTLTreeNode createNode(AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> model,
			DefaultMutableTLTreeNode parent, Object userObject) {
		return new DefaultMutableTLTreeNode(model, parent, userObject);
	}

	@Override
	public abstract List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node);

}

