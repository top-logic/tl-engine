/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import com.top_logic.basic.translation.TranslationService;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.FieldTranslator;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.StringValuedFieldTranslator;

/**
 * {@link ControlProvider} that renders a translate button for the given {@link FormField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TranslateButtonCP extends AbstractFormFieldControlProvider {

	private Collection<String> _additionals;

	private Iterable<? extends FormField> _languageFields;

	/**
	 * Creates a {@link TranslateButtonCP}.
	 *
	 * @param languageFields
	 *        All existing language members to determine source and target field.
	 */
	public TranslateButtonCP(Iterable<? extends FormField> languageFields) {
		this(languageFields, Collections.emptyList());
	}

	/**
	 * Creates a {@link TranslateButtonCP}.
	 *
	 * @param languageFields
	 *        All existing language members to determine source and target field.
	 * @param additionals
	 *        Suffix for neighbours of the source fields which must also be translated.
	 */
	public TranslateButtonCP(Iterable<? extends FormField> languageFields, Collection<String> additionals) {
		_languageFields = languageFields;
		_additionals = additionals;
	}

	@Override
	protected Control createInput(FormMember member) {
		FormField field = (FormField) member;

		Locale fieldLanguage = I18NTranslationUtil.getLocaleFromField(field);
		if (I18NTranslationUtil.equalLanguage(fieldLanguage, I18NTranslationUtil.getSourceLanguage())) {
			return null;
		}
		if (!TranslationService.getInstance().isSupported(fieldLanguage)) {
			return null;
		}

		return I18NTranslationUtil.getTranslateControl(field, _languageFields, new MultiFieldTranslator(_additionals));
	}

	private static class MultiFieldTranslator implements FieldTranslator {

		private Collection<String> _additionals;

		MultiFieldTranslator(Collection<String> additionals) {
			_additionals = additionals;
		}

		@Override
		public String translate(FormField source, FormField target) {
			StringValuedFieldTranslator dispatch = StringValuedFieldTranslator.INSTANCE;
			String result = dispatch.translate(source, target);
			if (!_additionals.isEmpty()) {
				String sourceLang = source.get(InternationalizationEditor.LOCALE).getLanguage();
				String targetLang = target.get(InternationalizationEditor.LOCALE).getLanguage();
				FormContainer container = source.getParent();
				for (String additional : _additionals) {
					FormMember derivedSource = container.getMember(InternationalizationEditor.suffixFieldName(sourceLang, additional));
					FormMember derivedTarget = container.getMember(InternationalizationEditor.suffixFieldName(targetLang, additional));
					if (derivedSource.isVisible() && derivedTarget.isVisible()) {
						dispatch.translate((FormField) derivedSource, (FormField) derivedTarget);
					}
				}
			}
			return result;
		}

	}

}