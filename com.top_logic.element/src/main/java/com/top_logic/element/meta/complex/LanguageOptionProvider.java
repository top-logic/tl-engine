/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.col.LRUCache;
import com.top_logic.element.meta.ListOptionProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.util.TLContext;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class LanguageOptionProvider implements ListOptionProvider {

	/**
	 * Maximum size of the {@link #sortedLanguagesCache}.
	 * <p>
	 * Reasons for the concrete value: Top-logic applications currently support only German and
	 * English. But some users may try to switch to other languages. This should not clear the
	 * cache. Therefore, the cache has to be a bit larger.
	 * </p>
	 */
	private static final int CACHE_SIZE = 5;

	private Collection<Locale> allLanguages;

	private Map<Locale, List<Locale>> sortedLanguagesCache = new LRUCache<>(CACHE_SIZE);

	/**
	 * The instance of the {@link LanguageOptionProvider}. This is not a singleton, as (potential)
	 * subclasses can create further instances.
	 */
	public static final LanguageOptionProvider INSTANCE = new LanguageOptionProvider();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected LanguageOptionProvider() {
		String[] languageStrings = Locale.getISOLanguages();
		allLanguages = new ArrayList<>(languageStrings.length);
		for (String languageString : languageStrings) {
			allLanguages.add(new Locale(languageString));
		}
	}

	@Override
	public final List<Locale> getOptionsList(EditContext editContext) {
		return getOptionsList(TLContext.getLocale());
	}

	/**
	 * The languages for the given display language.
	 */
	public final synchronized List<Locale> getOptionsList(Locale displayLanguage) {
		if (!sortedLanguagesCache.containsKey(displayLanguage)) {
			List<Locale> copiedLanguageCodes = getLanguageCodesCopy();
			Collections.sort(copiedLanguageCodes, new LanguageComparator(displayLanguage));
			sortedLanguagesCache.put(displayLanguage, copiedLanguageCodes);
		}
		return new ArrayList<>(sortedLanguagesCache.get(displayLanguage));
	}

	/**
	 * Hook for subclasses that want to filter the {@link Locale}s.
	 */
	protected List<Locale> getLanguageCodesCopy() {
		return new ArrayList<>(allLanguages);
	}

	/**
	 * Special {@link Comparator} using a {@link Collator} to sort the {@link Locale}s by their
	 * (language based) {@link Locale#getDisplayLanguage() language display name}.
	 * 
	 * @author <a href=mailto:jco@top-logic.com>jco</a>
	 */
	private class LanguageComparator implements Comparator<Locale> {

		private Locale languageToSortIn;

		private Collator collator;

		public LanguageComparator(Locale aLanguageToSortIn) {
			languageToSortIn = aLanguageToSortIn;
			collator = Collator.getInstance(languageToSortIn);
		}

		@Override
		public int compare(Locale aO1, Locale aO2) {
			String langString1 = aO1.getDisplayLanguage(languageToSortIn);
			String langString2 = aO2.getDisplayLanguage(languageToSortIn);

			return collator.compare(langString1, langString2);
		}

	}

}

