/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Component that provides an abstract tree view.
 */
public interface TreeModelOwner {

	/**
	 * The tree model displayed.
	 */
	TreeUIModel getTree();

}
