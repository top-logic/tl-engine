/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.table.AbstractCellClassProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellClassProvider} that creates no individual CSS classes for any cells.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultCellClassProvider extends AbstractCellClassProvider {

	/**
	 * Singleton {@link DefaultCellClassProvider} instance.
	 */
	public static final DefaultCellClassProvider INSTANCE = new DefaultCellClassProvider();

	private DefaultCellClassProvider() {
		// Singleton constructor.
	}

	@Override
	public String getCellClass(Cell cell) {
		return null;
	}

}
