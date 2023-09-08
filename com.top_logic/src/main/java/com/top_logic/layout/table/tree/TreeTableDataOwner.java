/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.layout.table.TableDataOwner;

/**
 * Owner of a {@link TreeTableData}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TreeTableDataOwner extends TableDataOwner {

	@Override
	public TreeTableData getTableData();

}
