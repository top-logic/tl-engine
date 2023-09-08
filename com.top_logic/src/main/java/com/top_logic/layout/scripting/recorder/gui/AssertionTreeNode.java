/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui;

import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Abstraction of a tree node used for the Assertion GUI.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AssertionTreeNode<N> {

	/**
	 * Label of the node. Only for debug purposes.
	 */
	String getNodeLabel();

	/**
	 * Whether the node is selected.
	 */
	boolean isSelected();

	/**
	 * The tree node representation.
	 */
	N getNode();

	/**
	 * The model displaying the {@link #getNode() tree node}
	 */
	TreeUIModel<N> treeModel();

	/**
	 * The holder of the {@link #treeModel()} that can be written to and read from XML.
	 */
	Object getContext();

}

