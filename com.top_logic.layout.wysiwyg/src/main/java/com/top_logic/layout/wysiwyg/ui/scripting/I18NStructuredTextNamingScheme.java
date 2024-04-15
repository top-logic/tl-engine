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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
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
		@Key(Translation.LANGUAGE)
		Map<String, Translation> getTranslations();

		/**
		 * @see #getTranslations()
		 */
		void setTranslations(Map<String, Translation> translations);

	}

	/**
	 * Translation object for a {@link Name}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Translation extends ConfigurationItem {
		/**
		 * Configuration name of {@link #getValue()}.
		 */
		String VALUE = "value";

		/**
		 * Configuration name of {@link #getLanguage()}.
		 */
		String LANGUAGE = "language";

		/**
		 * The code of the language for this translation.
		 */
		@com.top_logic.basic.config.annotation.Name(LANGUAGE)
		String getLanguage();

		/**
		 * Setter for {@link #getLanguage()}.
		 */
		void setLanguage(String value);

		/**
		 * The value localized in the language {@link #getLanguage()}.
		 */
		@com.top_logic.basic.config.annotation.Name(VALUE)
		String getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(String value);
	}

	/**
	 * Creates a {@link I18NStructuredTextNamingScheme}.
	 */
	public I18NStructuredTextNamingScheme() {
		super(I18NStructuredText.class, Name.class);
	}

	@Override
	protected void initName(Name name, I18NStructuredText model) {
		Map<String, Translation> translations = new HashMap<>();

		ResourcesModule resources = ResourcesModule.getInstance();
		for (String localeName : resources.getSupportedLocaleNames()) {
			Locale locale = ResourcesModule.localeFromString(localeName);
			String value = model.localizeSourceCode(locale);
			if (!StringServices.isEmpty(value)) {
				Translation translation = TypedConfiguration.newConfigItem(Translation.class);
				translation.setLanguage(localeName);
				translation.setValue(value);
				translations.put(translation.getLanguage(), translation);
			}
		}
		name.setTranslations(translations);
	}

	@Override
	public I18NStructuredText locateModel(ActionContext context, Name name) {
		Map<String, Translation> translationSources = name.getTranslations();
		Map<Locale, StructuredText> translations = new HashMap<>();

		ResourcesModule resources = ResourcesModule.getInstance();
		for (String localeName : resources.getSupportedLocaleNames()) {
			Locale locale = ResourcesModule.localeFromString(localeName);
			Translation source = translationSources.get(localeName);
			if (source != null) {
				translations.put(locale, new StructuredText(source.getValue()));
			}
		}

		return new I18NStructuredText(translations);
	}

}
