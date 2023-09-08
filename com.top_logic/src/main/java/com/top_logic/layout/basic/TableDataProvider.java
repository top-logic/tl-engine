/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.table.TableData;

/**
 * Provider of {@link TableData}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@FunctionalInterface
public interface TableDataProvider {

	/**
	 * {@link TableData} of this {@link TableDataProvider}, must not be <code>null</code>.
	 */
	TableData getTableData();

}
