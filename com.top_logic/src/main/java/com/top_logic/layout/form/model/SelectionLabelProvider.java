/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormField;

/**
 * {@link LabelProvider} that creates a label for a selection {@link List} in a {@link SelectField}
 * or {@link FormField} annotated through {@link SelectFieldUtils}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectionLabelProvider implements LabelProvider {
	private final FormField _field;

	private final String _separator;

	/**
	 * Creates a {@link SelectionLabelProvider}.
	 * 
	 * @param field
	 *        The {@link FormField} that provides the labels.
	 * @param separator
	 *        The separator used for options.
	 */
	public SelectionLabelProvider(FormField field, String separator) {
		_field = field;
		_separator = separator;
	}

	@Override
	public String getLabel(Object value) {
		List<?> selection = (List<?>) value;
		int size = selection.size();

		if (size == 1) {
			// Optimization for single selections.
			return SelectFieldUtils.getOptionLabel(_field, selection.get(0));
		}

		if (size == 0) {
			return SelectFieldUtils.getEmptySelectionLabelImmutable(_field);
		}

		// Multiple selections
		StringBuilder result = new StringBuilder(size << 5); // * 32
		result.append(SelectFieldUtils.getOptionLabel(_field, selection.get(0)));
		for (int i = 1; i < size; i++) {
			result.append(_separator);
			result.append(SelectFieldUtils.getOptionLabel(_field, selection.get(i)));
		}

		return result.toString();
	}
}