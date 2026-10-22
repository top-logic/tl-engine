/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration of an element displaying a tree, naming the node its tree functions are evaluated
 * for.
 *
 * @implNote The scripts declare the node with
 *           {@link com.top_logic.layout.view.channel.Supplies} renamed by {@link #NODE_AS}.
 */
public interface NodeAsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getNodeAs()}. */
	String NODE_AS = "node-as";

	/** Name of the variable holding the node when no other name is given. */
	String DEFAULT_NODE_AS = "node";

	/**
	 * The name of the variable holding the node in the functions computing the tree: the children
	 * of the node, and what holds it.
	 *
	 * <p>
	 * Unset, the variable is named {@value #DEFAULT_NODE_AS}. With <code>node-as="folder"</code>,
	 * the children of a node are computed by <code>$folder.get(`my:Folder#children`)</code>. The
	 * functions see the node behind the values of their inputs; an input of the same name is a
	 * configuration error and is renamed with <code>as</code>.
	 * </p>
	 */
	@Name(NODE_AS)
	@Nullable
	String getNodeAs();

}
