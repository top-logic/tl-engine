/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.scripting;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;

/**
 * {@link ModelNamingScheme} of {@link I18NStructuredText} values.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class I18NStructuredTextNamingScheme
		extends AbstractModelNamingScheme<I18NStructuredText, I18NStructuredTextNamingScheme.Name> {

	/**
	 * {@link ModelName} of {@link I18NStructuredTextNamingScheme}.
	 */
	public interface Name extends ContextDependent {

		/**
		 * The translations of an internationalized structured text.
		 */
		@MapBinding(tag = "translation", key = "lang", attribute = "value")
		Map<String, String> getTranslations();

		/**
		 * @see #getTranslations()
		 */
		void setTranslations(Map<String, String> translations);

	}

	/**
	 * Creates a {@link I18NStructuredTextNamingScheme}.
	 */
	public I18NStructuredTextNamingScheme() {
		super(I18NStructuredText.class, Name.class);
	}

	@Override
	protected void initName(Name name, I18NStructuredText model) {
		Map<String, String> translations = new HashMap<>();

		ResourcesModule resources = ResourcesModule.getInstance();
		for (String localeName : resources.getSupportedLocaleNames()) {
			Locale locale = ResourcesModule.localeFromString(localeName);
			String value = model.localizeSourceCode(locale);
			if (!StringServices.isEmpty(value)) {
				translations.put(localeName, value);
			}
		}
		name.setTranslations(translations);
	}

	@Override
	public I18NStructuredText locateModel(ActionContext context, Name name) {
		Map<String, String> translationSources = name.getTranslations();
		Map<Locale, StructuredText> translations = new HashMap<>();

		ResourcesModule resources = ResourcesModule.getInstance();
		for (String localeName : resources.getSupportedLocaleNames()) {
			Locale locale = ResourcesModule.localeFromString(localeName);
			String source = translationSources.get(localeName);
			if (source != null) {
				translations.put(locale, new StructuredText(source));
			}
		}

		return new I18NStructuredText(translations);
	}

}
