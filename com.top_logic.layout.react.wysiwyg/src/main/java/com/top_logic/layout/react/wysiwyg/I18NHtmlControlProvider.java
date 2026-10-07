/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.I18NEditorDialog;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;

/**
 * {@link ReactFieldControlProvider} for {@code tl.model.i18n:I18NHtml} attributes.
 *
 * <p>
 * Edits the current session locale's entry of the attribute value inline with a
 * {@link ReactWysiwygControl}; entries of other languages are preserved on save. The translation
 * between the internationalized attribute value and the edited {@code StructuredText} is done by
 * {@link I18NLocalizedHtmlFieldModel}. A languages button next to the inline editor opens the
 * {@link I18NEditorDialog} for viewing, editing, and translating the other languages.
 * </p>
 *
 * <p>
 * The editor is {@link #isLarge(FieldSpec) large}: where it has no room, the
 * {@link WysiwygControlProvider#htmlPreview(com.top_logic.layout.wysiwyg.ui.StructuredText) plain
 * text} of the value in the user's language stands for it - or in the best available other
 * language, as long as there is none in the user's.
 * </p>
 */
public class I18NHtmlControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		I18NWysiwygControl inline = new I18NWysiwygControl(context, model);
		return I18NEditorDialog.createEditor(context, model, inline, new I18NHtmlValueEditor(),
			field.getLabel());
	}

	@Override
	public boolean isLarge(FieldSpec field) {
		return true;
	}

	/**
	 * Whether no language of the given value has content, see
	 * {@link WysiwygControlProvider#isEmptyHtml(com.top_logic.layout.wysiwyg.ui.StructuredText)}.
	 */
	@Override
	public boolean isEmpty(FieldSpec field, Object value) {
		if (value instanceof I18NStructuredText text) {
			return text.getEntries().values().stream().allMatch(WysiwygControlProvider::isEmptyHtml);
		}
		return ReactFieldControlProvider.super.isEmpty(field, value);
	}

	@Override
	public String previewText(FieldSpec field, Object value) {
		if (value instanceof I18NStructuredText text) {
			return WysiwygControlProvider.htmlPreview(text.localize(I18NLocalizedHtmlFieldModel.editLocale()));
		}
		return ReactFieldControlProvider.super.previewText(field, value);
	}

}
