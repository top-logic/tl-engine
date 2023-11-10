/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.util.List;

import com.top_logic.element.i18n.I18NField;
import com.top_logic.element.i18n.I18NStringTagProvider.I18NStringControlRenderer;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructureTextTagProvider.StructuredTextFieldTranslator;

/**
 * {@link ControlProvider} creating a suitable {@link Control} for a
 * {@link I18NStructuredTextField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStructuredTextControlProvider implements ControlProvider {

	/**
	 * Singleton {@link I18NStructuredTextControlProvider} instance.
	 */
	public static final I18NStructuredTextControlProvider INSTANCE = new I18NStructuredTextControlProvider();

	private I18NStructuredTextControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model instanceof I18NField) {
			return internalCreateControl((I18NField<?, ?, ?>) model, style);
		}
		return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
	}

	private Control internalCreateControl(I18NField<?, ?, ?> member, String style) {
		OnVisibleControl block = new OnVisibleControl(member);
		List<? extends FormField> languageFields = member.getLanguageFields();
		for (FormField field : languageFields) {
			block.addChild(StructuredTextControlProvider.INSTANCE.createControl(field, style));
			block.addChild(new ErrorControl(field, true));
			if (!I18NTranslationUtil.isSourceField(field)) {
				block.addChild(I18NTranslationUtil.getTranslateControl(field, languageFields.iterator(),
					StructuredTextFieldTranslator.INSTANCE));
			}
		}
		block.setRenderer(I18NStringControlRenderer.ABOVE_INSTANCE);
		return block;
	}

}

