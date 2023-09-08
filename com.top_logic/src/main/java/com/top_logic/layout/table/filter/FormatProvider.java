/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.text.Format;

import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * Provider of {@link Format}s. Used, when {@link Format}s shall be created lazily.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface FormatProvider {

	/**
	 * @param column
	 *        The table column for which the format should be retrieved.
	 * 
	 * @return The {@link Format} to use, maybe <code>null</code>.
	 */
	Format getFormat(ColumnConfiguration column);
}
