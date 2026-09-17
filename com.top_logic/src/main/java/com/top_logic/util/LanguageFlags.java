/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.basic.ThemeImage;

/**
 * The flag a language is labelled with.
 *
 * <p>
 * A language is not a country, so which flag stands for it is a convention rather than a fact: the
 * flag of the country the language is most widely associated with, of the area it is spoken across
 * where no single country carries it, and the fallback flag where neither applies. The convention
 * is configuration because an application may disagree with it - an English-language product of a
 * British company labels English with the union flag rather than the star-spangled banner.
 * </p>
 *
 * <p>
 * Entries are keyed by language tag and consulted most specific first, so a region carries its own
 * flag only where the application supports that region as a language of its own: {@code de} is
 * German and {@code de-AT} is Austrian German, and an application offering only the former shows
 * only the former's flag.
 * </p>
 *
 * @see FlagIcon
 */
@ServiceDependencies({})
public class LanguageFlags extends ConfiguredManagedClass<LanguageFlags.Config> {

	/**
	 * Configuration of {@link LanguageFlags}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<LanguageFlags> {

		/** Configuration name for {@link #getFlags()}. */
		String FLAGS = "flags";

		/**
		 * The flag of each language, by language tag.
		 */
		@Name(FLAGS)
		@Key(LanguageFlag.LANGUAGE)
		Map<String, LanguageFlag> getFlags();

		/**
		 * The flag of one language.
		 */
		interface LanguageFlag extends ConfigurationItem {

			/** Configuration name for {@link #getLanguage()}. */
			String LANGUAGE = "language";

			/** Configuration name for {@link #getFlag()}. */
			String FLAG = "flag";

			/**
			 * The language tag this entry is the flag of, e.g. {@code de} or {@code de-AT}.
			 */
			@Name(LANGUAGE)
			@Mandatory
			String getLanguage();

			/**
			 * The code of the flag, as the flag stylesheet spells it.
			 *
			 * <p>
			 * The code of a country for a language one country carries, of a language area for one
			 * that no single country does.
			 * </p>
			 */
			@Name(FLAG)
			@Mandatory
			String getFlag();
		}
	}

	private final Map<String, ThemeImage> _flags = new HashMap<>();

	/**
	 * Creates a new {@link LanguageFlags} from configuration.
	 */
	public LanguageFlags(InstantiationContext context, Config config) {
		super(context, config);
		for (Config.LanguageFlag entry : config.getFlags().values()) {
			_flags.put(key(entry.getLanguage()), FlagIcon.forCode(entry.getFlag()));
		}
	}

	/**
	 * The flag of the given locale's language.
	 *
	 * <p>
	 * The entry for the locale's language and region is preferred over the one for its language
	 * alone, so an application supporting a region as a language of its own labels it separately.
	 * A language no entry covers is given the {@link FlagIcon#FALLBACK} rather than no flag at all,
	 * since a list in which some entries carry a flag and others do not reads worse than one in
	 * which they all do.
	 * </p>
	 *
	 * @param locale
	 *        The locale whose language is to be labelled; may be {@code null}.
	 * @return The flag, never {@code null}.
	 */
	public ThemeImage getFlag(Locale locale) {
		if (locale == null) {
			return FlagIcon.FALLBACK;
		}
		String language = locale.getLanguage();
		String country = locale.getCountry();
		if (!StringServices.isEmpty(country)) {
			ThemeImage regional = _flags.get(key(language + "-" + country));
			if (regional != null) {
				return regional;
			}
		}
		ThemeImage flag = _flags.get(key(language));
		return flag != null ? flag : FlagIcon.FALLBACK;
	}

	private static String key(String languageTag) {
		return languageTag.toLowerCase().replace('_', '-');
	}

	/**
	 * The singleton {@link LanguageFlags} service instance.
	 */
	public static LanguageFlags getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link LanguageFlags}.
	 */
	public static final class Module extends TypedRuntimeModule<LanguageFlags> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<LanguageFlags> getImplementation() {
			return LanguageFlags.class;
		}
	}

}
