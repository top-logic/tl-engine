/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link CellExistenceTester}, that always returns <code>true</code>.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class AllCellsExist implements CellExistenceTester {
	
	/** Static instance of {@link AllCellsExist} */
	public static final CellExistenceTester INSTANCE = new AllCellsExist();

	private AllCellsExist() {}
	
	@Override
	public boolean isCellExistent(Object rowObject, String columnName) {
		return true;
	}
}