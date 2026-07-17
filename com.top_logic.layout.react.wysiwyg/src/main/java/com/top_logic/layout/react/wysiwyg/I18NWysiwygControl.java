/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.Resources;

/**
 * {@link ReactWysiwygControl} editing the current session locale's entry of an {@code I18NHtml}
 * attribute inline.
 *
 * <p>
 * The control operates on the {@link I18NLocalizedHtmlFieldModel locale-scoped view} of the
 * internationalized value. When the user-locale entry is edited and a {@link TranslationService}
 * is active, the other locales are auto-translated from it when the editor is committed (loses
 * focus), overwriting their previous values; otherwise they are preserved. Translation runs on
 * commit rather than per keystroke, since it is a blocking remote call.
 * </p>
 */
public class I18NWysiwygControl extends ReactWysiwygControl {

	private final FieldModel _i18nModel;

	private final I18NHtmlValueEditor _valueEditor = new I18NHtmlValueEditor();

	/**
	 * Creates a new {@link I18NWysiwygControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param i18nModel
	 *        The field model holding the internationalized value.
	 */
	public I18NWysiwygControl(ReactContext context, FieldModel i18nModel) {
		super(context, new I18NLocalizedHtmlFieldModel(i18nModel));
		_i18nModel = i18nModel;
		if (TranslationService.isActive()) {
			// Defer auto-translation to editor commit (blur); see #handleCommit.
			putState(COMMIT_ON_BLUR, Boolean.TRUE);
		}
	}

	/**
	 * Translates the current-locale entry into the other locales, overwriting their previous
	 * values.
	 *
	 * <p>
	 * Sent by the client when an edited editor is committed (loses focus), so the blocking
	 * translation runs once per edit rather than on every keystroke. The client only sends this
	 * after an actual edit, so merely focusing and leaving the editor translates nothing.
	 * </p>
	 */
	@ReactCommandHandler(COMMIT_COMMAND)
	void handleCommit() {
		if (!TranslationService.isActive()) {
			return;
		}
		Object i18nValue = _i18nModel.getValue();
		Locale editLocale = I18NLocalizedHtmlFieldModel.editLocale();
		Object edited = _valueEditor.localize(i18nValue, editLocale);
		if (_valueEditor.isEmpty(edited)) {
			return;
		}
		Translator translator = TranslationService.getInstance();
		if (!translator.isSupported(editLocale)) {
			return;
		}

		Map<Locale, Object> entries = new HashMap<>();
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			Object entry;
			if (locale.getLanguage().equals(editLocale.getLanguage())) {
				entry = edited;
			} else if (translator.isSupported(locale)) {
				entry = translate(translator, (StructuredText) edited, editLocale, locale, i18nValue);
			} else {
				entry = _valueEditor.localize(i18nValue, locale);
			}
			entries.put(locale, entry);
		}
		_i18nModel.setValue(_valueEditor.merge(i18nValue, entries));
	}

	/**
	 * Translates {@code edited} from {@code source} to {@code target}; on a service failure keeps
	 * the previous entry of {@code target} from {@code i18nValue}.
	 */
	private Object translate(Translator translator, StructuredText edited, Locale source, Locale target,
			Object i18nValue) {
		try {
			return _valueEditor.translate(translator, edited, source, target);
		} catch (I18NRuntimeException ex) {
			return _valueEditor.localize(i18nValue, target);
		}
	}

}
