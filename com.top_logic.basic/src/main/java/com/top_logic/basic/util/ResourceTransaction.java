/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.Locale;

/**
 * Operation changing the application's resource database with translation strings for
 * {@link ResKey}s.
 * 
 * @see ResourcesModule#startResourceTransaction()
 */
public interface ResourceTransaction extends AutoCloseable {

	/**
	 * Adds the given translation for the given language to the given {@link ResKey}.
	 *
	 * @param localeCode
	 *        The language to update.
	 * @param key
	 *        The {@link ResKey} to update.
	 * @param translation
	 *        The new translation. A value of <code>null</code> means to delete the translation.
	 */
	default void saveI18N(String localeCode, ResKey key, String translation) {
		saveI18N(ResourcesModule.localeFromString(localeCode), key, translation);
	}

	/**
	 * Adds the given translation for the given locale to the given {@link ResKey}.
	 *
	 * @param locale
	 *        The {@link Locale} to update.
	 * @param key
	 *        The {@link ResKey} to update.
	 * @param translation
	 *        The new translation. A value of <code>null</code> means to delete the translation.
	 */
	void saveI18N(Locale locale, ResKey key, String translation);

	/**
	 * Makes the modification persistent.
	 */
	void commit();

	@Override
	void close();

}
