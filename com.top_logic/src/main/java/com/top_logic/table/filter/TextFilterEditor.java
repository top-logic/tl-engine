/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.table.FilterState;
import com.top_logic.util.Resources;

/**
 * {@link FilterEditor} for a {@link TextColumnFilter}: a pattern field, the
 * {@link TextMatchMode search mode} and a case-sensitive flag.
 *
 * <p>
 * The {@link TextFilterState#wholeField()} flag is not offered; the edited state keeps the one of
 * the state the editor is seeded from.
 * </p>
 */
public class TextFilterEditor implements FilterEditor {

	/**
	 * Displays a {@link TextMatchMode} by its name in the language of the current user.
	 */
	private static final LabelProvider MODE_LABELS = value -> {
		TextMatchMode mode = mode(value);
		return Resources.getInstance().getString(ResKey.forEnum(mode));
	};

	private final AbstractFieldModel _pattern;

	private final SimpleSelectFieldModel _mode;

	private final AbstractFieldModel _caseSensitive;

	private final boolean _wholeField;

	/**
	 * Creates a {@link TextFilterEditor} seeded from the current state (or empty defaults).
	 */
	public TextFilterEditor(TextFilterState current) {
		_pattern = new AbstractFieldModel(current != null ? current.pattern() : "");
		_mode = new SimpleSelectFieldModel(current != null ? current.mode() : TextMatchMode.CONTAINS,
			List.of(TextMatchMode.values()), false);
		// A search always has a mode.
		_mode.setNullable(false);
		_caseSensitive = new AbstractFieldModel(current != null && current.caseSensitive());
		_wholeField = current != null && current.wholeField();
	}

	@Override
	public List<FilterField> fields() {
		return List.of(
			new FilterField(I18NConstants.PATTERN, _pattern),
			new FilterField(I18NConstants.TEXT_MODE, _mode, MODE_LABELS),
			new FilterField(I18NConstants.CASE_SENSITIVE, _caseSensitive));
	}

	@Override
	public FilterState read() {
		return new TextFilterState(string(_pattern), bool(_caseSensitive), mode(_mode.getValue()), _wholeField);
	}

	private static TextMatchMode mode(Object value) {
		if (value instanceof TextMatchMode mode) {
			return mode;
		}
		return value == null ? TextMatchMode.CONTAINS : TextMatchMode.valueOf(String.valueOf(value));
	}

	static String string(FieldModel model) {
		Object value = model.getValue();
		return value == null ? "" : value.toString();
	}

	static boolean bool(FieldModel model) {
		return Boolean.TRUE.equals(model.getValue());
	}

}
