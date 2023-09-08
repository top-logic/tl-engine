/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.VetoListener;

/**
 * {@link VetoListener} that checks whether a node can be expanded (or collapsed).
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExpansionVetoListener extends VetoListener {

	/**
	 * Checks whether the given node in the given tree can be expanded (or collapsed).
	 * 
	 * @param tree
	 *        The tree that wants to change expansion state of the given node.
	 * @param node
	 *        The node to expand (or collapse).
	 * @param expanded
	 *        Whether the given node should be expanded.
	 * 
	 * @throws VetoException
	 *         iff the expansion state of the given node must not be changed.
	 */
	void checkVeto(TreeControl tree, Object node, boolean expanded) throws VetoException;

}

