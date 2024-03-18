/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.text.Format;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Factory for {@link Format}s.
 */
public interface FormatFactory {

	/**
	 * Creates the {@link Format}.
	 * 
	 * @param globalConfig
	 *        The global configuration, that can be used as fallback for local wildcards.
	 * @param timeZone
	 *        The {@link TimeZone} to create {@link Format} for. Usage of this variable depends on
	 *        the kind of the {@link Format}.
	 * @param locale
	 *        The {@link Locale} to create {@link Format} for. Usage of this variable depends on the
	 *        kind of the {@link Format}.
	 */
	public abstract Format newFormat(FormatConfig globalConfig, TimeZone timeZone, Locale locale);

	/**
	 * Returns the pattern string used to create {@link Format}
	 * 
	 * @return May be <code>null</code>, in case the {@link Format} that are created are not pattern
	 *         based.
	 */
	public abstract String getPattern();

}
