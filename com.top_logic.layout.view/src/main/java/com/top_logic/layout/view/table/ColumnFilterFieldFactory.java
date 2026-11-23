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
import com.top_logic.model.TLReference;

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
		if (type == null || value instanceof Date || value instanceof Boolean || isComposition(type)) {
			// A point in time is listed as the day it falls on, which the label states; a truth
			// value would be drawn as a checkbox next to the checkbox selecting it, and a part of a
			// composition with a button opening it: the label says all of them better.
			return null;
		}
		// An option is a single value, also of a column whose cells hold several of them, and no
		// single object owns it.
		ColumnType single = new ColumnType(type.type(), false, type.annotations(), type.part(), null);
		return FieldControlService.getInstance().createDisplayControl(context, single, value);
	}

	private static boolean isComposition(ColumnType type) {
		return type.part() instanceof TLReference reference && reference.isComposite();
	}

	private ColumnType resolved(String column) {
		ColumnType type = _types.get(column);
		return type == null || !type.resolved() ? null : type;
	}

}
