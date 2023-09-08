/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import com.top_logic.layout.table.AbstractCellClassProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellClassProvider} for testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CellClassProviderTestFixture extends AbstractCellClassProvider {

	/**
	 * Singleton {@link CellClassProviderTestFixture} instance.
	 */
	public static final CellClassProviderTestFixture INSTANCE = new CellClassProviderTestFixture();

	private CellClassProviderTestFixture() {
		// Singleton constructor.
	}

	@Override
	public String getCellClass(Cell cell) {
		return cell.getRowObject() instanceof String ? "cString" : null;
	}
}