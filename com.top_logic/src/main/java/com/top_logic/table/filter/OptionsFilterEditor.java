/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;

/**
 * {@link FilterEditor} for an {@link OptionsColumnFilter}: a checkbox per option, labelled
 * with the option label and (when available) its {@link MatchCounts facet count}.
 */
public class OptionsFilterEditor implements FilterEditor {

	private final List<Option> _options;

	private final List<AbstractFieldModel> _checks;

	private final MatchCounts _counts;

	private final boolean _columnValues;

	/**
	 * Creates an {@link OptionsFilterEditor}.
	 *
	 * @param options
	 *        The available options.
	 * @param current
	 *        The current selection, or {@code null}.
	 * @param counts
	 *        Facet counts, or {@link MatchCounts#NONE}.
	 * @param columnValues
	 *        Whether the values of the options are values of the column, apart from
	 *        {@link Option#NO_VALUE}, see {@link FilterField#isColumnValue()}.
	 */
	public OptionsFilterEditor(List<Option> options, OptionsFilterState current, MatchCounts counts,
			boolean columnValues) {
		_options = options;
		_counts = counts;
		_columnValues = columnValues;
		Set<Object> selected = current != null ? current.selected() : Set.of();
		_checks = new ArrayList<>(options.size());
		for (Option option : options) {
			_checks.add(new AbstractFieldModel(selected.contains(option.value())));
		}
	}

	@Override
	public List<FilterField> fields() {
		List<FilterField> fields = new ArrayList<>(_options.size());
		for (int n = 0; n < _options.size(); n++) {
			Option option = _options.get(n);
			Integer count = _counts.isAvailable() ? Integer.valueOf(_counts.count(option.value())) : null;
			boolean columnValue = _columnValues && option.value() != Option.NO_VALUE;
			fields.add(FilterField.option(label(option), _checks.get(n), option, count, columnValue));
		}
		return fields;
	}

	private ResKey label(Option option) {
		if (_counts.isAvailable()) {
			return I18NConstants.OPTION_LABEL__NAME_COUNT.fill(option.label(),
				Integer.valueOf(_counts.count(option.value())));
		}
		return option.label();
	}

	@Override
	public FilterState read() {
		Set<Object> selected = new LinkedHashSet<>();
		for (int n = 0; n < _options.size(); n++) {
			if (TextFilterEditor.bool(_checks.get(n))) {
				selected.add(_options.get(n).value());
			}
		}
		return new OptionsFilterState(selected);
	}

}
