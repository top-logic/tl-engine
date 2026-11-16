/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.form.model.SelectFieldModel;

/**
 * Conversion between a value and the selection a select control represents it by.
 *
 * <p>
 * A select control edits the value of a {@link SelectFieldModel} as the list of the chosen options,
 * whether the value is a single object or a collection of them. Where the value lives - an
 * attribute, a channel, a function writing it -, it is a single object, or nothing, for a field
 * holding one value, and a collection for a field holding several.
 * </p>
 */
public final class SelectionValues {

	private SelectionValues() {
		// Static utilities only.
	}

	/**
	 * The selection standing for the given value: the elements of a collection, the single object
	 * otherwise, and nothing at all for {@code null}.
	 *
	 * @param value
	 *        A single object, a collection of objects, or {@code null}.
	 * @return A new list holding the chosen options.
	 */
	public static List<Object> selection(Object value) {
		if (value == null) {
			return new ArrayList<>();
		}
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return new ArrayList<>(Collections.singletonList(value));
	}

	/**
	 * The value the given selection stands for.
	 *
	 * @param selection
	 *        The list of chosen options a select control produced, a single option, or
	 *        {@code null} for none.
	 * @param multiple
	 *        Whether the value is a collection of objects rather than a single one.
	 * @return For a value holding several objects, a new list of the chosen options; otherwise the
	 *         first chosen option, {@code null} where none is chosen.
	 */
	public static Object value(Object selection, boolean multiple) {
		List<?> options;
		if (selection instanceof List<?> list) {
			options = list;
		} else if (selection == null) {
			options = Collections.emptyList();
		} else {
			options = Collections.singletonList(selection);
		}
		if (multiple) {
			return new ArrayList<>(options);
		}
		return options.isEmpty() ? null : options.get(0);
	}

}
