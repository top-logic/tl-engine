/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tools.resources.translate.Translator;

/**
 * {@link I18NValueEditor} for {@code I18NString} values: the internationalized value is a
 * multi-locale {@link ResKey}, each entry is a plain {@link String} edited in a
 * {@link ReactTextInputControl}.
 */
public class I18NStringValueEditor implements I18NValueEditor {

	private final int _rows;

	/**
	 * Creates a {@link I18NStringValueEditor}.
	 *
	 * @param rows
	 *        The number of visible rows for a multi-line text area per language, or {@code 0} to
	 *        render each language as a single line.
	 */
	public I18NStringValueEditor(int rows) {
		_rows = rows;
	}

	@Override
	public Object localize(Object i18nValue, Locale locale) {
		if (i18nValue instanceof ResKey key) {
			return ResKeyUtil.translateWithoutFallback(locale, key);
		}
		return null;
	}

	@Override
	public boolean isEmpty(Object entry) {
		return entry == null || entry.toString().isEmpty();
	}

	@Override
	public Object merge(Object i18nValue, Map<Locale, Object> entries) {
		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Map.Entry<Locale, Object> entry : entries.entrySet()) {
			if (!isEmpty(entry.getValue())) {
				builder.add(entry.getKey(), entry.getValue().toString());
				any = true;
			}
		}
		return any ? builder.build() : null;
	}

	@Override
	public Object translate(Translator translator, Object entry, Locale source, Locale target) {
		return translator.translate(entry.toString(), source, target);
	}

	@Override
	public ReactControl createEntryEditor(ReactContext context, FieldModel entryModel) {
		ReactTextInputControl editor = new ReactTextInputControl(context, entryModel);
		if (isTall()) {
			editor.setMultiline(_rows);
		}
		return editor;
	}

	@Override
	public boolean isTall() {
		return _rows > 0;
	}

	@Override
	public DisplayDimension dialogWidth() {
		return DisplayDimension.px(480);
	}

}
