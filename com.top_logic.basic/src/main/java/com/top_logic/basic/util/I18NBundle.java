/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Bundle of resources for a given language.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface I18NBundle {

	/**
	 * The most specific locale this bundle provides resource for.
	 * 
	 * @see #getLocale()
	 */
	Locale getLocale();

	/**
	 * All supported {@link Locale} alphabetically sorted according to {@link #getLocale()} with the
	 * current locale as first element.
	 */
	List<Locale> getSupportedLocalesInDisplayOrder();

	/**
	 * Get all keys of this resource (excluding any fallback bundles).
	 * 
	 * @return the keys of this resource.
	 */
	Set<String> getLocalKeys();

	/**
	 * Get the string for the given key out from the {@link I18NBundle}. If there is no definition
	 * for this key, the key will be returned in brackets.
	 * 
	 * @param aKey
	 *        the key to get resource for.
	 * @return A resource from this {@link I18NBundle}.
	 */
	String getString(ResKey aKey);

	/**
	 * Get the string for the given key out from the {@link I18NBundle}. If there is no definition
	 * for this key, the key will be returned in brackets.
	 * 
	 * @param aKey
	 *        the key to get resource for.
	 * @param aDefault
	 *        The default value, if no entry found.
	 * @return A resource from this {@link I18NBundle}.
	 */
	default String getString(ResKey aKey, String aDefault) {
		return getStringWithDefaultKey(aKey, ResKey.text(aDefault));
	}

	/**
	 * This method returns an internationalization for the given key <code>aKey</code>. If there is
	 * no value for that key and <code>aDefaultKey</code> is not <code>null</code>, then the
	 * internationalization for the default key will be returned.
	 * 
	 * @param aKey
	 *        the key for which an internationalization is demanded.
	 * @param aDefaultKey
	 *        the key which is used if no entry for <code>aKey</code> is present.
	 */
	default String getStringWithDefaultKey(ResKey aKey, ResKey aDefaultKey) {
		return getString(ResKey.fallback(aKey, aDefaultKey));
	}

	/**
	 * Return the I18N message for the given parameter.
	 * 
	 * The parameter is expected to consist of at least 2 parts divided by an '/' symbol. The first
	 * part is the key of the message from the resources, the other parts are the values to be
	 * inserted into that message. If you want to display a date range, you would use a parameter
	 * like:
	 * 
	 * <pre>
	 * demo.message.date / d123443534634 / d345346457443
	 * </pre>
	 * 
	 * where the key is "demo.message.date" and the two values to be inserted into the message are
	 * two dates in a string representation provided by {@link ResKey#encode(ResKey)}.<br />
	 * <b>Note:</b> Because '/' is the delimiter, replace all '/' with "//" in the strings you want
	 * to display.
	 * 
	 * @param keyWithEncodedArguments
	 *        The encoding produced by {@link ResKey#encode(ResKey)}.
	 * @return The I18N message for the information or <code>null</code>, if the given key is empty
	 *         or <code>null</code>.
	 * 
	 * @deprecated Use {@link ResKey#decode(String)}.
	 */
	@Deprecated
	default String decodeMessageFromKeyWithEncodedArguments(String keyWithEncodedArguments) {
		return getString(ResKey.decode(keyWithEncodedArguments));
	}

	/**
	 * Return the I18N message for the given key with the given values.
	 * 
	 * The message will be created using the {@link java.text.MessageFormat} class, which is very
	 * powerful. Please refer to that class for the whole functional description of the messages.
	 * This method will search for the right pattern found in the resources and hand this over (with
	 * the values) to the formatter.
	 * 
	 * @param aKey
	 *        The key for the I18N message.
	 * @param someValues
	 *        The values to be inserted into the message.
	 * @return The I18N message for the given values.
	 * @see java.text.MessageFormat
	 */
	default String getMessage(ResKey aKey, Object... someValues) {
		if (someValues == null || someValues.length == 0) {
			return getString(aKey);
		} else {
			return getString(ResKey.message(aKey, someValues));
		}
	}

	/**
	 * Resolves the resource for the given key.
	 * 
	 * <p>
	 * In contrast to {@link #getString(ResKey)}, this method does not resolves the given key in a
	 * potential fallback locale.
	 * </p>
	 *
	 * @param key
	 *        The key to resolve.
	 * @return The resource for the key, or <code>null</code> if nothing can be found.
	 */
	String getStringWithoutFallback(ResKey key);

}
