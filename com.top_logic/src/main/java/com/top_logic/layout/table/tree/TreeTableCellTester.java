/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link CellExistenceTester} of nodes of {@link TreeTableComponent}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TreeTableCellTester implements CellExistenceTester {

	private CellExistenceTester _wrappedTester;

	/**
	 * Create a new {@link TreeTableCellTester}.
	 */
	public TreeTableCellTester(CellExistenceTester wrappedTester) {
		_wrappedTester = wrappedTester;
	}

	@Override
	public boolean isCellExistent(Object node, String columnName) {
		Object rowObject = ((TLTreeNode<?>) node).getBusinessObject();
		return _wrappedTester.isCellExistent(rowObject, columnName);
	}
}
