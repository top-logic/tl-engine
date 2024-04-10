/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKey.LiteralKey;

/**
 * Utilities for {@link ResKey}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ResKeyUtil {

	/**
	 * Resolves the translation for the given {@link ResKey} in the given {@link Locale}.
	 * 
	 * @param resKey
	 *        Resource key.
	 * @param locale
	 *        Context {@link Locale}.
	 * @return Translation of the {@link ResKey} in the given {@link Locale}.
	 */
	public static String getTranslation(ResKey resKey, Locale locale) {
		if (resKey instanceof LiteralKey) {
			/* Don't use the translation from the fallback language: */
			return getTranslation((LiteralKey) resKey, locale);
		}
		if (ResourcesModule.Module.INSTANCE.isActive()) {
			/* In this case, there is no way to detect whether the translation is coming from the
			 * fallback language. */
			return (ResourcesModule.getInstance().getBundle(locale)).getString(resKey, null);
		} else {
			/* This may happen when during the application startup a message is written. In this
			 * case the resources module may not be started. */
			return resKey.toString();
		}
	}

	private static String getTranslation(LiteralKey literalKey, Locale locale) {
		String result = literalKey.getTranslationWithoutFallbacks(locale);
		if (result != null) {
			return result;
		}
		/* Some parts of <i>TopLogic</i> use locales like "de", and other parts use locales like
		 * "de_DE_dynamic". And projects might use even further variants. As long as there is just
		 * one translation, or always the same, it is clear what the user intended to do and this
		 * storage should store it. */
		return searchOtherLocalesWithSameLanguage(literalKey, locale);
	}

	private static String searchOtherLocalesWithSameLanguage(LiteralKey literalKey, Locale locale) {
		String result = null;
		for (Entry<Locale, String> entry : literalKey.getTranslations().entrySet()) {
			if (isSameLanguage(entry.getKey(), locale)) {
				if (result != null && !result.equals(entry.getValue())) {
					throw new RuntimeException("Ambiguous translations: " + literalKey);
				}
				result = entry.getValue();
			}
		}
		return result;
	}

	private static boolean isSameLanguage(Locale a, Locale b) {
		return Objects.equals(a.getLanguage(), b.getLanguage());
	}

	/**
	 * Creates a new {@link ResKey} where the translation for the given locale is replaced.
	 * 
	 * @param resKey
	 *        Null is treated as a {@link ResKey} without translations.
	 * @param locale
	 *        Is not allowed to be null.
	 * @param newTranslation
	 *        Is allowed to be null and empty: The value is passed to
	 *        {@link Builder#add(Locale, String)} without changes.
	 * @return Never null.
	 */
	public static ResKey updateTranslation(ResKey resKey, Locale locale, String newTranslation) {
		requireNonNull(locale);
		Map<Locale, String> translations = toMap(resKey);
		translations.put(locale, newTranslation);
		return fromMap(translations);
	}

	/**
	 * Creates a map with the translations of this {@link ResKey}.
	 * <p>
	 * The locales are taken from {@link ResourcesModule#getSupportedLocales()}.
	 * </p>
	 * <p>
	 * If there is no translation for a {@link Locale}, null is stored as value.
	 * </p>
	 * 
	 * @param key
	 *        If null, an empty {@link Map} is returned.
	 * @return Never null. A new, mutable and resizable {@link Map}.
	 */
	public static Map<Locale, String> toMap(ResKey key) {
		if (key == null) {
			return map();
		}
		Map<Locale, String> map = map();
		List<Locale> locales = ResourcesModule.getInstance().getSupportedLocales();
		for (Locale locale : locales) {
			/* Don't use the fallback translation: That would make it impossible for callers to
			 * detect whether a translation is missing. */
			String translation = translateWithoutFallback(locale, key);
			map.put(locale, translation);
		}
		return map;
	}

	/**
	 * Creates a new {@link ResKey} with the given translations.
	 * 
	 * @param translations
	 *        None of the locales must be null. Translations are allowed to be null and empty: These
	 *        values are passed to {@link Builder#add(Locale, String)} without changes.
	 * @return Never null.
	 */
	public static ResKey fromMap(Map<Locale, String> translations) {
		ResKey.Builder builder = ResKey.builder();
		for (Entry<Locale, String> entry : CollectionUtil.nonNull(translations.entrySet())) {
			Locale locale = requireNonNull(entry.getKey());
			String translation = entry.getValue();
			builder.add(locale, translation);
		}
		return requireNonNull(builder.build());
	}

	/**
	 * Translates the given key in the given locale without applying any fallback language.
	 * 
	 * @param locale
	 *        Locale to get translation for.
	 * @param key
	 *        The key to resolve.
	 */
	public static String translateWithoutFallback(Locale locale, ResKey key) {
		return ResourcesModule.getInstance().getBundle(locale).getStringWithoutFallback(key);
	}

}
