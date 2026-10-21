/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.control.form.I18NLocalizedFieldModel;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;

/**
 * {@link I18NLocalizedFieldModel} exposing the current session locale's entry of an
 * {@link I18NStructuredText} value as a plain {@link StructuredText}.
 *
 * <p>
 * This lets a control operating on {@link StructuredText} values (e.g. {@link ReactWysiwygControl})
 * edit an {@code I18NHtml} attribute.
 * </p>
 *
 * <p>
 * While the underlying model is editable, extraction is strict (no fallback to other languages),
 * so editing never silently copies another language's content. In view mode the best available
 * translation is shown instead, so a value maintained only in another language is never hidden.
 * </p>
 */
public class I18NLocalizedHtmlFieldModel extends I18NLocalizedFieldModel {

	/**
	 * Creates a {@link I18NLocalizedHtmlFieldModel}.
	 *
	 * @param delegate
	 *        The underlying model holding the {@link I18NStructuredText} value.
	 */
	public I18NLocalizedHtmlFieldModel(FieldModel delegate) {
		super(delegate, new I18NHtmlValueEditor());
	}

	@Override
	protected Object localize(Object i18nValue, boolean editable) {
		if (i18nValue instanceof I18NStructuredText i18n) {
			return editable ? i18n.localizeStrict(editLocale()) : i18n.localize(editLocale());
		}
		return null;
	}

}
