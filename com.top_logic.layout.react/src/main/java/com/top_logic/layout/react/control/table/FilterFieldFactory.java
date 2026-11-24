/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.table.filter.FilterField;

/**
 * Creates the controls of the filter dialog of a {@link TableViewControl} that depend on the type
 * of the values of a column: the input of a {@link FilterField#valueCodec() value of the column},
 * e.g. a bound of a range, and the display of an {@link FilterField#option() option}.
 *
 * <p>
 * The table layer knows no model types; the layer building the table from a model sets a factory
 * entering a value as a form does (a date picker, a number field) and showing it as a cell of the
 * column does (color, icon, label), see {@link TableViewControl#setFilterFieldFactory(FilterFieldFactory)}.
 * A factory answering {@code null} leaves the field to the dialog's fallback: a text input written
 * and read by the codec of the field, and the label of the option.
 * </p>
 */
public interface FilterFieldFactory {

	/**
	 * A factory creating no control, so that the dialog uses its fallbacks for every field.
	 */
	FilterFieldFactory NONE = new FilterFieldFactory() {
		@Override
		public ReactControl valueInput(ReactContext context, String column, FieldModel model) {
			return null;
		}

		@Override
		public ReactControl valueDisplay(ReactContext context, String column, Object value) {
			return null;
		}
	};

	/**
	 * The input of a value of the given column, or {@code null} for a text input.
	 *
	 * @param column
	 *        The name of the filtered column.
	 * @param model
	 *        The field model holding the typed value.
	 */
	ReactControl valueInput(ReactContext context, String column, FieldModel model);

	/**
	 * The display of a value of the given column as a cell of the column shows it, or {@code null}
	 * for its label.
	 *
	 * @param column
	 *        The name of the filtered column.
	 * @param value
	 *        The value to show.
	 */
	ReactControl valueDisplay(ReactContext context, String column, Object value);

}
