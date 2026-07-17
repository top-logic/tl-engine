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
import com.top_logic.layout.view.form.ReactFieldControlProvider;
import com.top_logic.model.TLStructuredTypePart;

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
 */
public class I18NHtmlControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		ReactWysiwygControl inline = new ReactWysiwygControl(context, new I18NLocalizedHtmlFieldModel(model));
		return I18NEditorDialog.createEditor(context, model, inline, new I18NHtmlValueEditor());
	}

}
