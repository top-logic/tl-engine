/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

/**
 * Listener of changes to a {@link TLTreeModel}.
 * 
 * @see TLTreeModel#addTreeModelListener(TreeModelListener)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeModelListener {

	/**
	 * Callback that is invoked during a change to the {@link TLTreeModel} at
	 * which this listener is registered on.
	 * 
	 * @param evt
	 *        A description of the change.
	 */
	void handleTreeUIModelEvent(TreeModelEvent evt);

}
