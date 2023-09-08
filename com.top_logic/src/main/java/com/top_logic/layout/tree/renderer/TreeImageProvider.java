/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tree.NodeContext;

/**
 * Provider of images for displaying the structure of a tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeImageProvider {
	
	/**
	 * The node indentation image that replaces a node with the given position
	 * in child node lines.
	 * 
	 * @param nodePosition
	 *        one of {@link NodeContext#FIRST_NODE},
	 *        {@link NodeContext#MIDDLE_NODE}, {@link NodeContext#LAST_NODE},
	 *        or {@link NodeContext#SINGLE_NODE}.
	 */
	ThemeImage getPrefixImage(int nodePosition);
	
	/**
	 * The node image for a node with the given properties.
	 */
	ThemeImage getNodeImage(boolean isLeaf, boolean isExpanded, int nodePosition);

}
