/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * Helper class to descend the layout configuration tree from a given {@link LayoutConfigTreeNode}.
 * When in a branch a {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config} is
 * found, descending in this branched is stopped.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CompoundSecurityLayoutConfigDescender {

	/**
	 * Starts descending with the given {@link LayoutConfigTreeNode}.
	 */
	public void descend(LayoutConfigTreeNode node) {
		addCommandGroupsRecursive(node, node);
	}

	private void addCommandGroupsRecursive(LayoutConfigTreeNode rootNode, LayoutConfigTreeNode configNode) {
		if (rootNode != configNode && rootNode.equals(findCompoundSecurityNode(configNode.getParent()))) {
			visit(configNode);
		}
		configNode.getChildren().forEach(child -> addCommandGroupsRecursive(rootNode, child));
	}

	/**
	 * Visits the given {@link LayoutConfigTreeNode} node.
	 * 
	 * @param configNode
	 *        The visited node.
	 */
	protected abstract void visit(LayoutConfigTreeNode configNode);

	/**
	 * Find the next ancestor (or self) of the given node, that represents a
	 * {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config}.
	 * 
	 * <p>
	 * If there is no such ancestor, <code>null</code> is returned.
	 * </p>
	 * 
	 * @param node
	 *        The {@link LayoutConfigTreeNode} to get
	 *        {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config} for. May be
	 *        <code>null</code> in which case <code>null</code> is returned.
	 */
	public static LayoutConfigTreeNode findCompoundSecurityNode(LayoutConfigTreeNode node) {
		do {
			if (node == null) {
				return null;
			}
			if (node.getConfig() instanceof CompoundSecurityLayout.Config) {
				return node;
			}
			node = node.getParent();
		} while (true);
	}

}

