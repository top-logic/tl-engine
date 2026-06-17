/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.table.FilterState;

/**
 * {@link FilterEditor} for a {@link TextColumnFilter}: a pattern field plus case-sensitive,
 * regular-expression and whole-field flags.
 */
public class TextFilterEditor implements FilterEditor {

	private final AbstractFieldModel _pattern;

	private final AbstractFieldModel _caseSensitive;

	private final AbstractFieldModel _regexp;

	private final AbstractFieldModel _wholeField;

	/**
	 * Creates a {@link TextFilterEditor} seeded from the current state (or empty defaults).
	 */
	public TextFilterEditor(TextFilterState current) {
		_pattern = new AbstractFieldModel(current != null ? current.pattern() : "");
		_caseSensitive = new AbstractFieldModel(current != null && current.caseSensitive());
		_regexp = new AbstractFieldModel(current != null && current.regexp());
		_wholeField = new AbstractFieldModel(current != null && current.wholeField());
	}

	@Override
	public List<FilterField> fields() {
		return List.of(
			new FilterField(I18NConstants.PATTERN, _pattern, FilterFieldKind.TEXT),
			new FilterField(I18NConstants.CASE_SENSITIVE, _caseSensitive, FilterFieldKind.CHECKBOX),
			new FilterField(I18NConstants.REGEXP, _regexp, FilterFieldKind.CHECKBOX),
			new FilterField(I18NConstants.WHOLE_FIELD, _wholeField, FilterFieldKind.CHECKBOX));
	}

	@Override
	public FilterState read() {
		return new TextFilterState(string(_pattern), bool(_caseSensitive), bool(_regexp), bool(_wholeField));
	}

	static String string(FieldModel model) {
		Object value = model.getValue();
		return value == null ? "" : value.toString();
	}

	static boolean bool(FieldModel model) {
		return Boolean.TRUE.equals(model.getValue());
	}

}
