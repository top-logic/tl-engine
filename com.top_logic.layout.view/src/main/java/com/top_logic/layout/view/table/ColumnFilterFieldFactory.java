/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.FilterFieldFactory;
import com.top_logic.layout.view.form.FieldControlService;

/**
 * {@link FilterFieldFactory} of a table built from {@link ColumnSetup}s: enters a value of a column
 * as a form field of the column's {@link ColumnType} is entered, and shows a value as a cell of the
 * column shows it, both through the {@link FieldControlService}.
 *
 * <p>
 * A column whose type is not resolved, and a point in time (a value list of points in time offers
 * days, which a cell showing the time of day would not show as such), are left to the fallbacks of
 * the dialog.
 * </p>
 */
public class ColumnFilterFieldFactory implements FilterFieldFactory {

	private final Map<String, ColumnType> _types;

	/**
	 * Creates a {@link ColumnFilterFieldFactory}.
	 *
	 * @param types
	 *        The type of the values of each column, by column name.
	 */
	public ColumnFilterFieldFactory(Map<String, ColumnType> types) {
		_types = Map.copyOf(types);
	}

	/**
	 * The {@link ColumnFilterFieldFactory} for the columns of the given setups.
	 */
	public static ColumnFilterFieldFactory of(Collection<ColumnSetup> setups) {
		Map<String, ColumnType> types = new HashMap<>();
		for (ColumnSetup setup : setups) {
			types.put(setup.name(), setup.type());
		}
		return new ColumnFilterFieldFactory(types);
	}

	@Override
	public ReactControl valueInput(ReactContext context, String column, FieldModel model) {
		ColumnType type = resolved(column);
		if (type == null) {
			return null;
		}
		FieldControlService service = FieldControlService.getInstance();
		return service.createFieldControl(context, type.type(), type.annotations(),
			FieldControlService.fieldSpec(type.type(), type.annotations(), null, false, model), model, null);
	}

	@Override
	public ReactControl valueDisplay(ReactContext context, String column, Object value) {
		ColumnType type = resolved(column);
		if (type == null || value instanceof Date) {
			return null;
		}
		// An option is a single value, also of a column whose cells hold several of them, and no
		// single object owns it.
		ColumnType single = new ColumnType(type.type(), false, type.annotations(), type.part(), null);
		return FieldControlService.getInstance().createDisplayControl(context, single, value);
	}

	private ColumnType resolved(String column) {
		ColumnType type = _types.get(column);
		return type == null || !type.resolved() ? null : type;
	}

}
