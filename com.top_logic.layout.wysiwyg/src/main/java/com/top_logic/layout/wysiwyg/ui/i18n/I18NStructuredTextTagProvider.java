/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.EmptyRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.control.PopupEditControl.Settings;
import com.top_logic.layout.form.i18n.I18NField;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.AbstractFieldTranslator;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextWithButtonControl;

/**
 * {@link DisplayProvider} for I18N HTML attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStructuredTextTagProvider implements DisplayProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		return newControl((I18NStructuredTextField) member);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		return newControl((I18NStructuredTextField) member);
	}

	private Control newControl(I18NStructuredTextField i18n) {
		FormField sourceField = I18NTranslationUtil.getSourceField(i18n.getLanguageFields());
		StructuredTextWithButtonControl sourceControl = new StructuredTextWithButtonControl(sourceField);
		sourceControl.setButton(openPopup(i18n));
		OnVisibleControl block = new OnVisibleControl(i18n);
		block.addChild(sourceControl);
		return block;
	}

	private HTMLFragment openPopup(I18NField<?, ?, ?> i18n) {
		Settings settings = new Settings()
			.setFirstLineRenderer(EmptyRenderer.getInstance());
		return new I18NStructuredTextPopupControl(settings, i18n);
	}

	/**
	 * A {@link AbstractFieldTranslator} for {@link FormField}s that are {@link StructuredText}-valued.
	 */
	public static class StructuredTextFieldTranslator extends AbstractFieldTranslator {

		/**
		 * Singleton {@link StructuredTextFieldTranslator} instance.
		 */
		public static final StructuredTextFieldTranslator INSTANCE = new StructuredTextFieldTranslator();

		private StructuredTextFieldTranslator() {
			// Singleton constructor.
		}

		@Override
		public String getValueAsString(FormField field) {
			Object fieldValue = field.getValue();
			if (fieldValue == null) {
				return StringServices.EMPTY_STRING;
			}
			return ((StructuredText) fieldValue).getSourceCode();
		}

		@Override
		public void setValueFromString(String string, FormField source, FormField target) {
			Object value = source.getValue();
			StructuredText text;
			if (value == null) {
				if (StringServices.isEmpty(string)) {
					// Do not produce unnecessary empty objects.
					target.setValue(null);
					return;
				}
				text = new StructuredText();
			} else {
				text = ((StructuredText) value).copy();
			}
			text.setSourceCode(string);
			target.setValue(text);

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(target, text);
			}
		}
	}
}
