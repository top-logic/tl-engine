/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The class {@link DefaultMutableTreeNodeBuilder} is a {@link TreeBuilder} which creates ordinary
 * {@link DefaultMutableTLTreeNode}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultMutableTreeNodeBuilder extends AbstractTLTreeNodeBuilder {

	/**
	 * Sole instance of {@link DefaultMutableTreeNodeBuilder}
	 */
	public static final DefaultMutableTreeNodeBuilder INSTANCE = new DefaultMutableTreeNodeBuilder();

	/**
	 * creates a new instance of {@link DefaultMutableTreeNodeBuilder}
	 */
	protected DefaultMutableTreeNodeBuilder() {
		// nothing to set here, just hook constructor for subclasses
	}

	@Override
	public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
		return new ArrayList<>();
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}
