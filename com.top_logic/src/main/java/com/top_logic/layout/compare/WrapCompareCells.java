/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.decorator.CompareRowObject;
import com.top_logic.layout.table.CellAdapter;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * {@link TableConfigurationProvider} feeding new values to {@link CellClassProvider}s of table
 * cells in compare mode.
 */
public class WrapCompareCells implements TableConfigurationProvider {

	/**
	 * Singleton {@link WrapCompareCells} instance.
	 */
	public static final WrapCompareCells INSTANCE = new WrapCompareCells();

	private WrapCompareCells() {
		// Singleton constructor.
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		for (ColumnConfiguration column : table.getDeclaredColumns()) {
			adaptColumn(column);
		}
	}

	/**
	 * Wrap a potential {@link CellClassProvider} in the given column.
	 */
	public static void adaptColumn(ColumnConfiguration column) {
		CellClassProvider provider = column.getCssClassProvider();
		if (provider != null) {
			column.setCssClassProvider(wrap(provider));
		}
	}

	private static CellClassProvider wrap(CellClassProvider provider) {
		return new CellClassProvider() {
			@Override
			public String getCellClass(Cell cell) {
				return provider.getCellClass(wrap(cell));
			}

			private Cell wrap(Cell cell) {
				return new CellAdapter() {
					@Override
					public Object getValue() {
						Object value = super.getValue();
						if (value instanceof CompareInfo info) {
							value = info.getChangeValue();
						}
						return value;
					}

					@Override
					public Object getRowObject() {
						Object row = super.getRowObject();
						if (row instanceof CompareRowObject compareRow) {
							row = compareRow.changeValue();
						}
						return row;
					}

					@Override
					protected Cell impl() {
						return cell;
					}
				};
			}
		};
	}
}
