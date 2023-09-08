/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import static java.util.Objects.*;

import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * {@link TableConfigurationProvider} for demonstration purposes.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoTableConfigurationProvider implements TableConfigurationProvider {

	/** Singleton {@link DemoTableConfigurationProvider} instance. */
	public static final DemoTableConfigurationProvider INSTANCE = new DemoTableConfigurationProvider();

	/**
	 * Creates a new {@link DemoTableConfigurationProvider}.
	 */
	protected DemoTableConfigurationProvider() {
		// singleton instance
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		for (ColumnConfiguration columnConfiguration : table.getDeclaredColumns()) {
			setBackgroundColor(columnConfiguration);
		}
		setNonNullCellExistenceTester(table.getDeclaredColumn("name"));
	}

	private void setBackgroundColor(ColumnConfiguration columnConfiguration) {
		columnConfiguration.setCellStyle("background-color: lightgreen");
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		setBackgroundColor(defaultColumn);
	}

	private void setNonNullCellExistenceTester(ColumnConfiguration columnConfiguration) {
		CellExistenceTester cellExistenceTester = columnConfiguration.getCellExistenceTester();
		columnConfiguration.setCellExistenceTester((rowObject, columnName) -> {
			requireNonNull(rowObject, "Null values for row object are not permitted!");
			return cellExistenceTester.isCellExistent(rowObject, columnName);
		});
	}

}
