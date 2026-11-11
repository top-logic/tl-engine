/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.util.Locale;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.util.TLContext;

/**
 * {@link DefaultValueProvider} delivering the language of the current user.
 *
 * <p>
 * The language is the {@link Person#getLanguage() language} of the
 * {@link TLContext#currentUser() current user}. If the user has no language, or there is no
 * current user, it is the language of the {@link SubSessionContext#getCurrentLocale() current
 * locale}, if the application supports it, else the {@link DefaultLocaleDefault default
 * language} of the application.
 * </p>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.util:Language")
@Label("Language of the current user")
public class CurrentUserLanguageDefault extends DefaultValueProvider implements DefaultProvider, MODefaultProvider {

	/** Singleton {@link CurrentUserLanguageDefault} instance. */
	public static final CurrentUserLanguageDefault INSTANCE = new CurrentUserLanguageDefault();

	/**
	 * Creates a new {@link CurrentUserLanguageDefault}.
	 */
	protected CurrentUserLanguageDefault() {
		// singleton instance
	}

	@Override
	public Object createDefault(MOAttribute attribute) {
		return currentUserLanguage();
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute) {
		return currentUserLanguage();
	}

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return currentUserLanguage();
	}

	/**
	 * The language delivered by this {@link CurrentUserLanguageDefault}.
	 */
	public Locale currentUserLanguage() {
		Person user = TLContext.currentUser();
		if (user != null) {
			Locale language = user.getLanguage();
			if (language != null) {
				return language;
			}
		}
		SubSessionContext subSession = ThreadContextManager.getSubSession();
		if (subSession != null) {
			Locale supported = supportedLanguage(subSession.getCurrentLocale());
			if (supported != null) {
				return supported;
			}
		}
		return DefaultLocaleDefault.INSTANCE.defaultLocale();
	}

	private static Locale supportedLanguage(Locale locale) {
		if (locale == null) {
			return null;
		}
		String language = locale.getLanguage();
		for (Locale supported : ResourcesModule.getInstance().getSupportedLocales()) {
			if (supported.getLanguage().equals(language)) {
				return supported;
			}
		}
		return null;
	}

}
