/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLStructuredType;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * A read-only table of given objects, showing the
 * {@link ColumnDeclarations#mainColumns(TLStructuredType) main columns} of their type.
 *
 * <p>
 * The cells display the values the way a table in view mode displays them, so a value that needs
 * more room than a row offers is shown by a one-line preview with a button opening its full
 * display.
 * </p>
 */
public final class ReadOnlyObjectTable {

	private ReadOnlyObjectTable() {
		// Static factory.
	}

	/**
	 * Creates a read-only table of the given objects.
	 *
	 * @param context
	 *        The context to create the table in.
	 * @param rowType
	 *        The type of the objects, deciding the columns.
	 * @param rows
	 *        The objects to show, in display order.
	 * @return The table control.
	 */
	public static TableViewControl<Object> create(ReactContext context, TLStructuredType rowType,
			List<?> rows) {
		ViewContext viewContext = context instanceof ViewContext view ? view : new DefaultViewContext(context);
		ColumnResolution scope = new ColumnResolution(rowType, viewContext);
		List<Column<Object, ?>> columns = new ArrayList<>();
		for (ColumnSetup setup : ColumnDeclarations.resolve(ColumnDeclarations.mainColumns(rowType), scope)) {
			columns.add(setup.buildColumn());
		}
		ListRowSource<Object> source = new ListRowSource<>(new ArrayList<>(rows), columns);
		return new TableViewControl<>(viewContext, DefaultTableView.create(columns, source), false);
	}

}
