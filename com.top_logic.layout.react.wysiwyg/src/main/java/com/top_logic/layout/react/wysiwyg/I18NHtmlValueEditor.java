/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.I18NValueEditor;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.tools.resources.translate.Translator;

/**
 * {@link I18NValueEditor} for {@code tl.model.i18n:I18NHtml} values: the internationalized value
 * is an {@link I18NStructuredText}, each entry is a {@link StructuredText} edited in a
 * {@link ReactWysiwygControl}.
 */
public class I18NHtmlValueEditor implements I18NValueEditor {

	@Override
	public Object localize(Object i18nValue, Locale locale) {
		if (i18nValue instanceof I18NStructuredText i18n) {
			return i18n.localizeStrict(locale);
		}
		return null;
	}

	@Override
	public boolean isEmpty(Object entry) {
		if (entry == null) {
			return true;
		}
		StructuredText text = (StructuredText) entry;
		if (!text.getImages().isEmpty()) {
			return false;
		}
		String sourceCode = text.getSourceCode();
		// An empty wysiwyg document still has block markup (e.g. an empty paragraph), so emptiness
		// is decided by the rendered text content, not the raw source code.
		return sourceCode == null || sourceCode.isEmpty() || Jsoup.parse(sourceCode).text().isBlank();
	}

	@Override
	public Object merge(Object i18nValue, Map<Locale, Object> entries) {
		Map<Locale, StructuredText> merged = i18nValue instanceof I18NStructuredText old
			? I18NStructuredTextUtil.copyEntries(old)
			: new HashMap<>();
		for (Map.Entry<Locale, Object> entry : entries.entrySet()) {
			if (isEmpty(entry.getValue())) {
				merged.remove(entry.getKey());
			} else {
				merged.put(entry.getKey(), (StructuredText) entry.getValue());
			}
		}
		return merged.isEmpty() ? null : new I18NStructuredText(merged);
	}

	@Override
	public Object translate(Translator translator, Object entry, Locale source, Locale target) {
		StructuredText sourceText = (StructuredText) entry;
		// Translate the markup (the translation service preserves tags); keep the embedded images.
		StructuredText result = sourceText.copy();
		result.setSourceCode(translator.translate(sourceText.getSourceCode(), source, target));
		return result;
	}

	@Override
	public ReactControl createEntryEditor(ReactContext context, FieldModel entryModel) {
		return new ReactWysiwygControl(context, entryModel);
	}

	@Override
	public boolean isTall() {
		return true;
	}

	@Override
	public DisplayDimension dialogWidth() {
		return DisplayDimension.px(720);
	}

}
