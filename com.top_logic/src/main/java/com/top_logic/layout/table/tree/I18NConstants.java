/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The selected sub-tree is already completely selected.
	 */
	public static ResKey NO_EXEC_FULL;

	/**
	 * @en Select subtree
	 * @tooltip Selects all nodes in the sub-tree.
	 */
	public static ResKey SELECT_SUBTREE;

	/**
	 * @en Clears sub-tree selection
	 * @tooltip Removes all all nodes in the sub-tree from the current selection.
	 */
	public static ResKey DESELECT_SUBTREE;

	/**
	 * @en There are no selected nodes in the sub-tree.
	 */
	public static ResKey NO_EXEC_NONE;

	static {
		initConstants(I18NConstants.class);
	}
}
