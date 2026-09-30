/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.configedit.ConfigChildren;

/**
 * Utilities for relating a {@link DesignTreeNode} to the configuration property that holds it.
 */
public class DesignTreeNodes {

	/**
	 * The configuration a node represents, or {@code null} for a node that carries none (a virtual
	 * property group or a failed {@code <view-ref>}).
	 */
	public static ConfigurationItem configOf(DesignTreeNode node) {
		return node instanceof ConfigDesignTreeNode configNode ? configNode.getConfig() : null;
	}

	/**
	 * The container the given node is an element of, or {@code null} if the node is not an editable
	 * element of its parent.
	 *
	 * <p>
	 * The container is derived from the parent rather than stored on the node, and the node's
	 * configuration must actually be an element of it. Thus a node that merely appears below a
	 * container without being stored in it has no owning container: the root, a view resolved through
	 * {@code <view-ref>} (whose configuration belongs to another file), and an
	 * {@link ErrorDesignTreeNode} (which carries no configuration at all).
	 * </p>
	 */
	public static ConfigChildren owningContainer(DesignTreeNode node) {
		if (node == null) {
			return null;
		}
		DesignTreeNode parent = node.getParent();
		if (parent == null) {
			return null;
		}
		ConfigChildren container = parent.getChildContainer();
		if (container == null) {
			return null;
		}
		ConfigurationItem config = configOf(node);
		return config != null && container.indexOf(config) >= 0 ? container : null;
	}
}
