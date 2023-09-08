/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import com.top_logic.layout.table.AbstractCellClassProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * Demo {@link CellClassProvider} coloring floating point cells.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoFloatCssClassProvider extends AbstractCellClassProvider {

	/**
	 * Singleton {@link DemoFloatCssClassProvider} instance.
	 */
	public static final DemoFloatCssClassProvider INSTANCE = new DemoFloatCssClassProvider();

	private DemoFloatCssClassProvider() {
		// Singleton constructor.
	}

	@Override
	public String getCellClass(Cell cell) {
		Object value = cell.getValue();
		if (value instanceof Number) {
			double num = ((Number) value).doubleValue();
			if (num < 0.2) {
				return "demoGreen";
			}
			if (num > 0.8) {
				return "demoRed";
			}
			return null;
		} else {
			return "demoGray";
		}
	}

}
