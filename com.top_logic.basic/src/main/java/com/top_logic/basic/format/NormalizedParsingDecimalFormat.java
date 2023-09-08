/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.Locale;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * This format normalizes (formats) a string value before parsing it. This class is
 * necessary for example to get rounded values instead of raw inserted values from
 * FormFields.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class NormalizedParsingDecimalFormat {

	/**
	 * Creates a new {@link NormalizedParsingDecimalFormat} with {@link DecimalFormatSymbols format
	 * symbols} according to the {@link Locale} of the current user.
	 * 
	 * @param pattern
	 *        The format pattern.
	 */
	public static Format getNormalizingInstance(String pattern) {
		SubSessionContext subsession = ThreadContextManager.getSubSession();
		Locale currentLocale;
		if (subsession != null) {
			currentLocale = subsession.getCurrentLocale();
		} else {
			currentLocale = Locale.getDefault();
		}
		return getNormalizingInstance(pattern, currentLocale);
	}

	/**
	 * Creates a new {@link Format} with {@link DecimalFormatSymbols format symbols} according to
	 * the given {@link Locale}.
	 * 
	 * @param pattern
	 *        The format pattern.
	 */
	public static Format getNormalizingInstance(String pattern, Locale locale) {
		return getNormalizingInstance(pattern, DecimalFormatSymbols.getInstance(locale));
	}

	/**
	 * Creates a new normalizing {@link Format} parsing with a {@link DecimalFormat} with the given
	 * {@link DecimalFormatSymbols format symbols} and the given {@link Locale}.
	 * 
	 * @param pattern
	 *        See {@link DecimalFormat#DecimalFormat(String, DecimalFormatSymbols)}
	 * @param symbols
	 *        See {@link DecimalFormat#DecimalFormat(String, DecimalFormatSymbols)}
	 */
	public static Format getNormalizingInstance(String pattern, DecimalFormatSymbols symbols) {
		return NormalizingFormat.newInstance(new DecimalFormat(pattern, symbols));
	}

}
