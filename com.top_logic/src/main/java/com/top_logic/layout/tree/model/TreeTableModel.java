/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import com.top_logic.layout.table.TableModel;

/**
 * {@link TableModel} that consists of rows from an tree model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeTableModel extends TableModel {

	/**
	 * The {@link TreeUIModel} that is embedded into this {@link TreeTableModel}.
	 * 
	 * @return Never <code>null</code>.
	 */
	TreeUIModel<?> getTreeModel();

}
