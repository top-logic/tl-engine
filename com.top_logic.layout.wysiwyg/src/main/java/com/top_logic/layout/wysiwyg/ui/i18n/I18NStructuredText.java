/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.layout.wysiwyg.ui.StructuredText.*;
import static java.util.Collections.*;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.searching.FullTextSearchable;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.util.Resources;

/**
 * An internationalized {@link StructuredText}.
 * <p>
 * Stores one {@link StructuredText} per {@link Locale}.
 * </p>
 * <p>
 * As {@link StructuredText} is mutable, instances of this class are mutable, too.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStructuredText implements FullTextSearchable {

	/**
	 * {@link I18NStructuredText} without any content.
	 */
	public static final I18NStructuredText EMPTY = new I18NStructuredText(Collections.emptyMap());

	private final Map<Locale, StructuredText> _content;

	/**
	 * Creates an {@link I18NStructuredText}.
	 * 
	 * @param content
	 *        Is copied to avoid side effects. Null is equivalent to an empty {@link Map}.
	 */
	public I18NStructuredText(Map<Locale, StructuredText> content) {
		_content = unmodifiableMap(initFallbacks(copy(content)));
	}

	private static Map<Locale, StructuredText> copy(Map<Locale, StructuredText> original) {
		if (original == null) {
			return map();
		}
		Map<Locale, StructuredText> result = map();
		for (Entry<Locale, StructuredText> entry : original.entrySet()) {
			result.put(entry.getKey(), entry.getValue().copy());
		}
		return result;
	}

	private static Map<Locale, StructuredText> initFallbacks(Map<Locale, StructuredText> values) {
		/* Fill fallback locales with the same value to allow looking up the value with more general
		 * locales. */
		Set<Entry<Locale, StructuredText>> originalEntries = set(values.entrySet());
		for (Entry<Locale, StructuredText> originalEntry : originalEntries) {
			Locale locale = originalEntry.getKey();
			while (true) {
				locale = fallback(locale);
				if (locale == null) {
					break;
				}
				if (values.get(locale) != null) {
					break;
				}
				values.put(locale, originalEntry.getValue());
			}
		}
		return values;
	}

	private static Locale fallback(Locale locale) {
		if (!locale.getVariant().isEmpty()) {
			return new Locale(locale.getLanguage(), locale.getCountry());
		}
		if (!locale.getCountry().isEmpty()) {
			return new Locale(locale.getLanguage());
		}
		return null;
	}

	/**
	 * The {@link StructuredText} for the current session's {@link Locale}.
	 * <p>
	 * Creates a copy to prevent side effect changes.
	 * </p>
	 */
	public StructuredText localize() {
		return localize(Resources.getCurrentLocale());
	}

	/**
	 * The {@link StructuredText} for the given {@link Locale}.
	 * <p>
	 * Creates a copy to prevent side effect changes.
	 * </p>
	 */
	public StructuredText localize(Locale locale) {
		StructuredText localized = localizeInternal(locale);
		if (localized == null) {
			return null;
		}
		return localized.copy();
	}

	/**
	 * The {@link StructuredText} for the given {@link Locale} without any fallbacks.
	 * 
	 * <p>
	 * Creates a copy to prevent side effect changes.
	 * </p>
	 * 
	 * @return {@link StructuredText} for {@link Locale}. <code>null</code> if no
	 *         {@link StructuredText} was found.
	 */
	public StructuredText localizeStrict(Locale locale) {
		I18NBundleSPI lookup = Resources.getInstance(locale);
		String language = lookup.getLocale().getLanguage();
		while (true) {
			StructuredText structuredText = _content.get(lookup.getLocale());
			if (structuredText != null) {
				return structuredText.copy();
			}

			lookup = lookup.getFallback();
			if (lookup == null) {
				break;
			}

			if (!lookup.getLocale().getLanguage().equals(language)) {
				break;
			}
		}

		return null;
	}

	private StructuredText localizeInternal(Locale locale) {
		I18NBundleSPI lookup = Resources.getInstance(locale);
		do {
			StructuredText structuredText = _content.get(lookup.getLocale());
			if (structuredText != null) {
				return structuredText;
			}

			lookup = lookup.getFallback();
		} while (lookup != null);

		for (Locale fallbackLocale : ResourcesModule.getInstance().getSupportedLocales()) {
			StructuredText structuredText = _content.get(fallbackLocale);
			if (structuredText != null) {
				return structuredText;
			}
		}
		for (StructuredText entry : _content.values()) {
			if (entry != null) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * The {@link StructuredText#getSourceCode() source code} for the current session's
	 * {@link Locale}.
	 * 
	 * @return The empty {@link String} if there is no localization.
	 */
	public String localizeSourceCode() {
		return localizeSourceCode(Resources.getCurrentLocale());
	}

	/**
	 * The {@link StructuredText#getSourceCode() source code} for the given {@link Locale}.
	 * 
	 * @return The empty {@link String} if there is no localization.
	 */
	public String localizeSourceCode(Locale locale) {
		return getSourceCodeNullSafe(localizeInternal(locale));
	}

	/**
	 * The images for the current session's {@link Locale}.
	 * 
	 * @return An unmodifiable view. Never null. An empty {@link Map} if there is no localization.
	 */
	public Map<String, BinaryData> localizeImages() {
		return localizeImages(Resources.getCurrentLocale());
	}

	/**
	 * The images for the given {@link Locale}.
	 * 
	 * @return An unmodifiable view. Never null. An empty {@link Map} if there is no localization.
	 */
	public Map<String, BinaryData> localizeImages(Locale locale) {
		return unmodifiableMap((getImagesNullSafe(localizeInternal(locale))));
	}

	/**
	 * The internal {@link Map}.
	 * <p>
	 * Important: Use the <code>localizeXxx(...)</code> methods, instead. To display all
	 * translations for example, get the list of languages that the application supports and iterate
	 * over them, retrieving the corresponding value with <code>localizeXxx(Locale)</code>. Using
	 * this method ignores the {@link Locale} fallback behavior and will there probably cause wrong
	 * results in some situations.
	 * </p>
	 * <p>
	 * <em>Important:</em>Neither the {@link Map}, nor the {@link StructuredText}s must be modified.
	 * </p>
	 */
	@FrameworkInternal
	public Map<Locale, StructuredText> getEntries() {
		return _content;
	}

	@Override
	public void generateFullText(FullTextBuBuffer buffer) {
		for (StructuredText content : _content.values()) {
			content.generateFullText(buffer);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(_content);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		I18NStructuredText other = (I18NStructuredText) obj;
		return Objects.equals(_content, other._content);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("content", _content)
			.build();
	}

}
